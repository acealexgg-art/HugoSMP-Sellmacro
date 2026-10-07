package de.hugosmp.macro.gui;

import de.hugosmp.macro.HugoMacroClient;
import de.hugosmp.macro.config.MacroConfig;
import de.hugosmp.macro.macro.DropCatalog;
import de.hugosmp.macro.macro.MacroMode;
import de.hugosmp.macro.macro.MacroState;
import de.hugosmp.macro.macro.TempoStage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/** Hauptmenue: links Sell-Macro, rechts Spawner-Macro + Drop-Items, unten Statusleiste. */
public class MacroSettingsScreen extends Screen {

    private static final int PANEL = 0xE0181818;
    private static final int ACCENT = 0xFFB050D0;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFFAAAAAA;
    private static final int DARK = 0xFF666666;
    private static final int YELLOW = 0xFFFFFF55;
    private static final int STEP = 18;

    private record Label(String text, int x, int y, int color) {
    }

    private record ListRow(DropCatalog.Group group, DropCatalog.Entry entry) {
    }

    private final Screen parent;
    private final List<Label> labels = new ArrayList<>();
    private final List<ListRow> listRows = new ArrayList<>();
    private final Map<String, Button> dropButtons = new LinkedHashMap<>();

    private Button tempoBtn, sellItemsBtn;
    private AbstractSliderButton customSlider;
    private int lx, rx, colW, panelTop, panelBottom, rowH, ctrlH;
    private int tempoInfoY, delayValueY, spBottom, dpTop, listTop, listBottom;
    private int scroll;
    private String hint = "";
    private long hintUntil;

    public MacroSettingsScreen(Screen parent) {
        super(Component.literal("Settings"));
        this.parent = parent;
    }

    // ---------- Hilfen ----------

    private int labelY(int y) {
        return y + (ctrlH - 8) / 2;
    }

    private static Component onOff(boolean v) {
        return Component.literal(v ? "AN" : "AUS").withStyle(v ? ChatFormatting.GREEN : ChatFormatting.RED);
    }

    private Component keyText(KeyMapping key) {
        return key.getTranslatedKeyMessage();
    }

    private void setHint(String text) {
        hint = text;
        hintUntil = System.currentTimeMillis() + 4000;
    }

    private Button addToggle(int right, int y, String label, int labelX, BooleanSupplier get, Consumer<Boolean> set) {
        labels.add(new Label(label, labelX, labelY(y), WHITE));
        Button b = Button.builder(onOff(get.getAsBoolean()), btn -> {
            boolean v = !get.getAsBoolean();
            set.accept(v);
            btn.setMessage(onOff(v));
        }).bounds(right - 44, y, 44, ctrlH).build();
        return addRenderableWidget(b);
    }

    private Button addKeyButton(int right, int y, String label, int labelX, KeyMapping key) {
        labels.add(new Label(label, labelX, labelY(y), WHITE));
        return addRenderableWidget(Button.builder(keyText(key),
                btn -> setHint("Taste ändern: Optionen → Steuerung → Hugo Macro"))
                .bounds(right - 60, y, 60, ctrlH).build());
    }

    private Component tempoText() {
        return Component.literal("<  " + TempoStage.byIndex(MacroConfig.tempoStage).label + "  >");
    }

    private Component modeText() {
        return Component.literal("<  " + MacroMode.byIndex(MacroConfig.mode).label + "  >");
    }

    private String tempoInfo() {
        if (MacroConfig.customTimes) {
            return "1 Stack / " + MacroConfig.customDelayMs + " ms = "
                    + Math.round(1000.0 / MacroConfig.customDelayMs) + " Klicks/s";
        }
        TempoStage t = TempoStage.byIndex(MacroConfig.tempoStage);
        return t.stacks + (t.stacks == 1 ? " Stack" : " Stacks") + " / " + t.delayMs + " ms = "
                + Math.round(t.clicksPerSecond()) + " Klicks/s";
    }

    // ---------- Aufbau ----------

