package campusprint.observer;

import campusprint.domain.PrintOrder;
import java.util.ArrayList;
import java.util.List;

public final class PrintJournal implements PrintSubscriber {
    private final List<String> entries = new ArrayList<>();

    @Override
    public void onPrinted(PrintOrder order) {
        entries.add("#" + order.id() + " " + order.job().documentName());
    }

    public List<String> entries() {
        return List.copyOf(entries);
    }

    public String summary() {
        return "Journal: " + (entries.isEmpty() ? "(empty)" : String.join(", ", entries));
    }
}
