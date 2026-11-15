package campusprint.observer;

import campusprint.domain.PrintOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class PrintPublisher {
    private final List<PrintSubscriber> subscribers = new ArrayList<>();

    public void subscribe(PrintSubscriber subscriber) {
        Objects.requireNonNull(subscriber, "subscriber");
        if (!subscribers.contains(subscriber)) {
            subscribers.add(subscriber);
        }
    }

    public void unsubscribe(PrintSubscriber subscriber) {
        subscribers.remove(subscriber);
    }

    public void publish(PrintOrder order) {
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
