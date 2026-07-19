package ie.nci.smartwaste.gui.component;

import ie.nci.smartwaste.gui.util.GuiStyles;

import javax.swing.*;
import java.awt.*;

public class OperationCard extends SurfaceCard {

    private final JPanel fieldsPanel;
    private final JPanel actionPanel;

    public OperationCard(
            String title,
            String description,
            Color accentColor
    ) {
        super(accentColor, new Insets(24, 20, 19, 20));

        setLayout(new BorderLayout(0, 15));

        JPanel headingPanel = new JPanel();
        headingPanel.setLayout(new BoxLayout(headingPanel, BoxLayout.Y_AXIS));
        headingPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(GuiStyles.CARD_TITLE_FONT);
        titleLabel.setForeground(GuiStyles.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descriptionLabel = new JLabel(
                "<html><div style='width:245px'>" + description + "</div></html>"
        );
        descriptionLabel.setFont(GuiStyles.BODY_FONT);
        descriptionLabel.setForeground(GuiStyles.TEXT_SECONDARY);
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        headingPanel.add(titleLabel);
        headingPanel.add(Box.createVerticalStrut(7));
        headingPanel.add(descriptionLabel);

        fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setOpaque(false);

        actionPanel = new JPanel(new BorderLayout());
        actionPanel.setOpaque(false);

        add(headingPanel, BorderLayout.NORTH);
        add(fieldsPanel, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);
    }

    public void addField(String labelText, JComponent input) {
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
        fieldPanel.setOpaque(false);
        fieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText.toUpperCase());
        label.setFont(GuiStyles.LABEL_FONT);
        label.setForeground(GuiStyles.TEXT_SECONDARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, input.getPreferredSize().height));

        fieldPanel.add(label);
        fieldPanel.add(Box.createVerticalStrut(6));
        fieldPanel.add(input);

        fieldsPanel.add(fieldPanel);
        fieldsPanel.add(Box.createVerticalStrut(12));
    }

    public void setActionButton(JButton button) {
        actionPanel.removeAll();
        actionPanel.add(button, BorderLayout.WEST);
        actionPanel.revalidate();
        actionPanel.repaint();
    }
}
