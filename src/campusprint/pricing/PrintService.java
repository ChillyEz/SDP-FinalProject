package campusprint.pricing;

import campusprint.domain.PrintJob;
import java.util.List;

public interface PrintService {
    List<LineItem> itemsFor(PrintJob job);
}
