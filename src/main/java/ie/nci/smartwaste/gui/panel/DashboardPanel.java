package ie.nci.smartwaste.gui.panel;

import ie.nci.smartwaste.gui.component.MetricCard;
import ie.nci.smartwaste.gui.component.ServiceCard;
import ie.nci.smartwaste.gui.util.GuiStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DashboardPanel extends JPanel {

    private final MetricCard registeredBinsMetric;
    private final MetricCard activeCollectionsMetric;
    private final MetricCard centreIntakeMetric;
    private final MetricCard serviceAvailabilityMetric;

    private final ServiceCard smartBinCard;
    private final ServiceCard collectionCard;
    private final ServiceCard recyclingCard;

    private final JLabel networkStateLabel;
    private final JLabel networkSummaryLabel;

    private boolean smartBinConnected;
    private boolean collectionConnected;
    private boolean recyclingConnected;

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(GuiStyles.BACKGROUND);
        setBorder(new EmptyBorder(18, 22, 18, 22));

        registeredBinsMetric = new MetricCard(
                "Registered smart bins",
                "0",
                "Assets reporting to the network",
                GuiStyles.ACCENT_TEAL
        );

        activeCollectionsMetric = new MetricCard(
                "Active collections",
                "0",
                "Routes currently in progress",
                GuiStyles.ACCENT_AMBER
        );

        centreIntakeMetric = new MetricCard(
                "Centre intake",
                "0 L",
                "Waste received today",
                GuiStyles.PRIMARY
        );

        serviceAvailabilityMetric = new MetricCard(
                "Service availability",
                "0 / 3",
                "Municipal services online",
                GuiStyles.ACCENT_BLUE
        );

        smartBinCard = new ServiceCard(
                "Asset monitoring",
                "Smart Bin Network",
                "Monitor connected bins, fill levels and maintenance alerts across the city.",
                "Registration  ·  Fill levels  ·  Condition alerts",
                GuiStyles.ACCENT_TEAL
        );

        collectionCard = new ServiceCard(
                "Fleet coordination",
                "Collection Operations",
                "Coordinate collection tasks, vehicle assignments and priority routes.",
                "Assignments  ·  Routes  ·  Collection completion",
                GuiStyles.ACCENT_AMBER
        );

        recyclingCard = new ServiceCard(
                "Centre management",
                "Resource Recovery",
                "Track incoming waste, available capacity and processing activity.",
                "Deliveries  ·  Capacity  ·  Waste processing",
                GuiStyles.PRIMARY
        );

        networkStateLabel = new JLabel("Standby");
        networkSummaryLabel = new JLabel("0 of 3 services online");

        ScrollableContentPanel content = new ScrollableContentPanel();
        content.add(createPageHeading());
        content.add(Box.createVerticalStrut(16));
        content.add(createHeroPanel());
        content.add(Box.createVerticalStrut(18));
        content.add(createMetricsRow());
        content.add(Box.createVerticalStrut(24));
        content.add(createSectionHeading(
                "Service network",
                "The operational modules that keep the city waste network moving."
        ));
        content.add(Box.createVerticalStrut(12));
        content.add(createServiceCardsRow());
        content.add(Box.createVerticalStrut(18));
        content.add(createInformationRow());

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
        updateNetworkState();
    }

    private JPanel createPageHeading() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel title = new JLabel("Operations overview");
        title.setFont(GuiStyles.TITLE_FONT);
        title.setForeground(GuiStyles.TEXT_PRIMARY);

        JLabel subtitle = new JLabel(
                "A live view of municipal waste assets, collection activity and resource recovery."
        );
        subtitle.setFont(GuiStyles.BODY_FONT);
        subtitle.setForeground(GuiStyles.TEXT_SECONDARY);

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subtitle);

        JLabel areaLabel = new JLabel("CITY OPERATIONS");
        areaLabel.setFont(GuiStyles.LABEL_FONT);
        areaLabel.setForeground(GuiStyles.PRIMARY_DARK);
        areaLabel.setBorder(new EmptyBorder(8, 12, 8, 12));
        areaLabel.setOpaque(true);
        areaLabel.setBackground(GuiStyles.PRIMARY_LIGHT);

        panel.add(textPanel, BorderLayout.WEST);
        panel.add(areaLabel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createHeroPanel() {
        HeroPanel hero = new HeroPanel();
        hero.setLayout(new BorderLayout(32, 0));
        hero.setBorder(new EmptyBorder(25, 28, 25, 28));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        hero.setPreferredSize(new Dimension(1000, 180));
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setOpaque(false);

        JLabel eyebrow = new JLabel("MUNICIPAL WASTE OPERATIONS CENTRE");
        eyebrow.setFont(GuiStyles.LABEL_FONT);
        eyebrow.setForeground(new Color(183, 225, 203));
        eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Cleaner streets. Smarter routes. Better recovery.");
        title.setFont(GuiStyles.DISPLAY_FONT);
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel(
                "<html><div style='width:620px'>Coordinate the city-wide waste network from one place — "
                        + "from connected street bins to collection fleets and recycling centres.</div></html>"
        );
        description.setFont(GuiStyles.BODY_FONT);
        description.setForeground(new Color(220, 235, 227));
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        messagePanel.add(eyebrow);
        messagePanel.add(Box.createVerticalStrut(9));
        messagePanel.add(title);
        messagePanel.add(Box.createVerticalStrut(10));
        messagePanel.add(description);

        JPanel statePanel = new JPanel();
        statePanel.setLayout(new BoxLayout(statePanel, BoxLayout.Y_AXIS));
        statePanel.setOpaque(false);
        statePanel.setBorder(new EmptyBorder(12, 18, 12, 18));
        statePanel.setPreferredSize(new Dimension(220, 125));

        JLabel stateTitle = new JLabel("NETWORK STATUS");
        stateTitle.setFont(GuiStyles.LABEL_FONT);
        stateTitle.setForeground(new Color(185, 216, 201));

        networkStateLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 23));
        networkStateLabel.setForeground(Color.WHITE);

        networkSummaryLabel.setFont(GuiStyles.BODY_FONT);
        networkSummaryLabel.setForeground(new Color(216, 232, 224));

        stateTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        networkStateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        networkSummaryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        statePanel.add(stateTitle);
        statePanel.add(Box.createVerticalStrut(10));
        statePanel.add(networkStateLabel);
        statePanel.add(Box.createVerticalStrut(5));
        statePanel.add(networkSummaryLabel);

        JPanel stateSurface = new TranslucentPanel();
        stateSurface.setLayout(new BorderLayout());
        stateSurface.add(statePanel, BorderLayout.CENTER);

        hero.add(messagePanel, BorderLayout.CENTER);
        hero.add(stateSurface, BorderLayout.EAST);

        return hero;
    }

    private JPanel createMetricsRow() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 12, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 118));
        panel.setPreferredSize(new Dimension(1000, 118));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(registeredBinsMetric);
        panel.add(activeCollectionsMetric);
        panel.add(centreIntakeMetric);
        panel.add(serviceAvailabilityMetric);

        return panel;
    }

    private JPanel createSectionHeading(String titleText, String subtitleText) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel(titleText);
        title.setFont(GuiStyles.SECTION_TITLE_FONT);
        title.setForeground(GuiStyles.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel(subtitleText);
        subtitle.setFont(GuiStyles.BODY_FONT);
        subtitle.setForeground(GuiStyles.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(4));
        panel.add(subtitle);

        return panel;
    }

    private JPanel createServiceCardsRow() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 14, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        panel.setPreferredSize(new Dimension(1000, 180));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(smartBinCard);
        panel.add(collectionCard);
        panel.add(recyclingCard);

        return panel;
    }

    private JPanel createInformationRow() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 14, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        panel.setPreferredSize(new Dimension(1000, 220));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(createPrioritiesPanel());
        panel.add(createNetworkFlowPanel());

        return panel;
    }

    private JPanel createPrioritiesPanel() {
        SurfacePanel panel = new SurfacePanel();
        panel.setLayout(new BorderLayout(0, 16));

        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setOpaque(false);

        JLabel title = new JLabel("Today's priorities");
        title.setFont(GuiStyles.SECTION_TITLE_FONT);
        title.setForeground(GuiStyles.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Operational alerts that require attention.");
        subtitle.setFont(GuiStyles.BODY_FONT);
        subtitle.setForeground(GuiStyles.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        heading.add(title);
        heading.add(Box.createVerticalStrut(4));
        heading.add(subtitle);

        JPanel emptyState = new JPanel(new BorderLayout(14, 0));
        emptyState.setOpaque(true);
        emptyState.setBackground(GuiStyles.SURFACE_SECONDARY);
        emptyState.setBorder(new EmptyBorder(15, 16, 15, 16));

        JLabel icon = new JLabel("✓");
        icon.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        icon.setForeground(GuiStyles.PRIMARY);

        JLabel message = new JLabel(
                "<html><b>No active alerts</b><br>Start the service network to receive live operational events.</html>"
        );
        message.setFont(GuiStyles.BODY_FONT);
        message.setForeground(GuiStyles.TEXT_SECONDARY);

        emptyState.add(icon, BorderLayout.WEST);
        emptyState.add(message, BorderLayout.CENTER);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(emptyState, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createNetworkFlowPanel() {
        SurfacePanel panel = new SurfacePanel();
        panel.setLayout(new BorderLayout(0, 15));

        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setOpaque(false);

        JLabel title = new JLabel("How the network works");
        title.setFont(GuiStyles.SECTION_TITLE_FONT);
        title.setForeground(GuiStyles.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("One continuous flow from street assets to resource recovery.");
        subtitle.setFont(GuiStyles.BODY_FONT);
        subtitle.setForeground(GuiStyles.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        heading.add(title);
        heading.add(Box.createVerticalStrut(4));
        heading.add(subtitle);

        JPanel steps = new JPanel(new GridLayout(3, 1, 0, 7));
        steps.setOpaque(false);
        steps.add(createFlowStep("01", "Bins report", "Fill levels and condition updates arrive live."));
        steps.add(createFlowStep("02", "Routes adapt", "Collections are prioritised by operational need."));
        steps.add(createFlowStep("03", "Waste is recovered", "Centres track intake, capacity and processing."));

        panel.add(heading, BorderLayout.NORTH);
        panel.add(steps, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFlowStep(String number, String title, String description) {
        JPanel step = new JPanel(new BorderLayout(12, 0));
        step.setOpaque(false);

        JLabel numberLabel = new JLabel(number);
        numberLabel.setFont(GuiStyles.LABEL_FONT);
        numberLabel.setForeground(GuiStyles.PRIMARY_DARK);
        numberLabel.setHorizontalAlignment(SwingConstants.CENTER);
        numberLabel.setPreferredSize(new Dimension(28, 28));
        numberLabel.setOpaque(true);
        numberLabel.setBackground(GuiStyles.PRIMARY_LIGHT);

        JLabel text = new JLabel("<html><b>" + title + "</b>  ·  " + description + "</html>");
        text.setFont(GuiStyles.BODY_FONT);
        text.setForeground(GuiStyles.TEXT_SECONDARY);

        step.add(numberLabel, BorderLayout.WEST);
        step.add(text, BorderLayout.CENTER);

        return step;
    }

    public void setSmartBinConnected(boolean connected) {
        smartBinConnected = connected;
        smartBinCard.setConnected(connected);
        updateNetworkState();
    }

    public void setCollectionConnected(boolean connected) {
        collectionConnected = connected;
        collectionCard.setConnected(connected);
        updateNetworkState();
    }

    public void setRecyclingConnected(boolean connected) {
        recyclingConnected = connected;
        recyclingCard.setConnected(connected);
        updateNetworkState();
    }

    public void setRegisteredBinCount(int count) {
        registeredBinsMetric.setValue(String.valueOf(Math.max(0, count)));
    }

    public void setActiveCollectionCount(int count) {
        activeCollectionsMetric.setValue(String.valueOf(Math.max(0, count)));
    }

    public void setCentreIntakeLitres(int litres) {
        centreIntakeMetric.setValue(Math.max(0, litres) + " L");
    }

    private void updateNetworkState() {
        int connectedServices = 0;

        if (smartBinConnected) {
            connectedServices++;
        }
        if (collectionConnected) {
            connectedServices++;
        }
        if (recyclingConnected) {
            connectedServices++;
        }

        serviceAvailabilityMetric.setValue(connectedServices + " / 3");
        networkSummaryLabel.setText(connectedServices + " of 3 services online");

        if (connectedServices == 3) {
            networkStateLabel.setText("Operational");
        } else if (connectedServices > 0) {
            networkStateLabel.setText("Partially online");
        } else {
            networkStateLabel.setText("Standby");
        }
    }

    private static class ScrollableContentPanel extends JPanel implements Scrollable {

        private ScrollableContentPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(GuiStyles.BACKGROUND);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(visibleRect.height - 40, 40);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private static class HeroPanel extends JPanel {

        private HeroPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            GradientPaint gradient = new GradientPaint(
                    0,
                    0,
                    GuiStyles.HERO_START,
                    getWidth(),
                    getHeight(),
                    GuiStyles.HERO_END
            );

            graphics2D.setPaint(gradient);
            graphics2D.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    GuiStyles.RADIUS_LARGE,
                    GuiStyles.RADIUS_LARGE
            );
            graphics2D.dispose();

            super.paintComponent(graphics);
        }
    }

    private static class TranslucentPanel extends JPanel {

        private TranslucentPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );
            graphics2D.setColor(new Color(255, 255, 255, 24));
            graphics2D.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    GuiStyles.RADIUS_MEDIUM,
                    GuiStyles.RADIUS_MEDIUM
            );
            graphics2D.dispose();

            super.paintComponent(graphics);
        }
    }

    private static class SurfacePanel extends JPanel {

        private SurfacePanel() {
            setOpaque(false);
            setBorder(new EmptyBorder(20, 21, 19, 21));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            graphics2D.setColor(GuiStyles.SHADOW);
            graphics2D.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 6, 18, 18);

            graphics2D.setColor(GuiStyles.SURFACE);
            graphics2D.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 5, 18, 18);

            graphics2D.setColor(GuiStyles.BORDER_LIGHT);
            graphics2D.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 5, 18, 18);
            graphics2D.dispose();

            super.paintComponent(graphics);
        }
    }
}
