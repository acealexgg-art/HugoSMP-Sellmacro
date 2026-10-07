package de.hugosmp.macro.gui;

import de.hugosmp.macro.HugoMacroClient;
import de.hugosmp.macro.config.MacroConfig;
import de.hugosmp.macro.macro.ItemKeys;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/** Liste der Items, die der Sell-Filter verkaufen darf. Klick auf eine Zeile entfernt das Item. */
public class SellItemsScreen extends Screen {

    private static final int ROW_H = 22;

    private final Screen parent;
    private int page = 0;
    private int panelX, panelW, listTop, perPage;
    private final List<String> shown = new ArrayList<>();

    public SellItemsScreen(Screen parent) {
        super(Component.literal("Verkaufs-Items"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        shown.clear();
        panelW = Math.min(width - 20, 320);
        panelX = (width - panelW) / 2;
        listTop = 44;
        perPage = Math.max(1, (height - 60 - listTop) / ROW_H);

        List<String> all = new ArrayList<>(MacroConfig.sellItems);
        int pages = Math.max(1, (int) Math.ceil(all.size() / (double) perPage));
        page = Math.min(page, pages - 1);

        int from = page * perPage;
        for (int i = from; i < Math.min(all.size(), from + perPage); i++) {
            String key = all.get(i);
            shown.add(key);
            int y = listTop + (i - from) * ROW_H;
            addRenderableWidget(Button.builder(Component.empty(), b -> {
                MacroConfig.sellItems.remove(key);
                rebuildWidgets();
            }).bounds(panelX + 4, y, panelW - 8, ROW_H - 2).build());
        }

        int by1 = height - 52, by2 = height - 28;
        int bw = (panelW - 6) / 2;
        addRenderableWidget(Button.builder(Component.literal("Item in Hand"), b -> {
            if (minecraft != null && minecraft.player != null) {
                add(minecraft.player.getMainHandItem());
            }
            rebuildWidgets();
        }).bounds(panelX, by1, bw, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Inventar-Items"), b -> {
            if (minecraft != null && minecraft.player != null) {
                Inventory inv = minecraft.player.getInventory();
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    add(inv.getItem(i));
                }
            }
            rebuildWidgets();
        }).bounds(panelX + bw + 6, by1, bw, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Alle entfernen"), b -> {
            MacroConfig.sellItems.clear();
            rebuildWidgets();
        }).bounds(panelX, by2, bw, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Fertig"), b -> onClose())
                .bounds(panelX + bw + 6, by2, bw, 20).build());

        Button prev = Button.builder(Component.literal("<"), b -> {
            page = Math.max(0, page - 1);
            rebuildWidgets();
        }).bounds(panelX + panelW - 44, 20, 20, 18).build();
        Button next = Button.builder(Component.literal(">"), b -> {
            page++;
            rebuildWidgets();
        }).bounds(panelX + panelW - 22, 20, 20, 18).build();
        prev.active = page > 0;
        next.active = page < pages - 1;
        addRenderableWidget(prev);
        addRenderableWidget(next);
    }

    private void add(ItemStack stack) {
        if (stack.isEmpty()) return;
        Item item = stack.getItem();
        if (item == Items.AIR) return;
        MacroConfig.sellItems.add(ItemKeys.key(item));
    }

    @Override
    public void onClose() {
        MacroConfig.save();
        HugoMacroClient.openScreen(minecraft, parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        g.fill(panelX, 16, panelX + panelW, height - 56 + 54, 0xE0181818);
        g.fill(panelX, 16, panelX + panelW, 18, 0xFFB050D0);
        super.extractRenderState(g, mouseX, mouseY, partialTick);

        g.text(font, Component.literal("Verkaufs-Items (" + MacroConfig.sellItems.size() + ")"),
                panelX + 6, 26, 0xFFFFFFFF, true);
        if (MacroConfig.sellItems.isEmpty()) {
            g.text(font, Component.literal("Noch leer – Item in die Hand nehmen oder Inventar übernehmen."),
                    panelX + 8, listTop + 6, 0xFFAAAAAA, true);
        }
        for (int i = 0; i < shown.size(); i++) {
            int y = listTop + i * ROW_H;
            Item item = ItemKeys.resolve(shown.get(i));
            ItemStack stack = new ItemStack(item);
            g.item(stack, panelX + 10, y + 2);
            Component name = item == Items.AIR ? Component.literal(shown.get(i)) : stack.getHoverName();
            g.text(font, name, panelX + 34, y + 7, 0xFFFFFFFF, true);
            g.text(font, Component.literal("entfernen"), panelX + panelW - 12 - font.width("entfernen"),
                    y + 7, 0xFF777777, true);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
