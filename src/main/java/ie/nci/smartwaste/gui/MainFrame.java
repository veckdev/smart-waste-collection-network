package ie.nci.smartwaste.gui;

import ie.nci.smartwaste.gui.component.ActivityLogPanel;
import ie.nci.smartwaste.gui.component.ServiceStatusPanel;
import ie.nci.smartwaste.gui.component.StatusBadge;
import ie.nci.smartwaste.gui.panel.CollectionPanel;
import ie.nci.smartwaste.gui.panel.DashboardPanel;
import ie.nci.smartwaste.gui.panel.LiveOperationsPanel;
import ie.nci.smartwaste.gui.panel.RecyclingPanel;
import ie.nci.smartwaste.gui.panel.SmartBinPanel;
import ie.nci.smartwaste.gui.util.GuiStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {

    private final ActivityLogPanel activityLogPanel;
    private final DashboardPanel dashboardPanel;
    private final SmartBinPanel smartBinPanel;
    private final CollectionPanel collectionPanel;
    private final RecyclingPanel recyclingPanel;
    private final LiveOperationsPanel liveOperationsPanel;

    private final ServiceStatusPanel smartBinStatus;
    private final ServiceStatusPanel collectionStatus;
    private final ServiceStatusPanel recyclingStatus;

    public MainFrame() {
        super("Smart Waste Collection Network");

        activityLogPanel = new ActivityLogPanel();
        dashboardPanel = new DashboardPanel();
        smartBinPanel = new SmartBinPanel();
        collectionPanel = new CollectionPanel();
        recyclingPanel = new RecyclingPanel();
        liveOperationsPanel = new LiveOperationsPanel();
        recyclingPanel.setActivityLogger(activityLogPanel::log);

        smartBinStatus = new ServiceStatusPanel("Smart Bins");
        collectionStatus = new ServiceStatusPanel("Collections");
        recyclingStatus = new ServiceStatusPanel("Recycling");

        configureFrame();
        buildLayout();
    }

    private void configureFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 740));
        setSize(1320, 880);
        setLocationRelativeTo(null);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(GuiStyles.BACKGROUND);
    }

    private void buildLayout() {
        add(createHeaderPanel(), BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                createTabbedPane(),
                activityLogPanel
        );

        splitPane.setResizeWeight(0.79);
        splitPane.setDividerLocation(650);
        splitPane.setDividerSize(4);
        splitPane.setBorder(null);
        splitPane.setBackground(GuiStyles.BACKGROUND);
        splitPane.setContinuousLayout(true);

        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(GuiStyles.BACKGROUND);
        mainContent.add(splitPane, BorderLayout.CENTER);
        mainContent.add(createFooterPanel(), BorderLayout.SOUTH);

        add(mainContent, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(24, 0));
        headerPanel.setBackground(GuiStyles.HEADER);
        headerPanel.setBorder(new EmptyBorder(15, 24, 15, 24));

        headerPanel.add(createBrandPanel(), BorderLayout.WEST);
        headerPanel.add(createServicesPanel(), BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createBrandPanel() {
        JPanel brandPanel = new JPanel(new BorderLayout(14, 0));
        brandPanel.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Smart Waste Collection Network");
        title.setFont(GuiStyles.TITLE_FONT);
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Municipal Waste Operations Platform");
        subtitle.setFont(GuiStyles.BODY_FONT);
        subtitle.setForeground(new Color(191, 216, 204));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(3));
        titlePanel.add(subtitle);

        JLabel brandIcon = new JLabel("♻");
        brandIcon.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 35));
        brandIcon.setForeground(new Color(128, 218, 168));

        brandPanel.add(brandIcon, BorderLayout.WEST);
        brandPanel.add(titlePanel, BorderLayout.CENTER);

        return brandPanel;
    }

    private JPanel createServicesPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 3));
        wrapper.setOpaque(false);

        JLabel label = new JLabel("SERVICE NETWORK", SwingConstants.RIGHT);
        label.setFont(GuiStyles.LABEL_FONT);
        label.setForeground(new Color(156, 191, 174));

        JPanel services = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        services.setOpaque(false);
        services.add(smartBinStatus);
        services.add(collectionStatus);
        services.add(recyclingStatus);

        wrapper.add(label, BorderLayout.NORTH);
        wrapper.add(services, BorderLayout.CENTER);

        return wrapper;
    }

    private JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        tabbedPane.setBackground(GuiStyles.SURFACE);
        tabbedPane.setForeground(GuiStyles.TEXT_PRIMARY);
        tabbedPane.setBorder(new EmptyBorder(5, 8, 0, 8));

        tabbedPane.addTab("Control Centre", dashboardPanel);
        tabbedPane.addTab("Smart Bins", smartBinPanel);
        tabbedPane.addTab("Collections", collectionPanel);
        tabbedPane.addTab("Recycling", recyclingPanel);
        tabbedPane.addTab("Live Network", liveOperationsPanel);

        tabbedPane.addChangeListener(event -> {
            int selectedIndex = tabbedPane.getSelectedIndex();

            if (selectedIndex >= 0) {
                activityLogPanel.log(
                        "Opened " + tabbedPane.getTitleAt(selectedIndex) + "."
                );
            }
        });

        return tabbedPane;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(GuiStyles.SURFACE_SECONDARY);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, GuiStyles.BORDER),
                new EmptyBorder(8, 20, 8, 20)
        ));

        JLabel productLabel = new JLabel(
                "Smart Waste Collection Network  ·  Municipal Operations Platform  ·  v1.0"
        );
        productLabel.setFont(GuiStyles.CAPTION_FONT);
        productLabel.setForeground(GuiStyles.TEXT_SECONDARY);

        JLabel signatureLabel = new JLabel("Developed by Veck The Dev");
        signatureLabel.setFont(GuiStyles.CAPTION_FONT);
        signatureLabel.setForeground(GuiStyles.TEXT_SECONDARY);

        footer.add(productLabel, BorderLayout.WEST);
        footer.add(signatureLabel, BorderLayout.EAST);

        return footer;
    }

    public ActivityLogPanel getActivityLogPanel() {
        return activityLogPanel;
    }

    public void setSmartBinConnected(boolean connected) {
        smartBinStatus.setConnected(connected);
        dashboardPanel.setSmartBinConnected(connected);
        smartBinPanel.setNetworkConnected(connected);
        liveOperationsPanel.setSmartBinHealth(
                connected ? StatusBadge.State.CONNECTED : StatusBadge.State.OFFLINE
        );
    }

    public void setCollectionConnected(boolean connected) {
        collectionStatus.setConnected(connected);
        dashboardPanel.setCollectionConnected(connected);
        collectionPanel.setNetworkConnected(connected);
        liveOperationsPanel.setCollectionHealth(
                connected ? StatusBadge.State.CONNECTED : StatusBadge.State.OFFLINE
        );
    }

    public void setRecyclingConnected(boolean connected) {
        recyclingStatus.setConnected(connected);
        dashboardPanel.setRecyclingConnected(connected);
        recyclingPanel.setNetworkConnected(connected);
        liveOperationsPanel.setRecyclingHealth(
                connected ? StatusBadge.State.CONNECTED : StatusBadge.State.OFFLINE
        );
    }
}
