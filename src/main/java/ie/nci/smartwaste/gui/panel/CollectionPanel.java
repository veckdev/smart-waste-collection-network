package ie.nci.smartwaste.gui.panel;

import ie.nci.smartwaste.gui.component.MetricCard;
import ie.nci.smartwaste.gui.component.OperationCard;
import ie.nci.smartwaste.gui.component.SurfaceCard;
import ie.nci.smartwaste.gui.util.GuiStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CollectionPanel extends JPanel {

    private static final String EMPTY_VIEW = "empty";
    private static final String DATA_VIEW = "data";

    private final MetricCard activeCollectionsMetric;
    private final MetricCard completedTodayMetric;
    private final MetricCard priorityPickupsMetric;
    private final MetricCard fleetAvailabilityMetric;

    private final JLabel collectionStatusLabel;
    private final JLabel activeRouteCountLabel;
    private final JLabel routeCountBadge;

    private final DefaultTableModel routesTableModel;
    private final DefaultTableModel activityTableModel;
    private final JTable routesTable;
    private final JTable activityTable;

    private final CardLayout routesContentLayout;
    private final JPanel routesContentPanel;
    private final CardLayout inspectorContentLayout;
    private final JPanel inspectorContentPanel;
    private final CardLayout activityContentLayout;
    private final JPanel activityContentPanel;

    private final JLabel selectedRouteIdValue;
    private final JLabel selectedVehicleValue;
    private final JLabel selectedDriverValue;
    private final JLabel selectedBinsValue;
    private final JLabel selectedPriorityValue;
    private final JLabel selectedStatusValue;
    private final JLabel selectedDurationValue;
    private final JLabel selectedLastUpdateValue;

    private final JTextField createRouteIdField;
    private final JTextField createVehicleIdField;
    private final JTextField assignRouteIdField;
    private final JTextField assignBinIdField;
    private final JTextField completeRouteIdField;

    private final JButton createRouteButton;
    private final JButton assignBinButton;
    private final JButton completeRouteButton;

    private final Map<String, CollectionRouteViewData> routesById = new LinkedHashMap<>();

    public CollectionPanel() {
        setLayout(new BorderLayout());
        setBackground(GuiStyles.BACKGROUND);
        setBorder(new EmptyBorder(18, 22, 18, 22));

        activeCollectionsMetric = new MetricCard(
                "Active Collections",
                "0",
                "Routes currently in progress",
                GuiStyles.ACCENT_AMBER
        );
        completedTodayMetric = new MetricCard(
                "Completed Today",
                "0",
                "Successful collection operations",
                GuiStyles.PRIMARY
        );
        priorityPickupsMetric = new MetricCard(
                "Priority Pickups",
                "0",
                "High priority collection requests",
                new Color(213, 112, 48)
        );
        fleetAvailabilityMetric = new MetricCard(
                "Fleet Availability",
                "0 / 0",
                "Vehicles currently available",
                GuiStyles.ACCENT_BLUE
        );

        collectionStatusLabel = new JLabel("Offline");
        activeRouteCountLabel = new JLabel("0 active routes");
        routeCountBadge = new JLabel("0 ROUTES");

        routesTableModel = createReadOnlyTableModel(
                "Route ID",
                "Vehicle",
                "Driver",
                "Assigned Bins",
                "Status",
                "Priority"
        );
        activityTableModel = createReadOnlyTableModel(
                "Route ID",
                "Vehicle",
                "Current Activity",
                "Status",
                "Last Update"
        );

        routesTable = new JTable(routesTableModel);
        activityTable = new JTable(activityTableModel);
        GuiStyles.styleTable(routesTable);
        GuiStyles.styleTable(activityTable);
        routesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        routesContentLayout = new CardLayout();
        routesContentPanel = new JPanel(routesContentLayout);
        inspectorContentLayout = new CardLayout();
        inspectorContentPanel = new JPanel(inspectorContentLayout);
        activityContentLayout = new CardLayout();
        activityContentPanel = new JPanel(activityContentLayout);

        selectedRouteIdValue = createInspectorValue();
        selectedVehicleValue = createInspectorValue();
        selectedDriverValue = createInspectorValue();
        selectedBinsValue = createInspectorValue();
        selectedPriorityValue = createInspectorValue();
        selectedStatusValue = createInspectorValue();
        selectedDurationValue = createInspectorValue();
        selectedLastUpdateValue = createInspectorValue();

        createRouteIdField = GuiStyles.createTextField();
        createVehicleIdField = GuiStyles.createTextField();
        assignRouteIdField = GuiStyles.createTextField();
        assignBinIdField = GuiStyles.createTextField();
        completeRouteIdField = GuiStyles.createTextField();

        createRouteButton = GuiStyles.createPrimaryButton("Create Route");
        assignBinButton = GuiStyles.createPrimaryButton("Assign Bin");
        completeRouteButton = GuiStyles.createPrimaryButton("Complete Route");

        configureDataViews();

        ScrollableContentPanel content = new ScrollableContentPanel();
        content.add(createPageHeading());
        content.add(Box.createVerticalStrut(16));
        content.add(createHeroPanel());
        content.add(Box.createVerticalStrut(18));
        content.add(createMetricsRow());
        content.add(Box.createVerticalStrut(24));
        content.add(createSectionHeading(
                "Route workspace",
                "Review municipal collection routes and inspect their operational state."
        ));
        content.add(Box.createVerticalStrut(12));
        content.add(createWorkspace());
        content.add(Box.createVerticalStrut(24));
        content.add(createSectionHeading(
                "Operations",
                "Coordinate routes, smart bin assignments and collection completion."
        ));
        content.add(Box.createVerticalStrut(12));
        content.add(createOperationsRow());
        content.add(Box.createVerticalStrut(24));
        content.add(createLiveActivityCard());
        content.add(Box.createVerticalStrut(18));
        content.add(createPrioritiesCard());

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void configureDataViews() {
        routesContentPanel.setOpaque(false);
        routesContentPanel.add(
                createEmptyState(
                        "No collection routes available.",
                        "Create your first municipal collection route."
                ),
                EMPTY_VIEW
        );
        routesContentPanel.add(createTableScrollPane(routesTable), DATA_VIEW);
        routesContentLayout.show(routesContentPanel, EMPTY_VIEW);

        inspectorContentPanel.setOpaque(false);
        inspectorContentPanel.add(
                createEmptyState(
                        "No route selected.",
                        "Select a collection route to inspect operational details."
                ),
                EMPTY_VIEW
        );
        inspectorContentPanel.add(createRouteDetailsPanel(), DATA_VIEW);
        inspectorContentLayout.show(inspectorContentPanel, EMPTY_VIEW);

        activityContentPanel.setOpaque(false);
        activityContentPanel.add(
                createEmptyState(
                        "No active collection routes.",
                        "Live fleet activity will appear here when routes begin."
                ),
                EMPTY_VIEW
        );
        activityContentPanel.add(createTableScrollPane(activityTable), DATA_VIEW);
        activityContentLayout.show(activityContentPanel, EMPTY_VIEW);

        routesTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                updateSelectedRoute();
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

        JLabel title = new JLabel("Collection Operations");
        title.setFont(GuiStyles.TITLE_FONT);
        title.setForeground(GuiStyles.TEXT_PRIMARY);

        JLabel subtitle = new JLabel(
                "Coordinate municipal waste collection vehicles and city-wide pickup routes."
        );
        subtitle.setFont(GuiStyles.BODY_FONT);
        subtitle.setForeground(GuiStyles.TEXT_SECONDARY);

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subtitle);

        JLabel areaLabel = new JLabel("FLEET NETWORK");
        areaLabel.setFont(GuiStyles.LABEL_FONT);
        areaLabel.setForeground(new Color(132, 83, 21));
        areaLabel.setBorder(new EmptyBorder(8, 12, 8, 12));
        areaLabel.setOpaque(true);
        areaLabel.setBackground(new Color(250, 235, 209));

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

        JLabel eyebrow = new JLabel("MUNICIPAL FLEET COORDINATION");
        eyebrow.setFont(GuiStyles.LABEL_FONT);
        eyebrow.setForeground(new Color(239, 204, 146));
        eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Coordinate efficient waste collection across the city.");
        title.setFont(GuiStyles.DISPLAY_FONT);
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel(
                "<html><div style='width:650px'>Manage collection routes, assign pickup operations, "
                        + "monitor active collections and coordinate municipal fleet activity from a "
                        + "single operational workspace.</div></html>"
        );
        description.setFont(GuiStyles.BODY_FONT);
        description.setForeground(new Color(220, 235, 227));
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        messagePanel.add(eyebrow);
        messagePanel.add(Box.createVerticalStrut(9));
        messagePanel.add(title);
        messagePanel.add(Box.createVerticalStrut(10));
        messagePanel.add(description);

        JPanel statusContent = new JPanel();
        statusContent.setLayout(new BoxLayout(statusContent, BoxLayout.Y_AXIS));
        statusContent.setOpaque(false);
        statusContent.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel statusTitle = new JLabel("COLLECTION STATUS");
        statusTitle.setFont(GuiStyles.LABEL_FONT);
        statusTitle.setForeground(new Color(226, 209, 178));

        collectionStatusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 23));
        collectionStatusLabel.setForeground(Color.WHITE);

        activeRouteCountLabel.setFont(GuiStyles.BODY_FONT);
        activeRouteCountLabel.setForeground(new Color(216, 232, 224));

        statusTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        collectionStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        activeRouteCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        statusContent.add(statusTitle);
        statusContent.add(Box.createVerticalStrut(10));
        statusContent.add(collectionStatusLabel);
        statusContent.add(Box.createVerticalStrut(5));
        statusContent.add(activeRouteCountLabel);

        TranslucentPanel statusSurface = new TranslucentPanel();
        statusSurface.setLayout(new BorderLayout());
        statusSurface.setPreferredSize(new Dimension(220, 125));
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

        panel.add(activeCollectionsMetric);
        panel.add(completedTodayMetric);
        panel.add(priorityPickupsMetric);
        panel.add(fleetAvailabilityMetric);

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
        JPanel routesCard = createRoutesCard();
        routesCard.setPreferredSize(new Dimension(650, 350));
        workspace.add(routesCard, constraints);

        constraints.gridx = 1;
        constraints.weightx = 0.35;
        constraints.insets = new Insets(0, 7, 0, 0);
        JPanel inspectorCard = createRouteInspectorCard();
        inspectorCard.setPreferredSize(new Dimension(350, 350));
        workspace.add(inspectorCard, constraints);

        return workspace;
    }

    private JPanel createRoutesCard() {
        SurfaceCard card = new SurfaceCard();
        card.setLayout(new BorderLayout(0, 15));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);

        JLabel title = new JLabel("Collection Routes");
        title.setFont(GuiStyles.SECTION_TITLE_FONT);
        title.setForeground(GuiStyles.TEXT_PRIMARY);

        JLabel description = new JLabel("Municipal fleet routes and their current operational state.");
        description.setFont(GuiStyles.BODY_FONT);
        description.setForeground(GuiStyles.TEXT_SECONDARY);

        text.add(title);
        text.add(Box.createVerticalStrut(4));
        text.add(description);

        routeCountBadge.setFont(GuiStyles.LABEL_FONT);
        routeCountBadge.setForeground(new Color(132, 83, 21));
        routeCountBadge.setOpaque(true);
        routeCountBadge.setBackground(new Color(250, 235, 209));
        routeCountBadge.setBorder(new EmptyBorder(7, 10, 7, 10));

        heading.add(text, BorderLayout.WEST);
        heading.add(routeCountBadge, BorderLayout.EAST);

        card.add(heading, BorderLayout.NORTH);
        card.add(routesContentPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createRouteInspectorCard() {
        SurfaceCard card = new SurfaceCard();
        card.setLayout(new BorderLayout(0, 15));

        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setOpaque(false);

        JLabel title = new JLabel("Route Inspector");
        title.setFont(GuiStyles.SECTION_TITLE_FONT);
        title.setForeground(GuiStyles.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel("Operational details for the selected collection route.");
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

    private JPanel createRouteDetailsPanel() {
        JPanel details = new JPanel(new GridLayout(8, 2, 12, 8));
        details.setOpaque(false);
        addInspectorRow(details, "Route ID", selectedRouteIdValue);
        addInspectorRow(details, "Vehicle", selectedVehicleValue);
        addInspectorRow(details, "Driver", selectedDriverValue);
        addInspectorRow(details, "Assigned Smart Bins", selectedBinsValue);
        addInspectorRow(details, "Priority", selectedPriorityValue);
        addInspectorRow(details, "Status", selectedStatusValue);
        addInspectorRow(details, "Estimated Duration", selectedDurationValue);
        addInspectorRow(details, "Last Update", selectedLastUpdateValue);
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

    private JPanel createOperationsRow() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 14, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 365));
        panel.setPreferredSize(new Dimension(1000, 365));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(createRouteOperation());
        panel.add(createAssignmentOperation());
        panel.add(createCompletionOperation());

        return panel;
    }

    private JPanel createRouteOperation() {
        OperationCard card = new OperationCard(
                "Create Collection Route",
                "Create a new municipal waste collection route.",
                GuiStyles.ACCENT_AMBER
        );
        card.addField("Route ID", createRouteIdField);
        card.addField("Vehicle ID", createVehicleIdField);
        card.setActionButton(createRouteButton);
        return card;
    }

    private JPanel createAssignmentOperation() {
        OperationCard card = new OperationCard(
                "Assign Smart Bin",
                "Assign a registered smart bin to an existing collection route.",
                new Color(213, 112, 48)
        );
        card.addField("Route ID", assignRouteIdField);
        card.addField("Smart Bin ID", assignBinIdField);
        card.setActionButton(assignBinButton);
        return card;
    }

    private JPanel createCompletionOperation() {
        OperationCard card = new OperationCard(
                "Complete Collection",
                "Mark the selected municipal collection route as completed.",
                GuiStyles.PRIMARY
        );
        card.addField("Route ID", completeRouteIdField);
        card.setActionButton(completeRouteButton);
        return card;
    }

    private JPanel createLiveActivityCard() {
        SurfaceCard card = new SurfaceCard(GuiStyles.ACCENT_AMBER);
        card.setLayout(new BorderLayout(0, 15));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 245));
        card.setPreferredSize(new Dimension(1000, 245));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setOpaque(false);

        JLabel title = new JLabel("Live Collection Activity");
        title.setFont(GuiStyles.SECTION_TITLE_FONT);
        title.setForeground(GuiStyles.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel("Monitor active municipal collection operations.");
        description.setFont(GuiStyles.BODY_FONT);
        description.setForeground(GuiStyles.TEXT_SECONDARY);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        heading.add(title);
        heading.add(Box.createVerticalStrut(4));
        heading.add(description);

        card.add(heading, BorderLayout.NORTH);
        card.add(activityContentPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createPrioritiesCard() {
        SurfaceCard card = new SurfaceCard();
        card.setLayout(new BorderLayout(20, 0));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));
        card.setPreferredSize(new Dimension(1000, 105));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setOpaque(false);

        JLabel title = new JLabel("Collection Priorities");
        title.setFont(GuiStyles.SECTION_TITLE_FONT);
        title.setForeground(GuiStyles.TEXT_PRIMARY);

        JLabel description = new JLabel("Priority levels used across municipal pickup operations.");
        description.setFont(GuiStyles.BODY_FONT);
        description.setForeground(GuiStyles.TEXT_SECONDARY);

        heading.add(title);
        heading.add(Box.createVerticalStrut(4));
        heading.add(description);

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        badges.setOpaque(false);
        badges.add(new PriorityBadge("Normal", GuiStyles.ACCENT_TEAL, new Color(224, 243, 237)));
        badges.add(new PriorityBadge("Medium", GuiStyles.ACCENT_AMBER, new Color(250, 235, 209)));
        badges.add(new PriorityBadge("High", new Color(213, 112, 48), new Color(251, 226, 210)));
        badges.add(new PriorityBadge("Emergency", GuiStyles.ERROR, new Color(249, 224, 222)));

        card.add(heading, BorderLayout.WEST);
        card.add(badges, BorderLayout.EAST);

        return card;
    }

    private JPanel createEmptyState(String titleText, String descriptionText) {
        JPanel emptyState = new JPanel(new GridBagLayout());
        emptyState.setOpaque(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel icon = new JLabel("○");
        icon.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 27));
        icon.setForeground(GuiStyles.ACCENT_AMBER);
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

    private void updateSelectedRoute() {
        int selectedRow = routesTable.getSelectedRow();

        if (selectedRow < 0) {
            inspectorContentLayout.show(inspectorContentPanel, EMPTY_VIEW);
            return;
        }

        String routeId = String.valueOf(routesTableModel.getValueAt(selectedRow, 0));
        CollectionRouteViewData route = routesById.get(routeId);

        if (route == null) {
            inspectorContentLayout.show(inspectorContentPanel, EMPTY_VIEW);
            return;
        }

        selectedRouteIdValue.setText(route.routeId());
        selectedVehicleValue.setText(route.vehicle());
        selectedDriverValue.setText(route.driver());
        selectedBinsValue.setText(route.assignedBins());
        selectedPriorityValue.setText(route.priority());
        selectedStatusValue.setText(route.status());
        selectedDurationValue.setText(route.estimatedDuration());
        selectedLastUpdateValue.setText(route.lastUpdate());
        inspectorContentLayout.show(inspectorContentPanel, DATA_VIEW);
    }

    public void setNetworkConnected(boolean connected) {
        collectionStatusLabel.setText(connected ? "Online" : "Offline");
        collectionStatusLabel.setForeground(
                connected ? new Color(155, 238, 189) : Color.WHITE
        );
    }

    public void setCollectionRoutes(List<CollectionRouteViewData> routes) {
        routesById.clear();
        routesTableModel.setRowCount(0);

        int activeRoutes = 0;
        int completedRoutes = 0;
        int priorityRoutes = 0;

        for (CollectionRouteViewData route : routes) {
            routesById.put(route.routeId(), route);
            routesTableModel.addRow(new Object[]{
                    route.routeId(),
                    route.vehicle(),
                    route.driver(),
                    route.assignedBins(),
                    route.status(),
                    route.priority()
            });

            if (route.status().equalsIgnoreCase("Active")) {
                activeRoutes++;
            }
            if (route.status().equalsIgnoreCase("Completed")) {
                completedRoutes++;
            }
            if (route.priority().equalsIgnoreCase("High")
                    || route.priority().equalsIgnoreCase("Emergency")) {
                priorityRoutes++;
            }
        }

        activeCollectionsMetric.setValue(String.valueOf(activeRoutes));
        completedTodayMetric.setValue(String.valueOf(completedRoutes));
        priorityPickupsMetric.setValue(String.valueOf(priorityRoutes));
        activeRouteCountLabel.setText(activeRoutes + (activeRoutes == 1 ? " active route" : " active routes"));

        int count = routes.size();
        routeCountBadge.setText(count + (count == 1 ? " ROUTE" : " ROUTES"));

        routesTable.clearSelection();
        inspectorContentLayout.show(inspectorContentPanel, EMPTY_VIEW);
        routesContentLayout.show(routesContentPanel, count == 0 ? EMPTY_VIEW : DATA_VIEW);
    }

    public void setFleetAvailability(int availableVehicles, int totalVehicles) {
        int safeTotal = Math.max(0, totalVehicles);
        int safeAvailable = Math.max(0, Math.min(availableVehicles, safeTotal));
        fleetAvailabilityMetric.setValue(safeAvailable + " / " + safeTotal);
    }

    public void setLiveActivity(List<CollectionActivityViewData> activities) {
        activityTableModel.setRowCount(0);

        for (CollectionActivityViewData activity : activities) {
            activityTableModel.addRow(new Object[]{
                    activity.routeId(),
                    activity.vehicle(),
                    activity.currentActivity(),
                    activity.status(),
                    activity.lastUpdate()
            });
        }

        activityContentLayout.show(
                activityContentPanel,
                activities.isEmpty() ? EMPTY_VIEW : DATA_VIEW
        );
    }

    public void addCreateRouteActionListener(ActionListener listener) {
        createRouteButton.addActionListener(listener);
    }

    public void addAssignBinActionListener(ActionListener listener) {
        assignBinButton.addActionListener(listener);
    }

    public void addCompleteRouteActionListener(ActionListener listener) {
        completeRouteButton.addActionListener(listener);
    }

    public String getCreateRouteId() {
        return createRouteIdField.getText().trim();
    }

    public String getCreateVehicleId() {
        return createVehicleIdField.getText().trim();
    }

    public String getAssignmentRouteId() {
        return assignRouteIdField.getText().trim();
    }

    public String getAssignmentBinId() {
        return assignBinIdField.getText().trim();
    }

    public String getCompletionRouteId() {
        return completeRouteIdField.getText().trim();
    }

    public record CollectionRouteViewData(
            String routeId,
            String vehicle,
            String driver,
            String assignedBins,
            String status,
            String priority,
            String estimatedDuration,
            String lastUpdate
    ) {
    }

    public record CollectionActivityViewData(
            String routeId,
            String vehicle,
            String currentActivity,
            String status,
            String lastUpdate
    ) {
    }

    private static class PriorityBadge extends JLabel {

        private final Color backgroundColor;

        private PriorityBadge(String text, Color foregroundColor, Color backgroundColor) {
            super("●  " + text.toUpperCase());
            this.backgroundColor = backgroundColor;

            setFont(GuiStyles.LABEL_FONT);
            setForeground(foregroundColor);
            setBorder(new EmptyBorder(7, 11, 7, 11));
            setOpaque(false);
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
