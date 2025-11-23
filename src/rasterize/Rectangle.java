package rasterize;

/**
 * Speciální třída pro obdélník, dědící z Polygon.
 * Vytvořeno pro splnění požadavků úkolu 2.
 */
public class Rectangle extends Polygon {

    /**
     * Konstruktor volá rodičovský konstruktor (Polygon).
     */
    public Rectangle() {
        super();
    }

    /**
     * Speciální metoda pro nastavení vrcholů obdélníku na základě
     * základny (p1, p2) a třetího bodu (p3), který určuje výšku.
     * @param p1 První bod základny
     * @param p2 Druhý bod základny
     * @param p3 Třetí bod definující výšku
     */
    public void setVertices(Point p1, Point p2, Point p3) {
        // Vyčistit případné staré vrcholy
        this.clear();

        // Výpočet
        // Vektor základny
        double vx = p2.x - p1.x;
        double vy = p2.y - p1.y;

        // Vektor kolmý na základnu (normála)
        double nx = -vy;
        double ny = vx;

        // Normalizace normály
        double len = Math.sqrt(nx * nx + ny * ny);
        // Ošetření dělení nulou, pokud by p1 == p2
        if (len == 0) {
            return;
        }
        nx /= len;
        ny /= len;

        // Vektor od p1 k p3
        double v3x = p3.x - p1.x;
        double v3y = p3.y - p1.y;

        // Výška je skalární součin v3 a normalizované normály (projekce)
        double h = v3x * nx + v3y * ny;

        // Výpočet zbývajících dvou bodů obdélníku
        // Bod "p4" (protilehlý k p1)
        int p4x = (int) (p1.x + h * nx);
        int p4y = (int) (p1.y + h * ny);

        // Bod "p5" (protilehlý k p2)
        int p5x = (int) (p2.x + h * nx);
        int p5y = (int) (p2.y + h * ny);

        // Přidání vrcholů do polygonu
        this.addVertex(p1);
        this.addVertex(p2);
        this.addVertex(new Point(p5x, p5y));
        this.addVertex(new Point(p4x, p4y));
    }
}