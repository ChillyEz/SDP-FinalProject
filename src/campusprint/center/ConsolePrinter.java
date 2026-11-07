package campusprint.center;

import campusprint.domain.PrintOrder;

public final class ConsolePrinter {
    private boolean simulateFailure;

    public void setSimulateFailure(boolean simulateFailure) {
        this.simulateFailure = simulateFailure;
    }

    public void print(PrintOrder order) {
        if (simulateFailure) {
            throw new IllegalStateException("Paper jam on order #" + order.id() + ".");
        }
        System.out.println("Printed " + order.summary());
    }
}
