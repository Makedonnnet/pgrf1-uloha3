package Fill;

import rasterize.Point;
import rasterize.Polygon;
import rasterize.Raster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Třída implementující Scan-line algoritmus pro vyplňování polygonů.
 */
public class ScanLineFiller {

    /**
     * Pomocná vnitřní třída reprezentující jednu hranu v tabulce hran.
     */
    private static class Edge {
        int yMax;  // Horní y-souřadnice hrany
        double x;    // Aktuální x-souřadnice (kde hrana protíná scan-line)
        double k;    // Směrnice (1/m), tj. dx/dy

        Edge(int yMax, double x, double k) {
            this.yMax = yMax;
            this.x = x;
            this.k = k;
        }
    }

    /**
     * Vyplní polygon pomocí Scan-line algoritmu.
     * @param polygon Polygon k vyplnění
     * @param raster Raster pro kreslení
     * @param fillColor Barva vyplnění
     */
    public void fill(Polygon polygon, Raster raster, int fillColor) {
        if (polygon.size() < 3) {
            return; // Není co vyplňovat
        }

        // --- Krok 1: Vytvoření Tabulky Hran (Edge Table - ET) ---
        Map<Integer, List<Edge>> edgeTable = new HashMap<>();
        int yMinGlobal = Integer.MAX_VALUE;
        int yMaxGlobal = Integer.MIN_VALUE;

        List<Point> vertices = polygon.getVertices();
        for (int i = 0; i < vertices.size(); i++) {
            Point p1 = vertices.get(i);
            Point p2 = vertices.get((i + 1) % vertices.size()); // Další bod s uzavřením

            // Ignorujeme horizontální hrany
            if (p1.y == p2.y) {
                continue;
            }

            // Seřadíme body podle y
            Point start = (p1.y < p2.y) ? p1 : p2;
            Point end = (p1.y < p2.y) ? p2 : p1;

            int yMin = start.y;
            int yMax = end.y;
            double x = start.x;
            double k = (double) (p2.x - p1.x) / (double) (p2.y - p1.y); // Směrnice (1/m)

            // Vytvoříme novou hranu
            Edge edge = new Edge(yMax, x, k);

            // Přidáme hranu do tabulky na řádek yMin
            edgeTable.putIfAbsent(yMin, new ArrayList<>());
            edgeTable.get(yMin).add(edge);

            // Aktualizujeme globální min/max y
            if (yMin < yMinGlobal) yMinGlobal = yMin;
            if (yMax > yMaxGlobal) yMaxGlobal = yMax;
        }

        // --- Krok 2: Procházení Scan-line (od yMin do yMax) ---

        // Tabulka Aktivních Hran (Active Edge Table - AET)
        List<Edge> activeEdgeTable = new ArrayList<>();

        for (int y = yMinGlobal; y < yMaxGlobal; y++) {

            // 1. Přidáme nové hrany z ET do AET
            if (edgeTable.containsKey(y)) {
                activeEdgeTable.addAll(edgeTable.get(y));
            }

            // 2. Odstraníme hrany, které končí (y == yMax)
            final int currentY = y; // Nutné pro lambda výraz
            activeEdgeTable.removeIf(edge -> edge.yMax == currentY);

            // 3. Seřadíme AET podle x-ové souřadnice
            Collections.sort(activeEdgeTable, Comparator.comparingDouble(edge -> edge.x));

            // 4. Vyplníme pixely mezi páry hran v AET
            for (int i = 0; i < activeEdgeTable.size(); i += 2) {
                if (i + 1 < activeEdgeTable.size()) {
                    Edge e1 = activeEdgeTable.get(i);
                    Edge e2 = activeEdgeTable.get(i + 1);

                    // Vykreslíme horizontální čáru
                    int xStart = (int) Math.round(e1.x);
                    int xEnd = (int) Math.round(e2.x);

                    for (int x = xStart; x < xEnd; x++) {
                        raster.setPixel(x, y, fillColor);
                    }
                }
            }

            // 5. Aktualizujeme x-ové souřadnice hran v AET pro další řádek (y+1)
            for (Edge edge : activeEdgeTable) {
                edge.x += edge.k;
            }
        }
    }
    /**
     * NOVÁ PŘETÍŽENÁ METODA: Vyplní polygon pomocí Scan-line algoritmu
     * s použitím VZORU (PatternFill).
     *
     * @param polygon Polygon k vyplnění
     * @param raster Raster pro kreslení
     * @param pattern Vzor, kterým se má oblast vyplnit
     */
    public void fill(Polygon polygon, Raster raster, PatternFill pattern) {
        if (polygon.size() < 3) {
            return; // Není co vyplňovat
        }

        // --- Krok 1: Vytvoření Tabulky Hran (Edge Table - ET) ---
        Map<Integer, List<Edge>> edgeTable = new HashMap<>();
        int yMinGlobal = Integer.MAX_VALUE;
        int yMaxGlobal = Integer.MIN_VALUE;

        List<Point> vertices = polygon.getVertices();
        for (int i = 0; i < vertices.size(); i++) {
            Point p1 = vertices.get(i);
            Point p2 = vertices.get((i + 1) % vertices.size()); // Další bod s uzavřením

            if (p1.y == p2.y) continue; // Ignorujeme horizontální hrany

            Point start = (p1.y < p2.y) ? p1 : p2;
            Point end = (p1.y < p2.y) ? p2 : p1;

            int yMin = start.y;
            int yMax = end.y;
            double x = start.x;
            double k = (double) (p2.x - p1.x) / (double) (p2.y - p1.y);

            Edge edge = new Edge(yMax, x, k);
            edgeTable.putIfAbsent(yMin, new ArrayList<>());
            edgeTable.get(yMin).add(edge);

            if (yMin < yMinGlobal) yMinGlobal = yMin;
            if (yMax > yMaxGlobal) yMaxGlobal = yMax;
        }

        // --- Krok 2: Procházení Scan-line ---
        List<Edge> activeEdgeTable = new ArrayList<>();

        for (int y = yMinGlobal; y < yMaxGlobal; y++) {
            if (edgeTable.containsKey(y)) {
                activeEdgeTable.addAll(edgeTable.get(y));
            }
            final int currentY = y;
            activeEdgeTable.removeIf(edge -> edge.yMax == currentY);
            Collections.sort(activeEdgeTable, Comparator.comparingDouble(edge -> edge.x));

            // 4. Vyplníme pixely mezi páry hran
            for (int i = 0; i < activeEdgeTable.size(); i += 2) {
                if (i + 1 < activeEdgeTable.size()) {
                    Edge e1 = activeEdgeTable.get(i);
                    Edge e2 = activeEdgeTable.get(i + 1);

                    int xStart = (int) Math.round(e1.x);
                    int xEnd = (int) Math.round(e2.x);

                    // --- JEDINÁ ZMĚNA JE ZDE ---
                    // Místo sjednocené barvy voláme pattern.getPixelColor() pro každý pixel
                    for (int x = xStart; x < xEnd; x++) {
                        int patternColor = pattern.getPixelColor(x, y); // Získáme barvu ze vzoru
                        raster.setPixel(x, y, patternColor);
                    }
                    // --- KONEC ZMĚNY ---
                }
            }

            // 5. Aktualizujeme x-ové souřadnice
            for (Edge edge : activeEdgeTable) {
                edge.x += edge.k;
            }
        }
    }
}