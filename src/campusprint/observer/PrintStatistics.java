package campusprint.observer;

import campusprint.domain.PrintOrder;

public final class PrintStatistics implements PrintSubscriber {
    private int completedOrders;
    private long totalPrintedPages;

    @Override
    public void onPrinted(PrintOrder order) {
        completedOrders++;
        totalPrintedPages += order.printedPages();
    }

    public int completedOrders() {
        return completedOrders;
    }

    public long totalPrintedPages() {
        return totalPrintedPages;
    }

    public String summary() {
        return "Completed orders: %d | Printed pages: %d".formatted(completedOrders, totalPrintedPages);
    }
}
