package campusprint.ui;

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
import java.util.Optional;

public final class PrintDeskController {
    private final PrintDeskWindow view;
    private final PrintCenter center = new PrintCenter();
    private final PrintJournal journal = new PrintJournal();
    private final PrintStatistics statistics = new PrintStatistics();

    public PrintDeskController(PrintDeskWindow view) {
        this.view = view;
        center.subscribe(journal);
        center.subscribe(statistics);
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
        Optional<PrintOrder> printed = center.printNext();
        refreshQueue();
        printed.ifPresent(order ->
                view.showCompletion(order, journal.entries(), statistics.summary()));
    }

    public void simulateFailure(boolean enabled) {
        center.setSimulateFailure(enabled);
        view.setStatus(enabled ? "Paper jam enabled. Printing will fail." : "Printer ready.");
    }

    private void refreshQueue() {
        view.showQueue(center.pendingOrders(), center.peekNext().orElse(null));
    }
}
