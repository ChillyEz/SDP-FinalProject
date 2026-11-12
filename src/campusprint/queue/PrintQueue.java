package campusprint.queue;

import campusprint.domain.PrintOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class PrintQueue {
    private final List<PrintOrder> pendingOrders = new ArrayList<>();
    private QueuePolicy policy;

    public PrintQueue(QueuePolicy initialPolicy) {
        this.policy = Objects.requireNonNull(initialPolicy, "initialPolicy");
    }

    public void add(PrintOrder order) {
        pendingOrders.add(Objects.requireNonNull(order, "order"));
    }

    public void remove(PrintOrder order) {
        pendingOrders.remove(order);
    }

    public void setPolicy(QueuePolicy policy) {
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    public QueuePolicy policy() {
        return policy;
    }

    public Optional<PrintOrder> peekNext() {
        return policy.selectNext(pendingOrders());
    }

    public List<PrintOrder> pendingOrders() {
        return List.copyOf(pendingOrders);
    }
}
