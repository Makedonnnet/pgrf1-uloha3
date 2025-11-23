package solid;

import transforms.Point3D;
import java.awt.Color;

public class Axis extends Solid {

    public Axis() {
        // Střed (Počátek)
        vertexBuffer.add(new Point3D(0, 0, 0));

        // Osa X - Délka 1.0
        vertexBuffer.add(new Point3D(1, 0, 0));

        // Osa Y - Délka 1.0
        vertexBuffer.add(new Point3D(0, 1, 0));

        // Osa Z - Délka 1.0
        vertexBuffer.add(new Point3D(0, 0, 1));

        // Spojíme střed s konci
        addIndices(0, 1); // X
        addIndices(0, 2); // Y
        addIndices(0, 3); // Z

        this.color = Color.WHITE;
    }
}