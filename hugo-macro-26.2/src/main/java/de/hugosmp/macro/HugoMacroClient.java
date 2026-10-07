package de.hugosmp.macro;

import com.mojang.blaze3d.platform.InputConstants;
import de.hugosmp.macro.config.MacroConfig;
import de.hugosmp.macro.gui.MacroSettingsScreen;
import de.hugosmp.macro.macro.MacroState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class HugoMacroClient implements ClientModInitializer {

    public static KeyMapping sellKey;
    public static KeyMapping spawnerKey;
    public static KeyMapping settingsKey;

    @Override
    public void onInitializeClient() {
        MacroConfig.load();

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("hugomacro", "main"));
        sellKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.hugomacro.sell", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, category));
        spawnerKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.hugomacro.spawner", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, category));
        settingsKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.hugomacro.settings", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (sellKey.consumeClick()) {
                MacroState.toggleSell(client);
            }
            while (spawnerKey.consumeClick()) {
                MacroState.toggleSpawner(client);
            }
            while (settingsKey.consumeClick()) {
                if (client.player != null) {
                    openScreen(client, new MacroSettingsScreen(null));
                }
            }
        });
    }

    /** Falls setScreen in 26.2 anders heisst, nur diese Zeile anpassen. */
    public static void openScreen(Minecraft mc, Screen screen) {
        mc.gui.setScreen(screen);
    }
}
