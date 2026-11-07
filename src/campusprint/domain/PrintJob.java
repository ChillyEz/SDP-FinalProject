package campusprint.domain;

public final class PrintJob {
    private final String documentName;
    private final int totalPages;
    private final int fromPage;
    private final int toPage;
    private final int copies;

    private PrintJob(Builder builder) {
        documentName = builder.documentName;
        totalPages = builder.totalPages;
        fromPage = builder.fromPage;
        toPage = builder.toPage;
        copies = builder.copies;
    }

    public String documentName() {
        return documentName;
    }

    public int totalPages() {
        return totalPages;
    }

    public int fromPage() {
        return fromPage;
    }

    public int toPage() {
        return toPage;
    }

    public int copies() {
        return copies;
    }

    public int pagesPerCopy() {
        return toPage - fromPage + 1;
    }

    public int printedPages() {
        return pagesPerCopy() * copies;
    }

    public static final class Builder {
        private final String documentName;
        private final int totalPages;
        private int fromPage = 1;
        private int toPage;
        private int copies = 1;

        public Builder(String documentName, int totalPages) {
            this.documentName = documentName;
            this.totalPages = totalPages;
            this.toPage = totalPages;
        }

        public Builder range(int from, int to) {
            fromPage = from;
            toPage = to;
            return this;
        }

        public Builder copies(int count) {
            copies = count;
            return this;
        }

        public PrintJob build() {
            if (documentName == null || documentName.isBlank()) {
                throw new IllegalArgumentException("Enter a document name.");
            }
            if (totalPages < 1 || copies < 1) {
                throw new IllegalArgumentException("Page and copy counts must be positive.");
            }
            if (fromPage < 1 || toPage < fromPage || toPage > totalPages) {
                throw new IllegalArgumentException("Select a range within pages 1-" + totalPages + ".");
            }
            if ((long) (toPage - fromPage + 1) * copies > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Print volume exceeds the per-job page limit.");
            }
            return new PrintJob(this);
        }
    }
}
