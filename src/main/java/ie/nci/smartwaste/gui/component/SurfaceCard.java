package ie.nci.smartwaste.gui.component;

import ie.nci.smartwaste.gui.util.GuiStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SurfaceCard extends JPanel {

    private final Color accentColor;

    public SurfaceCard() {
        this(null, new Insets(20, 21, 19, 21));
    }

    public SurfaceCard(Color accentColor) {
        this(accentColor, new Insets(20, 21, 19, 21));
    }

    public SurfaceCard(Color accentColor, Insets padding) {
        this.accentColor = accentColor;

        setOpaque(false);
        setBorder(new EmptyBorder(
                padding.top,
                padding.left,
                padding.bottom,
                padding.right
        ));
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
        graphics2D.fillRoundRect(
                2,
                4,
                width - 4,
                height - 6,
                GuiStyles.RADIUS_MEDIUM,
                GuiStyles.RADIUS_MEDIUM
        );

        graphics2D.setColor(GuiStyles.SURFACE);
        graphics2D.fillRoundRect(
                1,
                1,
                width - 3,
                height - 5,
                GuiStyles.RADIUS_MEDIUM,
                GuiStyles.RADIUS_MEDIUM
        );

        graphics2D.setColor(GuiStyles.BORDER_LIGHT);
        graphics2D.drawRoundRect(
                1,
                1,
                width - 3,
                height - 5,
                GuiStyles.RADIUS_MEDIUM,
                GuiStyles.RADIUS_MEDIUM
        );

        if (accentColor != null) {
            graphics2D.setColor(accentColor);
            graphics2D.fillRoundRect(21, 13, 36, 4, 4, 4);
        }

        graphics2D.dispose();
        super.paintComponent(graphics);
    }
}
