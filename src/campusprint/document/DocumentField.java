package campusprint.document;

import java.util.Objects;

public record DocumentField(String label, String value) {
    public DocumentField {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("A document field needs a label.");
        }
        Objects.requireNonNull(value, "value");
    }
}
