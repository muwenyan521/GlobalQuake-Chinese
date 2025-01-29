package globalquake.ui.client;

import globalquake.core.Settings;
import globalquake.core.database.StationDatabaseManager;
import globalquake.core.database.StationSource;
import globalquake.core.earthquake.GQHypocs;
import globalquake.core.exception.FatalIOException;
import globalquake.core.exception.RuntimeApplicationException;
import globalquake.core.geo.taup.TauPTravelTimeCalculator;
import globalquake.core.regions.Regions;
import globalquake.core.training.EarthquakeAnalysisTraining;
import globalquake.intensity.ShakeMap;
import globalquake.client.GlobalQuakeLocal;
import globalquake.main.Main;
import globalquake.playground.GlobalQuakePlayground;
import globalquake.sounds.Sounds;
import globalquake.ui.GQFrame;
import globalquake.ui.database.DatabaseMonitorFrame;
import globalquake.ui.settings.SettingsFrame;
import globalquake.utils.Scale;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class MainFrame extends GQFrame {

    private static StationDatabaseManager databaseManager;
    private static DatabaseMonitorFrame databaseMonitorFrame;
    private final JProgressBar progressBar;
    private JButton connectButton;
    private JButton hostButton;

    private static boolean loaded = false;
    private JButton playgroundButton;
    private JButton settingsButton;

    public MainFrame() {
        setTitle(Main.fullName);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(600, 460));
        setMinimumSize(new Dimension(600, 460));

        JPanel contentPane = new JPanel();
        setContentPane(contentPane);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

        contentPane.setLayout(new BorderLayout());

        contentPane.add(createMainPanel(), BorderLayout.CENTER);

        contentPane.add(progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100), BorderLayout.SOUTH);
        progressBar.setStringPainted(true);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        if (!loaded) {
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    initAll();
                    onLoad();
                    loaded = true;
                } catch (Exception e) {
                    Main.getErrorHandler().handleException(e);
                }
            });
        }
    }

    private static final double PHASES = 6.0;
    private static int phase = 0;

    private void initAll() throws Exception {
        getProgressBar().setString("加载区域...");
        getProgressBar().setValue((int) ((phase++ / PHASES) * 100.0));
        Regions.init();
        getProgressBar().setString("加载地图比例...");
        getProgressBar().setValue((int) ((phase++ / PHASES) * 100.0));
        Scale.load();
        getProgressBar().setString("加载震度图...");
        getProgressBar().setValue((int) ((phase++ / PHASES) * 100.0));
        ShakeMap.init();
        getProgressBar().setString("加载音频...");
        getProgressBar().setValue((int) ((phase++ / PHASES) * 100.0));
        try {
            //Sound may fail to load for a variety of reasons. If it does, this method disables sound.
            Sounds.load();
        } catch (Exception e) {
            RuntimeApplicationException error = new RuntimeApplicationException("加载音频文件失败,声音将会被关闭.", e);
            Main.getErrorHandler().handleWarning(error);
        }
        getProgressBar().setString("加载震波走时表...");
        getProgressBar().setValue((int) ((phase++ / PHASES) * 100.0));
        TauPTravelTimeCalculator.init();

        getProgressBar().setString("加载 CUDA 支持库...");
        getProgressBar().setValue((int) ((phase++ / PHASES) * 100.0));
        GQHypocs.load();

        getProgressBar().setString("完成");
        getProgressBar().setValue((int) ((phase++ / PHASES) * 100.0));
    }

    private JPanel createMainPanel() {
        var grid = new GridLayout(5, 1);
        grid.setVgap(10);
        JPanel panel = new JPanel(grid);
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JLabel titleLabel = new JLabel(Main.fullName, SwingConstants.CENTER);
        titleLabel.setFont(new Font("MiSans Normal", Font.BOLD, 32));
        panel.add(titleLabel);

        hostButton = new JButton("在本地环境下运行");
        hostButton.setEnabled(loaded);
        panel.add(hostButton);

        hostButton.addActionListener(actionEvent -> {
            try {
                this.dispose();
                startDatabaseManager();
            } catch (FatalIOException e) {
                Main.getErrorHandler().handleException(e);
            }
        });

        connectButton = new JButton("连接至服务器");
        connectButton.setEnabled(loaded);
        panel.add(connectButton);

        connectButton.addActionListener(actionEvent -> {
            this.dispose();
            new ServerSelectionFrame().setVisible(true);
        });

        playgroundButton = new JButton("游乐场模式 (Beta)");
        playgroundButton.setEnabled(loaded);
        panel.add(playgroundButton);

        playgroundButton.addActionListener(actionEvent -> {
            this.dispose();
            new GlobalQuakePlayground();
        });

        JPanel buttons2 = createButtons();

        panel.add(buttons2);

        return panel;
    }

    private JPanel createButtons() {
        GridLayout grid2 = new GridLayout(1, 2);
        grid2.setHgap(10);
        JPanel buttons2 = new JPanel(grid2);

        settingsButton = new JButton("设置");
        settingsButton.setEnabled(false);
        // Listener for settings panel button
        settingsButton.addActionListener(actionEvent -> {
            // Check if an instance of SettingsFrame already exists
            if (SettingsFrame.getInstance() == null) {
                // If not, create a new instance and make it visible
                SettingsFrame settingsFrame = new SettingsFrame(MainFrame.this, false);
                settingsFrame.setVisible(true);
                // Ensure that the SettingsFrame is always on top
                settingsFrame.setAlwaysOnTop(true);
            }
        });

        buttons2.add(settingsButton);

        JButton exitButton = new JButton("退出");
        exitButton.addActionListener(actionEvent -> System.exit(0));
        buttons2.add(exitButton);
        return buttons2;
    }

    private static void startDatabaseManager() throws FatalIOException {
        databaseManager = new StationDatabaseManager();
        databaseManager.load();
        databaseMonitorFrame = new DatabaseMonitorFrame(databaseManager, MainFrame::launchGlobalQuake);
        databaseMonitorFrame.setVisible(true);

        Executors.newSingleThreadExecutor().submit(MainFrame::finishInit);
    }

    public static void updateProgressBar(String status, int value) {
        databaseMonitorFrame.getMainProgressBar().setString(status);
        databaseMonitorFrame.getMainProgressBar().setValue(value);
    }

    private static void finishInit() {
        updateProgressBar("正在校准...", (int) ((phase++ / (PHASES + 4)) * 100.0));

        if (Settings.recalibrateOnLaunch) {
            EarthquakeAnalysisTraining.calibrateResolution(MainFrame::updateProgressBar, null, true);
            if(GQHypocs.isCudaLoaded()){
                EarthquakeAnalysisTraining.calibrateResolution(MainFrame::updateProgressBar, null, false);
            }
        }

        updateProgressBar("更新测站源...", (int) ((phase++ / (PHASES + 4)) * 100.0));
        databaseManager.runUpdate(
                databaseManager.getStationDatabase().getStationSources().stream()
                        .filter(StationSource::isOutdated).collect(Collectors.toList()),
                () -> {
                    updateProgressBar("检查 Seedlink 节点...", (int) ((phase++ / (PHASES + 3)) * 100.0));
                    databaseManager.runAvailabilityCheck(databaseManager.getStationDatabase().getSeedlinkNetworks(), () -> {
                        updateProgressBar("保存...", (int) ((phase++ / (PHASES + 4)) * 100.0));

                        try {
                            databaseManager.save();
                        } catch (FatalIOException e) {
                            Main.getErrorHandler().handleException(new RuntimeException(e));
                        }
                        databaseMonitorFrame.initDone();

                        updateProgressBar("完成", (int) ((phase++ / (PHASES + 4)) * 100.0));
                    });
                });
    }

    public static void launchGlobalQuake() {
        new GlobalQuakeLocal(databaseManager).createFrame().initStations().startRuntime();
    }

    public void onLoad() {
        hostButton.setEnabled(true);
        connectButton.setEnabled(true);
        playgroundButton.setEnabled(true);
        settingsButton.setEnabled(true);
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }
}
