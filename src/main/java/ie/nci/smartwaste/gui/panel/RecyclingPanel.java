package ie.nci.smartwaste.gui.panel;

import ie.nci.smartwaste.gui.client.RecyclingGuiClient;
import ie.nci.smartwaste.gui.component.MetricCard;
import ie.nci.smartwaste.gui.component.StatusBadge;
import ie.nci.smartwaste.gui.component.SurfaceCard;
import ie.nci.smartwaste.gui.util.GuiStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class RecyclingPanel extends JPanel {

    private static final String EMPTY_VIEW = "empty";
    private static final String DATA_VIEW = "data";
    private static final String MATERIAL_PLACEHOLDER = "Select material";

    private final MetricCard intakeMetric;
    private final MetricCard deliveriesMetric;
    private final MetricCard processingQueueMetric;
    private final MetricCard capacityMetric;

    private final JLabel centreStatusLabel;
    private final JLabel centreStatusDetailLabel;
    private final JLabel deliveryCountBadge;

    private final DefaultTableModel deliveryHistoryModel;
    private final DefaultTableModel batchTableModel;
    private final DefaultTableModel processingQueueModel;
    private final JTable deliveryHistoryTable;
    private final JTable batchTable;
    private final JTable processingQueueTable;

    private final CardLayout deliveryContentLayout;
    private final JPanel deliveryContentPanel;
    private final CardLayout inspectorContentLayout;
    private final JPanel inspectorContentPanel;
    private final CardLayout batchContentLayout;
    private final JPanel batchContentPanel;
    private final CardLayout queueContentLayout;
    private final JPanel queueContentPanel;
    private final CardLayout activityContentLayout;
    private final JPanel activityContentPanel;

    private final JLabel selectedDeliveryIdValue;
    private final JLabel selectedVehicleIdValue;
    private final JLabel selectedMaterialValue;
    private final JLabel selectedWeightValue;
    private final JLabel selectedReceivedTimeValue;
    private final JLabel selectedProcessingStatusValue;
    private final JLabel selectedBatchPositionValue;
    private final JLabel selectedLastUpdateValue;

    private final JTextField deliveryIdField;
    private final JTextField vehicleIdField;
    private final JComboBox<String> materialTypeComboBox;
    private final JTextField weightField;
    private final JLabel feedbackLabel;

    private final JButton addDeliveryButton;
    private final JButton removeDeliveryButton;
    private final JButton clearBatchButton;
    private final JButton uploadBatchButton;

    private final DefaultListModel<ActivityViewData> activityListModel;
    private final Map<String, BatchDelivery> currentBatch = new LinkedHashMap<>();
    private final Map<String, DeliveryViewData> deliveriesById = new LinkedHashMap<>();

    private final DateTimeFormatter timestampFormatter =
            DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm:ss");

    private Consumer<String> activityLogger = message -> {
    };
    private boolean uploadInProgress;
    private double totalIntakeKg;
    private int completedDeliveries;

    public RecyclingPanel() {
        setLayout(new BorderLayout());
        setBackground(GuiStyles.BACKGROUND);
        setBorder(new EmptyBorder(18, 22, 18, 22));

        intakeMetric = new MetricCard(
                "Today's Intake",
                "0 kg",
                "Recyclable material received today",
                GuiStyles.ACCENT_TEAL
        );
        deliveriesMetric = new MetricCard(
                "Deliveries",
                "0",
                "Completed delivery uploads",
                GuiStyles.PRIMARY
        );
        processingQueueMetric = new MetricCard(
                "Processing Queue",
                "0",
                "Deliveries awaiting processing",
                GuiStyles.ACCENT_AMBER
        );
        capacityMetric = new MetricCard(
                "Centre Capacity",
                "0%",
                "Current storage utilisation",
                GuiStyles.ACCENT_BLUE
        );

        centreStatusLabel = new JLabel("Standby");
        centreStatusDetailLabel = new JLabel("No active intake");
        deliveryCountBadge = new JLabel("0 DELIVERIES");

        deliveryHistoryModel = createReadOnlyTableModel(
                "Delivery ID",
                "Vehicle ID",
                "Material Type",
                "Weight",
                "Received At",
                "Status"
        );
        batchTableModel = createReadOnlyTableModel(
                "Delivery ID",
                "Vehicle ID",
                "Material Type",
                "Weight",
                "Batch Status"
        );
        processingQueueModel = createReadOnlyTableModel(
                "Queue Position",
                "Delivery ID",
                "Material Type",
                "Weight",
                "Status",
                "Estimated Processing State"
        );

        deliveryHistoryTable = new JTable(deliveryHistoryModel);
        batchTable = new JTable(batchTableModel);
        processingQueueTable = new JTable(processingQueueModel);
        configureTables();

        deliveryContentLayout = new CardLayout();
        deliveryContentPanel = new JPanel(deliveryContentLayout);
        inspectorContentLayout = new CardLayout();
        inspectorContentPanel = new JPanel(inspectorContentLayout);
        batchContentLayout = new CardLayout();
        batchContentPanel = new JPanel(batchContentLayout);
        queueContentLayout = new CardLayout();
        queueContentPanel = new JPanel(queueContentLayout);
        activityContentLayout = new CardLayout();
        activityContentPanel = new JPanel(activityContentLayout);

        selectedDeliveryIdValue = createInspectorValue();
        selectedVehicleIdValue = createInspectorValue();
        selectedMaterialValue = createInspectorValue();
        selectedWeightValue = createInspectorValue();
        selectedReceivedTimeValue = createInspectorValue();
        selectedProcessingStatusValue = createInspectorValue();
        selectedBatchPositionValue = createInspectorValue();
        selectedLastUpdateValue = createInspectorValue();

        deliveryIdField = GuiStyles.createTextField();
        vehicleIdField = GuiStyles.createTextField();
        materialTypeComboBox = GuiStyles.createComboBox(
                MATERIAL_PLACEHOLDER,
                "Plastic",
                "Glass",
                "Paper",
                "Metal",
                "Organic Waste",
                "Electronic Waste",
                "Mixed Recyclables"
        );
        weightField = GuiStyles.createTextField();

        feedbackLabel = new JLabel(" ");
        feedbackLabel.setFont(GuiStyles.BODY_BOLD_FONT);
        feedbackLabel.setForeground(GuiStyles.TEXT_SECONDARY);

        addDeliveryButton = GuiStyles.createSecondaryButton("Add Delivery to Batch");
        removeDeliveryButton = GuiStyles.createSecondaryButton("Remove Selected Delivery");
        clearBatchButton = GuiStyles.createSecondaryButton("Clear Batch");
        uploadBatchButton = GuiStyles.createPrimaryButton("Upload Delivery Batch");

        activityListModel = new DefaultListModel<>();

        configureDataViews();
        configureActions();

        ScrollableContentPanel content = new ScrollableContentPanel();
        content.add(createPageHeading());
        content.add(Box.createVerticalStrut(16));
        content.add(createHeroPanel());
        content.add(Box.createVerticalStrut(18));
        content.add(createMetricsRow());
        content.add(Box.createVerticalStrut(24));
        content.add(createSectionHeading(
                "Delivery workspace",
                "Inspect recyclable material received by the municipal recycling centre."
        ));
        content.add(Box.createVerticalStrut(12));
        content.add(createDeliveryWorkspace());
        content.add(Box.createVerticalStrut(24));
        content.add(createBatchUploadSection());
        content.add(Box.createVerticalStrut(18));
        content.add(createProcessingQueueSection());
        content.add(Box.createVerticalStrut(18));
        content.add(createActivitySection());

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void configureTables() {
        GuiStyles.styleTable(deliveryHistoryTable);
        GuiStyles.styleTable(batchTable);
        GuiStyles.styleTable(processingQueueTable);

        deliveryHistoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        batchTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        processingQueueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        deliveryHistoryTable.getColumnModel()
                .getColumn(5)
                .setCellRenderer(new StatusTableCellRenderer());
        batchTable.getColumnModel()
                .getColumn(4)
                .setCellRenderer(new StatusTableCellRenderer());
        processingQueueTable.getColumnModel()
                .getColumn(4)
                .setCellRenderer(new StatusTableCellRenderer());
    }

    private void configureDataViews() {
        deliveryContentPanel.setOpaque(false);
        deliveryContentPanel.add(
                createEmptyState(
                        "No recycling deliveries recorded.",
                        "Upload the first delivery batch to begin monitoring centre intake."
                ),
                EMPTY_VIEW
        );
        deliveryContentPanel.add(createTableScrollPane(deliveryHistoryTable), DATA_VIEW);
        deliveryContentLayout.show(deliveryContentPanel, EMPTY_VIEW);

        inspectorContentPanel.setOpaque(false);
        inspectorContentPanel.add(
                createEmptyState(
                        "No delivery selected.",
                        "Select a delivery to inspect recycling details."
                ),
                EMPTY_VIEW
        );
        inspectorContentPanel.add(createDeliveryDetailsPanel(), DATA_VIEW);
        inspectorContentLayout.show(inspectorContentPanel, EMPTY_VIEW);

        batchContentPanel.setOpaque(false);
        batchContentPanel.add(
                createEmptyState(
                        "No deliveries added to the current batch.",
                        "Add one or more deliveries before uploading."
                ),
                EMPTY_VIEW
        );
        batchContentPanel.add(createTableScrollPane(batchTable), DATA_VIEW);
        batchContentLayout.show(batchContentPanel, EMPTY_VIEW);

        queueContentPanel.setOpaque(false);
        queueContentPanel.add(
                createEmptyState(
                        "No deliveries are currently awaiting processing.",
                        "New processing work will appear here when it becomes available."
                ),
                EMPTY_VIEW
        );
        queueContentPanel.add(createTableScrollPane(processingQueueTable), DATA_VIEW);
        queueContentLayout.show(queueContentPanel, EMPTY_VIEW);

        activityContentPanel.setOpaque(false);
        activityContentPanel.add(
                createEmptyState(
                        "No recycling activity recorded.",
                        "Delivery and processing events will appear here."
                ),
                EMPTY_VIEW
        );

        JList<ActivityViewData> activityList = new JList<>(activityListModel);
        activityList.setCellRenderer(new ActivityListCellRenderer());
        activityList.setBackground(GuiStyles.SURFACE_SECONDARY);
        activityList.setSelectionBackground(GuiStyles.PRIMARY_LIGHT);
        activityList.setSelectionForeground(GuiStyles.TEXT_PRIMARY);

        JScrollPane activityScroll = new JScrollPane(activityList);
        activityScroll.setBorder(BorderFactory.createLineBorder(GuiStyles.BORDER_LIGHT));
        activityScroll.getVerticalScrollBar().setUnitIncrement(12);
        activityContentPanel.add(activityScroll, DATA_VIEW);
        activityContentLayout.show(activityContentPanel, EMPTY_VIEW);

        deliveryHistoryTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                updateDeliveryInspector();
            }
        });
    }

    private void configureActions() {
        addDeliveryButton.addActionListener(event -> addDeliveryToBatch());
        removeDeliveryButton.addActionListener(event -> removeSelectedDelivery());
        clearBatchButton.addActionListener(event -> clearBatch(true));
        uploadBatchButton.addActionListener(event -> uploadDeliveryBatch());
    }

    private JPanel createPageHeading() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel title = new JLabel("Recycling Operations");
        title.setFont(GuiStyles.TITLE_FONT);
        title.setForeground(GuiStyles.TEXT_PRIMARY);

        JLabel subtitle = new JLabel(
                "Manage municipal recycling intake, deliveries and processing activity."
        );
        subtitle.setFont(GuiStyles.BODY_FONT);
        subtitle.setForeground(GuiStyles.TEXT_SECONDARY);

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subtitle);

        JLabel areaLabel = new JLabel("RESOURCE RECOVERY");
        areaLabel.setFont(GuiStyles.LABEL_FONT);
        areaLabel.setForeground(new Color(27, 103, 88));
        areaLabel.setBorder(new EmptyBorder(8, 12, 8, 12));
        areaLabel.setOpaque(true);
        areaLabel.setBackground(new Color(220, 240, 234));

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

        JLabel eyebrow = new JLabel("MUNICIPAL RESOURCE RECOVERY CENTRE");
        eyebrow.setFont(GuiStyles.LABEL_FONT);
        eyebrow.setForeground(new Color(177, 226, 212));
        eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Recover more value from every municipal collection.");
        title.setFont(GuiStyles.DISPLAY_FONT);
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel(
                "<html><div style='width:650px'>Monitor recyclable material deliveries, manage centre "
                        + "intake, process incoming waste and maintain visibility across the city’s "
                        + "recycling operations.</div></html>"
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

        JLabel statusTitle = new JLabel("RECYCLING CENTRE");
        statusTitle.setFont(GuiStyles.LABEL_FONT);
        statusTitle.setForeground(new Color(185, 216, 201));

        centreStatusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 23));
        centreStatusLabel.setForeground(Color.WHITE);

        centreStatusDetailLabel.setFont(GuiStyles.BODY_FONT);
        centreStatusDetailLabel.setForeground(new Color(216, 232, 224));

        statusTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        centreStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centreStatusDetailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        statusContent.add(statusTitle);
        statusContent.add(Box.createVerticalStrut(10));
        statusContent.add(centreStatusLabel);
        statusContent.add(Box.createVerticalStrut(5));
        statusContent.add(centreStatusDetailLabel);

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

        panel.add(intakeMetric);
        panel.add(deliveriesMetric);
        panel.add(processingQueueMetric);
        panel.add(capacityMetric);

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

    private JPanel createDeliveryWorkspace() {
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
        JPanel historyCard = createDeliveryHistoryCard();
        historyCard.setPreferredSize(new Dimension(650, 350));
        workspace.add(historyCard, constraints);

        constraints.gridx = 1;
        constraints.weightx = 0.35;
        constraints.insets = new Insets(0, 7, 0, 0);
        JPanel inspectorCard = createDeliveryInspectorCard();
        inspectorCard.setPreferredSize(new Dimension(350, 350));
        workspace.add(inspectorCard, constraints);

        return workspace;
    }

    private JPanel createDeliveryHistoryCard() {
        SurfaceCard card = new SurfaceCard();
        card.setLayout(new BorderLayout(0, 15));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);

        JLabel title = new JLabel("Delivery History");
        title.setFont(GuiStyles.SECTION_TITLE_FONT);
        title.setForeground(GuiStyles.TEXT_PRIMARY);

        JLabel description = new JLabel(
                "Review recyclable waste deliveries received by the municipal recycling centre."
        );
        description.setFont(GuiStyles.BODY_FONT);
        description.setForeground(GuiStyles.TEXT_SECONDARY);

        text.add(title);
        text.add(Box.createVerticalStrut(4));
        text.add(description);

        deliveryCountBadge.setFont(GuiStyles.LABEL_FONT);
        deliveryCountBadge.setForeground(new Color(27, 103, 88));
        deliveryCountBadge.setOpaque(true);
        deliveryCountBadge.setBackground(new Color(220, 240, 234));
        deliveryCountBadge.setBorder(new EmptyBorder(7, 10, 7, 10));

        heading.add(text, BorderLayout.WEST);
        heading.add(deliveryCountBadge, BorderLayout.EAST);

        card.add(heading, BorderLayout.NORTH);
        card.add(deliveryContentPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createDeliveryInspectorCard() {
        SurfaceCard card = new SurfaceCard();
        card.setLayout(new BorderLayout(0, 15));

        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setOpaque(false);

        JLabel title = new JLabel("Delivery Inspector");
        title.setFont(GuiStyles.SECTION_TITLE_FONT);
        title.setForeground(GuiStyles.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel("Operational details for the selected recycling delivery.");
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

    private JPanel createDeliveryDetailsPanel() {
        JPanel details = new JPanel(new GridLayout(8, 2, 12, 8));
        details.setOpaque(false);
        addInspectorRow(details, "Delivery ID", selectedDeliveryIdValue);
        addInspectorRow(details, "Vehicle ID", selectedVehicleIdValue);
        addInspectorRow(details, "Material Type", selectedMaterialValue);
        addInspectorRow(details, "Weight", selectedWeightValue);
        addInspectorRow(details, "Received Time", selectedReceivedTimeValue);
        addInspectorRow(details, "Processing Status", selectedProcessingStatusValue);
        addInspectorRow(details, "Batch Position", selectedBatchPositionValue);
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

    private JPanel createBatchUploadSection() {
        SurfaceCard card = new SurfaceCard(GuiStyles.ACCENT_TEAL);
        card.setLayout(new BorderLayout(0, 16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 455));
        card.setPreferredSize(new Dimension(1000, 455));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setOpaque(false);

        JLabel title = new JLabel("Upload Recycling Deliveries");
        title.setFont(GuiStyles.SECTION_TITLE_FONT);
        title.setForeground(GuiStyles.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel(
                "Submit one or more recyclable material deliveries to the municipal recycling centre."
        );
        description.setFont(GuiStyles.BODY_FONT);
        description.setForeground(GuiStyles.TEXT_SECONDARY);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        heading.add(title);
        heading.add(Box.createVerticalStrut(4));
        heading.add(description);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        JPanel fields = createDeliveryEntryFields();
        fields.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        fields.setAlignmentX(Component.LEFT_ALIGNMENT);

        feedbackLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel batchActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        batchActions.setOpaque(false);
        batchActions.add(addDeliveryButton);
        batchActions.add(removeDeliveryButton);
        batchActions.add(clearBatchButton);

        actions.add(batchActions, BorderLayout.WEST);
        actions.add(uploadBatchButton, BorderLayout.EAST);

        JPanel batchArea = new JPanel(new BorderLayout());
        batchArea.setOpaque(false);
        batchArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 205));
        batchArea.setPreferredSize(new Dimension(1000, 205));
        batchArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        batchArea.add(batchContentPanel, BorderLayout.CENTER);

        body.add(fields);
        body.add(Box.createVerticalStrut(10));
        body.add(feedbackLabel);
        body.add(Box.createVerticalStrut(8));
        body.add(actions);
        body.add(Box.createVerticalStrut(14));
        body.add(batchArea);

        card.add(heading, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel createDeliveryEntryFields() {
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0.25;

        constraints.gridx = 0;
        constraints.insets = new Insets(0, 0, 0, 8);
        fields.add(createField("Delivery ID", deliveryIdField), constraints);

        constraints.gridx = 1;
        constraints.insets = new Insets(0, 8, 0, 8);
        fields.add(createField("Vehicle ID", vehicleIdField), constraints);

        constraints.gridx = 2;
        fields.add(createField("Material Type", materialTypeComboBox), constraints);

        constraints.gridx = 3;
        constraints.insets = new Insets(0, 8, 0, 0);
        fields.add(createField("Weight in kg", weightField), constraints);

        return fields;
    }

    private JPanel createField(String labelText, JComponent input) {
        JPanel field = new JPanel();
        field.setLayout(new BoxLayout(field, BoxLayout.Y_AXIS));
        field.setOpaque(false);

        JLabel label = new JLabel(labelText.toUpperCase());
        label.setFont(GuiStyles.LABEL_FONT);
        label.setForeground(GuiStyles.TEXT_SECONDARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        field.add(label);
        field.add(Box.createVerticalStrut(6));
        field.add(input);
        return field;
    }

    private JPanel createProcessingQueueSection() {
        SurfaceCard card = new SurfaceCard(GuiStyles.ACCENT_TEAL);
        card.setLayout(new BorderLayout(0, 15));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 255));
        card.setPreferredSize(new Dimension(1000, 255));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setOpaque(false);

        JLabel title = new JLabel("Processing Queue");
        title.setFont(GuiStyles.SECTION_TITLE_FONT);
        title.setForeground(GuiStyles.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel(
                "Monitor deliveries currently awaiting recycling processing."
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

    private JPanel createActivitySection() {
        SurfaceCard card = new SurfaceCard();
        card.setLayout(new BorderLayout(0, 15));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 235));
        card.setPreferredSize(new Dimension(1000, 235));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setOpaque(false);

        JLabel title = new JLabel("Recycling Activity");
        title.setFont(GuiStyles.SECTION_TITLE_FONT);
        title.setForeground(GuiStyles.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel("Track recent delivery and processing events.");
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

    private JPanel createEmptyState(String titleText, String descriptionText) {
        JPanel emptyState = new JPanel(new GridBagLayout());
        emptyState.setOpaque(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel icon = new JLabel("○");
        icon.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 27));
        icon.setForeground(GuiStyles.ACCENT_TEAL);
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

    private void addDeliveryToBatch() {
        if (uploadInProgress) {
            return;
        }

        String deliveryId = deliveryIdField.getText().trim();
        String vehicleId = vehicleIdField.getText().trim();
        String materialType = String.valueOf(materialTypeComboBox.getSelectedItem());
        String weightText = weightField.getText().trim();

        if (deliveryId.isEmpty()) {
            showValidationError("Enter a valid delivery ID.");
            return;
        }
        if (vehicleId.isEmpty()) {
            showValidationError("Enter a valid vehicle ID.");
            return;
        }
        if (MATERIAL_PLACEHOLDER.equals(materialType)) {
            showValidationError("Select a material type.");
            return;
        }

        double weight;

        try {
            weight = Double.parseDouble(weightText);
        } catch (NumberFormatException exception) {
            showValidationError("Enter a valid numeric weight.");
            return;
        }

        if (weight <= 0) {
            showValidationError("Enter a weight greater than zero.");
            return;
        }
        if (currentBatch.containsKey(deliveryId)) {
            showValidationError("This delivery already exists in the current batch.");
            return;
        }

        BatchDelivery delivery = new BatchDelivery(
                deliveryId,
                vehicleId,
                materialType,
                weight,
                "Ready"
        );

        currentBatch.put(deliveryId, delivery);
        batchTableModel.addRow(new Object[]{
                delivery.deliveryId(),
                delivery.vehicleId(),
                delivery.materialType(),
                formatWeight(delivery.weightKg()),
                delivery.status()
        });

        batchContentLayout.show(batchContentPanel, DATA_VIEW);
        showFeedback("Delivery added to the current batch.", GuiStyles.PRIMARY_DARK);
        addActivity("Delivery added to batch", deliveryId);
        activityLogger.accept("Delivery " + deliveryId + " added to the recycling batch.");
        clearEntryFields();
    }

    private void removeSelectedDelivery() {
        if (uploadInProgress) {
            return;
        }

        int selectedRow = batchTable.getSelectedRow();

        if (selectedRow < 0) {
            showValidationError("Select a delivery to remove from the batch.");
            return;
        }

        String deliveryId = String.valueOf(batchTableModel.getValueAt(selectedRow, 0));
        currentBatch.remove(deliveryId);
        batchTableModel.removeRow(selectedRow);

        if (currentBatch.isEmpty()) {
            batchContentLayout.show(batchContentPanel, EMPTY_VIEW);
        }

        showFeedback("Delivery removed from the current batch.", GuiStyles.TEXT_SECONDARY);
        activityLogger.accept("Delivery " + deliveryId + " removed from the recycling batch.");
    }

    private void clearBatch(boolean notifyUser) {
        clearBatch(notifyUser, false);
    }

    private void clearBatch(boolean notifyUser, boolean force) {
        if (uploadInProgress && !force) {
            return;
        }

        currentBatch.clear();
        batchTableModel.setRowCount(0);
        batchContentLayout.show(batchContentPanel, EMPTY_VIEW);

        if (notifyUser) {
            showFeedback("The current delivery batch has been cleared.", GuiStyles.TEXT_SECONDARY);
            activityLogger.accept("Recycling delivery batch cleared.");
        }
    }

    private void uploadDeliveryBatch() {
        if (uploadInProgress) {
            return;
        }
        if (currentBatch.isEmpty()) {
            showValidationError("Add one or more deliveries before uploading.");
            return;
        }

        List<BatchDelivery> batchSnapshot = new ArrayList<>(currentBatch.values());
        List<RecyclingGuiClient.DeliveryUpload> uploadSnapshot = batchSnapshot
                .stream()
                .map(delivery -> new RecyclingGuiClient.DeliveryUpload(
                        delivery.deliveryId(),
                        delivery.vehicleId(),
                        delivery.materialType(),
                        delivery.weightKg()
                ))
                .toList();

        setUploadInProgress(true);
        setCentreState("Receiving Deliveries", batchSnapshot.size() + " deliveries in progress");
        showFeedback("Uploading the current delivery batch...", GuiStyles.ACCENT_TEAL);
        addActivity("Batch upload started", batchSnapshot.size() + " deliveries");
        activityLogger.accept("Recycling delivery batch upload started.");

        SwingWorker<RecyclingGuiClient.UploadResult, Void> worker = new SwingWorker<>() {
            @Override
            protected RecyclingGuiClient.UploadResult doInBackground() throws Exception {
                try (RecyclingGuiClient client = new RecyclingGuiClient()) {
                    return client.uploadDeliveries(uploadSnapshot);
                }
            }

            @Override
            protected void done() {
                try {
                    RecyclingGuiClient.UploadResult result = get();
                    handleUploadSuccess(batchSnapshot, result);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    handleUploadFailure("The upload was interrupted.");
                } catch (ExecutionException exception) {
                    handleUploadFailure("The recycling service is currently unavailable.");
                } finally {
                    setUploadInProgress(false);
                }
            }
        };

        worker.execute();
    }

    private void handleUploadSuccess(
            List<BatchDelivery> batchSnapshot,
            RecyclingGuiClient.UploadResult result
    ) {
        String timestamp = LocalDateTime.now().format(timestampFormatter);
        int accepted = Math.min(result.deliveriesProcessed(), batchSnapshot.size());

        for (int index = 0; index < accepted; index++) {
            BatchDelivery batchDelivery = batchSnapshot.get(index);
            DeliveryViewData delivery = new DeliveryViewData(
                    batchDelivery.deliveryId(),
                    batchDelivery.vehicleId(),
                    batchDelivery.materialType(),
                    batchDelivery.weightKg(),
                    timestamp,
                    "Received",
                    index + 1,
                    timestamp
            );
            deliveriesById.put(delivery.deliveryId(), delivery);
        }

        completedDeliveries += accepted;
        totalIntakeKg += result.totalWeightKg();
        deliveriesMetric.setValue(String.valueOf(completedDeliveries));
        intakeMetric.setValue(formatWeight(totalIntakeKg));
        refreshDeliveryHistory();
        clearBatch(false, true);

        setCentreState("Standby", "No active intake");
        showFeedback("Delivery batch uploaded successfully.", GuiStyles.PRIMARY_DARK);
        addActivity("Delivery received", accepted + " deliveries accepted");
        activityLogger.accept(
                "Delivery batch uploaded successfully: "
                        + accepted
                        + " deliveries, "
                        + formatWeight(result.totalWeightKg())
                        + "."
        );
    }

    private void handleUploadFailure(String userMessage) {
        setCentreState("Standby", "No active intake");
        showValidationError(userMessage);
        addActivity("Upload failed", "Delivery batch retained for retry");
        activityLogger.accept("Recycling delivery upload failed. The batch was retained for retry.");
    }

    private void setUploadInProgress(boolean inProgress) {
        uploadInProgress = inProgress;
        addDeliveryButton.setEnabled(!inProgress);
        removeDeliveryButton.setEnabled(!inProgress);
        clearBatchButton.setEnabled(!inProgress);
        uploadBatchButton.setEnabled(!inProgress);
        uploadBatchButton.setText(inProgress ? "Uploading..." : "Upload Delivery Batch");
    }

    private void refreshDeliveryHistory() {
        deliveryHistoryModel.setRowCount(0);

        for (DeliveryViewData delivery : deliveriesById.values()) {
            deliveryHistoryModel.addRow(new Object[]{
                    delivery.deliveryId(),
                    delivery.vehicleId(),
                    delivery.materialType(),
                    formatWeight(delivery.weightKg()),
                    delivery.receivedTime(),
                    delivery.processingStatus()
            });
        }

        int count = deliveriesById.size();
        deliveryCountBadge.setText(count + (count == 1 ? " DELIVERY" : " DELIVERIES"));
        deliveryHistoryTable.clearSelection();
        inspectorContentLayout.show(inspectorContentPanel, EMPTY_VIEW);
        deliveryContentLayout.show(deliveryContentPanel, count == 0 ? EMPTY_VIEW : DATA_VIEW);
    }

    private void updateDeliveryInspector() {
        int selectedRow = deliveryHistoryTable.getSelectedRow();

        if (selectedRow < 0) {
            inspectorContentLayout.show(inspectorContentPanel, EMPTY_VIEW);
            return;
        }

        String deliveryId = String.valueOf(deliveryHistoryModel.getValueAt(selectedRow, 0));
        DeliveryViewData delivery = deliveriesById.get(deliveryId);

        if (delivery == null) {
            inspectorContentLayout.show(inspectorContentPanel, EMPTY_VIEW);
            return;
        }

        selectedDeliveryIdValue.setText(delivery.deliveryId());
        selectedVehicleIdValue.setText(delivery.vehicleId());
        selectedMaterialValue.setText(delivery.materialType());
        selectedWeightValue.setText(formatWeight(delivery.weightKg()));
        selectedReceivedTimeValue.setText(delivery.receivedTime());
        selectedProcessingStatusValue.setText(delivery.processingStatus());
        selectedBatchPositionValue.setText(String.valueOf(delivery.batchPosition()));
        selectedLastUpdateValue.setText(delivery.lastUpdate());
        inspectorContentLayout.show(inspectorContentPanel, DATA_VIEW);
    }

    private void addActivity(String event, String details) {
        ActivityViewData activity = new ActivityViewData(
                LocalDateTime.now().format(timestampFormatter),
                event,
                details
        );
        activityListModel.insertElementAt(activity, 0);
        activityContentLayout.show(activityContentPanel, DATA_VIEW);
    }

    private void clearEntryFields() {
        deliveryIdField.setText("");
        vehicleIdField.setText("");
        materialTypeComboBox.setSelectedIndex(0);
        weightField.setText("");
        deliveryIdField.requestFocusInWindow();
    }

    private void showValidationError(String message) {
        showFeedback(message, GuiStyles.ERROR);
    }

    private void showFeedback(String message, Color color) {
        feedbackLabel.setText(message);
        feedbackLabel.setForeground(color);
    }

    private String formatWeight(double weightKg) {
        if (Math.rint(weightKg) == weightKg) {
            return String.format("%.0f kg", weightKg);
        }
        return String.format("%.2f kg", weightKg);
    }

    public void setActivityLogger(Consumer<String> activityLogger) {
        this.activityLogger = activityLogger == null ? message -> {
        } : activityLogger;
    }

    public void setNetworkConnected(boolean connected) {
        if (connected) {
            setCentreState("Standby", "No active intake");
        } else {
            setCentreState("Offline", "Centre unavailable");
        }
    }

    public void setCentreState(String state, String detail) {
        centreStatusLabel.setText(state);
        centreStatusDetailLabel.setText(detail);

        if ("At Capacity".equalsIgnoreCase(state)) {
            centreStatusLabel.setForeground(new Color(255, 218, 154));
        } else if ("Offline".equalsIgnoreCase(state)) {
            centreStatusLabel.setForeground(new Color(240, 193, 190));
        } else {
            centreStatusLabel.setForeground(Color.WHITE);
        }
    }

    public void setCentreCapacity(double utilisationPercent) {
        double safeValue = Math.max(0, Math.min(utilisationPercent, 100));
        capacityMetric.setValue(String.format("%.0f%%", safeValue));
    }

    public void setDeliveryHistory(List<DeliveryViewData> deliveries) {
        deliveriesById.clear();
        deliveries.forEach(delivery -> deliveriesById.put(delivery.deliveryId(), delivery));
        refreshDeliveryHistory();
    }

    public void setProcessingQueue(List<ProcessingQueueItem> items) {
        processingQueueModel.setRowCount(0);

        for (ProcessingQueueItem item : items) {
            processingQueueModel.addRow(new Object[]{
                    item.queuePosition(),
                    item.deliveryId(),
                    item.materialType(),
                    formatWeight(item.weightKg()),
                    item.status(),
                    item.estimatedProcessingState()
            });
        }

        processingQueueMetric.setValue(String.valueOf(items.size()));
        queueContentLayout.show(queueContentPanel, items.isEmpty() ? EMPTY_VIEW : DATA_VIEW);
    }

    public void setActivity(List<ActivityViewData> activities) {
        activityListModel.clear();
        activities.forEach(activityListModel::addElement);
        activityContentLayout.show(
                activityContentPanel,
                activities.isEmpty() ? EMPTY_VIEW : DATA_VIEW
        );
    }

    public record DeliveryViewData(
            String deliveryId,
            String vehicleId,
            String materialType,
            double weightKg,
            String receivedTime,
            String processingStatus,
            int batchPosition,
            String lastUpdate
    ) {
    }

    public record ProcessingQueueItem(
            int queuePosition,
            String deliveryId,
            String materialType,
            double weightKg,
            String status,
            String estimatedProcessingState
    ) {
    }

    public record ActivityViewData(
            String timestamp,
            String event,
            String details
    ) {
    }

    private record BatchDelivery(
            String deliveryId,
            String vehicleId,
            String materialType,
            double weightKg,
            String status
    ) {
    }

    private static class StatusTableCellRenderer extends StatusBadge
            implements TableCellRenderer {

        private StatusTableCellRenderer() {
            super(State.STANDBY);
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            String status = String.valueOf(value);
            setState(toState(status), status.toUpperCase());
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return this;
        }

        private State toState(String status) {
            return switch (status.toLowerCase()) {
                case "completed", "received", "ready" -> State.CONNECTED;
                case "queued", "processing" -> State.WARNING;
                case "rejected", "failed" -> State.DISCONNECTED;
                default -> State.STANDBY;
            };
        }
    }

    private static class ActivityListCellRenderer extends JPanel
            implements ListCellRenderer<ActivityViewData> {

        private final JLabel eventLabel = new JLabel();
        private final JLabel detailsLabel = new JLabel();
        private final JLabel timestampLabel = new JLabel();

        private ActivityListCellRenderer() {
            setLayout(new BorderLayout(12, 2));
            setBorder(new EmptyBorder(9, 11, 9, 11));

            eventLabel.setFont(GuiStyles.BODY_BOLD_FONT);
            eventLabel.setForeground(GuiStyles.TEXT_PRIMARY);

            detailsLabel.setFont(GuiStyles.CAPTION_FONT);
            detailsLabel.setForeground(GuiStyles.TEXT_SECONDARY);

            timestampLabel.setFont(GuiStyles.CAPTION_FONT);
            timestampLabel.setForeground(GuiStyles.TEXT_MUTED);

            JPanel text = new JPanel();
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            text.setOpaque(false);
            text.add(eventLabel);
            text.add(Box.createVerticalStrut(2));
            text.add(detailsLabel);

            add(text, BorderLayout.CENTER);
            add(timestampLabel, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends ActivityViewData> list,
                ActivityViewData value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            eventLabel.setText(value.event());
            detailsLabel.setText(value.details());
            timestampLabel.setText(value.timestamp());
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
