package de.hugosmp.macro.config;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

/** Alle Einstellungen. Wird in config/hugomacro.properties gespeichert. */
public final class MacroConfig {

    // Sell-Macro
    public static boolean sellOpenGui = true;
    public static int tempoStage = 2;          // Index in TempoStage (2 = "3 Normal")
    public static boolean customTimes = false;
    public static int customDelayMs = 100;
    public static boolean shiftClick = true;
    public static boolean skipHotbar = false;
    public static boolean fullStacksOnly = true;
    public static boolean confirmRest = true;
    public static boolean sellFilter = false;
    public static final Set<String> sellItems = new LinkedHashSet<>();

    // Spawner-Macro
    public static int spawnerDelayMs = 10;
    public static boolean dropFilter = true;
    public static int mode = 0;                // Index in MacroMode
    public static final Set<String> dropChecked = new LinkedHashSet<>(Set.of("minecraft:bone"));

    private MacroConfig() {
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("hugomacro.properties");
    }

    public static void load() {
        Path f = file();
        if (!Files.exists(f)) return;
        try (InputStream in = Files.newInputStream(f)) {
            Properties p = new Properties();
            p.load(in);
            sellOpenGui = bool(p, "sellOpenGui", sellOpenGui);
            tempoStage = num(p, "tempoStage", tempoStage, 0, 4);
            customTimes = bool(p, "customTimes", customTimes);
            customDelayMs = num(p, "customDelayMs", customDelayMs, 20, 500);
            shiftClick = bool(p, "shiftClick", shiftClick);
            skipHotbar = bool(p, "skipHotbar", skipHotbar);
            fullStacksOnly = bool(p, "fullStacksOnly", fullStacksOnly);
            confirmRest = bool(p, "confirmRest", confirmRest);
            sellFilter = bool(p, "sellFilter", sellFilter);
            spawnerDelayMs = num(p, "spawnerDelayMs", spawnerDelayMs, 1, 200);
            dropFilter = bool(p, "dropFilter", dropFilter);
            mode = num(p, "mode", mode, 0, 3);
            readSet(p, "sellItems", sellItems);
            if (p.containsKey("dropChecked")) readSet(p, "dropChecked", dropChecked);
        } catch (IOException ignored) {
        }
    }

    public static void save() {
        Properties p = new Properties();
        p.setProperty("sellOpenGui", Boolean.toString(sellOpenGui));
        p.setProperty("tempoStage", Integer.toString(tempoStage));
        p.setProperty("customTimes", Boolean.toString(customTimes));
        p.setProperty("customDelayMs", Integer.toString(customDelayMs));
        p.setProperty("shiftClick", Boolean.toString(shiftClick));
        p.setProperty("skipHotbar", Boolean.toString(skipHotbar));
        p.setProperty("fullStacksOnly", Boolean.toString(fullStacksOnly));
        p.setProperty("confirmRest", Boolean.toString(confirmRest));
        p.setProperty("sellFilter", Boolean.toString(sellFilter));
        p.setProperty("spawnerDelayMs", Integer.toString(spawnerDelayMs));
        p.setProperty("dropFilter", Boolean.toString(dropFilter));
        p.setProperty("mode", Integer.toString(mode));
        p.setProperty("sellItems", String.join(",", sellItems));
        p.setProperty("dropChecked", String.join(",", dropChecked));
        try (OutputStream out = Files.newOutputStream(file())) {
            p.store(out, "Hugo Macro");
        } catch (IOException ignored) {
        }
    }

    private static boolean bool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return v == null ? def : Boolean.parseBoolean(v);
    }

    private static int num(Properties p, String key, int def, int min, int max) {
        try {
            int v = Integer.parseInt(p.getProperty(key, Integer.toString(def)));
            return Math.min(Math.max(v, min), max);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static void readSet(Properties p, String key, Set<String> target) {
        target.clear();
        String v = p.getProperty(key, "");
        Arrays.stream(v.split(",")).map(String::trim).filter(s -> !s.isEmpty()).forEach(target::add);
    }
}
