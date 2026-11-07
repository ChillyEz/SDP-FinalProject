package campusprint.observer;

import campusprint.domain.PrintOrder;

@FunctionalInterface
public interface PrintSubscriber {
    void onPrinted(PrintOrder order);
}
