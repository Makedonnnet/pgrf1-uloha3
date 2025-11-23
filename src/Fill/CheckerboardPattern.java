package Fill;

import java.awt.Color;

/**
 * Implementace PatternFill, která vytváří vzor šachovnice.
 * Střídá dvě barvy v mřížce o dané velikosti.
 */
public class CheckerboardPattern implements PatternFill {

    private final int color1_ARGB; // První barva (např. černá)
    private final int color2_ARGB; // Druhá barva (např. bílá)
    private final int size;      // Velikost jednoho čtverečku (např. 10 pixelů)

    /**
     * Konstruktor pro šachovnicový vzor.
     * @param color1 První barva
     * @param color2 Druhá barva
     * @param size Velikost čtverečku v pixelech
     */
    public CheckerboardPattern(Color color1, Color color2, int size) {
        this.color1_ARGB = color1.getRGB();
        this.color2_ARGB = color2.getRGB();
        this.size = size;
    }

    /**
     * Vrací barvu pro daný pixel podle logiky šachovnice.
     */
    @Override
    public int getPixelColor(int x, int y) {
        // Celé dělení určí, ve kterém "čtverečku" se nacházíme
        int row = x / size;
        int col = y / size;

        // Pokud je součet řádku a sloupce sudý, použijeme první barvu,
        // pokud je lichý, použijeme druhou. (To vytváří šachovnici).
        if ((row + col) % 2 == 0) {
            return color1_ARGB;
        } else {
            return color2_ARGB;
        }
    }
}