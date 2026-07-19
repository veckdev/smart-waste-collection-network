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

public class SmartBinPanel extends JPanel {

    private static final String EMPTY_VIEW = "empty";
    private static final String DATA_VIEW = "data";

    private final MetricCard registeredBinsMetric;
    private final MetricCard averageFillMetric;
    private final MetricCard collectionAlertsMetric;
    private final MetricCard maintenanceReportsMetric;

    private final JLabel networkStatusLabel;
    private final JLabel networkAssetCountLabel;
    private final JLabel registeredCountBadge;

    private final DefaultTableModel binsTableModel;
    private final DefaultTableModel queueTableModel;
    private final JTable binsTable;
    private final JTable queueTable;

    private final CardLayout binsContentLayout;
    private final JPanel binsContentPanel;
    private final CardLayout inspectorContentLayout;
    private final JPanel inspectorContentPanel;
    private final CardLayout queueContentLayout;
    private final JPanel queueContentPanel;

    private final JLabel selectedBinIdValue;
    private final JLabel selectedLocationValue;
    private final JLabel selectedFillValue;
    private final JLabel selectedStatusValue;
    private final JLabel selectedConditionValue;
    private final JLabel selectedLastUpdateValue;

    private final JTextField registerBinIdField;
    private final JTextField registerLocationField;
    private final JTextField updateBinIdField;
    private final JTextField fillLevelField;
    private final JTextField damageBinIdField;
    private final JTextArea damageDescriptionArea;

    private final JButton registerAssetButton;
    private final JButton updateFillLevelButton;
    private final JButton reportDamageButton;

    private final Map<String, SmartBinViewData> assetsById = new LinkedHashMap<>();

