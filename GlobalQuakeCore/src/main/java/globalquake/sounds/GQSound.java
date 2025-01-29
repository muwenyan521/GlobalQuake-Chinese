package globalquake.sounds;

import globalquake.core.exception.FatalIOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import static globalquake.sounds.Sounds.EXPORT_DIR;

public class GQSound {

    private final String filename;
    private final String description;

    private Clip clip;

    public double volume;

    public static final Map<String, String> descriptions = new HashMap<>();

    static{
        descriptions.put("level_0.wav",
                "当地震序列被创建时播放的声音." +
                        "\n这种情况发生在4个或更多测站在邻近区域检测到震动时.");

        descriptions.put("level_1.wav",
                "当地震序列达到1级时播放的声音." +
                        "\n这种情况发生在至少7个测站观测值达到64或至少4个测站观测值达到1,000时.");

        descriptions.put("level_2.wav",
                "当地震序列达到2级时播放的声音." +
                        "\n这种情况发生在至少7个测站观测值达到1,000或至少3个测站观测值达到10,000时.");

        descriptions.put("level_3.wav",
                "当地震序列达到3级时播放的声音." +
                        "\n这种情况发生在至少5个测站观测值达到10,000或至少3个测站达到50,000时.");

        descriptions.put("level_4.wav",
                """
                        当地震序列达到4级时播放的声音.\s
                        这种情况发生在至少4个测站观测值达到50,000时.\s
                        默认情况下此音频文件为空白,因为尚未添加此警报声音!""");

        descriptions.put("intensify.wav", "当满足前一个警报设置选项卡中指定的条件时播放的声音.");
        descriptions.put("felt.wav", "当预计用户位置会感受到震动时播放的声音." +
                "\n可以在警报选项卡中配置阈值强度等级和级别.");
        descriptions.put("felt_strong.wav", """
                当预计用户位置会感受到强烈震动时播放的声音.
                可以在警报选项卡中配置阈值强度等级和级别.
                默认情况下此音频文件为空白,因为尚未添加此警报声音!""");

        descriptions.put("eew_warning.wav", """
                当对检测到的地震有高度确定性,并且\s
                在陆地上估计强度至少为MMI VI级时播放的声音.
                默认情况下此音频文件为空白,因为尚未添加此警报声音!""");

        descriptions.put("countdown.wav", "如果预计用户位置会感受到震动," +
                "则在S波到达前的最后10秒进行倒计时播放的声音.");

        descriptions.put("update.wav", "发出地震修正报时播放的声音.");
        descriptions.put("found.wav", "首次监测到地震震中并在地图上显示时播放的声音.");
    }

    public GQSound(String filename){
        this(filename, descriptions.getOrDefault(filename, "[未提供描述]"));
    }

    public GQSound(String filename, String description) {
        this.filename = filename;
        this.description = description;
        this.volume = 1.0;
    }

    public void load(boolean externalOnly) throws FatalIOException {
        try {
            // 尝试从导出文件夹加载
            Path soundPath = Paths.get(EXPORT_DIR.getAbsolutePath(), filename);
            InputStream audioInStream = Files.exists(soundPath) || externalOnly ?
                    new FileInputStream(soundPath.toFile()) :
                    ClassLoader.getSystemClassLoader().getResourceAsStream("sounds/" + filename);

            if (audioInStream == null) {
                throw new IOException("未找到声音文件:%s(来自文件 = %s)".formatted(filename,  Files.exists(soundPath)));
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(
                    new BufferedInputStream(audioInStream));
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            this.clip = clip;
        } catch(Exception e) {
            throw new FatalIOException("加载声音失败:" + filename, e);
        }
    }

    public void export(Path exportPath) throws IOException{
        Path exportedFilePath = exportPath.resolve(filename);
        if (!Files.exists(exportedFilePath)) { // 检查文件是否已存在
            InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("sounds/" + filename);
            if (is != null) {
                Files.copy(is, exportedFilePath, StandardCopyOption.REPLACE_EXISTING);
                is.close();
            }
        }
    }

    @SuppressWarnings("unused")
    public String getDescription() {
        return description;
    }

    public String getFilename() {
        return filename;
    }

    public Clip getClip() {
        return clip;
    }
}