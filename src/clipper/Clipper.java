package clipper;

import rasterize.Point;
import rasterize.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * Třída implementující Sutherland-Hodgman algoritmus pro ořezání
 * polygonu konvexním ořezávacím polygonem.
 */
public class Clipper {

    /**
     * Ořeže polygon (subject) pomocí konvexního ořezávacího polygonu (clipper).
     */
    public Polygon clip(Polygon subjectPolygon, Polygon clipPolygon) {
        // Kontrola vstupních podmínek
        if (subjectPolygon == null || clipPolygon == null || subjectPolygon.size() < 3 || clipPolygon.size() < 3) {
            System.out.println("Neplatné vstupní polygony pro ořezání");
            return null;
        }

        System.out.println("=== ZAČÁTEK OŘEZÁNÍ ===");
        System.out.println("Vstupní polygon vrcholů: " + subjectPolygon.size());
        System.out.println("Ořezávací polygon vrcholů: " + clipPolygon.size());

        // Začneme s vrcholy vstupního polygonu
        List<Point> subjectVertices = new ArrayList<>(subjectPolygon.getVertices());

        // Procházíme každou hranu ořezávacího polygonu
        for (int i = 0; i < clipPolygon.size(); i++) {
            // Hrana ořezávacího okna (E1 -> E2)
            Point E1 = clipPolygon.getVertices().get(i);
            Point E2 = clipPolygon.getVertices().get((i + 1) % clipPolygon.size());

            // Pokud už nemáme žádné vrcholy, ukončíme
            if (subjectVertices.isEmpty()) {
                break;
            }

            System.out.println("Ořezávací hrana: (" + E1.x + "," + E1.y + ") -> (" + E2.x + "," + E2.y + ")");
            System.out.println("Vstupní vrcholy: " + subjectVertices.size());

            // Seznam pro výstupní vrcholy po ořezání touto jednou hranou
            List<Point> outputVertices = new ArrayList<>();

            // S je "start point" - poslední bod z předchozí iterace
            Point S = subjectVertices.get(subjectVertices.size() - 1);

            // Projdeme všechny vrcholy aktuálního polygonu (P)
            for (Point P : subjectVertices) {
                // Kontrolujeme, zda jsou S a P "uvnitř" hrany E1-E2
                boolean isSInside = isInside(S, E1, E2);
                boolean isPInside = isInside(P, E1, E2);

                System.out.println("Bod S(" + S.x + "," + S.y + ") uvnitř: " + isSInside);
                System.out.println("Bod P(" + P.x + "," + P.y + ") uvnitř: " + isPInside);

                if (isPInside) {
                    if (!isSInside) {
                        // Případ 1: Vstupujeme do okna - přidáme průsečík I, pak P
                        Point I = getIntersection(S, P, E1, E2);
                        if (I != null) {
                            outputVertices.add(I);
                            System.out.println("Přidán vstupní průsečík: (" + I.x + "," + I.y + ")");
                        }
                    }
                    // Případ 2: Oba uvnitř - přidáme P
                    outputVertices.add(P);
                    System.out.println("Přidán vnitřní bod: (" + P.x + "," + P.y + ")");
                } else if (isSInside) {
                    // Případ 3: Opouštíme okno - přidáme jen průsečík I
                    Point I = getIntersection(S, P, E1, E2);
                    if (I != null) {
                        outputVertices.add(I);
                        System.out.println("Přidán výstupní průsečík: (" + I.x + "," + I.y + ")");
                    }
                } else {
                    System.out.println("Oba body vně - nic nepřidáno");
                }
                // Případ 4: Oba venku - nepřidáme nic

                // P se stává S pro příští iteraci
                S = P;
            }

            // Výstup z této iterace je vstupem pro další
            subjectVertices = outputVertices;
            System.out.println("Výstupní vrcholy: " + subjectVertices.size());
        }

        // Vytvoříme finální polygon z výsledných vrcholů
        if (subjectVertices.size() < 3) {
            System.out.println("Polygon kompletně ořezán. Finální vrcholy: " + subjectVertices.size());
            return null;
        }

        Polygon clippedPolygon = new Polygon();
        for (Point p : subjectVertices) {
            clippedPolygon.addVertex(p);
        }

        System.out.println("=== VÝSLEDEK OŘEZÁNÍ ===");
        System.out.println("Výsledný polygon: " + clippedPolygon.size() + " vrcholů");
        for (Point p : clippedPolygon.getVertices()) {
            System.out.println("Vrchol: (" + p.x + ", " + p.y + ")");
        }
        System.out.println("Ořezání úspěšné!");
        System.out.println("=== KONEC OŘEZÁNÍ ===");

        return clippedPolygon;
    }

