package solid;

import transforms.Point3D;
import java.awt.Color;

/**
 * Třída reprezentující jehlan (pyramidu).
 * Skupina 2 dle zadání.
 */
public class Pyramid extends Solid {

    public Pyramid() {
        // --- 1. Vertex Buffer (5 vrcholů) ---
        // Podstava je čtverec, špička nahoře

        // Podstava
        vertexBuffer.add(new Point3D(-0.5, -0.5, -0.5)); // 0: Vlevo vzadu
        vertexBuffer.add(new Point3D( 0.5, -0.5, -0.5)); // 1: Vpravo vzadu
        vertexBuffer.add(new Point3D( 0.5, -0.5,  0.5)); // 2: Vpravo vpředu
        vertexBuffer.add(new Point3D(-0.5, -0.5,  0.5)); // 3: Vlevo vpředu

        // Špička
        vertexBuffer.add(new Point3D( 0, 0.5, 0));       // 4: Vrchol nahoře

        // --- 2. Index Buffer ---
        // Hrany podstavy
        addIndices(0, 1, 1, 2, 2, 3, 3, 0);

        // Hrany ke špičce
        addIndices(0, 4, 1, 4, 2, 4, 3, 4);

        this.color = Color.YELLOW;
        this.name = "Pyramida";
    }
}