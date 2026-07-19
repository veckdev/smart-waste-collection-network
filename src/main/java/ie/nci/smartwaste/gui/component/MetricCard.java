package ie.nci.smartwaste.gui.component;

import ie.nci.smartwaste.gui.util.GuiStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MetricCard extends JPanel {

    private final JLabel valueLabel;
    private final JLabel detailLabel;
    private final Color accentColor;

    public MetricCard(
            String label,
            String value,
            String detail,
            Color accentColor
    ) {
        this.accentColor = accentColor;

        setLayout(new BorderLayout(0, 10));
        setOpaque(false);
        setBorder(new EmptyBorder(17, 18, 16, 18));

        JLabel labelComponent = new JLabel(label.toUpperCase());
        labelComponent.setFont(GuiStyles.LABEL_FONT);
        labelComponent.setForeground(GuiStyles.TEXT_SECONDARY);

        valueLabel = new JLabel(value);
        valueLabel.setFont(GuiStyles.METRIC_FONT);
        valueLabel.setForeground(GuiStyles.TEXT_PRIMARY);

        detailLabel = new JLabel(detail);
        detailLabel.setFont(GuiStyles.CAPTION_FONT);
        detailLabel.setForeground(GuiStyles.TEXT_MUTED);

        JPanel valuePanel = new JPanel();
        valuePanel.setLayout(new BoxLayout(valuePanel, BoxLayout.Y_AXIS));
        valuePanel.setOpaque(false);

        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        valuePanel.add(valueLabel);
        valuePanel.add(Box.createVerticalStrut(3));
        valuePanel.add(detailLabel);

        add(labelComponent, BorderLayout.NORTH);
        add(valuePanel, BorderLayout.CENTER);

        setPreferredSize(new Dimension(220, 116));
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setDetail(String detail) {
        detailLabel.setText(detail);
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
        graphics2D.fillRoundRect(2, 4, width - 4, height - 6, 18, 18);

        graphics2D.setColor(GuiStyles.SURFACE);
        graphics2D.fillRoundRect(1, 1, width - 3, height - 5, 18, 18);

        graphics2D.setColor(GuiStyles.BORDER_LIGHT);
        graphics2D.drawRoundRect(1, 1, width - 3, height - 5, 18, 18);

        graphics2D.setColor(accentColor);
        graphics2D.fillRoundRect(18, 12, 34, 4, 4, 4);

        graphics2D.dispose();

        super.paintComponent(graphics);
    }
}