    @Override
    protected void init() {
        labels.clear();
        listRows.clear();
        dropButtons.clear();

        int total = Math.min(width - 16, 580);
        int x0 = (width - total) / 2;
        int gap = 8;
        colW = (total - gap) / 2;
        lx = x0;
        rx = x0 + colW + gap;
        panelTop = 22;
        panelBottom = height - 34;
        rowH = Math.max(14, Math.min(22, (panelBottom - panelTop - 8) / 13));
        ctrlH = rowH - 3;

        buildLeft();
        buildRight();
        buildBottom();
        layoutList();
    }

    private void buildLeft() {
        int right = lx + colW - 6;
        int y = panelTop + 4;
        labels.add(new Label("Sell-Macro", lx + 6, labelY(y), YELLOW));
        y += rowH;

        addKeyButton(right, y, "Sell umschalten", lx + 6, HugoMacroClient.sellKey);
        y += rowH;

        addToggle(right, y, "GUI selbst oeffnen", lx + 6, () -> MacroConfig.sellOpenGui, v -> MacroConfig.sellOpenGui = v);
        y += rowH;

        labels.add(new Label("Tempo", lx + 6, labelY(y), WHITE));
        y += rowH;

        tempoBtn = addRenderableWidget(Button.builder(tempoText(), btn -> {
            MacroConfig.tempoStage = (MacroConfig.tempoStage + 1) % TempoStage.values().length;
            btn.setMessage(tempoText());
        }).bounds(lx + 6, y, colW - 12, ctrlH).build());
        customSlider = addRenderableWidget(new ValueSlider(lx + 6, y, colW - 12, ctrlH, 20, 500,
                () -> MacroConfig.customDelayMs, v -> MacroConfig.customDelayMs = v, "Eigene Zeit: "));
        y += rowH;

        tempoInfoY = y;
        y += rowH - 4;

        addToggle(right, y, "Eigene Zeiten", lx + 6, () -> MacroConfig.customTimes, v -> MacroConfig.customTimes = v);
        y += rowH;
        addToggle(right, y, "Shift-Klick", lx + 6, () -> MacroConfig.shiftClick, v -> MacroConfig.shiftClick = v);
        y += rowH;
        addToggle(right, y, "Hotbar auslassen", lx + 6, () -> MacroConfig.skipHotbar, v -> MacroConfig.skipHotbar = v);
        y += rowH;
        addToggle(right, y, "Nur volle Stacks", lx + 6, () -> MacroConfig.fullStacksOnly, v -> MacroConfig.fullStacksOnly = v);
        y += rowH;
        addToggle(right, y, "Rest bestaetigen", lx + 6, () -> MacroConfig.confirmRest, v -> MacroConfig.confirmRest = v);
        y += rowH;
        addToggle(right, y, "Sell-Filter", lx + 6, () -> MacroConfig.sellFilter, v -> MacroConfig.sellFilter = v);
        y += rowH;

        sellItemsBtn = addRenderableWidget(Button.builder(sellItemsText(),
                btn -> HugoMacroClient.openScreen(minecraft, new SellItemsScreen(this)))
                .bounds(lx + 6, y, colW - 12, ctrlH).build());
    }

    private Component sellItemsText() {
        return Component.literal("Verkaufs-Items (" + MacroConfig.sellItems.size() + ")");
    }

