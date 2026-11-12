package campusprint;

import campusprint.ui.PrintDeskController;
import campusprint.ui.PrintDeskWindow;
import javax.swing.SwingUtilities;

public final class DesktopMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PrintDeskWindow window = new PrintDeskWindow();
            new PrintDeskController(window);
            window.show();
        });
    }
}
