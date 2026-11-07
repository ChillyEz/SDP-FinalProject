package campusprint.document;

import campusprint.domain.PrintJob;
import campusprint.domain.PrintOrder;
import campusprint.pricing.LineItem;
import java.util.List;
import java.util.stream.Collectors;

public final class JobTicket extends OrderDocument {
    public JobTicket(DocumentRenderer renderer) {
        super(renderer);
    }

    @Override
    protected String title(PrintOrder order) {
        return "Job ticket #" + order.id() + " / " + order.job().documentName();
    }

    @Override
    protected List<DocumentField> fields(PrintOrder order) {
        PrintJob job = order.job();
        String operations = order.items().stream().map(LineItem::name).collect(Collectors.joining(", "));
        return List.of(
                new DocumentField("Document", job.documentName()),
                new DocumentField("Page range", "%d-%d of %d".formatted(job.fromPage(), job.toPage(), job.totalPages())),
                new DocumentField("Copies", Integer.toString(job.copies())),
                new DocumentField("Printed pages", Integer.toString(job.printedPages())),
                new DocumentField("Operations", operations));
    }
}
