package campusprint.ui;

import campusprint.document.DocumentRenderer;
import campusprint.document.JobTicket;
import campusprint.document.MarkdownRenderer;
import campusprint.document.OrderDocument;
import campusprint.document.PlainTextRenderer;
import campusprint.document.Receipt;
import campusprint.domain.PrintOrder;
import campusprint.pricing.LineItem;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.ParseException;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public final class PrintDeskWindow {
    private final JFrame frame = new JFrame("CampusPrint");
    private final JTextField name = new JTextField("Thesis");
    private final JSpinner pages = number(80);
    private final JSpinner from = number(1);
    private final JSpinner to = number(80);
    private final JSpinner copies = number(1);
    private final JCheckBox cover = new JCheckBox("Cover");
    private final JCheckBox binding = new JCheckBox("Binding");
    private final JButton add = new JButton("Add order");
    private final JButton print = new JButton("Print next");
    private final JCheckBox jam = new JCheckBox("Simulate paper jam");
    private final JComboBox<String> policy = new JComboBox<>(
            new String[] {"First come, first served", "Shortest job first"});
    private final JComboBox<String> documentType = new JComboBox<>(new String[] {"Receipt", "Job ticket"});
    private final JComboBox<String> format = new JComboBox<>(new String[] {"Plain text", "Markdown"});
    private final JLabel nextOrder = new JLabel("Queue empty.");
    private final JLabel documentTitle = new JLabel("Select a queued order or print an order.");
    private final JLabel statistics = new JLabel("Completed orders: 0 | Printed pages: 0");
    private final JLabel status = new JLabel("Enter an order to begin.");
    private final JTextArea document = outputArea();
    private final JTextArea journal = outputArea();
    private final DefaultTableModel queueModel = new DefaultTableModel(
            new String[] {"ID", "Document", "Range", "Copies", "Pages", "Total"}, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable queue = new JTable(queueModel);
    private List<PrintOrder> pending = List.of();
    private PrintOrder documentOrder;
    private boolean refreshingQueue;

    public PrintDeskWindow() {
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(orderForm(), BorderLayout.WEST);

        JPanel workspace = new JPanel(new GridLayout(2, 1, 0, 12));
        workspace.add(queuePanel());
        workspace.add(resultsPanel());
        content.add(workspace, BorderLayout.CENTER);
        content.add(status, BorderLayout.SOUTH);
        frame.setContentPane(content);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setSize(1100, 720);
        frame.setMinimumSize(new Dimension(950, 640));
        frame.setLocationRelativeTo(null);

        queue.getSelectionModel().addListSelectionListener(event -> {
            int row = queue.getSelectedRow();
            if (!refreshingQueue && !event.getValueIsAdjusting() && row >= 0) {
                showDocument(pending.get(row), "Pending");
            }
        });
        documentType.addActionListener(event -> renderDocument());
        format.addActionListener(event -> renderDocument());
    }

    public void bind(PrintDeskController controller) {
        add.addActionListener(event -> perform(() -> {
            for (JSpinner field : List.of(pages, from, to, copies)) {
                try {
                    field.commitEdit();
                } catch (ParseException invalid) {
                    throw new IllegalArgumentException("Enter a whole number in each page and copy field.");
                }
            }
            controller.accept(name.getText(), (int) pages.getValue(), (int) from.getValue(),
                    (int) to.getValue(), (int) copies.getValue(), cover.isSelected(), binding.isSelected());
        }));
        print.addActionListener(event -> perform(controller::printNext));
        policy.addActionListener(event -> controller.changePolicy(policy.getSelectedIndex() == 1));
        jam.addActionListener(event -> controller.simulateFailure(jam.isSelected()));
    }

    public void show() {
        frame.setVisible(true);
    }

    public void showQueue(List<PrintOrder> orders, PrintOrder next) {
        int selected = queue.getSelectedRow();
        PrintOrder previous = selected >= 0 ? pending.get(selected) : null;
        refreshingQueue = true;
        try {
            pending = orders;
            queueModel.setRowCount(0);
            for (PrintOrder order : orders) {
                queueModel.addRow(new Object[] {order.id(), order.job().documentName(),
                        order.job().fromPage() + "-" + order.job().toPage(), order.job().copies(),
                        order.printedPages(), LineItem.formatTenge(order.totalCost())});
            }
            int row = previous == null ? -1 : orders.indexOf(previous);
            if (row >= 0) {
                queue.setRowSelectionInterval(row, row);
            }
        } finally {
            refreshingQueue = false;
        }
        nextOrder.setText(next == null ? "Queue empty." : "Next: " + next.summary());
        print.setEnabled(next != null);
    }

    public void showDocument(PrintOrder order, String state) {
        documentOrder = order;
        documentTitle.setText(state + " order #" + order.id() + " / " + order.job().documentName());
        renderDocument();
    }

    public void showCompletion(PrintOrder order, List<String> entries, String summary) {
        journal.setText(String.join(System.lineSeparator(), entries));
        statistics.setText(summary);
        showDocument(order, "Completed");
        setStatus("Printed " + order.summary() + ". Journal, statistics and document updated.");
    }

    public void setStatus(String message) {
        status.setText(message);
    }

    private void renderDocument() {
        if (documentOrder == null) {
            return;
        }
        DocumentRenderer renderer = format.getSelectedIndex() == 0
                ? new PlainTextRenderer() : new MarkdownRenderer();
        OrderDocument selected = documentType.getSelectedIndex() == 0
                ? new Receipt(renderer) : new JobTicket(renderer);
        document.setText(selected.render(documentOrder));
        document.setCaretPosition(0);
    }

    private JPanel orderForm() {
        JPanel fields = new JPanel(new GridLayout(0, 2, 8, 8));
        fields.add(new JLabel("Document"));
        fields.add(name);
        fields.add(new JLabel("Document pages"));
        fields.add(pages);
        fields.add(new JLabel("From page"));
        fields.add(from);
        fields.add(new JLabel("To page"));
        fields.add(to);
        fields.add(new JLabel("Copies"));
        fields.add(copies);
        fields.add(cover);
        fields.add(binding);
        JPanel form = new JPanel(new BorderLayout(0, 10));
        form.setBorder(BorderFactory.createTitledBorder("New order"));
        form.add(fields, BorderLayout.NORTH);
        JPanel actions = new JPanel(new BorderLayout());
        actions.add(add, BorderLayout.NORTH);
        form.add(actions, BorderLayout.CENTER);
        form.setPreferredSize(new Dimension(280, 0));
        return form;
    }

    private JPanel queuePanel() {
        queue.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        queue.setFillsViewportHeight(true);
        JPanel controls = new JPanel();
        controls.add(new JLabel("Queue policy"));
        controls.add(policy);
        controls.add(print);
        controls.add(jam);
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBorder(BorderFactory.createTitledBorder("Pending orders (arrival order)"));
        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(queue), BorderLayout.CENTER);
        panel.add(nextOrder, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel resultsPanel() {
        JPanel choices = new JPanel();
        choices.add(new JLabel("Document"));
        choices.add(documentType);
        choices.add(new JLabel("Format"));
        choices.add(format);
        JPanel preview = new JPanel(new BorderLayout(0, 6));
        preview.add(choices, BorderLayout.NORTH);
        preview.add(new JScrollPane(document), BorderLayout.CENTER);
        preview.add(documentTitle, BorderLayout.SOUTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Documents", preview);
        tabs.addTab("Completed orders", new JScrollPane(journal));
        JPanel results = new JPanel(new BorderLayout(0, 6));
        results.add(statistics, BorderLayout.NORTH);
        results.add(tabs, BorderLayout.CENTER);
        return results;
    }

    private void perform(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException | IllegalStateException failure) {
            setStatus(failure.getMessage());
            JOptionPane.showMessageDialog(frame, failure.getMessage(), "Cannot complete action",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static JSpinner number(int value) {
        return new JSpinner(new SpinnerNumberModel(value, 1, Integer.MAX_VALUE, 1));
    }

    private static JTextArea outputArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        return area;
    }
}
