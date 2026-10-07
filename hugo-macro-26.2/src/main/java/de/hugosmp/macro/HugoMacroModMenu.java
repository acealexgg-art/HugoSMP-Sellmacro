package de.hugosmp.macro;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import de.hugosmp.macro.gui.MacroSettingsScreen;

public class HugoMacroModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new MacroSettingsScreen(parent);
    }
}
