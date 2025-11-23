package transforms;

/**
 * Kamera pro pohyb ve 3D scéně.
 * OPRAVENÁ VERZE: Kompatibilní s novou Mat4 (M * v).
 */
public class Camera {
    private Vec3D position;
    private double azimuth;
    private double zenith;
    private double radius;
    private boolean firstPerson; // True = FPS, False = Orbit

    public Camera() {
        this.position = new Vec3D(0, 0, 0);
        this.azimuth = 0;
        this.zenith = 0;
        this.radius = 10;
        this.firstPerson = true;
    }

    public Camera withPosition(Vec3D pos) {
        this.position = pos;
        return this;
    }

    public Camera withAzimuth(double azimuth) {
        this.azimuth = azimuth;
        return this;
    }

    public Camera withZenith(double zenith) {
        this.zenith = zenith;
        return this;
    }

    public Camera forward(double speed) {
        double dx = Math.sin(azimuth) * Math.cos(zenith);
        double dy = Math.sin(zenith);
        double dz = Math.cos(azimuth) * Math.cos(zenith);
        position = position.add(new Vec3D(dx, dy, dz).mul(speed));
        return this;
    }

    public Camera backward(double speed) { return forward(-speed); }

    public Camera left(double speed) {
        double dx = Math.sin(azimuth + Math.PI / 2);
        double dz = Math.cos(azimuth + Math.PI / 2);
        position = position.add(new Vec3D(dx, 0, dz).mul(speed));
        return this;
    }

    public Camera right(double speed) { return left(-speed); }

    public Camera addAzimuth(double angle) {
        azimuth += angle;
        return this;
    }

    public Camera addZenith(double angle) {
        zenith += angle;
        if (zenith <= -Math.PI / 2) zenith = -Math.PI / 2 + 0.001;
        if (zenith >= Math.PI / 2) zenith = Math.PI / 2 - 0.001;
        return this;
    }

    public Mat4 getViewMatrix() {
        Vec3D eye;
        Vec3D target;
        Vec3D up = new Vec3D(0, 1, 0);

        double dx = Math.sin(azimuth) * Math.cos(zenith);
        double dy = Math.sin(zenith);
        double dz = Math.cos(azimuth) * Math.cos(zenith);
        Vec3D dir = new Vec3D(dx, dy, dz);

        if (firstPerson) {
            eye = position;
            target = position.add(dir);
        } else {
            eye = position.sub(dir.mul(radius));
            target = position;
        }
        return lookAt(eye, target, up);
    }

    // --- ZDE BYLA CHYBA (OPRAVENO) ---
    private Mat4 lookAt(Vec3D eye, Vec3D target, Vec3D up) {
        Vec3D z = eye.sub(target).normalized();
        Vec3D x = up.cross(z).normalized();
        Vec3D y = z.cross(x).normalized();

        Mat4 res = new Mat4();

        // Opravené plnění matice (po řádcích)
        // Řádek 0 (X osa)
        res.mat[0][0] = x.x;
        res.mat[0][1] = x.y;
        res.mat[0][2] = x.z;
        res.mat[0][3] = -x.dot(eye); // Posunutí ve 4. sloupci

        // Řádek 1 (Y osa)
        res.mat[1][0] = y.x;
        res.mat[1][1] = y.y;
        res.mat[1][2] = y.z;
        res.mat[1][3] = -y.dot(eye);

        // Řádek 2 (Z osa)
        res.mat[2][0] = z.x;
        res.mat[2][1] = z.y;
        res.mat[2][2] = z.z;
        res.mat[2][3] = -z.dot(eye);

        // Řádek 3
        res.mat[3][0] = 0;
        res.mat[3][1] = 0;
        res.mat[3][2] = 0;
        res.mat[3][3] = 1;

        return res;
    }
}