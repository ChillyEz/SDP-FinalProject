package campusprint.ui;

import campusprint.domain.PrintOrder;
import campusprint.observer.PrintJournal;
import campusprint.observer.PrintStatistics;
import campusprint.observer.PrintSubscriber;
import javax.swing.SwingUtilities;

public final class PrintViewObserver implements PrintSubscriber {
    private final PrintDeskWindow view;
    private final PrintJournal journal;
    private final PrintStatistics statistics;

    public PrintViewObserver(PrintDeskWindow view, PrintJournal journal, PrintStatistics statistics) {
        this.view = view;
        this.journal = journal;
        this.statistics = statistics;
    }

    @Override
    public void onPrinted(PrintOrder order) {
        // Refresh after every subscriber has processed this completion, on Swing's UI thread.
        SwingUtilities.invokeLater(() -> view.showCompletion(order, journal.entries(), statistics.summary()));
    }
}
