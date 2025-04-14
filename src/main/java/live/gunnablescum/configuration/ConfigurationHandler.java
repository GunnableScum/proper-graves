package live.gunnablescum.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import live.gunnablescum.ProperGraves;
import live.gunnablescum.configuration.enums.GlowingMode;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigurationHandler {
    private static Configuration config = Configuration.loadConfig(getConfigFile());

    public static void reloadConfig() {
        config = Configuration.loadConfig(getConfigFile());
    }

    public static GlowingMode getGlowingMode() {
        String glowMode = getString("glowmode");
        if(glowMode != null) return GlowingMode.valueOf(glowMode.toUpperCase());
        ProperGraves.LOGGER.error("Key not found, returning ENABLED...");
        return GlowingMode.ENABLED; // Default to ENABLED if key not found
    }

    public static String getString(String key) {
        for (ConfigurationObject<?> section : config.sections) {
            if (section.values.get(key) instanceof String) {
                return (String) section.values.get(key);
            }
        }
        ProperGraves.LOGGER.error("Key not found, returning NULL...");
        return null; // Default to NULL if key not found
    }

    // Suppressing unchecked cast warning because the instanceof check ensures the cast is safe
    @SuppressWarnings("unchecked")
    public static void setGlowingMode(GlowingMode value) {
        for (ConfigurationObject<?> section : config.sections) {
            if (section.values.get("glowmode") instanceof String) {
                ((ConfigurationObject<String>)section).values.put("glowmode", value.toString());
            }
        }
    }

    private static File getConfigFile() {
        return new File(FabricLoader.getInstance().getConfigDir().toFile(), "proper-graves.json");
    }

    public static void saveConfig() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            FileWriter writer = new FileWriter(getConfigFile());
            writer.write(gson.toJson(config));
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class Configuration {

    List<ConfigurationObject<?>> sections;

    public Configuration() {
        sections = new java.util.ArrayList<>();
    }

    private static void loadDefaults(Configuration configuration) {
        ConfigurationObject<String> enumSection = new ConfigurationObject<>();
        enumSection.values.put("glowmode", GlowingMode.OWNER_ONLY.toString());
        configuration.sections.add(enumSection);
    }

    private static void createConfig(Gson gson, File file) throws IOException {
        ProperGraves.LOGGER.info("Creating configuration file: {}", file.getAbsolutePath());
        Configuration configuration = new Configuration();
        file.createNewFile();
        FileWriter writer = new FileWriter(file);
        loadDefaults(configuration);
        writer.write(gson.toJson(configuration));
        writer.close();
    }

    public static Configuration loadConfig(File file) {
        ProperGraves.LOGGER.info("Loading configuration file: {}", file.getAbsolutePath());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            if(!file.exists()) createConfig(gson, file);
            FileReader reader = new FileReader(file);
            return gson.fromJson(reader, Configuration.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

class ConfigurationObject<T> {
    Map<String, T> values;

    public ConfigurationObject() {
        values = new HashMap<>();
    }

}