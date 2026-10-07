package de.hugosmp.macro.gui;

import de.hugosmp.macro.HugoMacroClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HelpScreen extends Screen {

    private static final String[] LINES = {
            "Tasten (änderbar unter Optionen → Steuerung → Hugo Macro):",
            "  J = Sell-Macro an/aus, K = Spawner-Macro an/aus,",
            "  Rechts-Shift = diese Einstellungen öffnen",
            "",
            "Das Macro klickt nie blind: Es prüft Menü und Slots und",
            "pausiert bei einem unbekannten Zustand.",
            "",
            "Drop-Items: Angehakt = wird gedroppt.",
            "Hotbar auslassen: Hotbar-Slots werden nie verkauft.",
            "Eigene Zeiten: ersetzt die Tempo-Stufe durch einen eigenen Delay.",
            "",
            "Hinweis: Auf vielen Servern sind Makros verboten.",
            "Frag vorher die HugoSMP-Admins, ob es erlaubt ist."
    };

    private final Screen parent;

    public HelpScreen(Screen parent) {
        super(Component.literal("Help"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Zurück"), b -> onClose())
                .bounds(width / 2 - 50, height - 30, 100, 20).build());
    }

    @Override
    public void onClose() {
        HugoMacroClient.openScreen(minecraft, parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        int w = Math.min(width - 20, 380);
        int x = (width - w) / 2;
        g.fill(x, 20, x + w, height - 40, 0xE0181818);
        g.fill(x, 20, x + w, 22, 0xFFB050D0);
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        g.centeredText(font, title, width / 2, 8, 0xFFFFFFFF);
        int y = 30;
        for (String line : LINES) {
            g.text(font, Component.literal(line), x + 8, y, 0xFFDDDDDD, true);
            y += 12;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
