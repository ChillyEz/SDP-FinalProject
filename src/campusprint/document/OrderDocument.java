package campusprint.document;

import campusprint.domain.PrintOrder;
import java.util.List;
import java.util.Objects;

public abstract class OrderDocument {
    private final DocumentRenderer renderer;

    protected OrderDocument(DocumentRenderer renderer) {
        this.renderer = Objects.requireNonNull(renderer, "renderer");
    }

    protected abstract String title(PrintOrder order);

    protected abstract List<DocumentField> fields(PrintOrder order);

    public final String render(PrintOrder order) {
        return renderer.render(title(order), fields(order));
    }
}
