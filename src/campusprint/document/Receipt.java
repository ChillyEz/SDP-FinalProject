package campusprint.document;

import campusprint.domain.PrintOrder;
import campusprint.pricing.LineItem;
import java.util.ArrayList;
import java.util.List;

public final class Receipt extends OrderDocument {
    public Receipt(DocumentRenderer renderer) {
        super(renderer);
    }

    @Override
    protected String title(PrintOrder order) {
        return "Receipt #" + order.id() + " / " + order.job().documentName();
    }

    @Override
    protected List<DocumentField> fields(PrintOrder order) {
        List<DocumentField> fields = new ArrayList<>();
        for (LineItem item : order.items()) {
            fields.add(new DocumentField(item.name(), LineItem.formatTenge(item.amount())));
        }
        fields.add(new DocumentField("Total", LineItem.formatTenge(order.totalCost())));
        return fields;
    }
}
