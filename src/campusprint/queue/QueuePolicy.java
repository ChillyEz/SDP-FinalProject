package campusprint.queue;

import campusprint.domain.PrintOrder;
import java.util.List;
import java.util.Optional;

public interface QueuePolicy {
    /** The input is in arrival order. Return empty only if the queue is empty. */
    Optional<PrintOrder> selectNext(List<PrintOrder> pendingOrders);

    String name();
}
