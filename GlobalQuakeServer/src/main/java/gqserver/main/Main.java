package gqserver.main;

import globalquake.core.GlobalQuake;
import globalquake.core.Settings;
import globalquake.core.database.StationDatabaseManager;
import globalquake.core.database.StationSource;
import globalquake.core.earthquake.GQHypocs;
import globalquake.core.exception.ApplicationErrorHandler;
import globalquake.core.exception.FatalIOException;
import globalquake.core.training.EarthquakeAnalysisTraining;
import globalquake.core.regions.Regions;
import globalquake.core.geo.taup.TauPTravelTimeCalculator;

import gqserver.bot.DiscordBot;
import gqserver.fdsnws_event.FdsnwsEventsHTTPServer;

import globalquake.utils.Scale;
import gqserver.server.GlobalQuakeServer;
import gqserver.ui.server.DatabaseMonitorFrame;
import org.apache.commons.cli.*;
import org.tinylog.Logger;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Main {

    public static final File MAIN_FOLDER = new File("./.GlobalQuakeServerData/");

    private static ApplicationErrorHandler errorHandler;
    public static final String fullName = "GlobalQuake服务器 " + GlobalQuake.version;
    private static DatabaseMonitorFrame databaseMonitorFrame;
    private static StationDatabaseManager databaseManager;
    private static boolean headless = true;

    private static void startDatabaseManager() throws FatalIOException {
        databaseManager = new StationDatabaseManager();
        databaseManager.load();

        new GlobalQuakeServer(databaseManager);

        if (!headless) {
            databaseMonitorFrame = new DatabaseMonitorFrame(databaseManager);
            databaseMonitorFrame.setVisible(true);
        }
    }

    public static boolean isHeadless() {
        return headless;
    }

    public static void main(String[] args) {
        initErrorHandler();
        GlobalQuake.prepare(Main.MAIN_FOLDER, Main.getErrorHandler());

        Options options = new Options();

        Option headlessOption = new Option("h", "headless", false, "无头模式运行");
        headlessOption.setRequired(false);
        options.addOption(headlessOption);

        Option maxClientsOption = new Option("c", "clients", true, "最大客户端数");
        maxClientsOption.setRequired(false);
        options.addOption(maxClientsOption);

        Option maxGpuMemOption = new Option("g", "gpu-max-mem", true, "最大GPU内存限制(以GB为单位)");
        maxGpuMemOption.setRequired(false);
        options.addOption(maxGpuMemOption);

        CommandLineParser parser = new org.apache.commons.cli.BasicParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            formatter.printHelp("gqserver", options);

            System.exit(1);
        }

        headless = cmd.hasOption(headlessOption.getOpt());

        if(cmd.hasOption(maxClientsOption.getOpt())) {
            try {
                int maxCli =  Integer.parseInt(cmd.getOptionValue(maxClientsOption.getOpt()));
                if(maxCli < 1){
                    throw new IllegalArgumentException("客户端最大数量必须至少为1.!");
                }
                Settings.maxClients = maxCli;
                Logger.info("客户端最大数量已设置为%d.".formatted(Settings.maxClients));
            } catch(IllegalArgumentException e){
                Logger.error(e);
                System.exit(1);
            }
        }

        if(cmd.hasOption(maxGpuMemOption.getOpt())) {
            try {
                double maxMem =  Double.parseDouble(cmd.getOptionValue(maxGpuMemOption.getOpt()));
                if(maxMem <= 0){
                    throw new IllegalArgumentException("无效的最大GPU内存值");
                }
                GQHypocs.MAX_GPU_MEM = maxMem;
                Logger.info("最大GPU内存分配将被限制在大约 %.2f GB.".formatted(maxMem));
            } catch(IllegalArgumentException e){
                Logger.error(e);
                System.exit(1);
            }
        }

        Logger.info("无头模式状态:%s".formatted(headless));

        try {
            startDatabaseManager();
        } catch (FatalIOException e) {
            getErrorHandler().handleException(e);
        }

        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                initAll();
            } catch (Exception e) {
                getErrorHandler().handleException(e);
            }
        });
    }

    public static void updateProgressBar(String status, int value) {
        if(headless){
            Logger.info("加载中... %d%%: %s".formatted(value, status));
        }else{
            databaseMonitorFrame.getMainProgressBar().setString(status);
            databaseMonitorFrame.getMainProgressBar().setValue(value);
        }
    }

    private static final double PHASES = 10.0;
    private static int phase = 0;

    public static void initAll() throws Exception{
        updateProgressBar("正在加载区域...", (int) ((phase++ / PHASES) * 100.0));
        Regions.init();

        updateProgressBar("正在加载比例尺...", (int) ((phase++ / PHASES) * 100.0));
        Scale.load();

        updateProgressBar("正在加载震度走时表...", (int) ((phase++ / PHASES) * 100.0));
        TauPTravelTimeCalculator.init();

        updateProgressBar("正在尝试加载CUDA库...", (int) ((phase++ / PHASES) * 100.0));
        GQHypocs.load();

        updateProgressBar("正在校准...", (int) ((phase++ / PHASES) * 100.0));
        if(Settings.recalibrateOnLaunch) {
            EarthquakeAnalysisTraining.calibrateResolution(Main::updateProgressBar, null, true);
            if(GQHypocs.isCudaLoaded()) {
                EarthquakeAnalysisTraining.calibrateResolution(Main::updateProgressBar, null, false);
            }
        }

        //start up the FDSNWS_Event Server, if enabled
        updateProgressBar("正在启动FDSNWS_Event服务器...", (int) ((phase++ / PHASES) * 100.0));
        if(Settings.autoStartFDSNWSEventServer){
            try {
                FdsnwsEventsHTTPServer.getInstance().startServer();
            }catch (Exception e){
                getErrorHandler().handleWarning(new RuntimeException("无法启动FDSNWS EVENT服务器! Check logs for more info.", e));
            }
        }

        updateProgressBar("正在启动Discord机器人...", (int) ((phase++ / PHASES) * 100.0));
        if(Settings.discordBotEnabled){
            DiscordBot.init();
        }

        updateProgressBar("正在更新台站数据源...", (int) ((phase++ / PHASES) * 100.0));
        databaseManager.runUpdate(
                databaseManager.getStationDatabase().getStationSources().stream()
                        .filter(StationSource::isOutdated).collect(Collectors.toList()),
                () -> {
                    updateProgressBar("正在检查Seedlink节点...", (int) ((phase++ / PHASES) * 100.0));
                    databaseManager.runAvailabilityCheck(databaseManager.getStationDatabase().getSeedlinkNetworks(), () -> {
                        updateProgressBar("保存中...", (int) ((phase++ / PHASES) * 100.0));

                        try {
                            databaseManager.save();
                        } catch (FatalIOException e) {
                            getErrorHandler().handleException(new RuntimeException(e));
                        }

                        if(!headless) {
                            databaseMonitorFrame.initDone();
                        }

                        updateProgressBar("完成", (int) ((phase++ / PHASES) * 100.0));

                        if(headless){
                            autoStartServer();
                        }
                    });
                });
    }

    private static void autoStartServer() {
        GlobalQuakeServer.instance.initStations();
        GlobalQuakeServer.instance.getServerSocket().run(Settings.lastServerIP, Settings.lastServerPORT);
        GlobalQuakeServer.instance.startRuntime();
    }

    public static ApplicationErrorHandler getErrorHandler() {
        if(errorHandler == null) {
            errorHandler = new ApplicationErrorHandler(null, headless);
        }
        return errorHandler;
    }

    public static void initErrorHandler() {
        Thread.setDefaultUncaughtExceptionHandler(getErrorHandler());
    }
}
