package campusprint.pricing;

import campusprint.domain.PrintJob;

public final class Binding extends PrintServiceDecorator {
    public static final int PRICE_PER_COPY = 200;
    public static final String SERVICE_NAME = "Binding";

    public Binding(PrintService wrapped) {
        super(wrapped, SERVICE_NAME);
    }

    @Override
    protected long costFor(PrintJob job) {
        return (long) job.copies() * PRICE_PER_COPY;
    }
}
