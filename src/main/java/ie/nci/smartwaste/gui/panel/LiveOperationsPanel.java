package ie.nci.smartwaste.gui.panel;

import ie.nci.smartwaste.gui.component.MetricCard;
import ie.nci.smartwaste.gui.component.StatusBadge;
import ie.nci.smartwaste.gui.component.SurfaceCard;
import ie.nci.smartwaste.gui.util.GuiStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LiveOperationsPanel extends JPanel {

    private static final String EMPTY_VIEW = "empty";
    private static final String DATA_VIEW = "data";

    private final MetricCard connectedServicesMetric;
    private final MetricCard streamingEventsMetric;
    private final MetricCard networkRequestsMetric;
    private final MetricCard systemUptimeMetric;

    private final JLabel networkHealthLabel;
    private final JLabel networkSummaryLabel;
    private final JLabel serviceCountBadge;

    private final DefaultTableModel servicesTableModel;
    private final DefaultTableModel timelineTableModel;
    private final JTable servicesTable;
    private final JTable timelineTable;

    private final CardLayout servicesContentLayout;
    private final JPanel servicesContentPanel;
    private final CardLayout inspectorContentLayout;
    private final JPanel inspectorContentPanel;
    private final CardLayout timelineContentLayout;
    private final JPanel timelineContentPanel;

    private final JLabel selectedServiceNameValue;
    private final JLabel selectedHostValue;
    private final JLabel selectedPortValue;
    private final JLabel selectedStatusValue;
    private final JLabel selectedRegisteredSinceValue;
    private final JLabel selectedConnectionsValue;
    private final JLabel selectedLastActivityValue;

    private final MonitorFeed discoveryFeed;
    private final MonitorFeed streamingFeed;
    private final MonitorFeed requestFeed;

    private final StatusBadge smartBinHealthBadge;
    private final StatusBadge collectionHealthBadge;
    private final StatusBadge recyclingHealthBadge;
    private final StatusBadge overallHealthBadge;

    private final Map<String, ServiceViewData> servicesByName = new LinkedHashMap<>();

    public LiveOperationsPanel() {
        setLayout(new BorderLayout());
        setBackground(GuiStyles.BACKGROUND);
        setBorder(new EmptyBorder(18, 22, 18, 22));

        connectedServicesMetric = new MetricCard(
                "Connected Services",
                "0 / 3",
                "Smart Bin, Collection and Recycling",
                GuiStyles.ACCENT_TEAL
        );
        streamingEventsMetric = new MetricCard(
                "Streaming Events",
                "0",
                "Events received",
                GuiStyles.STREAMING
        );
        networkRequestsMetric = new MetricCard(
                "Network Requests",
                "0",
                "Operations processed",
                GuiStyles.ACCENT_AMBER
        );
        systemUptimeMetric = new MetricCard(
                "System Uptime",
                "--",
                "Awaiting service startup",
                GuiStyles.PRIMARY
        );

        networkHealthLabel = new JLabel("Standby");
        networkSummaryLabel = new JLabel("0 of 3 services connected");
        serviceCountBadge = new JLabel("0 SERVICES");

        servicesTableModel = createReadOnlyTableModel(
                "Service",
                "Address",
                "Port",
                "Status",
                "Last Heartbeat"
        );
        timelineTableModel = createReadOnlyTableModel(
                "Time",
                "Event",
                "Service",
                "Details"
        );

        servicesTable = new JTable(servicesTableModel);
        timelineTable = new JTable(timelineTableModel);
        GuiStyles.styleTable(servicesTable);
        GuiStyles.styleTable(timelineTable);
        servicesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        servicesContentLayout = new CardLayout();
        servicesContentPanel = new JPanel(servicesContentLayout);
        inspectorContentLayout = new CardLayout();
        inspectorContentPanel = new JPanel(inspectorContentLayout);
        timelineContentLayout = new CardLayout();
        timelineContentPanel = new JPanel(timelineContentLayout);

        selectedServiceNameValue = createInspectorValue();
        selectedHostValue = createInspectorValue();
        selectedPortValue = createInspectorValue();
        selectedStatusValue = createInspectorValue();
        selectedRegisteredSinceValue = createInspectorValue();
        selectedConnectionsValue = createInspectorValue();
        selectedLastActivityValue = createInspectorValue();

        discoveryFeed = new MonitorFeed(
                "Service Discovery",
                "Track services joining and leaving the municipal network.",
                "No discovery events.",
                GuiStyles.ACCENT_TEAL
        );
        streamingFeed = new MonitorFeed(
                "Streaming Monitor",
                "Observe live data moving between municipal services.",
                "No streaming activity.",
                GuiStyles.STREAMING
        );
        requestFeed = new MonitorFeed(
                "Request Activity",
                "Review recent operations processed across the platform.",
                "No request activity.",
                GuiStyles.ACCENT_AMBER
        );

        smartBinHealthBadge = new StatusBadge(StatusBadge.State.OFFLINE);
        collectionHealthBadge = new StatusBadge(StatusBadge.State.OFFLINE);
        recyclingHealthBadge = new StatusBadge(StatusBadge.State.OFFLINE);
        overallHealthBadge = new StatusBadge(StatusBadge.State.STANDBY);

        configureDataViews();

        ScrollableContentPanel content = new ScrollableContentPanel();
        content.add(createPageHeading());
        content.add(Box.createVerticalStrut(16));
        content.add(createHeroPanel());
        content.add(Box.createVerticalStrut(18));
        content.add(createMetricsRow());
        content.add(Box.createVerticalStrut(24));
        content.add(createSectionHeading(
                "Service status",
                "Inspect every municipal service currently visible on the operational network."
        ));
        content.add(Box.createVerticalStrut(12));
        content.add(createWorkspace());
        content.add(Box.createVerticalStrut(24));
        content.add(createSectionHeading(
                "Network activity",
                "Observe discovery, streaming and request activity across the platform."
        ));
        content.add(Box.createVerticalStrut(12));
        content.add(createMonitoringRow());
        content.add(Box.createVerticalStrut(24));
        content.add(createTimelineCard());
        content.add(Box.createVerticalStrut(18));
        content.add(createSystemHealthCard());

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void configureDataViews() {
        servicesContentPanel.setOpaque(false);
        servicesContentPanel.add(
                createEmptyState(
                        "No services discovered.",
                        "Start the platform to begin service discovery."
                ),
                EMPTY_VIEW
        );
        servicesContentPanel.add(createTableScrollPane(servicesTable), DATA_VIEW);
        servicesContentLayout.show(servicesContentPanel, EMPTY_VIEW);

        inspectorContentPanel.setOpaque(false);
        inspectorContentPanel.add(
                createEmptyState(
                        "No service selected.",
                        "Select a connected service to inspect operational details."
                ),
                EMPTY_VIEW
        );
        inspectorContentPanel.add(createServiceDetailsPanel(), DATA_VIEW);
        inspectorContentLayout.show(inspectorContentPanel, EMPTY_VIEW);

        timelineContentPanel.setOpaque(false);
        timelineContentPanel.add(
                createEmptyState(
                        "No events received.",
                        "Operational events will appear here as the network becomes active."
                ),
                EMPTY_VIEW
        );
        timelineContentPanel.add(createTableScrollPane(timelineTable), DATA_VIEW);
        timelineContentLayout.show(timelineContentPanel, EMPTY_VIEW);

        servicesTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                updateSelectedService();
            }
        });
    }

    private JPanel createPageHeading() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel title = new JLabel("Live Network Operations");
        title.setFont(GuiStyles.TITLE_FONT);
        title.setForeground(GuiStyles.TEXT_PRIMARY);

        JLabel subtitle = new JLabel(
                "Monitor the health and activity of the Smart Waste Collection Network."
        );
        subtitle.setFont(GuiStyles.BODY_FONT);
        subtitle.setForeground(GuiStyles.TEXT_SECONDARY);

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subtitle);

        JLabel areaLabel = new JLabel("NETWORK OPERATIONS");
        areaLabel.setFont(GuiStyles.LABEL_FONT);
        areaLabel.setForeground(new Color(39, 92, 117));
        areaLabel.setBorder(new EmptyBorder(8, 12, 8, 12));
        areaLabel.setOpaque(true);
        areaLabel.setBackground(new Color(221, 237, 244));

        panel.add(textPanel, BorderLayout.WEST);
        panel.add(areaLabel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createHeroPanel() {
        HeroPanel hero = new HeroPanel();
        hero.setLayout(new BorderLayout(32, 0));
        hero.setBorder(new EmptyBorder(25, 28, 25, 28));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 195));
        hero.setPreferredSize(new Dimension(1000, 195));
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setOpaque(false);

        JLabel eyebrow = new JLabel("MUNICIPAL NETWORK OPERATIONS CENTRE");
        eyebrow.setFont(GuiStyles.LABEL_FONT);
        eyebrow.setForeground(new Color(176, 218, 215));
        eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel heroTitle = new JPanel();
        heroTitle.setLayout(new BoxLayout(heroTitle, BoxLayout.Y_AXIS));
        heroTitle.setOpaque(false);
        heroTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLineOne = new JLabel("Real-time monitoring for every connected");
        titleLineOne.setFont(GuiStyles.DISPLAY_FONT);
        titleLineOne.setForeground(Color.WHITE);
        titleLineOne.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLineTwo = new JLabel("municipal service.");
        titleLineTwo.setFont(GuiStyles.DISPLAY_FONT);
        titleLineTwo.setForeground(Color.WHITE);
        titleLineTwo.setAlignmentX(Component.LEFT_ALIGNMENT);

        heroTitle.add(titleLineOne);
        heroTitle.add(titleLineTwo);

        JLabel description = new JLabel(
                "<html><div style='width:650px'>Monitor service discovery, streaming events, operational "
                        + "health and distributed communication across the municipal waste platform."
                        + "</div></html>"
        );
        description.setFont(GuiStyles.BODY_FONT);
        description.setForeground(new Color(220, 235, 227));
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        messagePanel.add(eyebrow);
        messagePanel.add(Box.createVerticalStrut(9));
        messagePanel.add(heroTitle);
        messagePanel.add(Box.createVerticalStrut(10));
        messagePanel.add(description);

        JPanel statusContent = new JPanel();
        statusContent.setLayout(new BoxLayout(statusContent, BoxLayout.Y_AXIS));
        statusContent.setOpaque(false);
        statusContent.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel statusTitle = new JLabel("NETWORK HEALTH");
        statusTitle.setFont(GuiStyles.LABEL_FONT);
        statusTitle.setForeground(new Color(185, 216, 201));

        networkHealthLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 23));
        networkHealthLabel.setForeground(Color.WHITE);

        networkSummaryLabel.setFont(GuiStyles.BODY_FONT);
        networkSummaryLabel.setForeground(new Color(216, 232, 224));

        statusTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        networkHealthLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        networkSummaryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        statusContent.add(statusTitle);
        statusContent.add(Box.createVerticalStrut(10));
        statusContent.add(networkHealthLabel);
        statusContent.add(Box.createVerticalStrut(5));
        statusContent.add(networkSummaryLabel);

        TranslucentPanel statusSurface = new TranslucentPanel();
        statusSurface.setLayout(new BorderLayout());
        statusSurface.setPreferredSize(new Dimension(230, 125));
        statusSurface.add(statusContent, BorderLayout.CENTER);

        hero.add(messagePanel, BorderLayout.CENTER);
        hero.add(statusSurface, BorderLayout.EAST);

        return hero;
    }

    private JPanel createMetricsRow() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 12, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 118));
        panel.setPreferredSize(new Dimension(1000, 118));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(connectedServicesMetric);
        panel.add(streamingEventsMetric);
        panel.add(networkRequestsMetric);
        panel.add(systemUptimeMetric);

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

    private JPanel createWorkspace() {
        JPanel workspace = new JPanel(new GridBagLayout());
        workspace.setOpaque(false);
        workspace.setMaximumSize(new Dimension(Integer.MAX_VALUE, 350));
        workspace.setPreferredSize(new Dimension(1000, 350));
        workspace.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weighty = 1;

        constraints.gridx = 0;
        constraints.weightx = 0.65;
        constraints.insets = new Insets(0, 0, 0, 7);
        JPanel servicesCard = createConnectedServicesCard();
        servicesCard.setPreferredSize(new Dimension(650, 350));
        workspace.add(servicesCard, constraints);

        constraints.gridx = 1;
        constraints.weightx = 0.35;
        constraints.insets = new Insets(0, 7, 0, 0);
        JPanel inspectorCard = createServiceInspectorCard();
        inspectorCard.setPreferredSize(new Dimension(350, 350));
        workspace.add(inspectorCard, constraints);

        return workspace;
    }

    private JPanel createConnectedServicesCard() {
        SurfaceCard card = new SurfaceCard();
        card.setLayout(new BorderLayout(0, 15));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);

        JLabel title = new JLabel("Connected Services");
        title.setFont(GuiStyles.SECTION_TITLE_FONT);
        title.setForeground(GuiStyles.TEXT_PRIMARY);

        JLabel description = new JLabel("Municipal services currently visible on the network.");
        description.setFont(GuiStyles.BODY_FONT);
        description.setForeground(GuiStyles.TEXT_SECONDARY);

        text.add(title);
        text.add(Box.createVerticalStrut(4));
        text.add(description);

        serviceCountBadge.setFont(GuiStyles.LABEL_FONT);
        serviceCountBadge.setForeground(new Color(39, 92, 117));
        serviceCountBadge.setOpaque(true);
        serviceCountBadge.setBackground(new Color(221, 237, 244));
        serviceCountBadge.setBorder(new EmptyBorder(7, 10, 7, 10));

        heading.add(text, BorderLayout.WEST);
        heading.add(serviceCountBadge, BorderLayout.EAST);

        card.add(heading, BorderLayout.NORTH);
        card.add(servicesContentPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createServiceInspectorCard() {
        SurfaceCard card = new SurfaceCard();
        card.setLayout(new BorderLayout(0, 15));

        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setOpaque(false);

        JLabel title = new JLabel("Service Inspector");
        title.setFont(GuiStyles.SECTION_TITLE_FONT);
        title.setForeground(GuiStyles.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel("Runtime details for the selected municipal service.");
        description.setFont(GuiStyles.BODY_FONT);
        description.setForeground(GuiStyles.TEXT_SECONDARY);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        heading.add(title);
        heading.add(Box.createVerticalStrut(4));
        heading.add(description);

        card.add(heading, BorderLayout.NORTH);
        card.add(inspectorContentPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createServiceDetailsPanel() {
        JPanel details = new JPanel(new GridLayout(7, 2, 12, 9));
        details.setOpaque(false);
        addInspectorRow(details, "Service Name", selectedServiceNameValue);
        addInspectorRow(details, "Host", selectedHostValue);
        addInspectorRow(details, "Port", selectedPortValue);
        addInspectorRow(details, "Current Status", selectedStatusValue);
        addInspectorRow(details, "Registered Since", selectedRegisteredSinceValue);
        addInspectorRow(details, "Active Connections", selectedConnectionsValue);
        addInspectorRow(details, "Last Activity", selectedLastActivityValue);
        return details;
    }

    private void addInspectorRow(JPanel panel, String labelText, JLabel valueLabel) {
        JLabel label = new JLabel(labelText);
        label.setFont(GuiStyles.BODY_FONT);
        label.setForeground(GuiStyles.TEXT_SECONDARY);
        panel.add(label);
        panel.add(valueLabel);
    }

    private JLabel createInspectorValue() {
        JLabel label = new JLabel("—");
        label.setFont(GuiStyles.BODY_BOLD_FONT);
        label.setForeground(GuiStyles.TEXT_PRIMARY);
        return label;
    }

    private JPanel createMonitoringRow() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 14, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 270));
        panel.setPreferredSize(new Dimension(1000, 270));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(discoveryFeed);
        panel.add(streamingFeed);
        panel.add(requestFeed);

        return panel;
    }

    private JPanel createTimelineCard() {
        SurfaceCard card = new SurfaceCard(GuiStyles.ACCENT_TEAL);
        card.setLayout(new BorderLayout(0, 15));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 285));
        card.setPreferredSize(new Dimension(1000, 285));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setOpaque(false);

        JLabel title = new JLabel("Live Event Timeline");
        title.setFont(GuiStyles.SECTION_TITLE_FONT);
        title.setForeground(GuiStyles.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel(
                "Monitor the latest operational events occurring across the municipal network."
        );
        description.setFont(GuiStyles.BODY_FONT);
        description.setForeground(GuiStyles.TEXT_SECONDARY);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        heading.add(title);
        heading.add(Box.createVerticalStrut(4));
        heading.add(description);

        card.add(heading, BorderLayout.NORTH);
        card.add(timelineContentPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createSystemHealthCard() {
        SurfaceCard card = new SurfaceCard();
        card.setLayout(new BorderLayout(20, 0));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        card.setPreferredSize(new Dimension(1000, 130));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setOpaque(false);

        JLabel title = new JLabel("System Health");
        title.setFont(GuiStyles.SECTION_TITLE_FONT);
        title.setForeground(GuiStyles.TEXT_PRIMARY);

        JLabel description = new JLabel("Current availability of the municipal service network.");
        description.setFont(GuiStyles.BODY_FONT);
        description.setForeground(GuiStyles.TEXT_SECONDARY);

        heading.add(title);
        heading.add(Box.createVerticalStrut(4));
        heading.add(description);

        JPanel indicators = new JPanel(new GridLayout(1, 4, 10, 0));
        indicators.setOpaque(false);
        indicators.add(createHealthIndicator("Smart Bin Service", smartBinHealthBadge));
        indicators.add(createHealthIndicator("Collection Service", collectionHealthBadge));
        indicators.add(createHealthIndicator("Recycling Service", recyclingHealthBadge));
        indicators.add(createHealthIndicator("Overall Network", overallHealthBadge));

        card.add(heading, BorderLayout.WEST);
        card.add(indicators, BorderLayout.CENTER);

        return card;
    }

    private JPanel createHealthIndicator(String serviceName, StatusBadge badge) {
        JPanel indicator = new JPanel();
        indicator.setLayout(new BoxLayout(indicator, BoxLayout.Y_AXIS));
        indicator.setOpaque(true);
        indicator.setBackground(GuiStyles.SURFACE_SECONDARY);
        indicator.setBorder(new EmptyBorder(11, 12, 10, 12));

        JLabel name = new JLabel(serviceName);
        name.setFont(GuiStyles.BODY_BOLD_FONT);
        name.setForeground(GuiStyles.TEXT_PRIMARY);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);

        indicator.add(name);
        indicator.add(Box.createVerticalStrut(7));
        indicator.add(badge);

        return indicator;
    }

    private JPanel createEmptyState(String titleText, String descriptionText) {
        JPanel emptyState = new JPanel(new GridBagLayout());
        emptyState.setOpaque(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel icon = new JLabel("○");
        icon.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 27));
        icon.setForeground(GuiStyles.ACCENT_BLUE);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel(titleText);
        title.setFont(GuiStyles.BODY_BOLD_FONT);
        title.setForeground(GuiStyles.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel description = new JLabel(descriptionText);
        description.setFont(GuiStyles.BODY_FONT);
        description.setForeground(GuiStyles.TEXT_SECONDARY);
        description.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(icon);
        content.add(Box.createVerticalStrut(8));
        content.add(title);
        content.add(Box.createVerticalStrut(4));
        content.add(description);

        emptyState.add(content);
        return emptyState;
    }

    private JScrollPane createTableScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(GuiStyles.BORDER_LIGHT));
        scrollPane.getViewport().setBackground(GuiStyles.SURFACE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        return scrollPane;
    }

    private DefaultTableModel createReadOnlyTableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void updateSelectedService() {
        int selectedRow = servicesTable.getSelectedRow();

        if (selectedRow < 0) {
            inspectorContentLayout.show(inspectorContentPanel, EMPTY_VIEW);
            return;
        }

        String serviceName = String.valueOf(servicesTableModel.getValueAt(selectedRow, 0));
        ServiceViewData service = servicesByName.get(serviceName);

        if (service == null) {
            inspectorContentLayout.show(inspectorContentPanel, EMPTY_VIEW);
            return;
        }

        selectedServiceNameValue.setText(service.serviceName());
        selectedHostValue.setText(service.host());
        selectedPortValue.setText(String.valueOf(service.port()));
        selectedStatusValue.setText(service.status());
        selectedRegisteredSinceValue.setText(service.registeredSince());
        selectedConnectionsValue.setText(String.valueOf(service.activeConnections()));
        selectedLastActivityValue.setText(service.lastActivity());
        inspectorContentLayout.show(inspectorContentPanel, DATA_VIEW);
    }

    public void setConnectedServices(List<ServiceViewData> services) {
        servicesByName.clear();
        servicesTableModel.setRowCount(0);

        for (ServiceViewData service : services) {
            servicesByName.put(service.serviceName(), service);
            servicesTableModel.addRow(new Object[]{
                    service.serviceName(),
                    service.host(),
                    service.port(),
                    service.status(),
                    service.lastHeartbeat()
            });

            StatusBadge.State state = toHealthState(service.status());
            String normalisedName = service.serviceName().toLowerCase();

            if (normalisedName.contains("smart") || normalisedName.contains("bin")) {
                smartBinHealthBadge.setState(state);
            } else if (normalisedName.contains("collection")) {
                collectionHealthBadge.setState(state);
            } else if (normalisedName.contains("recycling")) {
                recyclingHealthBadge.setState(state);
            }
        }

        int connectedCount = countConnectedServices();
        updateConnectedServiceSummary(connectedCount);

        int count = services.size();
        serviceCountBadge.setText(count + (count == 1 ? " SERVICE" : " SERVICES"));
        servicesTable.clearSelection();
        inspectorContentLayout.show(inspectorContentPanel, EMPTY_VIEW);
        servicesContentLayout.show(servicesContentPanel, count == 0 ? EMPTY_VIEW : DATA_VIEW);
        updateOverallHealth();
    }

    public void setDiscoveryEvents(List<MonitorEventViewData> events) {
        discoveryFeed.setEvents(events);
    }

    public void setStreamingEvents(List<MonitorEventViewData> events) {
        streamingFeed.setEvents(events);
        streamingEventsMetric.setValue(String.valueOf(events.size()));
    }

    public void setRequestActivity(List<MonitorEventViewData> events) {
        requestFeed.setEvents(events);
        networkRequestsMetric.setValue(String.valueOf(events.size()));
    }

    public void setTimelineEvents(List<NetworkEventViewData> events) {
        timelineTableModel.setRowCount(0);

        for (NetworkEventViewData event : events) {
            timelineTableModel.addRow(new Object[]{
                    event.timestamp(),
                    event.event(),
                    event.service(),
                    event.details()
            });
        }

        timelineContentLayout.show(
                timelineContentPanel,
                events.isEmpty() ? EMPTY_VIEW : DATA_VIEW
        );
    }

    public void setSystemUptime(String uptime) {
        String safeUptime = uptime == null || uptime.isBlank() ? "--" : uptime;
        systemUptimeMetric.setValue(safeUptime);
        systemUptimeMetric.setDetail(
                "--".equals(safeUptime) ? "Awaiting service startup" : "Platform operating time"
        );
    }

    public void setSmartBinHealth(StatusBadge.State state) {
        smartBinHealthBadge.setState(state);
        updateConnectedServiceSummary(countConnectedServices());
        updateOverallHealth();
    }

    public void setCollectionHealth(StatusBadge.State state) {
        collectionHealthBadge.setState(state);
        updateConnectedServiceSummary(countConnectedServices());
        updateOverallHealth();
    }

    public void setRecyclingHealth(StatusBadge.State state) {
        recyclingHealthBadge.setState(state);
        updateConnectedServiceSummary(countConnectedServices());
        updateOverallHealth();
    }

    private int countConnectedServices() {
        int connected = 0;

        if (smartBinHealthBadge.getState() == StatusBadge.State.CONNECTED) {
            connected++;
        }
        if (collectionHealthBadge.getState() == StatusBadge.State.CONNECTED) {
            connected++;
        }
        if (recyclingHealthBadge.getState() == StatusBadge.State.CONNECTED) {
            connected++;
        }

        return connected;
    }

    private void updateConnectedServiceSummary(int connectedCount) {
        connectedServicesMetric.setValue(connectedCount + " / 3");
        networkSummaryLabel.setText(connectedCount + " of 3 services connected");

        if (connectedCount == 3) {
            networkHealthLabel.setText("Operational");
        } else if (connectedCount > 0) {
            networkHealthLabel.setText("Degraded");
        } else {
            networkHealthLabel.setText("Standby");
        }
    }

    private void updateOverallHealth() {
        StatusBadge.State[] states = {
                smartBinHealthBadge.getState(),
                collectionHealthBadge.getState(),
                recyclingHealthBadge.getState()
        };

        boolean disconnected = false;
        boolean warning = false;
        int connected = 0;

        for (StatusBadge.State state : states) {
            if (state == StatusBadge.State.DISCONNECTED) {
                disconnected = true;
            } else if (state == StatusBadge.State.WARNING) {
                warning = true;
            } else if (state == StatusBadge.State.CONNECTED) {
                connected++;
            }
        }

        if (disconnected) {
            overallHealthBadge.setState(StatusBadge.State.DISCONNECTED);
        } else if (warning || (connected > 0 && connected < 3)) {
            overallHealthBadge.setState(StatusBadge.State.WARNING);
        } else if (connected == 3) {
            overallHealthBadge.setState(StatusBadge.State.CONNECTED);
        } else {
            overallHealthBadge.setState(StatusBadge.State.STANDBY);
        }
    }

    private StatusBadge.State toHealthState(String status) {
        if (status == null) {
            return StatusBadge.State.OFFLINE;
        }

        return switch (status.trim().toLowerCase()) {
            case "connected", "online", "operational" -> StatusBadge.State.CONNECTED;
            case "warning", "degraded" -> StatusBadge.State.WARNING;
            case "disconnected", "failed", "error" -> StatusBadge.State.DISCONNECTED;
            case "standby" -> StatusBadge.State.STANDBY;
            default -> StatusBadge.State.OFFLINE;
        };
    }

    public record ServiceViewData(
            String serviceName,
            String host,
            int port,
            String status,
            String lastHeartbeat,
            String registeredSince,
            int activeConnections,
            String lastActivity
    ) {
    }

    public record MonitorEventViewData(
            String timestamp,
            String event,
            String details
    ) {
    }

    public record NetworkEventViewData(
            String timestamp,
            String event,
            String service,
            String details
    ) {
    }

    private static class MonitorFeed extends SurfaceCard {

        private final DefaultListModel<MonitorEventViewData> model;
        private final CardLayout contentLayout;
        private final JPanel contentPanel;

        private MonitorFeed(
                String titleText,
                String descriptionText,
                String emptyMessage,
                Color accentColor
        ) {
            super(accentColor, new Insets(24, 20, 18, 20));
            setLayout(new BorderLayout(0, 14));

            JPanel heading = new JPanel();
            heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
            heading.setOpaque(false);

            JLabel title = new JLabel(titleText);
            title.setFont(GuiStyles.CARD_TITLE_FONT);
            title.setForeground(GuiStyles.TEXT_PRIMARY);
            title.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel description = new JLabel(
                    "<html><div style='width:250px'>" + descriptionText + "</div></html>"
            );
            description.setFont(GuiStyles.BODY_FONT);
            description.setForeground(GuiStyles.TEXT_SECONDARY);
            description.setAlignmentX(Component.LEFT_ALIGNMENT);

            heading.add(title);
            heading.add(Box.createVerticalStrut(6));
            heading.add(description);

            model = new DefaultListModel<>();
            JList<MonitorEventViewData> list = new JList<>(model);
            list.setBackground(GuiStyles.SURFACE_SECONDARY);
            list.setSelectionBackground(GuiStyles.PRIMARY_LIGHT);
            list.setSelectionForeground(GuiStyles.TEXT_PRIMARY);
            list.setCellRenderer(new MonitorEventRenderer());

            JScrollPane listScroll = new JScrollPane(list);
            listScroll.setBorder(BorderFactory.createLineBorder(GuiStyles.BORDER_LIGHT));
            listScroll.getVerticalScrollBar().setUnitIncrement(12);

            JPanel emptyState = new JPanel(new GridBagLayout());
            emptyState.setOpaque(false);
            JLabel emptyLabel = new JLabel(emptyMessage);
            emptyLabel.setFont(GuiStyles.BODY_BOLD_FONT);
            emptyLabel.setForeground(GuiStyles.TEXT_SECONDARY);
            emptyState.add(emptyLabel);

            contentLayout = new CardLayout();
            contentPanel = new JPanel(contentLayout);
            contentPanel.setOpaque(false);
            contentPanel.add(emptyState, EMPTY_VIEW);
            contentPanel.add(listScroll, DATA_VIEW);

            add(heading, BorderLayout.NORTH);
            add(contentPanel, BorderLayout.CENTER);
            contentLayout.show(contentPanel, EMPTY_VIEW);
        }

        private void setEvents(List<MonitorEventViewData> events) {
            model.clear();
            events.forEach(model::addElement);
            contentLayout.show(contentPanel, events.isEmpty() ? EMPTY_VIEW : DATA_VIEW);
        }
    }

    private static class MonitorEventRenderer extends JPanel
            implements ListCellRenderer<MonitorEventViewData> {

        private final JLabel eventLabel = new JLabel();
        private final JLabel detailsLabel = new JLabel();
        private final JLabel timeLabel = new JLabel();

        private MonitorEventRenderer() {
            setLayout(new BorderLayout(10, 2));
            setBorder(new EmptyBorder(8, 10, 8, 10));

            eventLabel.setFont(GuiStyles.BODY_BOLD_FONT);
            eventLabel.setForeground(GuiStyles.TEXT_PRIMARY);

            detailsLabel.setFont(GuiStyles.CAPTION_FONT);
            detailsLabel.setForeground(GuiStyles.TEXT_SECONDARY);

            timeLabel.setFont(GuiStyles.CAPTION_FONT);
            timeLabel.setForeground(GuiStyles.TEXT_MUTED);

            JPanel text = new JPanel();
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            text.setOpaque(false);
            text.add(eventLabel);
            text.add(Box.createVerticalStrut(2));
            text.add(detailsLabel);

            add(text, BorderLayout.CENTER);
            add(timeLabel, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends MonitorEventViewData> list,
                MonitorEventViewData value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            eventLabel.setText(value.event());
            detailsLabel.setText(value.details());
            timeLabel.setText(value.timestamp());
            setBackground(isSelected ? GuiStyles.PRIMARY_LIGHT : GuiStyles.SURFACE_SECONDARY);
            setOpaque(true);
            return this;
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
            graphics2D.setPaint(new GradientPaint(
                    0,
                    0,
                    GuiStyles.HERO_START,
                    getWidth(),
                    getHeight(),
                    GuiStyles.HERO_END
            ));
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
}