    public SmartBinPanel() {
        setLayout(new BorderLayout());
        setBackground(GuiStyles.BACKGROUND);
        setBorder(new EmptyBorder(18, 22, 18, 22));

        registeredBinsMetric = new MetricCard(
                "Registered Smart Bins",
                "0",
                "Assets currently registered",
                GuiStyles.ACCENT_TEAL
        );
        averageFillMetric = new MetricCard(
                "Average Fill Level",
                "0%",
                "Across all connected bins",
                GuiStyles.ACCENT_BLUE
        );
        collectionAlertsMetric = new MetricCard(
                "Collection Alerts",
                "0",
                "Bins above collection threshold",
                GuiStyles.ACCENT_AMBER
        );
        maintenanceReportsMetric = new MetricCard(
                "Maintenance Reports",
                "0",
                "Damaged assets reported",
                GuiStyles.ERROR
        );

        networkStatusLabel = new JLabel("Offline");
        networkAssetCountLabel = new JLabel("0 registered assets");
        registeredCountBadge = new JLabel("0 ASSETS");

        binsTableModel = createReadOnlyTableModel(
                "ID",
                "Location",
                "Fill Level",
                "Status",
                "Condition"
        );
        queueTableModel = createReadOnlyTableModel(
                "ID",
                "Location",
                "Fill Level",
                "Status",
                "Condition"
        );

        binsTable = new JTable(binsTableModel);
        queueTable = new JTable(queueTableModel);
        GuiStyles.styleTable(binsTable);
        GuiStyles.styleTable(queueTable);
        binsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        binsContentLayout = new CardLayout();
        binsContentPanel = new JPanel(binsContentLayout);
        inspectorContentLayout = new CardLayout();
        inspectorContentPanel = new JPanel(inspectorContentLayout);
        queueContentLayout = new CardLayout();
        queueContentPanel = new JPanel(queueContentLayout);

        selectedBinIdValue = createInspectorValue();
        selectedLocationValue = createInspectorValue();
        selectedFillValue = createInspectorValue();
        selectedStatusValue = createInspectorValue();
        selectedConditionValue = createInspectorValue();
        selectedLastUpdateValue = createInspectorValue();

        registerBinIdField = GuiStyles.createTextField();
        registerLocationField = GuiStyles.createTextField();
        updateBinIdField = GuiStyles.createTextField();
        fillLevelField = GuiStyles.createTextField();
        damageBinIdField = GuiStyles.createTextField();
        damageDescriptionArea = GuiStyles.createTextArea(3);

        registerAssetButton = GuiStyles.createPrimaryButton("Register Asset");
        updateFillLevelButton = GuiStyles.createPrimaryButton("Update Fill Level");
        reportDamageButton = GuiStyles.createPrimaryButton("Report Damage");

        configureDataViews();

        ScrollableContentPanel content = new ScrollableContentPanel();
        content.add(createPageHeading());
        content.add(Box.createVerticalStrut(16));
        content.add(createHeroPanel());
        content.add(Box.createVerticalStrut(18));
        content.add(createMetricsRow());
        content.add(Box.createVerticalStrut(24));
        content.add(createSectionHeading(
                "Asset workspace",
                "Inspect every connected smart bin and its current operational state."
        ));
        content.add(Box.createVerticalStrut(12));
        content.add(createWorkspace());
        content.add(Box.createVerticalStrut(24));
        content.add(createSectionHeading(
                "Operations",
                "Manage the municipal asset network through focused operational tasks."
        ));
        content.add(Box.createVerticalStrut(12));
        content.add(createOperationsRow());
        content.add(Box.createVerticalStrut(24));
        content.add(createCollectionQueueCard());

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void configureDataViews() {
        binsContentPanel.setOpaque(false);
        binsContentPanel.add(
                createEmptyState(
                        "No smart bins registered.",
                        "Register your first municipal asset to begin monitoring."
                ),
                EMPTY_VIEW
        );
        binsContentPanel.add(createTableScrollPane(binsTable), DATA_VIEW);
        binsContentLayout.show(binsContentPanel, EMPTY_VIEW);

        inspectorContentPanel.setOpaque(false);
        inspectorContentPanel.add(
                createEmptyState(
                        "No asset selected.",
                        "Select a registered smart bin to inspect its details."
                ),
                EMPTY_VIEW
        );
        inspectorContentPanel.add(createAssetDetailsPanel(), DATA_VIEW);
        inspectorContentLayout.show(inspectorContentPanel, EMPTY_VIEW);

        queueContentPanel.setOpaque(false);
        queueContentPanel.add(
                createEmptyState(
                        "No assets currently require collection.",
                        "Bins exceeding the collection threshold will appear here."
                ),
                EMPTY_VIEW
        );
        queueContentPanel.add(createTableScrollPane(queueTable), DATA_VIEW);
        queueContentLayout.show(queueContentPanel, EMPTY_VIEW);

        binsTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                updateSelectedAsset();
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

        JLabel title = new JLabel("Smart Bin Operations");
        title.setFont(GuiStyles.TITLE_FONT);
        title.setForeground(GuiStyles.TEXT_PRIMARY);

        JLabel subtitle = new JLabel(
                "Manage connected waste collection assets across the city."
        );
        subtitle.setFont(GuiStyles.BODY_FONT);
        subtitle.setForeground(GuiStyles.TEXT_SECONDARY);

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subtitle);

        JLabel areaLabel = new JLabel("ASSET NETWORK");
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

        JLabel eyebrow = new JLabel("CONNECTED MUNICIPAL ASSETS");
        eyebrow.setFont(GuiStyles.LABEL_FONT);
        eyebrow.setForeground(new Color(183, 225, 203));
        eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Monitor municipal waste infrastructure in real time.");
        title.setFont(GuiStyles.DISPLAY_FONT);
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel(
                "<html><div style='width:650px'>Register smart bins, monitor fill levels, report damaged "
                        + "assets and identify bins requiring collection across the municipal waste network."
                        + "</div></html>"
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

        JLabel statusTitle = new JLabel("NETWORK STATUS");
        statusTitle.setFont(GuiStyles.LABEL_FONT);
        statusTitle.setForeground(new Color(185, 216, 201));

        networkStatusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 23));
        networkStatusLabel.setForeground(Color.WHITE);

        networkAssetCountLabel.setFont(GuiStyles.BODY_FONT);
        networkAssetCountLabel.setForeground(new Color(216, 232, 224));

        statusTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        networkStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        networkAssetCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        statusContent.add(statusTitle);
        statusContent.add(Box.createVerticalStrut(10));
        statusContent.add(networkStatusLabel);
        statusContent.add(Box.createVerticalStrut(5));
        statusContent.add(networkAssetCountLabel);

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

        panel.add(registeredBinsMetric);
        panel.add(averageFillMetric);
        panel.add(collectionAlertsMetric);
        panel.add(maintenanceReportsMetric);

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
        workspace.setMaximumSize(new Dimension(Integer.MAX_VALUE, 330));
        workspace.setPreferredSize(new Dimension(1000, 330));
        workspace.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weighty = 1;

        constraints.gridx = 0;
        constraints.weightx = 0.65;
        constraints.insets = new Insets(0, 0, 0, 7);
        JPanel registeredBinsCard = createRegisteredBinsCard();
        registeredBinsCard.setPreferredSize(new Dimension(650, 330));
        workspace.add(registeredBinsCard, constraints);

        constraints.gridx = 1;
        constraints.weightx = 0.35;
        constraints.insets = new Insets(0, 7, 0, 0);
        JPanel selectedAssetCard = createSelectedAssetCard();
        selectedAssetCard.setPreferredSize(new Dimension(350, 330));
        workspace.add(selectedAssetCard, constraints);

