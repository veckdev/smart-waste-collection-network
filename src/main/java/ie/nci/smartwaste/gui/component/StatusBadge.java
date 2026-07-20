package ie.nci.smartwaste.gui.component;

import ie.nci.smartwaste.gui.util.GuiStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StatusBadge extends JLabel {

    public enum State {
        CONNECTED,
        OFFLINE,
        WARNING,
        DISCONNECTED,
        STANDBY
    }

    private Color backgroundColor;
    private State state;

    public StatusBadge(State initialState) {
        setFont(GuiStyles.LABEL_FONT);
        setHorizontalAlignment(SwingConstants.CENTER);
        setBorder(new EmptyBorder(7, 11, 7, 11));
        setOpaque(false);
        setState(initialState);
    }

    public void setState(State state) {
        setState(state, defaultLabel(state));
    }

    public void setState(State state, String label) {
        this.state = state;

        switch (state) {
            case CONNECTED -> configure(
                    label,
                    GuiStyles.PRIMARY_DARK,
                    GuiStyles.SUCCESS_BACKGROUND
            );
            case WARNING -> configure(
                    label,
                    GuiStyles.WARNING,
                    new Color(250, 235, 209)
            );
            case DISCONNECTED -> configure(
                    label,
                    GuiStyles.ERROR,
                    new Color(249, 224, 222)
            );
            case STANDBY -> configure(
                    label,
                    GuiStyles.TEXT_SECONDARY,
                    GuiStyles.SAGE
            );
            case OFFLINE -> configure(
                    label,
                    GuiStyles.OFFLINE,
                    GuiStyles.OFFLINE_BACKGROUND
            );
        }

        repaint();
    }

    private String defaultLabel(State state) {
        return switch (state) {
            case CONNECTED -> "CONNECTED";
            case OFFLINE -> "OFFLINE";
            case WARNING -> "WARNING";
            case DISCONNECTED -> "DISCONNECTED";
            case STANDBY -> "STANDBY";
        };
    }

    public State getState() {
        return state;
    }

    private void configure(String text, Color foreground, Color background) {
        setText("●  " + text);
        setForeground(foreground);
        backgroundColor = background;
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
