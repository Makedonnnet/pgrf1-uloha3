package transforms;

public class Mat4 {
    public final double[][] mat = new double[4][4];

    public Mat4() {
        for (int i = 0; i < 4; i++) mat[i][i] = 1.0;
    }

    public Mat4(double[][] m) {
        for (int i = 0; i < 4; i++) System.arraycopy(m[i], 0, this.mat[i], 0, 4);
    }

    // Násobení matic (standardní řádek * sloupec)
    public Mat4 mul(Mat4 other) {
        Mat4 res = new Mat4();
        for(int i=0; i<4; i++) for(int j=0; j<4; j++) res.mat[i][j] = 0;
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                for (int k = 0; k < 4; k++)
                    res.mat[i][j] += this.mat[i][k] * other.mat[k][j];
        return res;
    }

    // Násobení Matice * Bod (M * P)
    // Toto je klíčové pro správnou transformaci
    public Point3D multiply(Point3D p) {
        double x = p.x * mat[0][0] + p.y * mat[0][1] + p.z * mat[0][2] + p.w * mat[0][3];
        double y = p.x * mat[1][0] + p.y * mat[1][1] + p.z * mat[1][2] + p.w * mat[1][3];
        double z = p.x * mat[2][0] + p.y * mat[2][1] + p.z * mat[2][2] + p.w * mat[2][3];
        double w = p.x * mat[3][0] + p.y * mat[3][1] + p.z * mat[3][2] + p.w * mat[3][3];
        return new Point3D(x, y, z, w);
    }
}