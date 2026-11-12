package campusprint.ui;

import campusprint.center.ConsolePrinter;
import campusprint.center.PrintCenter;
import campusprint.domain.PrintJob;
import campusprint.domain.PrintOrder;
import campusprint.observer.PrintJournal;
import campusprint.observer.PrintStatistics;
import campusprint.pricing.BasicPrinting;
import campusprint.pricing.Binding;
import campusprint.pricing.Cover;
import campusprint.pricing.PrintService;
import campusprint.queue.FirstComeFirstServed;
import campusprint.queue.ShortestJobFirst;

public final class PrintDeskController {
    private final PrintDeskWindow view;
    private final ConsolePrinter printer = new ConsolePrinter();
    private final PrintCenter center = new PrintCenter(printer, new FirstComeFirstServed());

    public PrintDeskController(PrintDeskWindow view) {
        this.view = view;
        PrintJournal journal = new PrintJournal();
        PrintStatistics statistics = new PrintStatistics();
        center.subscribe(journal);
        center.subscribe(statistics);
        center.subscribe(new PrintViewObserver(view, journal, statistics));
        view.bind(this);
        refreshQueue();
    }

    public void accept(String name, int pages, int from, int to, int copies,
            boolean cover, boolean binding) {
        PrintJob job = new PrintJob.Builder(name, pages).range(from, to).copies(copies).build();
        PrintService service = new BasicPrinting();
        if (cover) {
            service = new Cover(service);
        }
        if (binding) {
            service = new Binding(service);
        }
        PrintOrder order = center.acceptOrder(job, service);
        refreshQueue();
        view.showDocument(order, "Pending");
        view.setStatus("Order #" + order.id() + " added to the queue.");
    }

    public void changePolicy(boolean shortestFirst) {
        center.setQueuePolicy(shortestFirst ? new ShortestJobFirst() : new FirstComeFirstServed());
        refreshQueue();
        view.setStatus("Queue policy changed to " + center.queuePolicy().name() + ".");
    }

    public void printNext() {
        center.printNext();
        refreshQueue();
    }

    public void simulateFailure(boolean enabled) {
        printer.setSimulateFailure(enabled);
        view.setStatus(enabled ? "Paper jam enabled. Printing will fail." : "Printer ready.");
    }

    private void refreshQueue() {
        view.showQueue(center.pendingOrders(), center.peekNext().orElse(null));
    }
}
