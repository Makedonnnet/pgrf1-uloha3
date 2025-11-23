package transforms;

/**
 * Třída reprezentující vektor ve 3D prostoru.
 * Slouží pro výpočty osvětlení, normál a pohybu kamery.
 */
public class Vec3D {
    public final double x, y, z;

    public Vec3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Vytvoření vektoru z bodu
    public Vec3D(Point3D p) {
        this.x = p.x;
        this.y = p.y;
        this.z = p.z;
    }

    // Sčítání vektorů (Add)
    public Vec3D add(Vec3D v) {
        return new Vec3D(x + v.x, y + v.y, z + v.z);
    }

    // --- TOTO TI CHYBĚLO (Odčítání vektorů) ---
    public Vec3D sub(Vec3D v) {
        return new Vec3D(x - v.x, y - v.y, z - v.z);
    }
    // ------------------------------------------

    // Násobení skalárem (číslem)
    public Vec3D mul(double d) {
        return new Vec3D(x * d, y * d, z * d);
    }

    // Délka vektoru
    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    // Normalizace (vytvoření jednotkového vektoru)
    public Vec3D normalized() {
        double len = length();
        if (len == 0) return this;
        return new Vec3D(x / len, y / len, z / len);
    }

    // Skalární součin (Dot Product)
    public double dot(Vec3D v) {
        return x * v.x + y * v.y + z * v.z;
    }

    // Vektorový součin (Cross Product) - důležité pro normály a lookAt
    public Vec3D cross(Vec3D v) {
        return new Vec3D(
                y * v.z - z * v.y,
                z * v.x - x * v.z,
                x * v.y - y * v.x
        );
    }
}