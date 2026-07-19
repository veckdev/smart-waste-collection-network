package ie.nci.smartwaste.gui.panel;

import ie.nci.smartwaste.gui.util.GuiStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SmartBinPanel extends JPanel {

    public SmartBinPanel() {
        setLayout(new BorderLayout());
        setBackground(GuiStyles.BACKGROUND);
        setBorder(new EmptyBorder(22, 22, 22, 22));

        add(createPlaceholder(), BorderLayout.CENTER);
    }

    private JPanel createPlaceholder() {
        JPanel panel = GuiStyles.createSurfacePanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Smart Bin Service");
        title.setFont(GuiStyles.TITLE_FONT);
        title.setForeground(GuiStyles.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel(
                "<html>Register bins, update fill levels, inspect bin status, " +
                        "report damage and stream bins requiring collection.</html>"
        );

        description.setFont(GuiStyles.BODY_FONT);
        description.setForeground(GuiStyles.TEXT_SECONDARY);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel phase = new JLabel("Service controls will be added in the next phase.");
        phase.setFont(GuiStyles.BODY_FONT);
        phase.setForeground(GuiStyles.PRIMARY);
        phase.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(12));
        panel.add(description);
        panel.add(Box.createVerticalStrut(22));
        panel.add(phase);
        panel.add(Box.createVerticalGlue());

        return panel;
    }
}