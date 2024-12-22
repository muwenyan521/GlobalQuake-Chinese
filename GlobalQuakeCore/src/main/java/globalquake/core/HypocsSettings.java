package globalquake.core;

import org.tinylog.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class HypocsSettings {

    private static final Map<String, Float> hypocsSettings = new HashMap<>();

    private static final File file = new File(GlobalQuake.mainFolder, "hypocs.properties");

    static{
        try {
            load();
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    public static void load() throws IOException {
        if(!file.exists()) {
            return;
        }
        Properties properties = new Properties();
        properties.load(new FileInputStream(file));
        for(var kv : properties.entrySet()){
            hypocsSettings.put((String) kv.getKey(), Float.valueOf((String) kv.getValue()));
        }
    }

    public static void save() throws IOException {
        Properties properties = new Properties();
        for(var kv : hypocsSettings.entrySet()){
            properties.setProperty(kv.getKey(), String.valueOf(kv.getValue()));
        }

        properties.store(new FileOutputStream(file), "如果你不知道这些是什么意思,那就不要碰它");
    }

    private static Float get(String key, float def){
        if(!hypocsSettings.containsKey(key)){
            hypocsSettings.put(key, def);
        }

        return hypocsSettings.getOrDefault(key, def);
    }

    public static float getOrDefault(String key, float def){
        return get(key, def);
    }

    public static int getOrDefaultInt(String key, int def){
        return Math.round(get(key, def));
    }

}
