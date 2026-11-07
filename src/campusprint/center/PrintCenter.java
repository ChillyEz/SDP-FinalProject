package campusprint.center;

import campusprint.domain.PrintJob;
import campusprint.domain.PrintOrder;
import campusprint.observer.PrintSubscriber;
import campusprint.pricing.PrintService;
import campusprint.queue.FirstComeFirstServed;
import campusprint.queue.QueuePolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class PrintCenter {
    private final List<PrintOrder> pendingOrders = new ArrayList<>();
    private final List<PrintSubscriber> subscribers = new ArrayList<>();
    private final ConsolePrinter printer;
    private QueuePolicy queuePolicy;
    private int nextOrderId = 1;

    public PrintCenter() {
        this(new ConsolePrinter(), new FirstComeFirstServed());
    }

    public PrintCenter(ConsolePrinter printer, QueuePolicy initialPolicy) {
        this.printer = Objects.requireNonNull(printer, "printer");
        this.queuePolicy = Objects.requireNonNull(initialPolicy, "initialPolicy");
    }

    public void subscribe(PrintSubscriber subscriber) {
        Objects.requireNonNull(subscriber, "subscriber");
        if (!subscribers.contains(subscriber)) {
            subscribers.add(subscriber);
        }
    }

    public void unsubscribe(PrintSubscriber subscriber) {
        subscribers.remove(subscriber);
    }

    public PrintOrder acceptOrder(PrintJob job, PrintService service) {
        PrintOrder order = new PrintOrder(nextOrderId, job, service);
        pendingOrders.add(order);
        nextOrderId++;
        return order;
    }

    public void setQueuePolicy(QueuePolicy policy) {
        queuePolicy = Objects.requireNonNull(policy, "policy");
    }

    public QueuePolicy queuePolicy() {
        return queuePolicy;
    }

    public Optional<PrintOrder> peekNext() {
        return queuePolicy.selectNext(pendingOrders());
    }

    public List<PrintOrder> pendingOrders() {
        return List.copyOf(pendingOrders);
    }

    public Optional<PrintOrder> printNext() {
        Optional<PrintOrder> next = peekNext();
        if (next.isEmpty()) {
            System.out.println("Queue empty.");
            return next;
        }
        PrintOrder order = next.orElseThrow();
        printer.print(order);
        pendingOrders.remove(order);
        notifySubscribers(order);
        return next;
    }

    private void notifySubscribers(PrintOrder order) {
        // Subscriptions changed during delivery take effect on the next print.
        for (PrintSubscriber subscriber : List.copyOf(subscribers)) {
            try {
                subscriber.onPrinted(order);
            } catch (RuntimeException failure) {
                System.err.printf("Notification failed for order #%d: %s%n", order.id(), failure.getMessage());
            }
        }
    }
}
