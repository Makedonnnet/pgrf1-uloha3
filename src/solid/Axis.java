package solid;

import transforms.Point3D;
import java.awt.Color;

/**
 * Třída pro reprezentaci jedné osy (úsečky) s vlastní barvou.
 */
public class Axis extends Solid {

    /**
     * Vytvoří osu z počátku [0,0,0] do bodu [x,y,z].
     */
    public Axis(double x, double y, double z, Color color) {
        // Bod 0: Počátek
        vertexBuffer.add(new Point3D(0, 0, 0));

        // Bod 1: Konec osy
        vertexBuffer.add(new Point3D(x, y, z));

        // Spojíme je úsečkou
        addIndices(0, 1);

        this.color = color;
        this.name = "Osa";
    }
}