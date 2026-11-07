package campusprint.queue;

import campusprint.domain.PrintOrder;
import java.util.List;
import java.util.Optional;

public final class ShortestJobFirst implements QueuePolicy {
    @Override
    public Optional<PrintOrder> selectNext(List<PrintOrder> pendingOrders) {
        if (pendingOrders == null || pendingOrders.isEmpty()) {
            return Optional.empty();
        }
        PrintOrder shortest = pendingOrders.get(0);
        for (PrintOrder order : pendingOrders) {
            if (order.printedPages() < shortest.printedPages()) {
                shortest = order;
            }
        }
        return Optional.of(shortest);
    }

    @Override
    public String name() {
        return "Shortest job first";
    }
}
