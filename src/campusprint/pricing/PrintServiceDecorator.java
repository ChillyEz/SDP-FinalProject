package campusprint.pricing;

import campusprint.domain.PrintJob;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class PrintServiceDecorator implements PrintService {
    private final PrintService wrapped;
    private final String name;

    protected PrintServiceDecorator(PrintService wrapped, String name) {
        this.wrapped = Objects.requireNonNull(wrapped, "wrapped");
        this.name = name;
        PrintService current = wrapped;
        while (current instanceof PrintServiceDecorator finish) {
            if (finish.name.equals(name)) {
                throw new IllegalArgumentException(name + " is already included.");
            }
            current = finish.wrapped;
        }
    }

    protected abstract long costFor(PrintJob job);

    @Override
    public final List<LineItem> itemsFor(PrintJob job) {
        List<LineItem> items = new ArrayList<>(wrapped.itemsFor(job));
        items.add(new LineItem(name, costFor(job)));
        return List.copyOf(items);
    }
}
