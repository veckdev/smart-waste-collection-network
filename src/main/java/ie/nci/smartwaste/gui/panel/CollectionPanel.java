package ie.nci.smartwaste.gui.panel;

import ie.nci.smartwaste.gui.util.GuiStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CollectionPanel extends JPanel {

    public CollectionPanel() {
        setLayout(new BorderLayout());
        setBackground(GuiStyles.BACKGROUND);
        setBorder(new EmptyBorder(22, 22, 22, 22));

        JPanel panel = GuiStyles.createSurfacePanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Collection Service");
        title.setFont(GuiStyles.TITLE_FONT);
        title.setForeground(GuiStyles.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel(
                "<html>Assign collection tasks, complete collections and " +
                        "retrieve collection route information.</html>"
        );

        description.setFont(GuiStyles.BODY_FONT);
        description.setForeground(GuiStyles.TEXT_SECONDARY);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel phase = new JLabel("Service controls will be added in a later phase.");
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