package campusprint.pricing;

import java.util.Locale;

public record LineItem(String name, long amount) {
    public LineItem {
        if (name == null || name.isBlank() || amount < 0) {
            throw new IllegalArgumentException("Each charge needs a name and a nonnegative amount.");
        }
    }

    public static String formatTenge(long amount) {
        return String.format(Locale.ROOT, "%,d KZT", amount);
    }
}
