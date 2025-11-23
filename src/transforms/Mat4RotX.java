package transforms;

/**
 * Matice rotace kolem osy X.
 */
public class Mat4RotX extends Mat4 {

    public Mat4RotX(double alpha) {
        super();
        double sin = Math.sin(alpha);
        double cos = Math.cos(alpha);

        mat[1][1] = cos;
        mat[1][2] = -sin;
        mat[2][1] = sin;
        mat[2][2] = cos;
    }
}