package ie.nci.smartwaste.gui.component;

import ie.nci.smartwaste.gui.util.GuiStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ServiceStatusPanel extends JPanel {

    private final JLabel indicatorLabel;
    private final JLabel statusLabel;
    private boolean connected;

    public ServiceStatusPanel(String serviceName) {
        setLayout(new BorderLayout(8, 0));
        setOpaque(false);
        setBorder(new EmptyBorder(7, 11, 7, 11));

        indicatorLabel = new JLabel("●");
        indicatorLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel serviceLabel = new JLabel(serviceName);
        serviceLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        serviceLabel.setForeground(Color.WHITE);
        serviceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        statusLabel = new JLabel();
        statusLabel.setFont(GuiStyles.LABEL_FONT);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(serviceLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(statusLabel);

        add(indicatorLabel, BorderLayout.WEST);
        add(textPanel, BorderLayout.CENTER);

        setConnected(false);
    }

    public void setConnected(boolean connected) {
        this.connected = connected;

        if (connected) {
            indicatorLabel.setForeground(new Color(103, 226, 153));
            statusLabel.setText("ONLINE");
            statusLabel.setForeground(new Color(184, 237, 207));
        } else {
            indicatorLabel.setForeground(new Color(158, 177, 168));
            statusLabel.setText("OFFLINE");
            statusLabel.setForeground(new Color(184, 200, 192));
        }

        repaint();
    }

    public boolean isConnected() {
        return connected;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics.create();
        graphics2D.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        graphics2D.setColor(
                connected
                        ? new Color(78, 158, 112, 45)
                        : new Color(255, 255, 255, 18)
        );
        graphics2D.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
        graphics2D.dispose();

        super.paintComponent(graphics);
    }
}
