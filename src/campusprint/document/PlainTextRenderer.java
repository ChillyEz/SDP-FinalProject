package campusprint.document;

import java.util.List;

public final class PlainTextRenderer implements DocumentRenderer {
    @Override
    public String render(String title, List<DocumentField> fields) {
        StringBuilder text = new StringBuilder(title);
        for (DocumentField field : fields) {
            text.append(System.lineSeparator())
                    .append("  %-16s %s".formatted(field.label(), field.value()));
        }
        return text.toString();
    }
}
