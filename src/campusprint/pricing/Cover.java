package campusprint.pricing;

import campusprint.domain.PrintJob;

public final class Cover extends PrintServiceDecorator {
    public static final int PRICE_PER_COPY = 100;
    public static final String SERVICE_NAME = "Cover";

    public Cover(PrintService wrapped) {
        super(wrapped, SERVICE_NAME);
    }

    @Override
    protected long costFor(PrintJob job) {
        return (long) job.copies() * PRICE_PER_COPY;
    }
}
