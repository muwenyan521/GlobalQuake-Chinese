package globalquake.utils;

import org.tinylog.Logger;
import java.awt.*;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 性能优化工具类
 * 提供硬件加速、渲染优化和性能监控功能
 */
public class PerformanceOptimizer {
    
    private static final boolean ENABLE_HARDWARE_ACCELERATION = true;
    private static final boolean ENABLE_RENDERING_HINTS = true;
    private static final boolean ENABLE_PERFORMANCE_MONITORING = true;
    
    private static long lastFPSUpdate = 0;
    private static int frameCount = 0;
    private static int currentFPS = 0;
    private static double cpuUsage = 0.0;
    private static double memoryUsage = 0.0;
    private static double gpuUsage = 0.0; // 注意：Java中GPU使用率监控有限
    private static long lastCPUTime = 0;
    private static long lastCPUUpdate = 0;
    private static long lastMemoryUpdate = 0;
    
    private static ScheduledExecutorService monitorExecutor;
    private static List<PerformanceMetric> performanceHistory = new ArrayList<>();
    private static final int HISTORY_SIZE = 100;
    
    /**
     * 性能指标记录
     */
    public static class PerformanceMetric {
        public final long timestamp;
        public final int fps;
        public final double cpu;
        public final double memory;
        
        public PerformanceMetric(long timestamp, int fps, double cpu, double memory) {
            this.timestamp = timestamp;
            this.fps = fps;
            this.cpu = cpu;
            this.memory = memory;
        }
    }
    
    /**
     * 启用硬件加速和渲染优化
     */
    public static void enableHardwareAcceleration() {
        if (!ENABLE_HARDWARE_ACCELERATION) {
            return;
        }
        
        try {
            // 启用OpenGL硬件加速（如果可用）
            System.setProperty("sun.java2d.opengl", "true");
            System.setProperty("sun.java2d.d3d", "true");
            System.setProperty("sun.java2d.noddraw", "false");
            
            // 启用Direct3D管道（Windows）
            System.setProperty("sun.java2d.ddforcevram", "true");
            
            // 启用纹理缓存
            System.setProperty("sun.java2d.accthreshold", "0");
            
            Logger.info("硬件加速已启用");
        } catch (Exception e) {
            Logger.error("启用硬件加速失败: " + e.getMessage());
        }
    }
    
    /**
     * 配置Graphics2D的渲染提示以获得最佳性能
     */
    public static void configureRenderingHints(Graphics2D g2d) {
        if (!ENABLE_RENDERING_HINTS) {
            return;
        }
        
        try {
            // 启用抗锯齿（仅在需要时）
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
            
            // 启用文本抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            // 优化渲染质量与速度的平衡
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                RenderingHints.VALUE_RENDER_QUALITY);
            
            // 启用插值（缩放时）
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            
            // 启用Alpha混合优化
            g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, 
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            
            // 启用颜色渲染优化
            g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, 
                RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            
            // 启用笔画规范化
            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, 
                RenderingHints.VALUE_STROKE_NORMALIZE);
            
