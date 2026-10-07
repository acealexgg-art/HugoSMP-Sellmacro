package de.hugosmp.macro.macro;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/** Laufender Zustand der Macros (live in der Statusleiste sichtbar). */
public final class MacroState {

    private static boolean sellActive;
    private static boolean spawnerActive;

    private MacroState() {
    }

    public static boolean sell() { return sellActive; }
    public static boolean spawner() { return spawnerActive; }

    public static void toggleSell(Minecraft mc) {
        sellActive = !sellActive;
        announce(mc, "Sell", sellActive);
    }

    public static void toggleSpawner(Minecraft mc) {
        spawnerActive = !spawnerActive;
        announce(mc, "Spawner", spawnerActive);
    }

    private static void announce(Minecraft mc, String name, boolean on) {
        if (mc.player == null) return;
        MutableComponent msg = Component.literal("[Macro] " + name + ": ").withStyle(ChatFormatting.GRAY);
        msg.append(Component.literal(on ? "AN" : "AUS").withStyle(on ? ChatFormatting.GREEN : ChatFormatting.RED));
        if (on) {
            msg.append(Component.literal(" (Klick-Logik folgt in Schritt 2)").withStyle(ChatFormatting.DARK_GRAY));
        }
        mc.player.sendSystemMessage(msg);
    }
}