    private void buildRight() {
        int right = rx + colW - 6;
        int y = panelTop + 4;
        labels.add(new Label("Spawner-Macro", rx + 6, labelY(y), YELLOW));
        y += rowH;

        addKeyButton(right, y, "Spawner umschalten", rx + 6, HugoMacroClient.spawnerKey);
        y += rowH;

        labels.add(new Label("Klick-Tempo", rx + 6, labelY(y), WHITE));
        delayValueY = labelY(y);
        y += rowH;

        addRenderableWidget(new ValueSlider(rx + 6, y, colW - 12, ctrlH, 1, 200,
                () -> MacroConfig.spawnerDelayMs, v -> MacroConfig.spawnerDelayMs = v, ""));
        y += rowH;

        addToggle(right, y, "Drop-Filter", rx + 6, () -> MacroConfig.dropFilter, v -> MacroConfig.dropFilter = v);
        y += rowH;

        labels.add(new Label("Modus", rx + 6, labelY(y), WHITE));
        y += rowH;

        addRenderableWidget(Button.builder(modeText(), btn -> {
            MacroConfig.mode = (MacroConfig.mode + 1) % MacroMode.values().length;
            btn.setMessage(modeText());
        }).bounds(rx + 6, y, colW - 12, ctrlH).build());
        y += rowH;

        spBottom = y + 2;
        dpTop = spBottom + 4;

        labels.add(new Label("Drop-Items", rx + 6, dpTop + 5, YELLOW));
        labels.add(new Label("Angehakt = wird gedroppt", rx + 6, dpTop + 5 + 12, GRAY));
        listTop = dpTop + 5 + 26;
        listBottom = panelBottom - 4;

        addRenderableWidget(Button.builder(Component.literal("▲"), b -> scrollBy(-STEP * 2))
                .bounds(rx + colW - 6 - 32, dpTop + 3, 15, 14).build());
        addRenderableWidget(Button.builder(Component.literal("▼"), b -> scrollBy(STEP * 2))
                .bounds(rx + colW - 6 - 15, dpTop + 3, 15, 14).build());

        for (DropCatalog.Group group : DropCatalog.GROUPS) {
            listRows.add(new ListRow(group, null));
            for (DropCatalog.Entry entry : group.entries()) {
                listRows.add(new ListRow(group, entry));
                Button b = Button.builder(Component.empty(), btn -> toggleDrop(entry.key()))
                        .bounds(rx + 6, listTop, colW - 12, STEP - 2).build();
                dropButtons.put(entry.key(), b);
                addRenderableWidget(b);
            }
        }
    }

