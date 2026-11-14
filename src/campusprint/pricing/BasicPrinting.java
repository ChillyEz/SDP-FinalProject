package campusprint.pricing;

import campusprint.domain.PrintJob;
import java.util.List;

public final class BasicPrinting implements PrintService {
    public static final int PRICE_PER_PAGE = 20;
    public static final String SERVICE_NAME = "Printing";

    @Override
    public List<LineItem> itemsFor(PrintJob job) {
        return List.of(new LineItem(SERVICE_NAME, (long) job.printedPages() * PRICE_PER_PAGE));
    }
}
