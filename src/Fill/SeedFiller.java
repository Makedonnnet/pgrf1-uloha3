package Fill;


import rasterize.Point;
import rasterize.Raster;

import java.util.Stack;

/**
 * Třída implementující algoritmus semínkového vyplňování (Flood Fill).
 * Používá ne-rekurzivní přístup se zásobníkem (Stack).
 */
public class SeedFiller {

    /**
     * Vyplnění oblasti omezené barvou hranice (Boundary Fill).
     * Vyplňuje všechny pixely, které *nemají* barvu hranice, dokud nenarazí na hranici.
     *
     * @param raster        Raster pro kreslení
     * @param x             Počáteční souřadnice x
     * @param y             Počáteční souřadnice y
     * @param fillColor     Barva, kterou se má oblast vyplnit
     * @param boundaryColor Barva hranice
     */
    public void fillBoundary(Raster raster, int x, int y, int fillColor, int boundaryColor) {
        // Získání aktuální barvy v bodě kliknutí
        int startColor = raster.getPixel(x, y);

        // Pokud jsme klikli na hranici nebo na již vyplněnou oblast, neděláme nic
        if (startColor == boundaryColor || startColor == fillColor) {
            return;
        }

        Stack<Point> stack = new Stack<>();
        stack.push(new Point(x, y));

        while (!stack.isEmpty()) {
            Point p = stack.pop();

            // Kontrola hranic rasteru
            if (p.x < 0 || p.x >= raster.getWidth() || p.y < 0 || p.y >= raster.getHeight()) {
                continue;
            }

            int currentColor = raster.getPixel(p.x, p.y);

            // Podmínka pro Boundary Fill:
            // Pokračujeme, pokud aktuální barva NENÍ barva hranice a NENÍ cílová barva vyplnění
            if (currentColor != boundaryColor && currentColor != fillColor) {
                raster.setPixel(p.x, p.y, fillColor);

                // Přidání sousedů (4-směrné)
                stack.push(new Point(p.x + 1, p.y));
                stack.push(new Point(p.x - 1, p.y));
                stack.push(new Point(p.x, p.y + 1));
                stack.push(new Point(p.x, p.y - 1));
            }
        }
    }

    /**
     * Vyplnění oblasti omezené barvou pozadí (Background Fill / Flood Fill).
     * Nahrazuje všechny souvislé pixely s barvou pozadí.
     *
     * @param raster          Raster pro kreslení
     * @param x               Počáteční souřadnice x
     * @param y               Počáteční souřadnice y
     * @param fillColor       Nová barva, kterou se má oblast vyplnit
     * @param backgroundColor Barva pozadí, která má být nahrazena
     */
    public void fillBackground(Raster raster, int x, int y, int fillColor, int backgroundColor) {
        // Pokud je cílová barva stejná jako barva pozadí, neděláme nic
        if (fillColor == backgroundColor) {
            return;
        }

        // Pokud bod startu nemá barvu pozadí, neděláme nic (kliknuto na hranu atd.)
        if (raster.getPixel(x, y) != backgroundColor) {
            return;
        }

        Stack<Point> stack = new Stack<>();
        stack.push(new Point(x, y));

        while (!stack.isEmpty()) {
            Point p = stack.pop();

            // Kontrola hranic rasteru
            if (p.x < 0 || p.x >= raster.getWidth() || p.y < 0 || p.y >= raster.getHeight()) {
                continue;
            }

            // Podmínka pro Background Fill:
            // Pokračujeme, pouze pokud má pixel cílovou barvu pozadí
            if (raster.getPixel(p.x, p.y) == backgroundColor) {
                raster.setPixel(p.x, p.y, fillColor);

                // Přidání sousedů (4-směrné)
                stack.push(new Point(p.x + 1, p.y));
                stack.push(new Point(p.x - 1, p.y));
                stack.push(new Point(p.x, p.y + 1));
                stack.push(new Point(p.x, p.y - 1));
            }
        }
    }
    /**
     * NOVÁ PŘETÍŽENÁ METODA: Vyplnění oblasti omezené barvou hranice (Boundary Fill)
     * pomocí VZORU (PatternFill).
     *
     * @param raster        Raster pro kreslení
     * @param x             Počáteční souřadnice x
     * @param y             Počáteční souřadnice y
     * @param pattern       Vzor, kterým se má oblast vyplnit
     * @param boundaryColor Barva hranice
     */
    public void fillBoundary(Raster raster, int x, int y, PatternFill pattern, int boundaryColor) {
        // Získání aktuální barvy v bodě kliknutí
        int startColor = raster.getPixel(x, y);

        // Pokud jsme klikli na hranici, neděláme nic
        if (startColor == boundaryColor) {
            return;
        }

        Stack<Point> stack = new Stack<>();
        stack.push(new Point(x, y));

        while (!stack.isEmpty()) {
            Point p = stack.pop();

            // Kontrola hranic rasteru
            if (p.x < 0 || p.x >= raster.getWidth() || p.y < 0 || p.y >= raster.getHeight()) {
                continue;
            }

            int currentColor = raster.getPixel(p.x, p.y);
            int patternColor = pattern.getPixelColor(p.x, p.y); // Získáme barvu vzoru pro tento pixel

            // Podmínka pro Boundary Fill:
            // Pokračujeme, pokud aktuální barva NENÍ barva hranice a NENÍ cílová barva vzoru
            // (Musíme kontrolovat patternColor, abychom nezacyklili, pokud vzor obsahuje boundaryColor)
            if (currentColor != boundaryColor && currentColor != patternColor) {
                raster.setPixel(p.x, p.y, patternColor); // Nastavíme barvu ze vzoru

                // Přidání sousedů (4-směrné)
                stack.push(new Point(p.x + 1, p.y));
                stack.push(new Point(p.x - 1, p.y));
                stack.push(new Point(p.x, p.y + 1));
                stack.push(new Point(p.x, p.y - 1));
            }
        }
    }

    /**
     * NOVÁ PŘETÍŽENÁ METODA: Vyplnění oblasti omezené barvou pozadí (Background Fill)
     * pomocí VZORU (PatternFill).
     *
     * @param raster          Raster pro kreslení
     * @param x               Počáteční souřadnice x
     * @param y               Počáteční souřadnice y
     * @param pattern         Vzor, kterým se má oblast vyplnit
     * @param backgroundColor Barva pozadí, která má být nahrazena
     */
    public void fillBackground(Raster raster, int x, int y, PatternFill pattern, int backgroundColor) {
        // Pokud bod startu nemá barvu pozadí, neděláme nic
        if (raster.getPixel(x, y) != backgroundColor) {
            return;
        }

        Stack<Point> stack = new Stack<>();
        stack.push(new Point(x, y));

        while (!stack.isEmpty()) {
            Point p = stack.pop();

            // Kontrola hranic rasteru
            if (p.x < 0 || p.x >= raster.getWidth() || p.y < 0 || p.y >= raster.getHeight()) {
                continue;
            }

            // Podmínka pro Background Fill:
            // Pokračujeme, pouze pokud má pixel cílovou barvu pozadí
            if (raster.getPixel(p.x, p.y) == backgroundColor) {
                int patternColor = pattern.getPixelColor(p.x, p.y); // Získáme barvu vzoru
                raster.setPixel(p.x, p.y, patternColor); // Nastavíme ji

                // Přidání sousedů (4-směrné)
                stack.push(new Point(p.x + 1, p.y));
                stack.push(new Point(p.x - 1, p.y));
                stack.push(new Point(p.x, p.y + 1));
                stack.push(new Point(p.x, p.y - 1));
            }
        }
    }
}