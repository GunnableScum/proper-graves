package live.gunnablescum.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import live.gunnablescum.ProperGraves;
import live.gunnablescum.configuration.enums.GlowingMode;
import live.gunnablescum.configuration.enums.PermissableAction;
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

    // Config Get-Methods
    public static GlowingMode getGlowingMode() {
        return getEnumValue("glowmode", GlowingMode.class, GlowingMode.ENABLED);
    }

    public static PermissableAction getGraveRobbingMode() {
        return getEnumValue("graverobbing", PermissableAction.class, PermissableAction.DENY);
    }

    public static <T extends Enum<T>> T getEnumValue(String key, Class<T> enumtype, T fallback) {
        String value = getString(key);
        if(value != null) return Enum.valueOf(enumtype, value.toUpperCase());
        return fallback; // Default to fallback if key not found
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

    // Config Set-Methods
    public static void setGraveRobbingMode(PermissableAction value) {
        setEnumValue("graverobbing", value);
    }

    public static void setGlowingMode(GlowingMode value) {
        setEnumValue("glowmode", value);
    }

    // Suppressing unchecked cast warning because the instanceof check ensures the cast is safe
    @SuppressWarnings("unchecked")
    public static <T> void setEnumValue(String key, T value) {
        for (ConfigurationObject<?> section : config.sections) {
            if (section.values.get(key) instanceof String) {
                ((ConfigurationObject<String>)section).values.put(key, value.toString());
            }
        }
    }

    // Config Data-Methods
    public static void reloadConfig() {
        config = Configuration.loadConfig(getConfigFile());
    }


    private static File getConfigFile() {
        return new File(FabricLoader.getInstance().getConfigDir().toFile(), "proper-graves.json");
    }

    public static void saveConfig() {
        saveConfig(config);
    }
    public static void saveConfig(Configuration config) {
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
        enumSection.values.put("graverobbing", PermissableAction.DENY.toString());
        configuration.sections.add(enumSection);
    }

    private static void extendMissingDefaults(Configuration configuration) {
        boolean modified = false;
        for(ConfigurationObject<?> section : configuration.sections) {
            if(isStringType(section)) {
                if(setIfAbsent(section, "glowmode", GlowingMode.OWNER_ONLY)) modified = true;
                if(setIfAbsent(section, "graverobbing", PermissableAction.DENY)) modified = true;
                if(modified) {
                    ConfigurationHandler.saveConfig(configuration);
                }
                break;
            }
        }
    }

    private static boolean isStringType(ConfigurationObject<?> section) {
        return section.values.values().stream().allMatch(value -> value instanceof String);
    }

    private static <T extends Enum<T>> boolean setIfAbsent(ConfigurationObject<?> section, String key, T value) {
        if(!section.values.containsKey(key)) {
            ((ConfigurationObject<String>)section).values.put(key, value.toString());
            return true;
        }
        return false;
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
            Configuration configuration = gson.fromJson(reader, Configuration.class);
            reader.close();
            extendMissingDefaults(configuration);
            return configuration;
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