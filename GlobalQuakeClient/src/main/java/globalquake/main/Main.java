package globalquake.main;

import globalquake.core.GlobalQuake;
import globalquake.core.earthquake.GQHypocs;
import globalquake.core.exception.ApplicationErrorHandler;
import globalquake.core.exception.FatalIOException;
import globalquake.ui.client.MainFrame;
import org.apache.commons.cli.*;
import org.tinylog.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Objects;

public class Main {

    private static ApplicationErrorHandler errorHandler;
    public static final String fullName = "GlobalQuake " + GlobalQuake.version;
    public static final File MAIN_FOLDER = new File("./.GlobalQuakeData/");

    public static final Image LOGO = new ImageIcon(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("logo/logo.png"))).getImage();

    public static void main(String[] args) {
        initErrorHandler();
        initMainDirectory();
        GlobalQuake.prepare(MAIN_FOLDER, getErrorHandler());
        
        Options options = new Options();
        
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
            formatter.printHelp("globalquake", options);

            System.exit(1);
        }

        if(cmd.hasOption(maxGpuMemOption.getOpt())) {
            try {
                double maxMem =  Double.parseDouble(cmd.getOptionValue(maxGpuMemOption.getOpt()));
                if(maxMem <= 0){
                    throw new IllegalArgumentException("无效的最大GPU内存数量");
                }
                GQHypocs.MAX_GPU_MEM = maxMem;
                Logger.info("最大GPU内存分配将限制在大约 %.2f GB".formatted(maxMem));
            } catch(IllegalArgumentException e){
                Logger.error(e);
                System.exit(1);
            }
        }

        MainFrame mainFrame = new MainFrame();
        mainFrame.setVisible(true);
    }

    private static void initMainDirectory() {
        if (!MAIN_FOLDER.exists()) {
            if (!MAIN_FOLDER.mkdirs()) {
                getErrorHandler().handleException(new FatalIOException("无法创建主目录!", null));
            }
        }
        File VOLUME_FOLDER = new File(MAIN_FOLDER, "volume/");
        if (!VOLUME_FOLDER.exists()) {
            if (!VOLUME_FOLDER.mkdirs()) {
                getErrorHandler().handleException(new FatalIOException("无法创建卷目录!", null));
            }
        }
    }

    public static ApplicationErrorHandler getErrorHandler() {
        if(errorHandler == null) {
            errorHandler = new ApplicationErrorHandler(null, false);
        }
        return errorHandler;
    }

    public static void initErrorHandler() {
        Thread.setDefaultUncaughtExceptionHandler(getErrorHandler());
    }
}
