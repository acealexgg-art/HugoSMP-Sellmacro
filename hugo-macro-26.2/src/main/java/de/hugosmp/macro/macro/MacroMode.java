package de.hugosmp.macro.macro;

/** Modi des Spawner-Macros. Neue Modi einfach hier ergaenzen. */
public enum MacroMode {
    DROP_ONLY("Nur droppen"),
    SELL_ONLY("Nur verkaufen"),
    DROP_AND_SELL("Droppen + verkaufen"),
    CLEAR_ONLY("Nur leeren");

    public final String label;

    MacroMode(String label) {
        this.label = label;
    }

    public static MacroMode byIndex(int i) {
        MacroMode[] v = values();
        return v[Math.min(Math.max(i, 0), v.length - 1)];
    }
}
