package globalquake.utils;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.tinylog.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class CSVReaderUtil {
    
    /**
     * 创建CSV读取器，自动检测编码（优先尝试GBK，然后UTF-8）
     */
    public static CSVReader createCSVReader(String resourcePath) throws IOException {
        InputStream inputStream = Objects.requireNonNull(
            ClassLoader.getSystemClassLoader().getResource(resourcePath)
        ).openStream();
        
        // 先尝试GBK编码（根据检测，城市CSV文件是GBK编码）
        try {
            return new CSVReaderBuilder(new InputStreamReader(inputStream, Charset.forName("GBK")))
                    .withSkipLines(1)
                    .build();
        } catch (Exception e) {
            // 如果GBK失败，回退到UTF-8
            Logger.warn("GBK编码读取失败，尝试UTF-8: " + resourcePath);
            inputStream.close();
            inputStream = Objects.requireNonNull(
                ClassLoader.getSystemClassLoader().getResource(resourcePath)
            ).openStream();
            return new CSVReaderBuilder(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .withSkipLines(1)
                    .build();
        }
    }
    
    /**
     * 创建CSV读取器，跳过指定行数
     */
    public static CSVReader createCSVReader(String resourcePath, int skipLines) throws IOException {
        InputStream inputStream = Objects.requireNonNull(
            ClassLoader.getSystemClassLoader().getResource(resourcePath)
        ).openStream();
        
        // 先尝试GBK编码
        try {
            return new CSVReaderBuilder(new InputStreamReader(inputStream, Charset.forName("GBK")))
                    .withSkipLines(skipLines)
                    .build();
        } catch (Exception e) {
            // 如果GBK失败，回退到UTF-8
            Logger.warn("GBK编码读取失败，尝试UTF-8: " + resourcePath);
            inputStream.close();
            inputStream = Objects.requireNonNull(
                ClassLoader.getSystemClassLoader().getResource(resourcePath)
            ).openStream();
            return new CSVReaderBuilder(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .withSkipLines(skipLines)
                    .build();
        }
    }
    
    /**
     * 测试文件编码
     */
    public static String detectEncoding(String resourcePath) {
        try (InputStream inputStream = Objects.requireNonNull(
                ClassLoader.getSystemClassLoader().getResource(resourcePath)
            ).openStream()) {
            
            // 读取前4个字节检查BOM
            byte[] bom = new byte[4];
            int bytesRead = inputStream.read(bom);
            
            if (bytesRead >= 3 && bom[0] == (byte)0xEF && bom[1] == (byte)0xBB && bom[2] == (byte)0xBF) {
                return "UTF-8 with BOM";
            } else if (bytesRead >= 2 && bom[0] == (byte)0xFE && bom[1] == (byte)0xFF) {
                return "UTF-16BE";
            } else if (bytesRead >= 2 && bom[0] == (byte)0xFF && bom[1] == (byte)0xFE) {
                return "UTF-16LE";
            }
            
            // 尝试用常见编码读取第一行
            String[] commonEncodings = {"GBK", "UTF-8", "ISO-8859-1", "Windows-1252"};
            
            for (String encoding : commonEncodings) {
                try {
                    inputStream.close();
                    inputStream = Objects.requireNonNull(
                        ClassLoader.getSystemClassLoader().getResource(resourcePath)
                    ).openStream();
                    
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, encoding));
                    String line = reader.readLine();
                    if (line != null && !line.trim().isEmpty()) {
                        // 简单检查是否包含中文字符特征
                        if (encoding.equals("GBK") || containsChinese(line)) {
                            return encoding;
                        }
                    }
                } catch (Exception e) {
                    // 继续尝试下一个编码
                }
            }
            
            return "Unknown";
        } catch (IOException e) {
            Logger.error("检测编码失败: " + resourcePath, e);
            return "Error";
        }
    }
    
    private static boolean containsChinese(String str) {
        // 简单检查是否包含中文字符（CJK统一表意文字范围）
        for (char c : str.toCharArray()) {
            if (c >= '\u4E00' && c <= '\u9FFF') {
                return true;
            }
        }
        return false;
    }
}
