package de.tmxx.abilities.util;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.gson.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Project: abilities
 * 11.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
@Singleton
public class BlockStateIDLoader {
    private static final String[] PARAMETERS = new String[] {
            "--output",
            "plugins/Abilities",
            "--reports"
    };

    private final JavaPlugin plugin;
    private final Map<Material, Integer> states = new HashMap<>();

    @Inject
    BlockStateIDLoader(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public int getBlockStateId(Material material) {
        return states.get(material);
    }

    public void loadBlockStateIDs() {
        File file = new File(plugin.getDataFolder(), "blocks.json");
        if (!file.exists()) {
            if (!generateReports()) return;
            compileReports();
        }

        loadReports();
    }

    private void loadReports() {
        File file = new File(plugin.getDataFolder(), "blocks.json");
        if (!file.exists()) return;

        long start = System.currentTimeMillis();
        try (FileReader reader = new FileReader(file)) {
            JsonElement rootElement = JsonParser.parseReader(reader);
            JsonObject rootObject = rootElement.getAsJsonObject();

            for (String key : rootObject.keySet()) {
                Material material = Material.valueOf(key);
                int id = rootObject.get(key).getAsInt();

                states.put(material, id);
            }

            plugin.getLogger().info("Took " + (System.currentTimeMillis() - start) + "ms to load block state ids");
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Error while reading block states", e);
        }
    }

    private boolean generateReports() {
        try {
            Class<?> dataMainClass = MinecraftReflection.getMinecraftClass("data.Main");
            Method mainMethod = dataMainClass.getMethod("main", String[].class);

            long start = System.currentTimeMillis();
            mainMethod.invoke(null, new Object[] { PARAMETERS });
            plugin.getLogger().info("Took " + (System.currentTimeMillis() - start) + "ms to generate block state ids");

            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Could not generate block state ids", e);
            return false;
        }
    }

    private void compileReports() {
        File reportsFile = new File(plugin.getDataFolder(), "reports/blocks.json");
        File compiledFile = new File(plugin.getDataFolder(), "blocks.json");
        if (!reportsFile.exists()) return;

        Map<String, Integer> map = new HashMap<>();
        long start = System.currentTimeMillis();

        try (FileReader reader = new FileReader(reportsFile); FileWriter writer = new FileWriter(compiledFile)) {
            JsonElement rootElement = JsonParser.parseReader(reader);
            JsonObject rootObject = rootElement.getAsJsonObject();

            for (String key : rootObject.keySet()) {
                JsonObject object = rootObject.getAsJsonObject(key);
                if (!object.has("states")) continue;

                JsonArray states = object.getAsJsonArray("states");
                for (JsonElement stateElement : states) {
                    JsonObject state = stateElement.getAsJsonObject();
                    if (!state.has("id") || !(state.has("default") && state.get("default").getAsBoolean())) continue;

                    map.put(key.substring(key.lastIndexOf(":") + 1).toUpperCase(), state.get("id").getAsInt());
                }
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(map, writer);

            plugin.getLogger().info("Took " + (System.currentTimeMillis() - start) + "ms to compile the generated block state ids");
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Error while compiling generated reports", e);
        }
    }
}
