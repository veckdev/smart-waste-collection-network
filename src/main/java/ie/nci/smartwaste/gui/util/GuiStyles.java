package ie.nci.smartwaste.gui.util;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public final class GuiStyles {

    /*
     * ECO DESIGN SYSTEM
     *
     * The palette combines forest tones, sage surfaces and neutral stone
     * colours to communicate sustainability without making the interface
     * excessively green.
     */

    public static final Color BACKGROUND = new Color(242, 246, 243);
    public static final Color SURFACE = Color.WHITE;
    public static final Color SURFACE_SECONDARY = new Color(247, 250, 248);

    public static final Color HEADER = new Color(23, 52, 43);
    public static final Color HEADER_SECONDARY = new Color(31, 73, 58);
    public static final Color HERO_START = new Color(28, 78, 60);
    public static final Color HERO_END = new Color(47, 118, 82);

    public static final Color PRIMARY = new Color(45, 132, 85);
    public static final Color PRIMARY_DARK = new Color(32, 101, 66);
    public static final Color PRIMARY_LIGHT = new Color(219, 242, 228);

    public static final Color SAGE = new Color(229, 238, 232);
    public static final Color SAGE_DARK = new Color(96, 127, 108);

    public static final Color TEXT_PRIMARY = new Color(28, 45, 38);
    public static final Color TEXT_SECONDARY = new Color(91, 111, 101);
    public static final Color TEXT_MUTED = new Color(126, 143, 134);

    public static final Color BORDER = new Color(211, 223, 216);
    public static final Color BORDER_LIGHT = new Color(229, 236, 232);
    public static final Color SHADOW = new Color(30, 61, 48, 22);

    public static final Color SUCCESS = new Color(39, 145, 88);
    public static final Color SUCCESS_BACKGROUND = new Color(220, 244, 230);

    public static final Color OFFLINE = new Color(133, 146, 140);
    public static final Color OFFLINE_BACKGROUND = new Color(235, 239, 237);

    public static final Color WARNING = new Color(190, 132, 45);
    public static final Color ERROR = new Color(184, 72, 67);
    public static final Color STREAMING = new Color(55, 116, 151);
    public static final Color ACCENT_BLUE = new Color(61, 126, 147);
    public static final Color ACCENT_AMBER = new Color(197, 143, 55);
    public static final Color ACCENT_TEAL = new Color(50, 139, 121);

    public static final Font DISPLAY_FONT =
            new Font(Font.SANS_SERIF, Font.BOLD, 31);

    public static final Font TITLE_FONT =
            new Font(Font.SANS_SERIF, Font.BOLD, 24);

    public static final Font SECTION_TITLE_FONT =
            new Font(Font.SANS_SERIF, Font.BOLD, 17);

    public static final Font CARD_TITLE_FONT =
            new Font(Font.SANS_SERIF, Font.BOLD, 16);

    public static final Font METRIC_FONT =
            new Font(Font.SANS_SERIF, Font.BOLD, 27);

    public static final Font LABEL_FONT =
            new Font(Font.SANS_SERIF, Font.BOLD, 10);

    public static final Font BODY_FONT =
            new Font(Font.SANS_SERIF, Font.PLAIN, 13);

    public static final Font BODY_BOLD_FONT =
            new Font(Font.SANS_SERIF, Font.BOLD, 13);

    public static final Font CAPTION_FONT =
            new Font(Font.SANS_SERIF, Font.PLAIN, 11);

    public static final int RADIUS_LARGE = 22;
    public static final int RADIUS_MEDIUM = 16;

    private GuiStyles() {
    }

    public static Border createPanelBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        );
    }

    public static Border createCompactPanelBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        );
    }

    public static JPanel createSurfacePanel() {
        JPanel panel = new JPanel();
        panel.setBackground(SURFACE);
        panel.setBorder(createPanelBorder());
        return panel;
    }

    public static JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SECTION_TITLE_FONT);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    public static JLabel createBodyLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(BODY_FONT);
        label.setForeground(TEXT_SECONDARY);
        return label;
    }

    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);

        button.setFont(BODY_BOLD_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setBorder(
                BorderFactory.createEmptyBorder(10, 18, 10, 18)
        );
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        return button;
    }

    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);

        button.setFont(BODY_BOLD_FONT);
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(SURFACE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        BorderFactory.createEmptyBorder(7, 14, 7, 14)
                )
        );
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        return button;
    }
}
