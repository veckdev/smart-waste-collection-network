package ie.nci.smartwaste.gui.component;

import ie.nci.smartwaste.gui.util.GuiStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ServiceCard extends JPanel {

    private final StatusBadge statusBadge;
    private final Color accentColor;

    public ServiceCard(
            String category,
            String title,
            String description,
            String capabilities,
            Color accentColor
    ) {
        this.accentColor = accentColor;

        setLayout(new BorderLayout(0, 14));
        setOpaque(false);
        setBorder(new EmptyBorder(19, 20, 18, 20));

        JPanel headingPanel = new JPanel(new BorderLayout(12, 0));
        headingPanel.setOpaque(false);

        JLabel categoryLabel = new JLabel(category.toUpperCase());
        categoryLabel.setFont(GuiStyles.LABEL_FONT);
        categoryLabel.setForeground(accentColor);

        statusBadge = new StatusBadge();
        statusBadge.setConnected(false);

        headingPanel.add(categoryLabel, BorderLayout.WEST);
        headingPanel.add(statusBadge, BorderLayout.EAST);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(GuiStyles.CARD_TITLE_FONT);
        titleLabel.setForeground(GuiStyles.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descriptionLabel = new JLabel(
                "<html><div style='width:230px'>" + description + "</div></html>"
        );
        descriptionLabel.setFont(GuiStyles.BODY_FONT);
        descriptionLabel.setForeground(GuiStyles.TEXT_SECONDARY);
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel capabilitiesLabel = new JLabel(capabilities);
        capabilitiesLabel.setFont(GuiStyles.CAPTION_FONT);
        capabilitiesLabel.setForeground(GuiStyles.TEXT_MUTED);
        capabilitiesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(descriptionLabel);
        contentPanel.add(Box.createVerticalGlue());
        contentPanel.add(Box.createVerticalStrut(12));
        contentPanel.add(capabilitiesLabel);

        add(headingPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        setPreferredSize(new Dimension(290, 178));
    }

    public void setConnected(boolean connected) {
        statusBadge.setConnected(connected);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics.create();

        graphics2D.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        int width = getWidth();
        int height = getHeight();

        graphics2D.setColor(GuiStyles.SHADOW);
        graphics2D.fillRoundRect(2, 4, width - 4, height - 6, 20, 20);

        graphics2D.setColor(GuiStyles.SURFACE);
        graphics2D.fillRoundRect(1, 1, width - 3, height - 5, 20, 20);

        graphics2D.setColor(GuiStyles.BORDER_LIGHT);
        graphics2D.drawRoundRect(1, 1, width - 3, height - 5, 20, 20);

        graphics2D.setColor(accentColor);
        graphics2D.fillRoundRect(1, 1, 6, height - 5, 10, 10);

        graphics2D.dispose();

        super.paintComponent(graphics);
    }

    private static class StatusBadge extends JLabel {

        private Color backgroundColor;

        private StatusBadge() {
            setFont(GuiStyles.LABEL_FONT);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(new EmptyBorder(5, 9, 5, 9));
            setOpaque(false);
        }

        private void setConnected(boolean connected) {
            if (connected) {
                setText("●  ONLINE");
                setForeground(GuiStyles.PRIMARY_DARK);
                backgroundColor = GuiStyles.SUCCESS_BACKGROUND;
            } else {
                setText("●  OFFLINE");
                setForeground(GuiStyles.OFFLINE);
                backgroundColor = GuiStyles.OFFLINE_BACKGROUND;
            }

            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();

            graphics2D.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            graphics2D.setColor(backgroundColor);
            graphics2D.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            graphics2D.dispose();

            super.paintComponent(graphics);
        }
    }
}
