package solid;

import transforms.Point3D;
import java.awt.Color;

/**
 * Třída reprezentující krychli (drátový model).
 * Skupina 1 dle zadání.
 */
public class Cube extends Solid {

    public Cube() {
        // --- 1. Vertex Buffer (8 vrcholů) ---
        // Krychle o velikosti 1x1x1 kolem středu [0,0,0]
        vertexBuffer.add(new Point3D(-0.5, -0.5, -0.5)); // 0: Vlevo dole vzadu
        vertexBuffer.add(new Point3D( 0.5, -0.5, -0.5)); // 1: Vpravo dole vzadu
        vertexBuffer.add(new Point3D( 0.5,  0.5, -0.5)); // 2: Vpravo nahoře vzadu
        vertexBuffer.add(new Point3D(-0.5,  0.5, -0.5)); // 3: Vlevo nahoře vzadu
        vertexBuffer.add(new Point3D(-0.5, -0.5,  0.5)); // 4: Vlevo dole vpředu
        vertexBuffer.add(new Point3D( 0.5, -0.5,  0.5)); // 5: Vpravo dole vpředu
        vertexBuffer.add(new Point3D( 0.5,  0.5,  0.5)); // 6: Vpravo nahoře vpředu
        vertexBuffer.add(new Point3D(-0.5,  0.5,  0.5)); // 7: Vlevo nahoře vpředu

        // --- 2. Index Buffer (12 hran) ---
        // Definujeme úsečky (vždy start, konec)

        // Zadní stěna
        addIndices(0, 1, 1, 2, 2, 3, 3, 0);

        // Přední stěna
        addIndices(4, 5, 5, 6, 6, 7, 7, 4);

        // Propojení přední a zadní stěny
        addIndices(0, 4, 1, 5, 2, 6, 3, 7);

        this.color = Color.MAGENTA; // Výchozí barva
        this.name = "Krychle";
    }
}