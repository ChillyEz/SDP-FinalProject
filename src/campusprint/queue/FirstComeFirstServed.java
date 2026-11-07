package campusprint.queue;

import campusprint.domain.PrintOrder;
import java.util.List;
import java.util.Optional;

public final class FirstComeFirstServed implements QueuePolicy {
    @Override
    public Optional<PrintOrder> selectNext(List<PrintOrder> pendingOrders) {
        if (pendingOrders == null || pendingOrders.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(pendingOrders.get(0));
    }

    @Override
    public String name() {
        return "First come, first served";
    }
}
