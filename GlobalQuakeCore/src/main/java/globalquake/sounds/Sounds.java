package globalquake.sounds;

import globalquake.core.GlobalQuake;
import globalquake.core.Settings;
import globalquake.core.exception.FatalIOException;
import globalquake.core.exception.RuntimeApplicationException;
import org.tinylog.Logger;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.*;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Sounds {

	public static final File EXPORT_DIR = new File(GlobalQuake.mainFolder, "sounds/");

	public static final File VOLUMES_FILE = new File(EXPORT_DIR, "soundVolumes.properties");
	public static final GQSound level_0 = new GQSound("level_0.wav");
	public static final GQSound level_1 = new GQSound("level_1.wav");
	public static final GQSound level_2 = new GQSound("level_2.wav");
	public static final GQSound level_3 = new GQSound("level_3.wav");
	public static final GQSound level_4 = new GQSound("level_4.wav");
	public static final GQSound intensify = new GQSound("intensify.wav");
	public static final GQSound felt = new GQSound("felt.wav");
	public static final GQSound eew_warning = new GQSound("eew_warning.wav");
	public static final GQSound felt_strong = new GQSound("felt_strong.wav");
	public static final GQSound countdown = new GQSound("countdown.wav");
	public static final GQSound countdown2 = new GQSound("countdown.wav");
	public static final GQSound update = new GQSound("update.wav");
	public static final GQSound found = new GQSound("found.wav");

	public static final GQSound[] ALL_SOUNDS = {
			found,
			update,
			level_0,
			level_1,
			level_2,
			level_3,
			level_4,
			intensify,
			felt,
			felt_strong,
			eew_warning,
			countdown,
			countdown2, // workaround
	};

	public static final GQSound[] ALL_ACTUAL_SOUNDS = {
			found,
			update,
			level_0,
			level_1,
			level_2,
			level_3,
			level_4,
			intensify,
			felt,
			felt_strong,
			eew_warning,
			countdown,
	};

	public static boolean soundsAvailable = true;

	private static final ExecutorService soundService = Executors.newCachedThreadPool();

	public static void exportSounds() throws IOException {
		Path exportPath = Paths.get(EXPORT_DIR.getAbsolutePath());
		if (!Files.exists(exportPath)) {
			Files.createDirectory(exportPath);
			writeReadmeFile(exportPath);
		}

		try {
			for (GQSound gqSound : ALL_ACTUAL_SOUNDS) {
				gqSound.export(exportPath);
			}
		} catch(IOException e){
			GlobalQuake.getErrorHandler().handleWarning(new RuntimeApplicationException("无法将声音导出到 %s!".formatted(exportPath.toString())));
		}
	}


	public static void loadSounds() {
		try {
			for (GQSound gqSound : ALL_SOUNDS) {
				gqSound.load(false);
			}
			soundsAvailable = true;
		} catch(FatalIOException e){
			soundsAvailable = false;
			if(GlobalQuake.errorHandler != null) {
				GlobalQuake.errorHandler.handleWarning(e);
			}else{
				Logger.error(e);
			}
		}
	}

	private static void writeReadmeFile(Path exportPath) throws IOException {
		String readmeContent = """
                README

				这个目录包含了从GlobalQuake导出的声音文件.
				你可以根据个人喜好编辑这些声音文件.
				请注意,声音文件只会导出一次,这意味着你在这里所做的任何更改都将被保留并由GlobalQuake使用.
				上传声音后,请重新启动GlobalQuake.

				享受自定义声音体验的乐趣!""";

		Files.writeString(exportPath.resolve("README.txt"), readmeContent, StandardOpenOption.CREATE);
	}

	public static void load() throws Exception {
		exportSounds();
		loadSounds();
		loadVolumes();
	}

	public static void playSound(GQSound sound) {
		if(!Settings.enableSound || !soundsAvailable || sound == null || sound.getClip() == null) {
			return;
		}

		soundService.submit(() -> {
			try {
				playClipRuntime(sound);
			} catch(Exception e){
				Logger.error(e);
			}
		});
	}

	private static void loadVolumes() {
		if(!VOLUMES_FILE.exists()){
			Logger.info("声音音量文件不存在,中止操作!");
			return;
		}
		Properties properties = new Properties();

		try (FileInputStream inputStream = new FileInputStream(VOLUMES_FILE)) {
			// Load properties from the file
			properties.load(inputStream);

			for (GQSound sound : ALL_ACTUAL_SOUNDS) {
				// Retrieve the volume from the properties file using the filename as the key
				String volumeString = properties.getProperty(sound.getFilename());
				if (volumeString != null) {
					// Parse the volume as a double and set it in the GQSound instance
					sound.volume = Math.max(0.0, Math.min(1.0, Double.parseDouble(volumeString)));
				}
			}
		} catch (IOException | NumberFormatException e) {
			Logger.error(new RuntimeApplicationException("无法加载声音音量!", e));
		}

		countdown2.volume = countdown.volume; // workaround
	}
	public static void storeVolumes() {
		Properties properties = new Properties();

		for (GQSound sound : ALL_ACTUAL_SOUNDS) {
			// Use the filename as the key and the volume as the value
			properties.setProperty(sound.getFilename(), String.valueOf(sound.volume));
		}

		try (FileOutputStream outputStream = new FileOutputStream(VOLUMES_FILE)) {
			// Store the properties in the file
			properties.store(outputStream, "Sound Volumes");
		} catch (IOException e) {
			Logger.error(new RuntimeApplicationException("无法存储声音音量!", e));
		}
	}

	private static void playClipRuntime(GQSound sound) {
		Clip clip = sound.getClip();
		clip.stop();
		clip.flush();
		clip.setFramePosition(0);

		double volume = Math.max(0.0, Math.min(1.0, sound.volume * (Settings.globalVolume / 100.0)));
		FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		gainControl.setValue(20f * (float) Math.log10(volume));

		clip.start();

        try {
            Thread.sleep(clip.getMicrosecondLength() / 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
	}

	public static void main(String[] args) throws Exception {
		GlobalQuake.prepare(new File("."), null);
		load();

		playSound(level_2);

		Thread.sleep(3000);

		System.exit(0);
	}

}
