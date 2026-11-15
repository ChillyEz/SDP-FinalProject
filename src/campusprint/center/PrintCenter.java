package campusprint.center;

import campusprint.domain.PrintJob;
import campusprint.domain.PrintOrder;
import campusprint.observer.PrintPublisher;
import campusprint.observer.PrintSubscriber;
import campusprint.pricing.PrintService;
import campusprint.queue.FirstComeFirstServed;
import campusprint.queue.PrintQueue;
import campusprint.queue.QueuePolicy;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class PrintCenter {
    private final PrintQueue queue;
    private final ConsolePrinter printer;
    private final PrintPublisher publisher = new PrintPublisher();
    private int nextOrderId = 1;

    public PrintCenter() {
        this(new ConsolePrinter(), new FirstComeFirstServed());
    }

    public PrintCenter(ConsolePrinter printer, QueuePolicy initialPolicy) {
        this.printer = Objects.requireNonNull(printer, "printer");
        this.queue = new PrintQueue(initialPolicy);
    }

    public void subscribe(PrintSubscriber subscriber) {
        publisher.subscribe(subscriber);
    }

    public void unsubscribe(PrintSubscriber subscriber) {
        publisher.unsubscribe(subscriber);
    }

    public PrintOrder acceptOrder(PrintJob job, PrintService service) {
        PrintOrder order = new PrintOrder(nextOrderId++, job, service);
        queue.add(order);
        return order;
    }

    public void setQueuePolicy(QueuePolicy policy) {
        queue.setPolicy(policy);
    }

    public QueuePolicy queuePolicy() {
        return queue.policy();
    }

    public Optional<PrintOrder> peekNext() {
        return queue.peekNext();
    }

    public List<PrintOrder> pendingOrders() {
        return queue.pendingOrders();
    }

    public void setSimulateFailure(boolean simulateFailure) {
        printer.setSimulateFailure(simulateFailure);
    }

    public Optional<PrintOrder> printNext() {
        Optional<PrintOrder> next = queue.peekNext();
        if (next.isEmpty()) {
            System.out.println("Queue empty.");
            return next;
        }
        PrintOrder order = next.orElseThrow();
        printer.print(order);
        queue.remove(order);
        publisher.publish(order);
        return next;
    }
}
