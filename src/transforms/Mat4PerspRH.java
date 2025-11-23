package transforms;

/**
 * Matice pro perspektivní projekci (Right-Handed).
 * OPRAVENÁ VERZE: Správné rozložení prvků pro řádkové násobení.
 */
public class Mat4PerspRH extends Mat4 {

    public Mat4PerspRH(double fov, double aspect, double near, double far) {
        super();
        // Vynulujeme matici
        for(int i=0; i<4; i++) for(int j=0; j<4; j++) mat[i][j] = 0;

        double h = 1.0 / Math.tan(fov / 2.0);
        double w = h / aspect;

        mat[0][0] = w;
        mat[1][1] = h;

        // Zde byla chyba - prohozené indexy
        mat[2][2] = far / (near - far);
        mat[2][3] = (near * far) / (near - far); // Posunutí Z (B) patří sem

        mat[3][2] = -1.0; // Perspektivní dělení (-1) patří sem (aby w' = -z)
        mat[3][3] = 0.0;
    }
}
