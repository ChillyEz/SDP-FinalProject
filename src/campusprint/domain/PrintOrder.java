package campusprint.domain;

import campusprint.pricing.LineItem;
import campusprint.pricing.PrintService;
import java.util.List;
import java.util.Objects;

public final class PrintOrder {
    private final int id;
    private final PrintJob job;
    private final PrintService service;

    public PrintOrder(int id, PrintJob job, PrintService service) {
        if (id <= 0) {
            throw new IllegalArgumentException("Order IDs must be positive.");
        }
        this.id = id;
        this.job = Objects.requireNonNull(job, "job");
        this.service = Objects.requireNonNull(service, "service");
    }

    public int id() {
        return id;
    }

    public PrintJob job() {
        return job;
    }

    public PrintService service() {
        return service;
    }

    public int printedPages() {
        return job.printedPages();
    }

    public String summary() {
        int pages = printedPages();
        return "#%d %s (%d %s)".formatted(id, job.documentName(), pages, pages == 1 ? "page" : "pages");
    }

    public List<LineItem> items() {
        return List.copyOf(service.itemsFor(job));
    }

    public long totalCost() {
        return items().stream().mapToLong(LineItem::amount).sum();
    }

    @Override
    public String toString() {
        return "#%d %s | copies: %d | pages: %d | %s".formatted(
                id, job.documentName(), job.copies(), printedPages(), LineItem.formatTenge(totalCost()));
    }
}
