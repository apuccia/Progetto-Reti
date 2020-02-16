package clientGUI;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class WQGUIUtilities {
    public WQGUIUtilities() {

    }

    public static JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setBackground(Color.LIGHT_GRAY);
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        return button;
    }

    public static JLabel createStandardLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setForeground(Color.BLACK);
        label.setBackground(Color.WHITE);
        label.setHorizontalAlignment(JLabel.CENTER);

        return label;
    }

    public static JTextField createTextField() {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Arial", Font.PLAIN, 16));
        field.setForeground(Color.BLACK);
        field.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        return field;
    }

    public static JPasswordField createPasswordField() {
        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordField.setForeground(Color.BLACK);
        passwordField.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        return passwordField;
    }

    public static JPanel createSouthPanel() {
        JPanel panel = new JPanel() {
            public Dimension getPreferredSize() {
                return new Dimension(100, 50);
            };
        };
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.GRAY);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLACK));

        return panel;
    }

    public static JPanel createTable(String headers[], String values[][]) {

        JTable table = new JTable(values, headers);
        JScrollPane scrollPanel = new JScrollPane(table);

        table.setFillsViewportHeight(true);
        table.setRowHeight(25);
        table.setShowVerticalLines(false);
        table.setEnabled(false);
        table.setGridColor(Color.BLACK);
        table.setCellSelectionEnabled(false);
        table.setDragEnabled(false);
        table.setRowHeight(50);
        table.setFont(new Font("Arial", Font.PLAIN, 16));
        table.setForeground(Color.BLACK);
        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        table.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JTableHeader header = table.getTableHeader();

        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);
        header.setFont(new Font("Arial", Font.BOLD, 20));
        header.setForeground(Color.BLACK);
        header.setBackground(Color.LIGHT_GRAY);
        header.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        DefaultTableCellRenderer stringRenderer = (DefaultTableCellRenderer)
                table.getDefaultRenderer(String.class);
        stringRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        scrollPanel.setColumnHeader(new JViewport() {
            @Override public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.height = 50;
                return d;
            }
        });
        scrollPanel.setBorder(BorderFactory.createEmptyBorder());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.ipadx = 700;
        gbc.ipady = 350;

        panel.add(scrollPanel, gbc);

        return panel;
    }
}
