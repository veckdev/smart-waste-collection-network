package ie.nci.smartwaste.gui.component;

import ie.nci.smartwaste.gui.util.GuiStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ActivityLogPanel extends JPanel {

    private final JTextArea logArea;
    private JLabel eventCountLabel;

    private final DateTimeFormatter timeFormatter =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    private int eventCount;

    public ActivityLogPanel() {
        setLayout(new BorderLayout());
        setBackground(GuiStyles.SURFACE);
        setBorder(
                BorderFactory.createMatteBorder(
                        1,
                        0,
                        0,
                        0,
                        GuiStyles.BORDER
                )
        );

        add(createTopAccent(), BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(
                new BorderLayout(0, 10)
        );

        contentPanel.setBackground(GuiStyles.SURFACE);
        contentPanel.setBorder(
                new EmptyBorder(12, 20, 14, 20)
        );

        contentPanel.add(
                createHeaderPanel(),
                BorderLayout.NORTH
        );

        logArea = new JTextArea();
        configureLogArea();

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(
                BorderFactory.createLineBorder(
                        GuiStyles.BORDER_LIGHT
                )
        );

        scrollPane.getVerticalScrollBar().setUnitIncrement(12);

        contentPanel.add(scrollPane, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        eventCount = 0;

        log("Operations console ready.");
        log("Waiting for the municipal service network.");
    }

    private JPanel createTopAccent() {
        JPanel accentPanel = new JPanel();
        accentPanel.setBackground(GuiStyles.PRIMARY);
        accentPanel.setPreferredSize(new Dimension(1, 3));
        return accentPanel;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(
                new BorderLayout()
        );

        headerPanel.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(
                new BoxLayout(titlePanel, BoxLayout.Y_AXIS)
        );
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("System Activity");
        titleLabel.setFont(GuiStyles.SECTION_TITLE_FONT);
        titleLabel.setForeground(GuiStyles.TEXT_PRIMARY);

        eventCountLabel = new JLabel("0 events");
        eventCountLabel.setFont(GuiStyles.CAPTION_FONT);
        eventCountLabel.setForeground(
                GuiStyles.TEXT_SECONDARY
        );

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(eventCountLabel);

        JButton clearButton =
                GuiStyles.createSecondaryButton("Clear Log");

        clearButton.addActionListener(
                event -> clearLog()
        );

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(clearButton, BorderLayout.EAST);

        return headerPanel;
    }

    private void configureLogArea() {
        logArea.setEditable(false);
        logArea.setFont(
                new Font(
                        Font.MONOSPACED,
                        Font.PLAIN,
                        12
                )
        );

        logArea.setForeground(GuiStyles.TEXT_PRIMARY);
        logArea.setBackground(
                GuiStyles.SURFACE_SECONDARY
        );

        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setRows(5);

        logArea.setBorder(
                new EmptyBorder(10, 12, 10, 12)
        );
    }

    public void log(String message) {
        String time =
                LocalTime.now().format(timeFormatter);

        SwingUtilities.invokeLater(() -> {
            eventCount++;

            logArea.append(
                    time
                            + "  ·  "
                            + message
                            + System.lineSeparator()
            );

            eventCountLabel.setText(
                    eventCount
                            + (eventCount == 1
                            ? " event"
                            : " events")
            );

            logArea.setCaretPosition(
                    logArea.getDocument().getLength()
            );
        });
    }

    public void clearLog() {
        logArea.setText("");
        eventCount = 0;
        eventCountLabel.setText("0 events");
    }
}
