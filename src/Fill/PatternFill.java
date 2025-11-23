package Fill;

/**
 * Rozhraní (interface) pro definování vyplňovacího vzoru.
 * Umožňuje definovat barvu pro každý pixel na základě jeho souřadnic.
 */
public interface PatternFill {

    /**
     * Metoda, která vrací barvu pro daný pixel (x, y).
     * @param x x-ová souřadnice pixelu
     * @param y y-ová souřadnice pixelu
     * @return Barva (jako integer ARGB) pro tento pixel
     */
    int getPixelColor(int x, int y);

}