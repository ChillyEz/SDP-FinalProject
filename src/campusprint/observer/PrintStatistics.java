package campusprint.observer;

import campusprint.domain.PrintOrder;

public final class PrintStatistics implements PrintSubscriber {
    private int completedOrders;
    private int totalPrintedPages;

    @Override
    public void onPrinted(PrintOrder order) {
        completedOrders++;
        totalPrintedPages += order.printedPages();
    }

    public int completedOrders() {
        return completedOrders;
    }

    public int totalPrintedPages() {
        return totalPrintedPages;
    }

    public String summary() {
        return "Completed orders: %d | Printed pages: %d".formatted(completedOrders, totalPrintedPages);
    }
}
