package transforms;

/**
 * Matice rotace kolem osy Z.
 */
public class Mat4RotZ extends Mat4 {

    public Mat4RotZ(double alpha) {
        super();
        double sin = Math.sin(alpha);
        double cos = Math.cos(alpha);

        mat[0][0] = cos;
        mat[0][1] = -sin;
        mat[1][0] = sin;
        mat[1][1] = cos;
    }
}