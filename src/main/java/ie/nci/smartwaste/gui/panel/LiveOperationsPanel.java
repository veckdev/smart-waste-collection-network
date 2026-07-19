package ie.nci.smartwaste.gui.panel;

import ie.nci.smartwaste.gui.util.GuiStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LiveOperationsPanel extends JPanel {

    public LiveOperationsPanel() {
        setLayout(new BorderLayout());
        setBackground(GuiStyles.BACKGROUND);
        setBorder(new EmptyBorder(22, 22, 22, 22));

        JPanel panel = GuiStyles.createSurfacePanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Live Collection Route Monitoring");
        title.setFont(GuiStyles.TITLE_FONT);
        title.setForeground(GuiStyles.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel(
                "<html>Start a bidirectional stream and exchange live collection " +
                        "updates with the Smart Bin Service.</html>"
        );

        description.setFont(GuiStyles.BODY_FONT);
        description.setForeground(GuiStyles.TEXT_SECONDARY);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel phase =
                new JLabel("Bidirectional streaming controls will be added later.");

        phase.setFont(GuiStyles.BODY_FONT);
        phase.setForeground(GuiStyles.PRIMARY);
        phase.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(12));
        panel.add(description);
        panel.add(Box.createVerticalStrut(22));
        panel.add(phase);
        panel.add(Box.createVerticalGlue());

        add(panel, BorderLayout.CENTER);
    }
}