package controller;

import rasterize.LineRasterizer;
import rasterize.LineRasterizerGraphics;
import rasterize.Raster;
import renderer3d.Renderer3D;
import solid.*;
import transforms.*;
import view.Panel;

import java.awt.Color;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Kontroler pro 3D úlohu.
 * Řídí logiku aplikace, obsluhuje vstupy a spravuje 3D scénu.
 */
public class Controller3D {

    private final Panel panel;
    private final Raster raster;

    private Renderer3D renderer3D;
    private Camera camera;

    // Seznam objektů ve scéně
    private List<Solid> scene = new ArrayList<>();

    // Proměnné pro ovládání myší
    private int startX, startY;
    private boolean mouseLeftDown = false;

    // Proměnná pro animaci (Bonus 2)
    private double animationAngle = 0.0;

    public Controller3D(Panel panel) {
        this.panel = panel;
        this.raster = panel.getRaster();

        initObjects();
        initListeners();

        // První vykreslení
        display();
    }

    /**
     * Inicializace objektů, kamery a rendereru.
     */
    private void initObjects() {
        // 1. Rasterizer
        LineRasterizer lineRasterizer = new LineRasterizerGraphics(panel.getRaster());

        // 2. Renderer
        renderer3D = new Renderer3D(raster, lineRasterizer);

        // 3. Kamera - Izometrický pohled (z rohu)
        camera = new Camera()
                .withPosition(new Vec3D(6, 4, 6))
                .withAzimuth(Math.toRadians(225))
                .withZenith(Math.toRadians(-25));

        // 4. Projekce
        double aspect = (double) raster.getWidth() / raster.getHeight();
        Mat4 pers = new Mat4PerspRH(Math.PI / 4, aspect, 0.1, 100.0);
        renderer3D.setProjMatrix(pers);

        // --- SCÉNA ---

        // 1. Krychle (vlevo)
        Cube cube = new Cube();
        cube.setModelMatrix(new Mat4Transl(-2, 0, 0));
        cube.setColor(Color.MAGENTA);
        scene.add(cube);

        // 2. Pyramida (vpravo)
        Pyramid pyramid = new Pyramid();
        pyramid.setModelMatrix(new Mat4Transl(2, 0, 0));
        pyramid.setColor(Color.YELLOW);
        scene.add(pyramid);


        // 3. OSY (RGB - Splnění zadání "barevné úsečky")
        // Osa X - Červená (Red)
        scene.add(new Axis(2, 0, 0, Color.RED));

        // Osa Y - Zelená (Green)
        scene.add(new Axis(0, 2, 0, Color.GREEN));

        // Osa Z - Modrá (Blue)
        scene.add(new Axis(0, 0, 2, Color.BLUE));

        // 4. Křivka (Bézierův oblouk nad krychlí)
        Point3D p1 = new Point3D(-3, 0, 0);
        Point3D p2 = new Point3D(-3, 3, 0);
        Point3D p3 = new Point3D(-1, 3, 0);
        Point3D p4 = new Point3D(-1, 0, 0);
        BezierCurve curve = new BezierCurve(p1, p2, p3, p4);
        scene.add(curve);

        // BONUS 1: Parametrická plocha (Vlnobití dole)
        Surface surface = new Surface();
        surface.setModelMatrix(new Mat4Transl(0, -2, 0)); // Posuneme dolů
        scene.add(surface);
    }

    /**
     * Nastavení ovládání (Myš, Klávesnice + Animace).
     */
    private void initListeners() {
        // --- Myš (Rozhlížení) ---
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    startX = e.getX();
                    startY = e.getY();
                    mouseLeftDown = true;
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) mouseLeftDown = false;
            }
        });

        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (mouseLeftDown) {
                    int dx = startX - e.getX();
                    int dy = startY - e.getY();
                    double sensitivity = 0.005;

                    camera = camera.addAzimuth(dx * sensitivity);
                    camera = camera.addZenith(dy * sensitivity);

                    startX = e.getX();
                    startY = e.getY();
                    display();
                }
            }
        });

        // --- Klávesnice (Pohyb WSAD + Reset R) ---
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                double speed = 0.3;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W: camera = camera.forward(speed); break;
                    case KeyEvent.VK_S: camera = camera.backward(speed); break;
                    case KeyEvent.VK_A: camera = camera.left(speed); break;
                    case KeyEvent.VK_D: camera = camera.right(speed); break;

                    // Reset kamery
                    case KeyEvent.VK_R:
                        camera = new Camera()
                                .withPosition(new Vec3D(6, 4, 6))
                                .withAzimuth(Math.toRadians(225))
                                .withZenith(Math.toRadians(-25));
                        break;
                }
                display();
            }
        });

        // --- BONUS 2: ANIMACE (Timer) ---
        // Každých 15 ms se provede pootočení plochy
        javax.swing.Timer timer = new javax.swing.Timer(15, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                animationAngle += 0.02; // Rychlost otáčení

                // Najdeme objekt Surface (je poslední v seznamu) a otočíme ho
                if (!scene.isEmpty()) {
                    Solid surface = scene.get(scene.size() - 1);
                    // Pokud je to skutečně Surface (pro jistotu)
                    if (surface instanceof Surface) {
                        Mat4 rot = new Mat4RotY(animationAngle);
                        Mat4 transl = new Mat4Transl(0, -2, 0);
                        // Nejdřív otočit, pak posunout
                        surface.setModelMatrix(rot.mul(transl));
                    }
                }
                display(); // Překreslení
            }
        });
        timer.start();

        panel.setFocusable(true);
        panel.requestFocusInWindow();
    }

    /**
     * Vykreslení scény.
     */
    private void display() {
        raster.clear();
        renderer3D.setViewMatrix(camera.getViewMatrix());
        renderer3D.render(scene);
        panel.repaint();
    }
}