package de.hugosmp.macro.macro;

/** Tempo-Stufen des Sell-Macros (Standardwerte, spaeter anpassbar). */
public enum TempoStage {
    VERY_SLOW("1 Sehr langsam", 1, 250),
    SLOW("2 Langsam", 1, 100),
    NORMAL("3 Normal", 2, 100),
    FAST("4 Schnell", 3, 100),
    MAX("5 Maximal", 4, 100);

    public final String label;
    public final int stacks;
    public final int delayMs;

    TempoStage(String label, int stacks, int delayMs) {
        this.label = label;
        this.stacks = stacks;
        this.delayMs = delayMs;
    }

    public double clicksPerSecond() {
        return stacks * 1000.0 / delayMs;
    }

    public static TempoStage byIndex(int i) {
        TempoStage[] v = values();
        return v[Math.min(Math.max(i, 0), v.length - 1)];
    }
}