        return workspace;
    }

    private JPanel createRegisteredBinsCard() {
        SurfaceCard card = new SurfaceCard();
        card.setLayout(new BorderLayout(0, 15));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);

        JLabel title = new JLabel("Registered Smart Bins");
        title.setFont(GuiStyles.SECTION_TITLE_FONT);
        title.setForeground(GuiStyles.TEXT_PRIMARY);

        JLabel description = new JLabel("Connected municipal assets and their latest reported state.");
        description.setFont(GuiStyles.BODY_FONT);
        description.setForeground(GuiStyles.TEXT_SECONDARY);

        text.add(title);
        text.add(Box.createVerticalStrut(4));
        text.add(description);

        registeredCountBadge.setFont(GuiStyles.LABEL_FONT);
        registeredCountBadge.setForeground(GuiStyles.PRIMARY_DARK);
        registeredCountBadge.setOpaque(true);
        registeredCountBadge.setBackground(GuiStyles.PRIMARY_LIGHT);
        registeredCountBadge.setBorder(new EmptyBorder(7, 10, 7, 10));

        heading.add(text, BorderLayout.WEST);
        heading.add(registeredCountBadge, BorderLayout.EAST);

        card.add(heading, BorderLayout.NORTH);
        card.add(binsContentPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createSelectedAssetCard() {
        SurfaceCard card = new SurfaceCard();
        card.setLayout(new BorderLayout(0, 15));

        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setOpaque(false);

        JLabel title = new JLabel("Selected Asset");
        title.setFont(GuiStyles.SECTION_TITLE_FONT);
        title.setForeground(GuiStyles.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel("Detailed state for the selected municipal asset.");
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

    private JPanel createAssetDetailsPanel() {
        JPanel details = new JPanel(new GridLayout(6, 2, 12, 10));
        details.setOpaque(false);
        addInspectorRow(details, "Bin ID", selectedBinIdValue);
        addInspectorRow(details, "Location", selectedLocationValue);
        addInspectorRow(details, "Fill Level", selectedFillValue);
        addInspectorRow(details, "Status", selectedStatusValue);
        addInspectorRow(details, "Condition", selectedConditionValue);
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
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 390));
        panel.setPreferredSize(new Dimension(1000, 390));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(createRegisterOperation());
        panel.add(createFillLevelOperation());
        panel.add(createDamageOperation());

        return panel;
    }

    private JPanel createRegisterOperation() {
        OperationCard card = new OperationCard(
                "Register Smart Bin",
                "Register a new connected waste asset.",
                GuiStyles.ACCENT_TEAL
        );
        card.addField("Bin ID", registerBinIdField);
        card.addField("Location", registerLocationField);
        card.setActionButton(registerAssetButton);
        return card;
    }

    private JPanel createFillLevelOperation() {
        OperationCard card = new OperationCard(
                "Update Fill Level",
                "Update the latest sensor reading received from a connected smart bin.",
                GuiStyles.ACCENT_BLUE
        );
        card.addField("Bin ID", updateBinIdField);
        card.addField("Fill Level", fillLevelField);
        card.setActionButton(updateFillLevelButton);
        return card;
    }

    private JPanel createDamageOperation() {
        OperationCard card = new OperationCard(
                "Report Damage",
                "Notify maintenance teams about damaged municipal infrastructure.",
                GuiStyles.ERROR
        );
        card.addField("Bin ID", damageBinIdField);

        JScrollPane descriptionScroll = new JScrollPane(damageDescriptionArea);
        descriptionScroll.setBorder(BorderFactory.createLineBorder(GuiStyles.BORDER));
        descriptionScroll.setPreferredSize(new Dimension(220, 72));
        descriptionScroll.getVerticalScrollBar().setUnitIncrement(10);

        card.addField("Damage Description", descriptionScroll);
        card.setActionButton(reportDamageButton);
        return card;
    }

    private JPanel createCollectionQueueCard() {
        SurfaceCard card = new SurfaceCard(GuiStyles.ACCENT_AMBER);
        card.setLayout(new BorderLayout(0, 15));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 245));
        card.setPreferredSize(new Dimension(1000, 245));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setOpaque(false);

        JLabel title = new JLabel("Bins Requiring Collection");
        title.setFont(GuiStyles.SECTION_TITLE_FONT);
        title.setForeground(GuiStyles.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel(
                "Assets currently exceeding the configured collection threshold."
        );
        description.setFont(GuiStyles.BODY_FONT);
        description.setForeground(GuiStyles.TEXT_SECONDARY);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        heading.add(title);
        heading.add(Box.createVerticalStrut(4));
        heading.add(description);

        card.add(heading, BorderLayout.NORTH);
        card.add(queueContentPanel, BorderLayout.CENTER);

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
        icon.setForeground(GuiStyles.SAGE_DARK);
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

    private void updateSelectedAsset() {
        int selectedRow = binsTable.getSelectedRow();

        if (selectedRow < 0) {
            inspectorContentLayout.show(inspectorContentPanel, EMPTY_VIEW);
            return;
        }

        String binId = String.valueOf(binsTableModel.getValueAt(selectedRow, 0));
        SmartBinViewData asset = assetsById.get(binId);

        if (asset == null) {
            inspectorContentLayout.show(inspectorContentPanel, EMPTY_VIEW);
            return;
        }

        selectedBinIdValue.setText(asset.binId());
        selectedLocationValue.setText(asset.location());
        selectedFillValue.setText(asset.fillLevel() + "%");
        selectedStatusValue.setText(asset.status());
        selectedConditionValue.setText(asset.condition());
        selectedLastUpdateValue.setText(asset.lastUpdate());
        inspectorContentLayout.show(inspectorContentPanel, DATA_VIEW);
    }

    public void setNetworkConnected(boolean connected) {
        networkStatusLabel.setText(connected ? "Online" : "Offline");
        networkStatusLabel.setForeground(
                connected ? new Color(155, 238, 189) : Color.WHITE
        );
    }

    public void setSmartBins(List<SmartBinViewData> assets) {
        assetsById.clear();
        binsTableModel.setRowCount(0);

        int totalFill = 0;
        int collectionAlerts = 0;
        int maintenanceReports = 0;

        for (SmartBinViewData asset : assets) {
            assetsById.put(asset.binId(), asset);
            binsTableModel.addRow(new Object[]{
                    asset.binId(),
                    asset.location(),
                    asset.fillLevel() + "%",
                    asset.status(),
                    asset.condition()
            });

            totalFill += asset.fillLevel();

            if (asset.status().equalsIgnoreCase("Collection required")) {
                collectionAlerts++;
            }
            if (!asset.condition().equalsIgnoreCase("Operational")) {
                maintenanceReports++;
            }
        }

        int count = assets.size();
        int averageFill = count == 0 ? 0 : totalFill / count;

        registeredBinsMetric.setValue(String.valueOf(count));
        averageFillMetric.setValue(averageFill + "%");
        collectionAlertsMetric.setValue(String.valueOf(collectionAlerts));
        maintenanceReportsMetric.setValue(String.valueOf(maintenanceReports));
        registeredCountBadge.setText(count + (count == 1 ? " ASSET" : " ASSETS"));
        networkAssetCountLabel.setText(count + (count == 1 ? " registered asset" : " registered assets"));

        binsTable.clearSelection();
        inspectorContentLayout.show(inspectorContentPanel, EMPTY_VIEW);
        binsContentLayout.show(binsContentPanel, count == 0 ? EMPTY_VIEW : DATA_VIEW);
    }

    public void setCollectionQueue(List<CollectionQueueItem> items) {
        queueTableModel.setRowCount(0);

        for (CollectionQueueItem item : items) {
            queueTableModel.addRow(new Object[]{
                    item.binId(),
                    item.location(),
                    item.fillLevel() + "%",
                    item.status(),
                    item.condition()
            });
        }

        collectionAlertsMetric.setValue(String.valueOf(items.size()));
        queueContentLayout.show(queueContentPanel, items.isEmpty() ? EMPTY_VIEW : DATA_VIEW);
    }

    public void addRegisterActionListener(ActionListener listener) {
        registerAssetButton.addActionListener(listener);
    }

    public void addUpdateFillLevelActionListener(ActionListener listener) {
        updateFillLevelButton.addActionListener(listener);
    }

    public void addReportDamageActionListener(ActionListener listener) {
        reportDamageButton.addActionListener(listener);
    }

    public String getRegistrationBinId() {
        return registerBinIdField.getText().trim();
    }

    public String getRegistrationLocation() {
        return registerLocationField.getText().trim();
    }

    public String getUpdateBinId() {
        return updateBinIdField.getText().trim();
    }

    public String getFillLevel() {
        return fillLevelField.getText().trim();
    }

    public String getDamageBinId() {
        return damageBinIdField.getText().trim();
    }

    public String getDamageDescription() {
        return damageDescriptionArea.getText().trim();
    }

    public record SmartBinViewData(
            String binId,
            String location,
            int fillLevel,
            String status,
            String condition,
            String lastUpdate
    ) {
    }

    public record CollectionQueueItem(
            String binId,
            String location,
            int fillLevel,
            String status,
            String condition
    ) {
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
