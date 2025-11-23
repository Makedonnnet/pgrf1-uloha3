package transforms;

/**
 * Matice pro posunutí (translaci) ve 3D.
 */
public class Mat4Transl extends Mat4 {

    /**
     * Vytvoří translační matici.
     * @param x posun po ose X
     * @param y posun po ose Y
     * @param z posun po ose Z
     */
    public Mat4Transl(double x, double y, double z) {
        // Zavolá konstruktor rodiče (Mat4), který vytvoří jednotkovou matici
        super();

        // Nastavíme hodnoty pro posun v posledním sloupci
        mat[0][3] = x;
        mat[1][3] = y;
        mat[2][3] = z;
    }
}