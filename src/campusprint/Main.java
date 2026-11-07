package campusprint;

import campusprint.center.PrintCenter;
import campusprint.document.JobTicket;
import campusprint.document.MarkdownRenderer;
import campusprint.document.PlainTextRenderer;
import campusprint.document.Receipt;
import campusprint.domain.PrintJob;
import campusprint.domain.PrintOrder;
import campusprint.observer.PrintJournal;
import campusprint.observer.PrintStatistics;
import campusprint.pricing.BasicPrinting;
import campusprint.pricing.Binding;
import campusprint.pricing.Cover;
import campusprint.queue.ShortestJobFirst;

public final class Main {
    public static void main(String[] args) {
        try {
            int comparisonCopies = args.length == 0 ? 50 : Integer.parseInt(args[0]);
            if (args.length > 1 || comparisonCopies < 1) {
                throw new IllegalArgumentException("Expected one positive copy count.");
            }
            PrintJob comparisonJob = application(comparisonCopies);
            runPrintDesk();
            compareVolume(comparisonJob);
        } catch (IllegalArgumentException invalid) {
            System.err.println(invalid.getMessage());
            System.err.println("Usage: java -cp out campusprint.Main [comparisonCopies]");
            System.exit(2);
        }
    }

    private static PrintOrder acceptThesis(PrintCenter center) {
        PrintJob thesis = new PrintJob.Builder("Thesis", 80).build();
        return center.acceptOrder(thesis, new Binding(new Cover(new BasicPrinting())));
    }

    private static PrintJob application(int copies) {
        return new PrintJob.Builder("Application", 5).range(2, 3).copies(copies).build();
    }

    private static void runPrintDesk() {
        PrintCenter center = new PrintCenter();
        PrintJournal journal = new PrintJournal();
        PrintStatistics statistics = new PrintStatistics();
        center.subscribe(journal);
        center.subscribe(statistics);

        System.out.println("CampusPrint / Print desk");
        PrintOrder thesis = acceptThesis(center);
        PrintOrder application = center.acceptOrder(application(2), new BasicPrinting());
        System.out.println(thesis);
        System.out.println(application);
        System.out.println();
        showPreview(center);
        center.setQueuePolicy(new ShortestJobFirst());
        showPreview(center);
        center.printNext();
        System.out.println(journal.summary());
        System.out.println(statistics.summary());

        PlainTextRenderer text = new PlainTextRenderer();
        MarkdownRenderer markdown = new MarkdownRenderer();
        System.out.println();
        System.out.println(new Receipt(text).render(thesis));
        System.out.println();
        System.out.println(new JobTicket(text).render(thesis));
        System.out.println();
        System.out.println(new Receipt(markdown).render(thesis));
        System.out.println();
        System.out.println(new JobTicket(markdown).render(thesis));
        System.out.println();

        center.printNext();
        center.printNext();
        System.out.println(journal.summary());
        System.out.println(statistics.summary());
    }

    private static void compareVolume(PrintJob application) {
        PrintCenter center = new PrintCenter();
        PrintOrder thesis = acceptThesis(center);
        PrintOrder other = center.acceptOrder(application, new BasicPrinting());
        center.setQueuePolicy(new ShortestJobFirst());
        System.out.println();
        System.out.println("Volume comparison / fresh queue");
        System.out.println(thesis);
        System.out.println(other);
        showPreview(center);
    }

    private static void showPreview(PrintCenter center) {
        PrintOrder next = center.peekNext().orElseThrow();
        System.out.println(center.queuePolicy().name() + " -> " + next.summary());
    }
}
