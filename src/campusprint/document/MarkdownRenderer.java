package campusprint.document;

import java.util.List;

public final class MarkdownRenderer implements DocumentRenderer {
    @Override
    public String render(String title, List<DocumentField> fields) {
        StringBuilder table = new StringBuilder("### ").append(escape(title));
        table.append("\n\n| Field | Value |\n| --- | --- |");
        for (DocumentField field : fields) {
            table.append("\n| ").append(escape(field.label()))
                    .append(" | ").append(escape(field.value())).append(" |");
        }
        return table.toString();
    }

    private static String escape(String text) {
        return text.replace("|", "\\|").replaceAll("\\R", " ");
    }
}
