package transforms;

/**
 * Matice pro změnu měřítka (scale).
 */
public class Mat4Scale extends Mat4 {

    /**
     * Změna měřítka v osách X, Y, Z.
     */
    public Mat4Scale(double x, double y, double z) {
        super();
        mat[0][0] = x;
        mat[1][1] = y;
        mat[2][2] = z;
    }

    /**
     * Rovnoměrná změna měřítka.
     */
    public Mat4Scale(double s) {
        this(s, s, s);
    }
}