package campusprint.document;

import java.util.List;

public interface DocumentRenderer {
    String render(String title, List<DocumentField> fields);
}
