package globalquake.ui.settings;

import globalquake.core.Settings;
import globalquake.core.earthquake.quality.QualityClass;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class GraphicsSettingsPanel extends SettingsPanel{

    private JCheckBox chkBoxScheme;
    private JSlider sliderFpsIdle;
    private JCheckBox chkBoxEnableTimeFilter;
    private JTextField textFieldTimeFilter;

    private JCheckBox chkBoxEnableMagnitudeFilter;
    private JTextField textFieldMagnitudeFilter;
    private JSlider sliderOpacity;
    private JComboBox<String> comboBoxDateFormat;
    private JCheckBox chkBox24H;
    private JCheckBox chkBoxDeadStations;
    private JSlider sliderIntensityZoom;
    private JTextField textFieldMaxArchived;
    private JSlider sliderStationsSize;
    private JRadioButton[] colorButtons;

    // Cinema mode
    private JTextField textFieldTime;
    private JSlider sliderZoomMul;

    private JCheckBox chkBoxEnableOnStartup;
    private JCheckBox chkBoxReEnable;
    private JCheckBox chkBoxDisplayMagnitudeHistogram;
    private JCheckBox chkBoxDisplaySystemInfo;
    private JCheckBox chkBoxDisplayQuakeAdditionalInfo;
    private JCheckBox chkBoxAlertBox;
    private JCheckBox chkBoxTime;
    private JCheckBox chkBoxShakemap;
    private JCheckBox chkBoxCityIntensities;
    private JCheckBox chkBoxCapitals;
    private JComboBox<QualityClass> comboBoxQuality;

    private JCheckBox chkBoxClusters;
    private JCheckBox chkBoxClusterRoots;
    private JCheckBox chkBoxHideClusters;
    private JCheckBox chkBoxAntialiasStations;
    private JCheckBox chkBoxAntialiasClusters;

    private JCheckBox chkBoxAntialiasOldQuakes;

    private JCheckBox chkBoxAntialiasQuakes;


    public GraphicsSettingsPanel() {
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("通用", createGeneralTab());
        tabbedPane.addTab("历史事件", createEventsTab());
        tabbedPane.addTab("测站", createStationsTab());
        tabbedPane.addTab("Cinema 自动聚焦", createCinemaModeTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private Component createCinemaModeTab() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(6,6,6,6));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        textFieldTime = new JTextField(String.valueOf(Settings.cinemaModeSwitchTime), 12);

        JPanel timePanel = new JPanel();
        timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.X_AXIS));
        timePanel.add(new JLabel("切换到下一个聚焦点的时间间隔(秒):"));
        timePanel.add(textFieldTime);
        panel.add(timePanel);

        JPanel zoomPanel = new JPanel();
        zoomPanel.setBorder(new EmptyBorder(5,5,5,5));

        zoomPanel.setLayout(new BoxLayout(zoomPanel, BoxLayout.X_AXIS));
        zoomPanel.add(new JLabel("缩放倍数(向右移动以放大):"));

        sliderZoomMul = new JSlider(JSlider.HORIZONTAL, 20,500, Settings.cinemaModeZoomMultiplier);
        sliderZoomMul.setMinorTickSpacing(10);
        sliderZoomMul.setMajorTickSpacing(50);
        sliderZoomMul.setPaintTicks(true);

        zoomPanel.add(sliderZoomMul);
        panel.add(zoomPanel);

        JPanel checkboxPanel = new JPanel();

        checkboxPanel.add(chkBoxEnableOnStartup = new JCheckBox("启动时开启 Cinema 自动聚焦", Settings.cinemaModeOnStartup));
        checkboxPanel.add(chkBoxReEnable = new JCheckBox("自动重新启用 Cinema 自动聚焦", Settings.cinemaModeReenable));
        panel.add(checkboxPanel);

        fill(panel, 32);

        return panel;
    }

    private Component createGeneralTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel performancePanel = new JPanel();
        performancePanel.setLayout(new BoxLayout(performancePanel, BoxLayout.Y_AXIS));
        performancePanel.setBorder(BorderFactory.createTitledBorder("性能"));

        sliderFpsIdle = new JSlider(JSlider.HORIZONTAL, 10, 200, Settings.fpsIdle);
        sliderFpsIdle.setPaintLabels(true);
        sliderFpsIdle.setPaintTicks(true);
        sliderFpsIdle.setMajorTickSpacing(10);
        sliderFpsIdle.setMinorTickSpacing(5);
        sliderFpsIdle.setBorder(new EmptyBorder(5,5,10,5));

        JLabel label = new JLabel("FPS 限制:"+sliderFpsIdle.getValue());

        sliderFpsIdle.addChangeListener(changeEvent -> label.setText("FPS 限制:"+sliderFpsIdle.getValue()));

        performancePanel.add(label);
        performancePanel.add(sliderFpsIdle);

        panel.add(performancePanel);

        JPanel dateFormatPanel = new JPanel();
        dateFormatPanel.setBorder(BorderFactory.createTitledBorder("日期和时间设置"));

        comboBoxDateFormat = new JComboBox<>();
        Instant now = Instant.now();
        for(DateTimeFormatter formatter: Settings.DATE_FORMATS){
            comboBoxDateFormat.addItem(formatter.format(now));
        }

        comboBoxDateFormat.setSelectedIndex(Settings.selectedDateFormatIndex);

        dateFormatPanel.add(new JLabel("首选日期格式:"));
        dateFormatPanel.add(comboBoxDateFormat);
        dateFormatPanel.add(chkBox24H = new JCheckBox("使用 24 小时制", Settings.use24HFormat));

        panel.add(dateFormatPanel);

        JPanel mainWindowPanel = new JPanel(new GridLayout(4,2));
        mainWindowPanel.setBorder(new TitledBorder("主屏幕"));

        mainWindowPanel.add(chkBoxDisplaySystemInfo = new JCheckBox("显示系统信息", Settings.displaySystemInfo));
        mainWindowPanel.add(chkBoxDisplayMagnitudeHistogram = new JCheckBox("显示震级直方图", Settings.displayMagnitudeHistogram));
        mainWindowPanel.add(chkBoxDisplayQuakeAdditionalInfo = new JCheckBox("显示地震技术数据", Settings.displayAdditionalQuakeInfo));
        mainWindowPanel.add(chkBoxAlertBox = new JCheckBox("显示附近地震警报框", Settings.displayAlertBox));
        mainWindowPanel.add(chkBoxShakemap = new JCheckBox("显示震度图六边形", Settings.displayShakemaps));
        mainWindowPanel.add(chkBoxTime = new JCheckBox("显示时间", Settings.displayTime));
        mainWindowPanel.add(chkBoxCityIntensities = new JCheckBox("显示城市估计震度", Settings.displayCityIntensities));
        mainWindowPanel.add(chkBoxCapitals = new JCheckBox("显示首都", Settings.displayCapitalCities));

        panel.add(mainWindowPanel);

        JPanel clustersPanel = new JPanel(new GridLayout(3,1));
        clustersPanel.setBorder(new TitledBorder("震群序列设置"));

        clustersPanel.add(chkBoxClusterRoots = new JCheckBox("显示震群序列(可能的震动位置)", Settings.displayClusterRoots));
        clustersPanel.add(chkBoxClusters = new JCheckBox("显示分配给震群序列的测站(仅限本地模式)", Settings.displayClusters));
        clustersPanel.add(chkBoxHideClusters = new JCheckBox("实际发现地震后隐藏震群序列", Settings.hideClustersWithQuake));

        panel.add(clustersPanel);

        JPanel antialiasPanel = new JPanel(new GridLayout(3,1));
        antialiasPanel.setBorder(new TitledBorder("抗锯齿"));
        antialiasPanel.add(chkBoxAntialiasStations = new JCheckBox("测站", Settings.antialiasing));
        antialiasPanel.add(chkBoxAntialiasClusters = new JCheckBox("集群", Settings.antialiasingClusters));
        antialiasPanel.add(chkBoxAntialiasQuakes = new JCheckBox("地震", Settings.antialiasingQuakes));
        antialiasPanel.add(chkBoxAntialiasOldQuakes = new JCheckBox("历史地震", Settings.antialiasingOldQuakes));

        panel.add(antialiasPanel);

        return panel;
    }

    private Component createEventsTab() {
        JPanel eventsPanel = new JPanel();
        eventsPanel.setBorder(BorderFactory.createTitledBorder("历史事件设置"));
        eventsPanel.setLayout(new BoxLayout(eventsPanel, BoxLayout.Y_AXIS));

        JPanel timePanel = new JPanel();
        timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.X_AXIS));
        timePanel.setBorder(new EmptyBorder(5,5,5,5));

        chkBoxEnableTimeFilter = new JCheckBox("不显示超过以下时间的事件(小时):");
        chkBoxEnableTimeFilter.setSelected(Settings.oldEventsTimeFilterEnabled);

        textFieldTimeFilter = new JTextField(Settings.oldEventsTimeFilter.toString(), 12);
        textFieldTimeFilter.setEnabled(chkBoxEnableTimeFilter.isSelected());

        chkBoxEnableTimeFilter.addChangeListener(changeEvent -> textFieldTimeFilter.setEnabled(chkBoxEnableTimeFilter.isSelected()));

        timePanel.add(chkBoxEnableTimeFilter);
        timePanel.add((textFieldTimeFilter));

        eventsPanel.add(timePanel);

        JPanel magnitudePanel = new JPanel();
        magnitudePanel.setBorder(new EmptyBorder(5,5,5,5));
        magnitudePanel.setLayout(new BoxLayout(magnitudePanel, BoxLayout.X_AXIS));

        chkBoxEnableMagnitudeFilter = new JCheckBox("不显示小于以下震级的事件:");
        chkBoxEnableMagnitudeFilter.setSelected(Settings.oldEventsMagnitudeFilterEnabled);

        textFieldMagnitudeFilter = new JTextField(Settings.oldEventsMagnitudeFilter.toString(), 12);
        textFieldMagnitudeFilter.setEnabled(chkBoxEnableMagnitudeFilter.isSelected());

        chkBoxEnableMagnitudeFilter.addChangeListener(changeEvent -> textFieldMagnitudeFilter.setEnabled(chkBoxEnableMagnitudeFilter.isSelected()));

        magnitudePanel.add(chkBoxEnableMagnitudeFilter);
        magnitudePanel.add((textFieldMagnitudeFilter));

        eventsPanel.add(magnitudePanel);

        JPanel removeOldPanel = new JPanel();
        removeOldPanel.setLayout(new BoxLayout(removeOldPanel, BoxLayout.X_AXIS));
        removeOldPanel.setBorder(new EmptyBorder(5,5,5,5));

        textFieldMaxArchived = new JTextField(Settings.maxArchivedQuakes.toString(), 12);

        removeOldPanel.add(new JLabel("历史地震最大总数:"));
        removeOldPanel.add(textFieldMaxArchived);

        eventsPanel.add(removeOldPanel);

        JPanel opacityPanel = new JPanel();
        opacityPanel.setBorder(new EmptyBorder(5,5,5,5));
        opacityPanel.setLayout(new BoxLayout(opacityPanel, BoxLayout.X_AXIS));

        sliderOpacity = new JSlider(JSlider.HORIZONTAL,0,100, Settings.oldEventsOpacity.intValue());
        sliderOpacity.setMajorTickSpacing(10);
        sliderOpacity.setMinorTickSpacing(2);
        sliderOpacity.setPaintTicks(true);
        sliderOpacity.setPaintLabels(true);
        sliderOpacity.setPaintTrack(true);

        sliderOpacity.addChangeListener(changeEvent -> {
            Settings.oldEventsOpacity = (double) sliderOpacity.getValue();
            Settings.changes++;
        });

        opacityPanel.add(new JLabel("历史事件透明度:"));
        opacityPanel.add(sliderOpacity);

        eventsPanel.add(opacityPanel);

        JPanel colorsPanel = new JPanel();
        colorsPanel.setBorder(BorderFactory.createTitledBorder("历史事件颜色"));

        JRadioButton buttonColorByAge = new JRadioButton("按时间着色");
        JRadioButton buttonColorByDepth = new JRadioButton("按深度着色");
        JRadioButton buttonColorByMagnitude = new JRadioButton("按震级着色");

        colorButtons = new JRadioButton[]{buttonColorByAge, buttonColorByDepth, buttonColorByMagnitude};
        ButtonGroup bg = new ButtonGroup();

        colorButtons[Math.max(0, Math.min(colorButtons.length - 1, Settings.selectedEventColorIndex))].setSelected(true);

        var colorButtonActionListener = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                for (int i = 0; i < colorButtons.length; i++) {
                    JRadioButton button = colorButtons[i];
                    if(button.isSelected()){
                        Settings.selectedEventColorIndex = i;
                        break;
                    }
                }
            }
        };

        for(JRadioButton button : colorButtons) {
            bg.add(button);
            button.addActionListener(colorButtonActionListener);
            colorsPanel.add(button);
        }

        eventsPanel.add(colorsPanel);

        JPanel qualityFilterPanel = new JPanel();
        qualityFilterPanel.setBorder(BorderFactory.createTitledBorder("质量"));

        qualityFilterPanel.add(new JLabel("仅显示质量等于或优于以下级别的历史事件:"));

        comboBoxQuality = new JComboBox<>(QualityClass.values());
        comboBoxQuality.setSelectedIndex(Math.max(0, Math.min(QualityClass.values().length-1, Settings.qualityFilter)));
        qualityFilterPanel.add(comboBoxQuality);

        eventsPanel.add(qualityFilterPanel);

        fill(eventsPanel, 12);

        return eventsPanel;
    }

    private Component createStationsTab() {
        JPanel stationsPanel = new JPanel();
        stationsPanel.setLayout(new BoxLayout(stationsPanel, BoxLayout.Y_AXIS));
        stationsPanel.setBorder(BorderFactory.createTitledBorder("测站"));

        JPanel checkBoxes = new JPanel(new GridLayout(1,2));
        checkBoxes.setBorder(BorderFactory.createTitledBorder("外观"));

        chkBoxScheme = new JCheckBox("使用旧配色方案(夸张效果)");
        chkBoxScheme.setSelected(Settings.useOldColorScheme);
        checkBoxes.add(chkBoxScheme);

        checkBoxes.add(chkBoxDeadStations = new JCheckBox("隐藏无数据的测站", Settings.hideDeadStations));

        stationsPanel.add(checkBoxes);

        JPanel stationsShapePanel = new JPanel();
        stationsShapePanel.setBorder(BorderFactory.createTitledBorder("形状"));

        ButtonGroup buttonGroup = new ButtonGroup();

        JRadioButton buttonCircles = new JRadioButton("圆形");
        JRadioButton buttonTriangles = new JRadioButton("三角形");
        JRadioButton buttonDepends = new JRadioButton("基于传感器类型");

        JRadioButton[] buttons = new JRadioButton[]{buttonCircles, buttonTriangles, buttonDepends};

        var shapeActionListener = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                for (int i = 0; i < buttons.length; i++) {
                    JRadioButton button = buttons[i];
                    if(button.isSelected()){
                        Settings.stationsShapeIndex = i;
                        break;
                    }
                }
            }
        };

        for(JRadioButton button : buttons){
            buttonGroup.add(button);
            stationsShapePanel.add(button);
            button.addActionListener(shapeActionListener);
        }

        buttons[Settings.stationsShapeIndex].setSelected(true);

        stationsPanel.add(stationsShapePanel);

        JPanel intensityPanel = new JPanel(new GridLayout(2,1));
        intensityPanel.add(new JLabel("在以下缩放级别显示测站的震度标签(0非常近,200非常远):"));

        sliderIntensityZoom = new JSlider(SwingConstants.HORIZONTAL, 0, 200, (int) (Settings.stationIntensityVisibilityZoomLevel * 100));
        sliderIntensityZoom.setMajorTickSpacing(10);
        sliderIntensityZoom.setMinorTickSpacing(5);
        sliderIntensityZoom.setPaintTicks(true);
        sliderIntensityZoom.setPaintLabels(true);

        sliderIntensityZoom.addChangeListener(changeEvent -> {
            Settings.stationIntensityVisibilityZoomLevel = sliderIntensityZoom.getValue() / 100.0;
            Settings.changes++;
        });

        intensityPanel.add(sliderIntensityZoom);
        stationsPanel.add(intensityPanel);

        JPanel stationSizePanel = new JPanel(new GridLayout(2,1));
        stationSizePanel.add(new JLabel("测站大小倍数(100默认,20极小,300极大):"));

        sliderStationsSize = new JSlider(SwingConstants.HORIZONTAL, 20, 300, (int) (Settings.stationsSizeMul * 100));
        sliderStationsSize.setMajorTickSpacing(20);
        sliderStationsSize.setMinorTickSpacing(10);
        sliderStationsSize.setPaintTicks(true);
        sliderStationsSize.setPaintLabels(true);

        sliderStationsSize.addChangeListener(changeEvent -> {
            Settings.stationsSizeMul = sliderStationsSize.getValue() / 100.0;
            Settings.changes++;
        });

        stationSizePanel.add(sliderStationsSize);
        stationsPanel.add(stationSizePanel);

        fill(stationsPanel, 6);

        return stationsPanel;
    }

    @Override
    public void save() {
        Settings.useOldColorScheme = chkBoxScheme.isSelected();
        Settings.fpsIdle = sliderFpsIdle.getValue();

        Settings.antialiasing = chkBoxAntialiasStations.isSelected();
        Settings.antialiasingClusters = chkBoxAntialiasClusters.isSelected();
        Settings.antialiasingQuakes = chkBoxAntialiasQuakes.isSelected();
        Settings.antialiasingOldQuakes = chkBoxAntialiasOldQuakes.isSelected();

        Settings.oldEventsTimeFilterEnabled = chkBoxEnableTimeFilter.isSelected();
        Settings.oldEventsTimeFilter = parseDouble(textFieldTimeFilter.getText(), "历史事件最大保留时间", 0, 24 * 365);

        Settings.oldEventsMagnitudeFilterEnabled = chkBoxEnableMagnitudeFilter.isSelected();
        Settings.oldEventsMagnitudeFilter = parseDouble(textFieldMagnitudeFilter.getText(), "历史事件最小震级", 0, 10);

        Settings.oldEventsOpacity = (double) sliderOpacity.getValue();
        Settings.selectedDateFormatIndex = comboBoxDateFormat.getSelectedIndex();
        Settings.use24HFormat = chkBox24H.isSelected();

        Settings.hideDeadStations = chkBoxDeadStations.isSelected();
        Settings.stationIntensityVisibilityZoomLevel = sliderIntensityZoom.getValue() / 100.0;
        Settings.stationsSizeMul = sliderStationsSize.getValue() / 100.0;

        Settings.maxArchivedQuakes = parseInt(textFieldMaxArchived.getText(), "历史地震最大数量", 1, Integer.MAX_VALUE);

        Settings.cinemaModeZoomMultiplier = sliderZoomMul.getValue();
        Settings.cinemaModeSwitchTime = parseInt(textFieldTime.getText(), "Cinema 自动聚焦切换时间", 1, 3600);
        Settings.cinemaModeOnStartup = chkBoxEnableOnStartup.isSelected();
        Settings.cinemaModeReenable = chkBoxReEnable.isSelected();

        Settings.displaySystemInfo = chkBoxDisplaySystemInfo.isSelected();
        Settings.displayMagnitudeHistogram = chkBoxDisplayMagnitudeHistogram.isSelected();
        Settings.displayAdditionalQuakeInfo = chkBoxDisplayQuakeAdditionalInfo.isSelected();
        Settings.displayAlertBox = chkBoxAlertBox.isSelected();
        Settings.displayShakemaps = chkBoxShakemap.isSelected();
        Settings.displayTime = chkBoxTime.isSelected();
        Settings.displayCityIntensities = chkBoxCityIntensities.isSelected();
        Settings.displayCapitalCities = chkBoxCapitals.isSelected();

        Settings.qualityFilter = comboBoxQuality.getSelectedIndex();

        Settings.displayClusters = chkBoxClusters.isSelected();
        Settings.displayClusterRoots = chkBoxClusterRoots.isSelected();
        Settings.hideClustersWithQuake = chkBoxHideClusters.isSelected();
    }

    @Override
    public String getTitle() {
        return "图形";
    }
}