    private void buildBottom() {
        int by = height - 26;
        addRenderableWidget(Button.builder(Component.literal("Help"),
                b -> HugoMacroClient.openScreen(minecraft, new HelpScreen(this)))
                .bounds(lx, by, 70, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(rx + colW - 70, by, 70, 20).build());
    }

    private void toggleDrop(String key) {
        if (!MacroConfig.dropChecked.remove(key)) {
            MacroConfig.dropChecked.add(key);
        }
    }

    // ---------- Scrollen ----------

    private int maxScroll() {
        return Math.max(0, listRows.size() * STEP - (listBottom - listTop));
    }

    private void scrollBy(int delta) {
        scroll = Math.min(Math.max(scroll + delta, 0), maxScroll());
        layoutList();
    }

    private void layoutList() {
        scroll = Math.min(Math.max(scroll, 0), maxScroll());
        int y = listTop - scroll;
        for (ListRow row : listRows) {
            if (row.entry() != null) {
                Button b = dropButtons.get(row.entry().key());
                b.setY(y);
                b.visible = y >= listTop && y + STEP - 2 <= listBottom;
            }
            y += STEP;
        }
    }

    // bewusst ohne @Override: falls die Signatur in 26.2 anders ist, geht nur das Mausrad nicht (die Pfeil-Knoepfe schon)
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= rx && mouseX <= rx + colW && mouseY >= listTop && mouseY <= listBottom) {
            scrollBy((int) (-scrollY * STEP));
            return true;
        }
        return false;
    }

    // ---------- Update & Schliessen ----------

    @Override
    public void tick() {
        tempoBtn.visible = !MacroConfig.customTimes;
        customSlider.visible = MacroConfig.customTimes;
        sellItemsBtn.setMessage(sellItemsText());
    }

    @Override
    public void onClose() {
        MacroConfig.save();
        HugoMacroClient.openScreen(minecraft, parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ---------- Zeichnen ----------

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        // Panels zuerst, damit die Widgets (von super gezeichnet) darueber liegen
        g.fill(lx, panelTop, lx + colW, panelBottom, PANEL);
        g.fill(rx, panelTop, rx + colW, spBottom, PANEL);
        g.fill(rx, dpTop, rx + colW, panelBottom, PANEL);
        g.fill(lx, panelTop, lx + colW, panelTop + 2, ACCENT);
        g.fill(rx, panelTop, rx + colW, panelTop + 2, ACCENT);
        g.fill(rx, dpTop, rx + colW, dpTop + 2, ACCENT);
        g.fill(0, height - 32, width, height, 0xE0101010);

        super.extractRenderState(g, mouseX, mouseY, partialTick);

        g.centeredText(font, title, width / 2, 8, WHITE);

        for (Label l : labels) {
            g.text(font, Component.literal(l.text()), l.x(), l.y(), l.color(), true);
        }
        g.text(font, Component.literal(tempoInfo()), lx + 6, tempoInfoY, GRAY, true);

        String ms = MacroConfig.spawnerDelayMs + " ms";
        g.text(font, Component.literal(ms), rx + colW - 6 - font.width(ms), delayValueY, WHITE, true);

        drawList(g);
        drawStatusBar(g);
    }

    private void drawList(GuiGraphicsExtractor g) {
        int y = listTop - scroll;
        for (ListRow row : listRows) {
            boolean visible = y >= listTop && y + STEP - 2 <= listBottom;
            if (visible) {
                if (row.entry() == null) {
                    g.text(font, Component.literal(row.group().mob()), rx + 8, y + 5, YELLOW, true);
                } else {
                    DropCatalog.Entry e = row.entry();
                    boolean checked = MacroConfig.dropChecked.contains(e.key());
                    // Checkbox
                    g.fill(rx + 9, y + 2, rx + 21, y + 14, 0xFF000000);
                    g.fill(rx + 10, y + 3, rx + 20, y + 13, checked ? 0xFFB050D0 : 0xFF2A2A2A);
                    // Icon + Name + Rate
                    g.item(new ItemStack(e.item()), rx + 25, y - 1);
                    g.text(font, Component.literal(e.label()), rx + 45, y + 5, checked ? WHITE : DARK, true);
                    g.text(font, Component.literal(e.rate()), rx + colW - 12 - font.width(e.rate()),
                            y + 5, DARK, true);
                }
            }
            y += STEP;
        }
    }

    private void drawStatusBar(GuiGraphicsExtractor g) {
        MutableComponent status = Component.literal("Sell: ").withStyle(ChatFormatting.GRAY);
        status.append(MacroState.sell()
                ? Component.literal("AN").withStyle(ChatFormatting.GREEN)
                : Component.literal("aus").withStyle(ChatFormatting.GRAY));
        status.append(Component.literal("    Spawner: ").withStyle(ChatFormatting.GRAY));
        status.append(MacroState.spawner()
                ? Component.literal("AN").withStyle(ChatFormatting.GREEN)
                : Component.literal("aus").withStyle(ChatFormatting.GRAY));
        g.centeredText(font, status, width / 2, height - 22, GRAY);
        if (System.currentTimeMillis() < hintUntil) {
            g.centeredText(font, Component.literal(hint), width / 2, height - 11, DARK);
        }
    }

    // ---------- Slider ----------

    private static class ValueSlider extends AbstractSliderButton {
        private final int min;
        private final int max;
        private final IntConsumer setter;
        private final String prefix;

        ValueSlider(int x, int y, int w, int h, int min, int max, IntSupplier getter, IntConsumer setter, String prefix) {
            super(x, y, w, h, Component.empty(), (getter.getAsInt() - min) / (double) (max - min));
            this.min = min;
            this.max = max;
            this.setter = setter;
            this.prefix = prefix;
            updateMessage();
        }

        private int current() {
            return min + (int) Math.round(value * (max - min));
        }

        @Override
        protected void updateMessage() {
            if (prefix == null || prefix.isEmpty()) {
                setMessage(Component.empty());
            } else {
                setMessage(Component.literal(prefix + current() + " ms"));
            }
        }

        @Override
        protected void applyValue() {
            if (setter != null) {
                setter.accept(current());
            }
        }
    }
}
