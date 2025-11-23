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

public class Controller3D {

    private final Panel panel;
    private final Raster raster;

    private Renderer3D renderer3D;
    private Camera camera;

    private List<Solid> scene = new ArrayList<>();

    private int startX, startY;
    private boolean mouseLeftDown = false;

    public Controller3D(Panel panel) {
        this.panel = panel;
        this.raster = panel.getRaster();

        initObjects();
        initListeners();

        display();
    }

    private void initObjects() {
        // 1. Vytvoření Rasterizeru
        LineRasterizer lineRasterizer = new LineRasterizerGraphics(panel.getRaster());

        // 2. Vytvoření Renderer3D
        renderer3D = new Renderer3D(raster, lineRasterizer);

        // 3. Vytvoření Kamery - OPRAVA POZICE
        // Pozice: [4, 4, 4] (vpravo, nahoře, vpředu)
        // Azimut: 225 stupňů (dívá se šikmo zpět na střed [0,0,0])
        // Zenit: -25 stupňů (dívá se mírně dolů)
        camera = new Camera()
                .withPosition(new Vec3D(6, 4, 6))
                .withAzimuth(Math.toRadians(225))
                .withZenith(Math.toRadians(-25));;

        // 4. Projekce
        double aspect = (double) raster.getWidth() / raster.getHeight();
        Mat4 pers = new Mat4PerspRH(Math.PI / 4, aspect, 0.1, 100.0);
        renderer3D.setProjMatrix(pers);

        // --- SCÉNA ---
        // 1. Krychle vlevo
        Cube cube = new Cube();
        cube.setModelMatrix(new Mat4Transl(-2, 0, 0));
        cube.setColor(Color.MAGENTA);
        scene.add(cube);

        // 2. Pyramida vpravo
        Pyramid pyramid = new Pyramid();
        pyramid.setModelMatrix(new Mat4Transl(2, 0, 0));
        pyramid.setColor(Color.YELLOW);
        scene.add(pyramid);


        // 3. OSY
        scene.add(new Axis());

        // 4. Křivka (Bezier) - pokud ji máš vytvořenou
        Point3D p1 = new Point3D(-3, 0, 0);  // Start (vlevo od krychle)
        Point3D p2 = new Point3D(-3, 3, 0);  // Táhne nahoru
        Point3D p3 = new Point3D(-1, 3, 0);  // Táhne nahoru
        Point3D p4 = new Point3D(-1, 0, 0);  // Konec (vpravo od krychle)

        BezierCurve curve = new BezierCurve(p1, p2, p3, p4);
        scene.add(curve);
    }

    private void initListeners() {
        // --- MYŠ (Rozhlížení) ---
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
                    // Citlivost myši
                    double sensitivity = 0.005;
                    int dx = startX - e.getX();
                    int dy = startY - e.getY();

                    camera = camera.addAzimuth(dx * sensitivity);
                    camera = camera.addZenith(dy * sensitivity);

                    startX = e.getX();
                    startY = e.getY();
                    display();
                }
            }
        });

        // --- KLÁVESNICE (Pohyb WSAD) ---
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                double speed = 0.3; // Rychlost pohybu

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        camera = camera.forward(speed);
                        break;
                    case KeyEvent.VK_S:
                        camera = camera.backward(speed);
                        break;
                    case KeyEvent.VK_A:
                        // Pokud A fungovalo divně, zkusíme 'right' místo 'left' nebo obráceně
                        // Standardně: A = Left
                        camera = camera.left(speed);
                        break;
                    case KeyEvent.VK_D:
                        // Standardně: D = Right
                        camera = camera.right(speed);
                        break;

                    // Reset kamery do "hezké" pozice
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
        panel.setFocusable(true);
        panel.requestFocusInWindow();
    }

    private void display() {
        raster.clear();
        renderer3D.setViewMatrix(camera.getViewMatrix());
        renderer3D.render(scene);
        panel.repaint();
    }
}