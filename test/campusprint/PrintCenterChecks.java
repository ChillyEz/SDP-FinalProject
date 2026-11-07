package campusprint;

import campusprint.center.ConsolePrinter;
import campusprint.center.PrintCenter;
import campusprint.document.JobTicket;
import campusprint.document.MarkdownRenderer;
import campusprint.domain.PrintJob;
import campusprint.domain.PrintOrder;
import campusprint.observer.PrintJournal;
import campusprint.observer.PrintStatistics;
import campusprint.observer.PrintSubscriber;
import campusprint.pricing.BasicPrinting;
import campusprint.pricing.Binding;
import campusprint.pricing.Cover;
import campusprint.pricing.LineItem;
import campusprint.queue.ShortestJobFirst;
import java.util.List;

public final class PrintCenterChecks {
    public static void main(String[] args) {
        checkJobLimitsAndPrices();
        checkSchedulingAndRetry();
        checkSubscriptionChanges();
        checkMarkdownCells();
        System.out.println("All regression checks passed.");
    }

    private static void checkJobLimitsAndPrices() {
        expectFailure(IllegalArgumentException.class, () -> new PrintJob.Builder(" ", 5).build());
        expectFailure(IllegalArgumentException.class, () -> new PrintJob.Builder("Letter", 5).copies(0).build());
        expectFailure(IllegalArgumentException.class, () -> new PrintJob.Builder("Letter", 5).range(3, 2).build());
        expectFailure(IllegalArgumentException.class, () -> new PrintJob.Builder("Letter", 5).range(1, 6).build());
        expectFailure(IllegalArgumentException.class, () -> new PrintJob.Builder("Batch", 65_536).copies(65_536).build());
        expectFailure(IllegalArgumentException.class, () -> new Cover(new Binding(new Cover(new BasicPrinting()))));

        PrintJob job = new PrintJob.Builder("Booklet", 5).range(2, 5).copies(3).build();
        PrintOrder order = new PrintOrder(1, job, new Cover(new Binding(new BasicPrinting())));
        check(job.printedPages() == 12, "Range endpoints count towards each copy");
        check(order.items().equals(List.of(new LineItem("Printing", 240),
                new LineItem("Binding", 600), new LineItem("Cover", 300))), "Finishing charges per copy");
        check(order.totalCost() == 1_140 && order.items().size() == 3, "Repeatable quote");

        PrintJob batch = new PrintJob.Builder("Batch", 1).copies(Integer.MAX_VALUE).build();
        PrintOrder maximum = new PrintOrder(2, batch, new Cover(new Binding(new BasicPrinting())));
        check(maximum.totalCost() == 320L * Integer.MAX_VALUE, "64-bit charge total");
        PrintStatistics statistics = new PrintStatistics();
        statistics.onPrinted(maximum);
        statistics.onPrinted(order);
        check(statistics.totalPrintedPages() == (long) Integer.MAX_VALUE + 12, "64-bit cumulative pages");
    }

    private static void checkSchedulingAndRetry() {
        ConsolePrinter printer = new ConsolePrinter();
        PrintCenter center = new PrintCenter(printer, new ShortestJobFirst());
        PrintJournal journal = new PrintJournal();
        PrintStatistics statistics = new PrintStatistics();
        center.subscribe(journal);
        center.subscribe(statistics);
        PrintOrder older = accept(center, "Handout", 10);
        PrintOrder newer = accept(center, "Worksheet", 10);
        check(center.peekNext().orElseThrow() == older, "Earlier arrival wins a tie");
        check(center.pendingOrders().equals(List.of(older, newer)), "Preview preserves the queue");

        printer.setSimulateFailure(true);
        expectFailure(IllegalStateException.class, center::printNext);
        check(center.pendingOrders().equals(List.of(older, newer)), "Jam preserves both orders");
        check(journal.entries().isEmpty() && statistics.completedOrders() == 0, "Jam produces no completion");
        printer.setSimulateFailure(false);
        check(center.printNext().orElseThrow() == older, "Retry prints the pending order");
        check(center.printNext().orElseThrow() == newer, "Next order prints once");
        check(center.printNext().isEmpty(), "Empty queue");
        check(journal.entries().equals(List.of("#1 Handout", "#2 Worksheet")), "Completion order");
        check(statistics.completedOrders() == 2 && statistics.totalPrintedPages() == 20, "No phantom completion");
    }

    private static void checkSubscriptionChanges() {
        PrintCenter center = new PrintCenter();
        PrintJournal joinedLater = new PrintJournal();
        PrintStatistics statistics = new PrintStatistics();
        center.subscribe(new PrintSubscriber() {
            @Override
            public void onPrinted(PrintOrder order) {
                center.unsubscribe(this);
                center.subscribe(joinedLater);
            }
        });
        center.subscribe(statistics);
        center.subscribe(statistics);
        accept(center, "First", 1);
        accept(center, "Second", 2);
        center.printNext();
        center.printNext();
        check(statistics.completedOrders() == 2, "Self-unsubscribe preserves the other subscription");
        check(joinedLater.entries().equals(List.of("#2 Second")), "New subscription starts with the next event");
    }

    private static void checkMarkdownCells() {
        PrintOrder order = accept(new PrintCenter(), "Part I | Appendix\nA", 1);
        String markdown = new JobTicket(new MarkdownRenderer()).render(order);
        check(markdown.contains("| Document | Part I \\| Appendix A |"), "Markdown table preserves one cell");
    }

    private static PrintOrder accept(PrintCenter center, String title, int pages) {
        return center.acceptOrder(new PrintJob.Builder(title, pages).build(), new BasicPrinting());
    }

    private static void expectFailure(Class<? extends RuntimeException> expected, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException failure) {
            if (expected.isInstance(failure)) {
                return;
            }
            throw failure;
        }
        throw new AssertionError("Expected " + expected.getSimpleName());
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