    /**
     * Zjistí, zda je bod P "uvnitř" ve vztahu k ořezávací hraně E1->E2.
     * UPRAVENO: funguje pro CW polygony
     */
    private boolean isInside(Point P, Point E1, Point E2) {
        // Vektorový součin: (E2 - E1) × (P - E1)
        double crossProduct = (E2.x - E1.x) * (P.y - E1.y) - (E2.y - E1.y) * (P.x - E1.x);

        // UPRAVENO: změněno na < 0 pro CW polygony
        // Pro CW (clockwise) polygony: záporný výsledek = bod je uvnitř
        return crossProduct >= 0;
    }

    /**
     * Najde průsečík dvou úseček S-P a E1-E2.
     */
    private Point getIntersection(Point S, Point P, Point E1, Point E2) {
        // Vektor první úsečky (S -> P)
        double dx1 = P.x - S.x;
        double dy1 = P.y - S.y;

        // Vektor druhé úsečky (E1 -> E2)
        double dx2 = E2.x - E1.x;
        double dy2 = E2.y - E1.y;

        // Vektor od E1 k S
        double dx3 = S.x - E1.x;
        double dy3 = S.y - E1.y;

        // Jmenovatel pro výpočet parametrů
        double denominator = dy2 * dx1 - dx2 * dy1;

        // Ošetření rovnoběžných čar
        if (Math.abs(denominator) < 1e-10) {
            System.out.println("Rovnoběžné čáry detekovány, žádný průsečík");
            return null;
        }

        // Parametr pro první úsečku (S-P)
        double t = (dx2 * dy3 - dy2 * dx3) / denominator;

        // Kontrola, zda je průsečík na úsečce S-P
        if (t < 0 || t > 1) {
            System.out.println("Průsečík mimo úsečku S-P, t=" + t);
            return null;
        }

        // Výpočet bodu průsečíku pomocí parametru t
        int intersectX = (int) Math.round(S.x + t * dx1);
        int intersectY = (int) Math.round(S.y + t * dy1);

        System.out.println("Průsečík nalezen: (" + intersectX + "," + intersectY + ") s parametrem t=" + t);
        return new Point(intersectX, intersectY);
    }

    /**
     * Pomocná metoda pro kontrolu konvexnosti polygonu.
     */
    public boolean isConvex(Polygon polygon) {
        if (polygon.size() < 3) {
            return false;
        }

        boolean hasPositive = false;
        boolean hasNegative = false;

        for (int i = 0; i < polygon.size(); i++) {
            Point A = polygon.getVertices().get(i);
            Point B = polygon.getVertices().get((i + 1) % polygon.size());
            Point C = polygon.getVertices().get((i + 2) % polygon.size());

            double crossProduct = (B.x - A.x) * (C.y - B.y) - (B.y - A.y) * (C.x - B.x);

            if (crossProduct > 0) {
                hasPositive = true;
            } else if (crossProduct < 0) {
                hasNegative = true;
            }

            if (hasPositive && hasNegative) {
                return false;
            }
        }

        return true;
    }
}