            // 启用分数度量（更好的文本定位）
            g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, 
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        } catch (Exception e) {
            Logger.error("配置渲染提示失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前FPS
     */
    public static int getCurrentFPS() {
        return currentFPS;
    }
    
    /**
     * 更新FPS计数
     */
    public static void updateFPS() {
        if (!ENABLE_PERFORMANCE_MONITORING) {
            return;
        }
        
        frameCount++;
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastFPSUpdate >= 1000) {
            currentFPS = frameCount;
            frameCount = 0;
            lastFPSUpdate = currentTime;
            
            // 更新内存使用率
            updateMemoryUsage();
            
            // 记录性能指标
            recordPerformanceMetric(currentTime, currentFPS, cpuUsage, memoryUsage);
        }
    }
    
    /**
     * 更新内存使用率
     */
    private static void updateMemoryUsage() {
        if (!ENABLE_PERFORMANCE_MONITORING) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMemoryUpdate >= 1000) {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            long maxMemory = runtime.maxMemory();
            
            memoryUsage = (double) usedMemory / maxMemory * 100.0;
            lastMemoryUpdate = currentTime;
        }
    }
    
    /**
     * 记录性能指标
     */
    private static void recordPerformanceMetric(long timestamp, int fps, double cpu, double memory) {
        performanceHistory.add(new PerformanceMetric(timestamp, fps, cpu, memory));
        
        // 保持历史记录大小
        if (performanceHistory.size() > HISTORY_SIZE) {
            performanceHistory.remove(0);
        }
    }
    
    /**
     * 获取性能历史记录
     */
    public static List<PerformanceMetric> getPerformanceHistory() {
        return new ArrayList<>(performanceHistory);
    }
    
    /**
     * 获取平均FPS
     */
    public static double getAverageFPS() {
        if (performanceHistory.isEmpty()) {
            return 0;
        }
        
        double sum = 0;
        for (PerformanceMetric metric : performanceHistory) {
            sum += metric.fps;
        }
        return sum / performanceHistory.size();
    }
    
    /**
     * 获取平均CPU使用率
     */
    public static double getAverageCPUUsage() {
        if (performanceHistory.isEmpty()) {
            return 0;
        }
        
        double sum = 0;
        for (PerformanceMetric metric : performanceHistory) {
            sum += metric.cpu;
        }
        return sum / performanceHistory.size();
    }
    
    /**
     * 获取平均内存使用率
     */
    public static double getAverageMemoryUsage() {
        if (performanceHistory.isEmpty()) {
            return 0;
        }
        
        double sum = 0;
        for (PerformanceMetric metric : performanceHistory) {
            sum += metric.memory;
        }
        return sum / performanceHistory.size();
    }
    
    /**
     * 获取性能报告
     */
    public static String getPerformanceReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== 性能报告 ===\n");
        report.append(String.format("当前FPS: %d\n", currentFPS));
        report.append(String.format("平均FPS: %.1f\n", getAverageFPS()));
        report.append(String.format("当前CPU使用率: %.1f%%\n", cpuUsage));
        report.append(String.format("平均CPU使用率: %.1f%%\n", getAverageCPUUsage()));
        report.append(String.format("当前内存使用率: %.1f%%\n", memoryUsage));
        report.append(String.format("平均内存使用率: %.1f%%\n", getAverageMemoryUsage()));
        report.append(String.format("性能历史记录数: %d\n", performanceHistory.size()));
        
        // 添加操作系统信息
        report.append(String.format("操作系统: %s %s\n", 
            System.getProperty("os.name"), 
            System.getProperty("os.version")));
        report.append(String.format("Java版本: %s\n", 
            System.getProperty("java.version")));
        report.append(String.format("JVM内存: %dMB / %dMB\n",
            (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024),
            Runtime.getRuntime().maxMemory() / (1024 * 1024)));
        
        return report.toString();
    }
    /**
     * 更新CPU使用率统计
     */
    private static void updateCPUUsage() {
        if (!ENABLE_PERFORMANCE_MONITORING) {
            return;
        }
        
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
                double cpuLoad = sunOsBean.getProcessCpuLoad();
                if (cpuLoad >= 0) {
                    cpuUsage = cpuLoad * 100.0;
                } else {
                    // 使用备用方法估算CPU使用率
                    cpuUsage = estimateCPUUsage();
                }
            } else {
                // 使用备用方法估算CPU使用率
                cpuUsage = estimateCPUUsage();
            }
        } catch (Exception e) {
            // 在某些系统上可能不可用
            cpuUsage = estimateCPUUsage();
        }
    }
    
    /**
     * 估算CPU使用率（备用方法）
     */
    private static double estimateCPUUsage() {
        try {
            long currentTime = System.currentTimeMillis();
            long currentCPUTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
            
            if (lastCPUTime > 0 && lastCPUUpdate > 0) {
                long elapsedTime = currentTime - lastCPUUpdate;
                long elapsedCPUTime = currentCPUTime - lastCPUTime;
                
                if (elapsedTime > 0) {
                    // 转换为百分比（假设单核）
                    double usage = (elapsedCPUTime / 1_000_000.0) / elapsedTime * 100.0;
                    // 限制在0-100%之间
                    return Math.max(0, Math.min(100, usage));
                }
            }
            
            lastCPUTime = currentCPUTime;
            lastCPUUpdate = currentTime;
            return 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    /**
     * 启动性能监控
     */
    public static void startPerformanceMonitoring() {
        if (!ENABLE_PERFORMANCE_MONITORING) {
            return;
        }
        
        if (monitorExecutor != null && !monitorExecutor.isShutdown()) {
            return;
        }
        
        monitorExecutor = Executors.newSingleThreadScheduledExecutor();
        monitorExecutor.scheduleAtFixedRate(() -> {
            updateCPUUsage();
            updateMemoryUsage();
            
            // 记录性能指标（每10秒）
            long currentTime = System.currentTimeMillis();
            if (currentTime % 10000 < 100) {
                Logger.debug("性能指标 - FPS: " + currentFPS + 
                           ", CPU: " + String.format("%.1f", cpuUsage) + "%" +
                           ", 内存: " + String.format("%.1f", memoryUsage) + "%");
                
                // 每30秒记录详细报告
                if (currentTime % 30000 < 100) {
                    Logger.info(getPerformanceReport());
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
        
        Logger.info("性能监控已启动");
    }
    
    /**
     * 停止性能监控
     */
    public static void stopPerformanceMonitoring() {
        if (monitorExecutor != null) {
            monitorExecutor.shutdown();
            try {
                if (!monitorExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                    monitorExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                monitorExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            Logger.info("性能监控已停止");
        }
    }
    
    /**
     * 优化JVM内存和GC设置
     */
    public static void optimizeJVMSettings() {
        try {
            // 建议的JVM参数（通过系统属性设置）
            System.setProperty("java.awt.headless", "false");
            
            // 启用字符串去重（如果可用）
            try {
                System.setProperty("+UseStringDeduplication", "true");
            } catch (Exception e) {
                // 忽略，某些JVM可能不支持
            }
            
            // 设置堆内存使用建议
            long maxMemory = Runtime.getRuntime().maxMemory();
            long totalMemory = Runtime.getRuntime().totalMemory();
            
            Logger.info("JVM内存 - 最大: " + (maxMemory / (1024 * 1024)) + "MB, " +
                       "已分配: " + (totalMemory / (1024 * 1024)) + "MB");
            
        } catch (Exception e) {
            Logger.error("优化JVM设置失败: " + e.getMessage());
        }
    }
    
    /**
     * 检测操作系统类型
     */
    public static String detectOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return "windows";
        } else if (osName.contains("linux")) {
            return "linux";
        } else if (osName.contains("mac")) {
            return "mac";
        } else {
            return "unknown";
        }
    }
    
    /**
     * 应用操作系统特定的优化
     */
    public static void applyOSSpecificOptimizations() {
        String os = detectOS();
        
        switch (os) {
            case "linux":
                applyLinuxOptimizations();
                break;
            case "windows":
                applyWindowsOptimizations();
                break;
            case "mac":
                applyMacOptimizations();
                break;
            default:
                Logger.warn("未知操作系统: " + os);
                break;
        }
    }
    
    /**
     * Linux特定优化
     */
    private static void applyLinuxOptimizations() {
        try {
            // 启用更好的字体渲染
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
            
            // 使用GTK+外观（如果可用）
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            } catch (Exception e) {
                // 回退到系统外观
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            
            // 优化文件系统访问
            System.setProperty("sun.nio.fs.maxCachedBufferSize", "524288"); // 512KB
            
            // Linux特定性能优化
            System.setProperty("sun.java2d.opengl", "true");
            System.setProperty("sun.java2d.xrender", "true");
            
            // 启用更好的事件处理
            System.setProperty("sun.awt.noerasebackground", "true");
            
            // 优化Linux上的AWT事件队列
            System.setProperty("sun.awt.event.queue.length", "10000");
            
            Logger.info("已应用Linux特定优化");
        } catch (Exception e) {
            Logger.error("应用Linux优化失败: " + e.getMessage());
        }
    }
    
    /**
     * Windows特定优化
     */
    private static void applyWindowsOptimizations() {
        try {
            // 启用Direct2D（Windows 7+）
            System.setProperty("sun.java2d.ddoffscreen", "true");
            
            // 使用Windows外观
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            
            Logger.info("已应用Windows特定优化");
        } catch (Exception e) {
            Logger.error("应用Windows优化失败: " + e.getMessage());
        }
    }
    
    /**
     * Mac特定优化
     */
    private static void applyMacOptimizations() {
        try {
            // 启用Quartz渲染管道
            System.setProperty("apple.awt.graphics.UseQuartz", "true");
            
            // 使用Mac外观
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            
            Logger.info("已应用Mac特定优化");
        } catch (Exception e) {
            Logger.error("应用Mac优化失败: " + e.getMessage());
        }
    }
    
    /**
     * 初始化所有性能优化
     */
    public static void initialize() {
        Logger.info("初始化性能优化...");
        
        // 记录初始系统信息
        Logger.info("系统信息 - OS: " + System.getProperty("os.name") + 
                   " " + System.getProperty("os.version") + 
                   ", Java: " + System.getProperty("java.version") +
                   ", 架构: " + System.getProperty("os.arch"));
        
        // 应用操作系统特定优化
        applyOSSpecificOptimizations();
        
        // 启用硬件加速
        enableHardwareAcceleration();
        
        // 优化JVM设置
        optimizeJVMSettings();
        
        // 启动性能监控
        startPerformanceMonitoring();
        
        // 初始性能报告
        Logger.info(getPerformanceReport());
        
        Logger.info("性能优化初始化完成");
    }
    
    /**
     * 检查系统是否支持硬件加速
     */
    public static boolean isHardwareAccelerationSupported() {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            
            // 检查是否支持双缓冲
            boolean doubleBuffered = gc.getBufferCapabilities().isPageFlipping();
            
            // 检查是否支持加速
            boolean accelerated = gc.getImageCapabilities().isAccelerated();
            
            Logger.info("硬件加速检查 - 双缓冲: " + doubleBuffered + ", 加速: " + accelerated);
            
            return doubleBuffered && accelerated;
        } catch (Exception e) {
            Logger.error("检查硬件加速失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 优化渲染循环
     */
    public static void optimizeRenderLoop(Component component) {
        if (component == null) {
            return;
        }
        
        try {
            // 启用双缓冲（如果组件支持）
            if (component instanceof JComponent) {
                ((JComponent) component).setDoubleBuffered(true);
            }
            
            // 设置渲染提示
            component.setIgnoreRepaint(false);
            
            Logger.debug("已优化渲染循环: " + component.getClass().getSimpleName());
        } catch (Exception e) {
            Logger.error("优化渲染循环失败: " + e.getMessage());
        }
    }
    
    /**
     * 建议的渲染优化设置
     */
    public static void applyRenderingOptimizations(Graphics2D g2d) {
        if (g2d == null) {
            return;
        }
        
        // 根据性能需求调整渲染质量
        if (currentFPS < 30) {
            // 低FPS时降低渲染质量以提高性能
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_OFF);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                RenderingHints.VALUE_RENDER_SPEED);
        } else {
            // 高FPS时使用高质量渲染
            configureRenderingHints(g2d);
        }
    }
    
    /**
     * 清理资源
     */
    public static void cleanup() {
        stopPerformanceMonitoring();
        Logger.info("性能优化资源已清理");
    }
}
