package transforms;

/**
 * Třída reprezentující bod ve 3D prostoru pomocí homogenních souřadnic.
 */
public class Point3D {
    public final double x, y, z, w;

    /**
     * Vytvoří 3D bod (w je automaticky 1.0).
     */
    public Point3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = 1.0;
    }

    /**
     * Vytvoří bod s plnou specifikací homogenní složky w.
     */
    public Point3D(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    /**
     * Provede dehomogenizaci (perspektivní dělení).
     * @return Nový bod se souřadnicemi ve 3D (kde w=1)
     */
    public Point3D mul(double factor) {
        return new Point3D(x * factor, y * factor, z * factor, w * factor);
    }

    public Point3D add(Point3D other) {
        return new Point3D(x + other.x, y + other.y, z + other.z, w + other.w);
    }
}
