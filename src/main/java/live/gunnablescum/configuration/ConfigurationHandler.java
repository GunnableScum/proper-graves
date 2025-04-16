package live.gunnablescum.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import live.gunnablescum.ProperGraves;
import live.gunnablescum.configuration.configdatatypes.ArmorStandDesign;
import live.gunnablescum.configuration.configdatatypes.GlowingMode;
import live.gunnablescum.configuration.configdatatypes.PermissableAction;
import live.gunnablescum.configuration.configdatatypes.SerializedArmorStandDesign;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Suppressing unchecked cast warning because the type checks ensures the cast is safe
@SuppressWarnings("unchecked")
public class ConfigurationHandler {
    private static Configuration config = Configuration.loadConfig(getConfigFile());

    // Config Get-Methods
    public static GlowingMode getGlowingMode() {
        return getEnumValue("glowmode", GlowingMode.class, GlowingMode.ENABLED);
    }

    public static PermissableAction getGraveRobbingMode() {
        return getEnumValue("graverobbing", PermissableAction.class, PermissableAction.DENY);
    }

    public static SerializedArmorStandDesign getArmorStandDesign() {
        Gson gson = new GsonBuilder().create();
        for (ConfigurationObject<?> section : config.sections) {
            if(!Configuration.isArmorStandType(section)) continue;
            if(!section.values.containsKey("armorstand")) continue;
            return gson.fromJson((String) section.values.get("armorstand"), SerializedArmorStandDesign.class);
        }
        ProperGraves.LOGGER.error("Key not found, returning NULL...");
        return null; // Default to NULL if key not found
    }

    public static <T extends Enum<T>> T getEnumValue(String key, Class<T> enumtype, T fallback) {
        String value = getString(key);
        if(value != null) return Enum.valueOf(enumtype, value.toUpperCase());
        return fallback; // Default to fallback if key not found
    }

    public static String getString(String key) {
        for (ConfigurationObject<?> section : config.sections) {
            if(!Configuration.isStringType(section)) continue;
            if(!section.values.containsKey(key)) continue;
            return (String) section.values.get(key);
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

    public static <T extends Enum<T>> void setEnumValue(String key, T value) {
        for (ConfigurationObject<?> section : config.sections) {
            if(!Configuration.isStringType(section)) continue;
            // This cast being marked as redundant is a bug in IntelliJ, it's reported here: https://youtrack.jetbrains.com/issue/IDEA-370995
            ((ConfigurationObject<String>)section).values.put(key, value.toString());
            break;
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
    static Gson gson;

    public Configuration() {
        sections = new java.util.ArrayList<>();
        gson = new GsonBuilder().create();
    }

    private static void loadDefaults(Configuration configuration) {
        ConfigurationObject<String> enumSection = new ConfigurationObject<>();
        enumSection.values.put("glowmode", GlowingMode.OWNER_ONLY.toString());
        enumSection.values.put("graverobbing", PermissableAction.DENY.toString());
        ConfigurationObject<String> armorStandSection = new ConfigurationObject<>();
        armorStandSection.values.put("armorstand", gson.toJson(ArmorStandDesign.getDefault().serialize()));
        configuration.sections.add(enumSection);
    }

    private static void extendMissingDefaults(Configuration configuration) {
        boolean modified = false;
        for(ConfigurationObject<?> section : configuration.sections) {
            if(isStringType(section)) {
                if(setIfAbsent(section, "glowmode", GlowingMode.OWNER_ONLY)) modified = true;
                if(setIfAbsent(section, "graverobbing", PermissableAction.DENY)) modified = true;
                break;
            }
            if(isArmorStandType(section)) {
                if(setIfAbsent(section, "armorstand", gson.toJson(ArmorStandDesign.getDefault().serialize()))) modified = true;
                break;
            }
        }
        if(modified) {
            ConfigurationHandler.saveConfig(configuration);
        }
    }

   private static void createMissingSections(Configuration configuration) {
        boolean modified = false;

        // Check if a section for string-based configuration is missing
        if (configuration.sections.stream().noneMatch(Configuration::isStringType)) {
            ConfigurationObject<String> enumSection = new ConfigurationObject<>();
            enumSection.values.put("glowmode", GlowingMode.OWNER_ONLY.toString());
            enumSection.values.put("graverobbing", PermissableAction.DENY.toString());
            configuration.sections.add(enumSection);
            modified = true;
        }

        // Check if a section for armor stand design is missing
        if (configuration.sections.stream().noneMatch(Configuration::isArmorStandType)) {
            ConfigurationObject<String> armorStandSection = new ConfigurationObject<>();
            armorStandSection.values.put("armorstand", gson.toJson(ArmorStandDesign.getDefault().serialize()));
            configuration.sections.add(armorStandSection);
            modified = true;
        }

        // Save the configuration if any modifications were made
        if (modified) {
            ConfigurationHandler.saveConfig(configuration);
        }
    }

    public static boolean isStringType(ConfigurationObject<?> section) {
        return section.values.values().stream().allMatch(value -> value instanceof String);
    }

    public static boolean isArmorStandType(ConfigurationObject<?> section) {
        return section.values.containsKey("armorstand");
    }

    private static <T extends Enum<T>> boolean setIfAbsent(ConfigurationObject<?> section, String key, T value) {
        if(!section.values.containsKey(key)) {
            ((ConfigurationObject<String>)section).values.put(key, value.toString());
            return true;
        }
        return false;
    }

    private static boolean setIfAbsent(ConfigurationObject<?> section, String key, String value) {
        if(!section.values.containsKey(key)) {
            ((ConfigurationObject<String>)section).values.put(key, value);
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
            createMissingSections(configuration);
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