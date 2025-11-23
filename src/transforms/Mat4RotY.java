package transforms;

/**
 * Matice rotace kolem osy Y.
 */
public class Mat4RotY extends Mat4 {

    public Mat4RotY(double alpha) {
        super();
        double sin = Math.sin(alpha);
        double cos = Math.cos(alpha);

        mat[0][0] = cos;
        mat[0][2] = sin;
        mat[2][0] = -sin;
        mat[2][2] = cos;
    }
}