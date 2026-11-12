package campusprint;

import campusprint.center.PrintCenter;
import campusprint.document.DocumentField;
import campusprint.document.DocumentRenderer;
import campusprint.document.JobTicket;
import campusprint.document.MarkdownRenderer;
import campusprint.document.Receipt;
import campusprint.domain.PrintJob;
import campusprint.domain.PrintOrder;
import campusprint.observer.PrintJournal;
import campusprint.observer.PrintStatistics;
import campusprint.pricing.BasicPrinting;
import campusprint.pricing.Binding;
import campusprint.pricing.Cover;
import campusprint.pricing.LineItem;
import campusprint.queue.ShortestJobFirst;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class PrintCenterChecks {
    public static void main(String[] args) {
        checkJobLimitsAndPrices();
        checkSchedulingAndRetry();
        checkSubscriptionChanges();
        checkDocumentBridge();
        checkMarkdownCells();
        checkConsoleDemo();
        System.out.println("All regression checks passed.");
    }

    static void checkJobLimitsAndPrices() {
        expectFailure(IllegalArgumentException.class, () -> new PrintJob.Builder(" ", 5).build());
        expectFailure(IllegalArgumentException.class, () -> new PrintJob.Builder("Letter", 5).copies(0).build());
        expectFailure(IllegalArgumentException.class, () -> new PrintJob.Builder("Letter", 5).range(3, 2).build());
        expectFailure(IllegalArgumentException.class, () -> new PrintJob.Builder("Letter", 5).range(1, 6).build());
        expectFailure(IllegalArgumentException.class, () -> new Cover(new Binding(new Cover(new BasicPrinting()))));

        PrintJob job = new PrintJob.Builder("Booklet", 5).range(2, 5).copies(3).build();
        PrintOrder order = new PrintOrder(1, job, new Cover(new Binding(new BasicPrinting())));
        check(job.printedPages() == 12, "Range endpoints count towards each copy");
        check(order.items().equals(List.of(new LineItem("Printing", 240),
                new LineItem("Binding", 600), new LineItem("Cover", 300))), "Finishing charges per copy");
        check(order.totalCost() == 1_140 && order.items().size() == 3, "Repeatable quote");
        PrintOrder onePage = accept(new PrintCenter(), "Letter", 1);
        check(onePage.summary().equals("#1 Letter (1 page)"), "Single-page order summary");
    }

    static void checkSchedulingAndRetry() {
        PrintCenter center = new PrintCenter();
        center.setQueuePolicy(new ShortestJobFirst());
        PrintJournal journal = new PrintJournal();
        PrintStatistics statistics = new PrintStatistics();
        center.subscribe(journal);
        center.subscribe(statistics);
        PrintOrder older = accept(center, "Handout", 10);
        PrintOrder newer = accept(center, "Worksheet", 10);
        check(center.peekNext().orElseThrow() == older, "Earlier arrival wins a tie");
        check(center.pendingOrders().equals(List.of(older, newer)), "Preview preserves the queue");

        center.setSimulateFailure(true);
        expectFailure(IllegalStateException.class, center::printNext);
        check(center.pendingOrders().equals(List.of(older, newer)), "Jam preserves both orders");
        check(journal.entries().isEmpty() && statistics.completedOrders() == 0, "Jam produces no completion");
        center.setSimulateFailure(false);
        check(center.printNext().orElseThrow() == older, "Retry prints the pending order");
        check(center.printNext().orElseThrow() == newer, "Next order prints once");
        check(center.printNext().isEmpty(), "Empty queue");
        check(journal.entries().equals(List.of("#1 Handout", "#2 Worksheet")), "Completion order");
        check(statistics.completedOrders() == 2 && statistics.totalPrintedPages() == 20, "No phantom completion");
    }

    static void checkSubscriptionChanges() {
        PrintCenter center = new PrintCenter();
        PrintJournal journal = new PrintJournal();
        PrintStatistics statistics = new PrintStatistics();
        center.subscribe(journal);
        center.subscribe(statistics);
        center.subscribe(statistics);
        accept(center, "First", 1);
        accept(center, "Second", 2);
        center.printNext();
        center.unsubscribe(statistics);
        center.printNext();
        check(journal.entries().equals(List.of("#1 First", "#2 Second")), "Journal records both prints");
        check(statistics.completedOrders() == 1 && statistics.totalPrintedPages() == 1,
                "Unsubscribed observer stops receiving completions");
    }

    static void checkDocumentBridge() {
        PrintJob notes = new PrintJob.Builder("Lab notes", 12).range(3, 5).copies(2).build();
        PrintOrder order = new PrintCenter().acceptOrder(notes, new Cover(new BasicPrinting()));
        List<List<DocumentField>> received = new ArrayList<>();
        DocumentRenderer renderer = (title, fields) -> {
            received.add(List.copyOf(fields));
            return "Rendered " + title;
        };

        check(new Receipt(renderer).render(order).equals("Rendered Receipt #1 / Lab notes"),
                "Receipt delegates rendering");
        check(new JobTicket(renderer).render(order).equals("Rendered Job ticket #1 / Lab notes"),
                "Ticket delegates rendering");

        List<DocumentField> charges = List.of(new DocumentField("Printing", "120 KZT"),
                new DocumentField("Cover", "200 KZT"), new DocumentField("Total", "320 KZT"));
        List<DocumentField> instructions = List.of(new DocumentField("Document", "Lab notes"),
                new DocumentField("Page range", "3-5 of 12"), new DocumentField("Copies", "2"),
                new DocumentField("Printed pages", "6"), new DocumentField("Operations", "Printing, Cover"));
        check(received.equals(List.of(charges, instructions)), "One renderer receives charges and instructions");
    }

    static void checkMarkdownCells() {
        PrintOrder order = accept(new PrintCenter(), "Part I | Appendix\nA", 1);
        String markdown = new JobTicket(new MarkdownRenderer()).render(order);
        check(markdown.contains("| Document | Part I \\| Appendix A |"), "Markdown table preserves one cell");
    }

    static void checkConsoleDemo() {
        String standard = demoOutput();
        check(standard.contains("First come, first served -> #1 Thesis (80 pages)"), "FIFO preview");
        check(standard.contains("Shortest job first -> #2 Application (4 pages)"), "Shortest job preview");
        check(standard.contains("Completed orders: 2 | Printed pages: 84"), "Final desk statistics");
        check(standard.contains("| Total | 1,900 KZT |"), "Markdown receipt");
        check(standard.contains("| Operations | Printing, Cover, Binding |"), "Markdown ticket");
        check(standard.endsWith("Shortest job first -> #1 Thesis (80 pages)"), "50-copy comparison");

        String smaller = demoOutput("10");
        check(smaller.contains("#2 Application | copies: 10 | pages: 20 | 400 KZT"), "Repriced comparison");
        check(smaller.endsWith("Shortest job first -> #2 Application (20 pages)"), "Live selection");
    }

    private static String demoOutput(String... args) {
        PrintStream original = System.out;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            Main.main(args);
        } finally {
            System.setOut(original);
        }
        return bytes.toString(StandardCharsets.UTF_8).stripTrailing();
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
