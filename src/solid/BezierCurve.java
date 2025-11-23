package solid;

import transforms.Point3D;
import transforms.Mat4;
import java.awt.Color;

/**
 * Třída reprezentující Bézierovu kubiku.
 * Počítá body křivky pomocí maticového násobení (Bernsteinovy polynomy).
 */
public class BezierCurve extends Solid {

    /**
     * Vytvoří Bézierovu křivku ze 4 bodů.
     * P1, P4 = krajní body (křivka jimi prochází).
     * P2, P3 = řídící body (určují tvar).
     */
    public BezierCurve(Point3D p1, Point3D p2, Point3D p3, Point3D p4) {
        // Bézierova báze (matice) - pro splnění zadání "Definice kubik pomocí matic"
        Mat4 basis = new Mat4(new double[][] {
                {-1,  3, -3,  1},
                { 3, -6,  3,  0},
                {-3,  3,  0,  0},
                { 1,  0,  0,  0}
        });

        // Počet segmentů (čím více, tím je křivka hladší)
        int detail = 30;

        for (int i = 0; i <= detail; i++) {
            double t = (double) i / detail;

            // Vektor parametrů T = [t^3, t^2, t, 1]
            double t3 = t * t * t;
            double t2 = t * t;

            // Výpočet vah (Bernsteinovy polynomy)
            // Odpovídá násobení vektoru T a matice báze
            double h1 = -1*t3 + 3*t2 - 3*t + 1; // (1-t)^3
            double h2 =  3*t3 - 6*t2 + 3*t;     // 3t(1-t)^2
            double h3 = -3*t3 + 3*t2;           // 3t^2(1-t)
            double h4 =  1*t3;                  // t^3

            // Finální bod Q(t) = h1*P1 + h2*P2 + h3*P3 + h4*P4
            double x = h1*p1.x + h2*p2.x + h3*p3.x + h4*p4.x;
            double y = h1*p1.y + h2*p2.y + h3*p3.y + h4*p4.y;
            double z = h1*p1.z + h2*p2.z + h3*p3.z + h4*p4.z;

            vertexBuffer.add(new Point3D(x, y, z));

            // Spojíme bod s předchozím bodem (vytvoříme úsečku)
            if (i > 0) {
                addIndices(i - 1, i);
            }
        }

        this.color = Color.CYAN; // Azurová barva (aby byla dobře vidět)
    }
}