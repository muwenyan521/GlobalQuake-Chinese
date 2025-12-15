package globalquake.ui;

import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

public class FontManager {
    
    private static final String[] PREFERRED_FONTS = {
        "MiSans Normal",           // Windows MiSans
        "Microsoft YaHei",         // Windows 微软雅黑
        "SimHei",                  // Windows 黑体
        "Noto Sans CJK SC",        // Linux/Android 思源黑体
        "WenQuanYi Micro Hei",     // Linux 文泉驿微米黑
        "SansSerif"                // 最终回退
    };
    
    private static final Map<String, Font> fontCache = new HashMap<>();
    
    /**
     * 获取最佳字体
     * @param style 字体样式 (Font.PLAIN, Font.BOLD, Font.ITALIC)
     * @param size 字体大小
     * @return 可用的最佳字体
     */
    public static Font getBestFont(int style, float size) {
        String cacheKey = style + "_" + size;
        if (fontCache.containsKey(cacheKey)) {
            return fontCache.get(cacheKey);
        }
        
        for (String fontName : PREFERRED_FONTS) {
            Font font = new Font(fontName, style, 12); // 先用12号测试
            if (!font.getFamily().equalsIgnoreCase("dialog")) {
                // 字体可用
                Font result = font.deriveFont(style, size);
                fontCache.put(cacheKey, result);
                return result;
            }
        }
        
        // 所有首选字体都不可用，使用默认字体
        Font defaultFont = new Font("SansSerif", style, (int) size);
        fontCache.put(cacheKey, defaultFont);
        return defaultFont;
    }
    
    /**
     * 获取城市震度显示字体（16号，普通样式）
     */
    public static Font getCityIntensityFont() {
        return getBestFont(Font.PLAIN, 16f);
    }
    
    /**
     * 获取城市标签字体（14号，普通样式）
     */
    public static Font getCityLabelFont() {
        return getBestFont(Font.PLAIN, 14f);
    }
    
    /**
     * 获取默认字体
     */
    public static Font getDefaultFont(float size) {
        return getBestFont(Font.PLAIN, size);
    }
    
    /**
     * 获取粗体字体
     */
    public static Font getBoldFont(float size) {
        return getBestFont(Font.BOLD, size);
    }
    
    /**
     * 获取警报框字体（16号，普通样式）
     */
    public static Font getAlertBoxFont() {
        return getBestFont(Font.PLAIN, 16f);
    }
    
    /**
     * 获取警报框标题字体（22号，普通样式）
     */
    public static Font getAlertBoxTitleFont() {
        return getBestFont(Font.PLAIN, 22f);
    }
    
    /**
     * 清理字体缓存
     */
    public static void clearCache() {
        fontCache.clear();
    }
    
    /**
     * 获取当前系统可用的字体列表（用于调试）
     */
    public static String[] getAvailableFonts() {
        return java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
    }
    
    /**
     * 检查特定字体是否可用
     */
    public static boolean isFontAvailable(String fontName) {
        String[] availableFonts = getAvailableFonts();
        for (String availableFont : availableFonts) {
            if (availableFont.equalsIgnoreCase(fontName)) {
                return true;
            }
        }
        return false;
    }
}
