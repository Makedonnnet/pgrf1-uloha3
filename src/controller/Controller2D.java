package controller;

// --- OPRAVENÁ SEKCE IMPORTŮ ---
import clipper.Clipper;
import Fill.PatternFill;
import Fill.CheckerboardPattern;
import Fill.ScanLineFiller;
import Fill.SeedFiller;
import rasterize.FilledLineRasterizer;
import rasterize.LineRasterizer;
import rasterize.Point; // Přesný import našeho Point
import rasterize.Polygon; // Přesný import našeho Polygon
import rasterize.RasterBufferedImage;
import rasterize.Rectangle;
import view.Panel;

import java.awt.Color; // Přesný import barvy
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
// --- KONEC OPRAVENÉ SEKCE IMPORTŮ ---

public class Controller2D {
    private final Panel panel;
    private final RasterBufferedImage raster;
    private final LineRasterizer lineRasterizer;
    private final SeedFiller seedFiller;
    private final ScanLineFiller scanLineFiller;

    private List<Polygon> polygons = new ArrayList<>(); // Seznam všech hotových polygonů
    private Polygon currentPolygon = new Polygon(); // Aktuálně kreslený polygon

    // Proměnné pro interaktivní kreslení
    private int startX, startY;
    private boolean drawing = false;
    private boolean shiftPressed = false;

    // Proměnné pro gradient
    private boolean gradientMode = false;
    private int currentColor1 = 0xff0000;
    private int currentColor2 = 0x0000ff;

    // Proměnné pro editaci vrcholů
    private Point selectedVertex = null;
    private boolean editing = false;

    // Proměnná pro režim kreslení
    private boolean lineMode = false;

    // Proměnné pro režimy vyplňování
    private boolean seedFillBackgroundMode = false;
    private boolean seedFillBoundaryMode = false;

    // Proměnné pro kreslení obdélníku
    private boolean rectangleMode = false;
    private Point rectP1 = null;
    private Point rectP2 = null;

    // Proměnné pro ořezávání
    private Clipper clipper = new Clipper();
    private Polygon clippingPolygon; // Náš fixní ořezávací polygon
    private List<Polygon> clippedPolygons = new ArrayList<>(); // Seznam výsledků ořezání
    private boolean clipMode = false; // Režim pro výběr polygonu k ořezání

    // --- NOVÉ PROMĚNNÉ PRO BONUS 2 (PATTERN FILL) ---
    private boolean patternMode = false; // Režim vyplňování vzorem
    private PatternFill activePattern; // Aktivní vzor
    // --- KONEC NOVÝCH PROMĚNNÝCH ---


    public Controller2D(Panel panel) {
        this.panel = panel;
        this.raster = panel.getRaster();
        this.lineRasterizer = new FilledLineRasterizer(raster);
        this.seedFiller = new SeedFiller();
        this.scanLineFiller = new ScanLineFiller();

        // --- INICIALIZACE VZORU (BONUS 2) ---
        // OPRAVA: Vytvoříme šachovnici 20x20 pixelů, střídající TMAVĚ ŠEDOU a bílou
        // (aby se zabránilo nekonečné smyčce s černým pozadím)
        this.activePattern = new CheckerboardPattern(Color.DARK_GRAY, Color.WHITE, 20);
        // --- KONEC INICIALIZACE VZORU ---

        lineRasterizer.setColor(Color.RED);
        panel.setFocusable(true);
        panel.requestFocusInWindow();

        // Inicializace ořezávacího polygonu (PO SMĚRU HODIN (CW))
        this.clippingPolygon = new Polygon();
        int centerX = panel.getRaster().getWidth() / 2;
        int centerY = panel.getRaster().getHeight() / 2;
        int radius = 150;

        clippingPolygon.addVertex(centerX, centerY - radius); // Horní bod
        clippingPolygon.addVertex(centerX + (int)(radius * 0.95), centerY - (int)(radius * 0.31)); // Pravý horní
        clippingPolygon.addVertex(centerX + (int)(radius * 0.59), centerY + (int)(radius * 0.81)); // Pravý dolní
        clippingPolygon.addVertex(centerX - (int)(radius * 0.59), centerY + (int)(radius * 0.81)); // Levý dolní
        clippingPolygon.addVertex(centerX - (int)(radius * 0.95), centerY - (int)(radius * 0.31)); // Levý horní

        System.out.println("Ořezávací polygon vytvořen s " + clippingPolygon.size() + " vrcholy (CW pořadí)");

        // POZNÁMKA: Necháme plátno při startu čisté
        // redrawAll();
        initListeners();
    }

    private void initListeners() {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

                // --- LOGIKA PRO VÝBĚR A OŘEZÁNÍ ('K') ---
                if (clipMode) {
                    System.out.println("Hledám polygon k ořezání v bodě [" + e.getX() + ", " + e.getY() + "]");
                    Point clickedVertex = findNearestVertex(e.getX(), e.getY());
                    Polygon subjectPolygon = null;

                    if (clickedVertex != null) {
                        for (Polygon poly : polygons) {
                            if (poly.getVertices().contains(clickedVertex)) {
                                subjectPolygon = poly;
                                break;
                            }
                        }
                    }

                    if (subjectPolygon != null) {
                        System.out.println("Polygon nalezen. Provádím ořezání...");
                        Polygon result = clipper.clip(subjectPolygon, clippingPolygon);

                        if (result != null && result.size() > 0) {
                            clippedPolygons.add(result);
                            System.out.println("Ořezání úspěšné, výsledek uložen.");
                        } else {
                            System.out.println("Polygon byl kompletně ořezán (mimo okno).");
                        }

                        polygons.remove(subjectPolygon);

                    } else {
                        System.out.println("Polygon k ořezání nebyl nalezen.");
                    }

                    clipMode = false; // Vypneme režim
                    redrawAll(); // Překreslíme scénu (již bez pětiúhelníku)
                    panel.repaint();
                    return;
                }
                // --- KONEC LOGIKY OŘEZÁVÁNÍ ---

                // --- LOGIKA OBDÉLNÍKU ('O') ---
                if (rectangleMode) {
                    if (rectP1 == null) {
                        rectP1 = new Point(e.getX(), e.getY());
                        drawing = true;
                    } else if (rectP2 != null) {
                        Point p3 = new Point(e.getX(), e.getY());

                        Rectangle rect = new Rectangle();
                        rect.setVertices(rectP1, rectP2, p3);
                        polygons.add(rect);

                        System.out.println("=== OBDÉLNÍK VYTVOŘEN A ULOŽEN ===");
                        rectP1 = null;
                        rectP2 = null;
                        rectangleMode = false;
                        System.out.println("REŽIM: Kreslení obdélníku vypnuto, návrat do polygon módu.");

                        redrawAll();
                        panel.repaint();
                    }
                    return;
                }
                // --- KONEC LOGIKY OBDÉLNÍKU ---

                // --- AKTUALIZOVANÁ LOGIKA VYPLŇOVÁNÍ ('F' a 'B') (s podporou 'T') ---
                if (seedFillBackgroundMode || seedFillBoundaryMode) {
                    int x = e.getX();
                    int y = e.getY();
                    System.out.println("Pokus o vyplnění v bodě [" + x + ", " + y + "]");

                    if (seedFillBackgroundMode) {
                        // REŽIM VYPLNĚNÍ POZADÍ
                        final int BACKGROUND_COLOR = 0xFF000000;
                        if (patternMode) {
                            // Vyplňování vzorem
                            System.out.println("...vyplňuji vzorem (pozadí)");
                            seedFiller.fillBackground(raster, x, y, activePattern, BACKGROUND_COLOR);
                        } else {
                            // Normální vyplňování barvou
                            int fillColor = lineRasterizer.getColor().getRGB();
                            if ((fillColor & 0x00FFFFFF) == (BACKGROUND_COLOR & 0x00FFFFFF)) {
                                System.out.println("Barva vyplnění je stejná jako barva pozadí!");
                            } else {
                                System.out.println("...vyplňuji barvou (pozadí)");
                                seedFiller.fillBackground(raster, x, y, fillColor, BACKGROUND_COLOR);
                            }
                        }

                    } else if (seedFillBoundaryMode) {
                        // REŽIM VYPLNĚNÍ HRANICE
                        final int BOUNDARY_COLOR = 0xFFFFFFFF;

                        // Překreslíme hranice na bílo
                        System.out.println("Překresluji VŠECHNY hranice na bílo pro Boundary Fill...");
                        Color oldColor = lineRasterizer.getColor();
                        lineRasterizer.setColor(new Color(BOUNDARY_COLOR, true));
                        drawAllPolygons();
                        lineRasterizer.setColor(oldColor);

                        if (patternMode) {
                            // Vyplňování vzorem
                            System.out.println("...vyplňuji vzorem (hranice)");
                            seedFiller.fillBoundary(raster, x, y, activePattern, BOUNDARY_COLOR);
                        } else {
                            // Normální vyplňování barvou
                            int fillColor = lineRasterizer.getColor().getRGB();
                            if ((fillColor & 0x00FFFFFF) == (BOUNDARY_COLOR & 0x00FFFFFF)) {
                                System.out.println("Barva vyplnění je stejná jako barva hranice! Měním na červenou.");
                                fillColor = 0xFFFF0000;
                            }
                            System.out.println("...vyplňuji barvou (hranice)");
                            seedFiller.fillBoundary(raster, x, y, fillColor, BOUNDARY_COLOR);
                        }
                    }
                    seedFillBackgroundMode = false;
                    seedFillBoundaryMode = false;
                    panel.repaint();
                    return;
                }
                // --- KONEC AKTUALIZOVANÉ LOGIKY VYPLŇOVÁNÍ ---

                // --- KRESLENÍ POLYGONŮ A EDITACE ---
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (e.isControlDown()) {
                        System.out.println("Ctrl + levé tlačítko - pokus o mazání");
                        if (!drawing) {
                            Point vertexToDelete = findNearestVertex(e.getX(), e.getY());
                            if (vertexToDelete != null) {
                                boolean removed = false;
                                for(Polygon poly : polygons) {
                                    if(poly.getVertices().remove(vertexToDelete)) {
                                        removed = true;
                                        break;
                                    }
                                }
                                if (!removed) {
                                    currentPolygon.getVertices().remove(vertexToDelete);
                                }

                                redrawAll();
                                panel.repaint();
                                System.out.println("=== Vrchol úspěšně smazán ===");
                            } else {
                                System.out.println("Žádný vrchol k smazání nebyl nalezen");
                            }
                        }
                    }
                    else if (e.isAltDown()) {
                        System.out.println("Alt + levé tlačítko - pokus o přidání vrcholu na hranu");
                        if (!drawing && !lineMode) {
                            Polygon targetPolygon = polygons.isEmpty() ? currentPolygon : polygons.get(polygons.size() - 1);
                            int edgeIndex = findNearestEdge(e.getX(), e.getY(), targetPolygon);
                            if (edgeIndex != -1) {
                                targetPolygon.getVertices().add(edgeIndex, new Point(e.getX(), e.getY()));
                                redrawAll();
                                panel.repaint();
                                System.out.println("=== Nový vrchol přidán na hranu: [" + e.getX() + ", " + e.getY() + "] ===");
                            } else {
                                System.out.println("Žádná hrana nebyla nalezena v dosahu");
                            }
                        }
                    }
                    else { // Normální levé tlačítko - kreslení
                        if (!drawing && !editing) {
                            startX = e.getX();
                            startY = e.getY();
                            drawing = true;

                            if (lineMode) {
                                System.out.println("=== ZAČÁTEK KRESLENÍ ÚSEČKY ===");
                            } else {
                                currentPolygon.addVertex(startX, startY);
                                System.out.println("=== PŘIDÁN PRVNÍ VRCHOL: [" + startX + ", " + startY + "] ===");
                                System.out.println("Počet vrcholů: " + currentPolygon.size());
                            }
                        } else if (drawing && !editing) {
                            int endX = e.getX();
                            int endY = e.getY();

                            if (shiftPressed) {
                                Point snapped = getSnappedPoint(startX, startY, endX, endY);
                                endX = snapped.x;
                                endY = snapped.y;
                            }
                            drawLine(startX, startY, endX, endY);

                            if (lineMode) {
                                drawing = false;
                                System.out.println("=== ÚSEČKA DOKONČENA ===");
                                Polygon line = new Polygon();
                                line.addVertex(startX, startY);
                                line.addVertex(endX, endY);
                                polygons.add(line);
                            } else {
                                currentPolygon.addVertex(endX, endY);
                                System.out.println("=== PŘIDÁN DALŠÍ VRCHOL: [" + endX + ", " + endY + "] ===");
                                System.out.println("Počet vrcholů: " + currentPolygon.size());
                                startX = endX;
                                startY = endY;
                            }
                        }
                    }
                    panel.repaint();
                }
                else if (e.getButton() == MouseEvent.BUTTON3) {
                    if (!drawing && !lineMode) {
                        selectedVertex = findNearestVertex(e.getX(), e.getY());
                        if (selectedVertex != null) {
                            editing = true;
                            System.out.println("Vybraný vrchol pro editaci: (" + selectedVertex.x + ", " + selectedVertex.y + ")");
                        } else {
                            System.out.println("Žádný vrchol nebyl nalezen pro editaci");
                        }
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // --- LOGIKA OBDÉLNÍKU ---
                if (rectangleMode && drawing && rectP1 != null && rectP2 == null) {
                    rectP2 = new Point(e.getX(), e.getY());
                    drawing = false;
                    System.out.println("Základna obdélníku definována. Čekám na 3. bod...");

                    redrawAll();
                    drawLine(rectP1.x, rectP1.y, rectP2.x, rectP2.y);

                    panel.repaint();
                    return;
                }
                // --- KONEC LOGIKY OBDÉLNÍKU ---

                if (editing) {
                    editing = false;
                    selectedVertex = null;
                    System.out.println("Editace dokončena");
                }
            }
        });

        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // --- LOGIKA OBDÉLNÍKU (elastická čára) ---
                if (rectangleMode && drawing && rectP1 != null && rectP2 == null) {
                    redrawAll();
                    int currentX = e.getX();
                    int currentY = e.getY();
                    drawLine(rectP1.x, rectP1.y, currentX, currentY);
                    panel.repaint();
                    return;
                }
                // --- KONEC LOGIKY OBDÉLNÍKU ---

                if (drawing && !editing && !lineMode) {
                    // "Pružná" čára pro polygon
                    redrawAll();

                    int currentX = e.getX();
                    int currentY = e.getY();

                    if (shiftPressed) {
                        Point snapped = getSnappedPoint(startX, startY, currentX, currentY);
                        currentX = snapped.x;
                        currentY = snapped.y;
                    }

                    drawLine(startX, startY, currentX, currentY);
                    panel.repaint();
                }
                else if (editing && selectedVertex != null) {
                    // Režim editace - přesun vrcholu
                    selectedVertex.x = e.getX();
                    selectedVertex.y = e.getY();
                    redrawAll();
                    panel.repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                // --- LOGIKA OBDÉLNÍKU (náhled výšky) ---
                if (rectangleMode && rectP1 != null && rectP2 != null) {
                    Point p3 = new Point(e.getX(), e.getY());
                    Rectangle rect = new Rectangle();
                    rect.setVertices(rectP1, rectP2, p3);

                    redrawAll();

                    for (int i = 0; i < rect.size(); i++) {
                        Point p1 = rect.getVertices().get(i);
                        Point p2 = rect.getVertices().get((i + 1) % rect.size());
                        drawLine(p1.x, p1.y, p2.x, p2.y);
                    }
                    panel.repaint();
                }
                // --- KONEC LOGIKY OBDÉLNÍKU ---
            }
        });

        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                System.out.println("Stisknuta klávesa: " + KeyEvent.getKeyText(e.getKeyCode()) + " (kód: " + e.getKeyCode() + ")");

                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    shiftPressed = true;
                }

                if (e.getKeyCode() == KeyEvent.VK_C) {
                    clearCanvas();
                }

                if (e.getKeyCode() == KeyEvent.VK_G) {
                    gradientMode = !gradientMode;
                    System.out.println("Gradient mode: " + (gradientMode ? "ON" : "OFF"));
                    panel.repaint();
                }

                if (e.getKeyCode() == KeyEvent.VK_L) {
                    lineMode = !lineMode;
                    drawing = false;
                    rectangleMode = false;
                    clipMode = false;
                    System.out.println("Line mode: " + (lineMode ? "ON - kreslení jednotlivých úseček" : "OFF - kreslení polygonu"));
                    panel.repaint();
                }

                // --- KLÁVESA 'O' PRO OBDÉLNÍK ---
                if (e.getKeyCode() == KeyEvent.VK_O) {
                    System.out.println("REŽIM: Kreslení obdélníku");
                    rectangleMode = true;
                    drawing = false;
                    editing = false;
                    lineMode = false;
                    seedFillBackgroundMode = false;
                    seedFillBoundaryMode = false;
                    clipMode = false;
                    currentPolygon.clear();
                    rectP1 = null;
                    rectP2 = null;
                }

                // --- KLÁVESA 'K' PRO OŘEZÁVÁNÍ ---
                else if (e.getKeyCode() == KeyEvent.VK_K) {
                    System.out.println("REŽIM: Ořezání (Clipping)");
                    System.out.println("Vyber polygon, který chceš ořezat.");
                    clipMode = true;
                    rectangleMode = false;
                    drawing = false;
                    editing = false;
                    lineMode = false;
                    seedFillBackgroundMode = false;
                    seedFillBoundaryMode = false;

                    // --- OPRAVA: PŘEKRESLÍME PLÁTNO, ABY SE ZOBRAZIL PĚTIÚHELNÍK ---
                    redrawAll();
                    panel.repaint();
                    // --- KONEC OPRAVY ---
                }

                // --- NOVÁ KLÁVESA 'T' PRO VZOR (TEXTURE) ---
                else if (e.getKeyCode() == KeyEvent.VK_T) {
                    patternMode = !patternMode; // Přepneme režim
                    System.out.println("REŽIM: Vyplňování vzorem " + (patternMode ? "ZAPNUTO" : "VYPNUTO"));
                }
                // --- KONEC NOVÉ KLÁVESY ---

                // Blok pro přepínání režimů a barev
                else if (e.getKeyCode() == KeyEvent.VK_F) {
                    System.out.println("REŽIM: Semínkové vyplnění (Pozadí)");
                    seedFillBackgroundMode = true;
                    seedFillBoundaryMode = false;
                    drawing = false;
                    editing = false;
                    lineMode = false;
                    rectangleMode = false;
                    clipMode = false;
                }
                else if (e.getKeyCode() == KeyEvent.VK_B) {
                    System.out.println("REŽIM: Semínkové vyplnění (Hranice)");
                    seedFillBoundaryMode = true;
                    seedFillBackgroundMode = false;
                    drawing = false;
                    editing = false;
                    lineMode = false;
                    rectangleMode = false;
                    clipMode = false;
                }

                // Výběr barev
                else if (e.getKeyCode() == KeyEvent.VK_R) {
                    lineRasterizer.setColor(Color.RED);
                    System.out.println("Color: RED");
                }
                else if (e.getKeyCode() == KeyEvent.VK_B) {
                    lineRasterizer.setColor(Color.BLUE);
                    System.out.println("Color: BLUE");
                }
                else if (e.getKeyCode() == KeyEvent.VK_Y) {
                    lineRasterizer.setColor(Color.YELLOW);
                    System.out.println("Color: YELLOW");
                }
                else if (e.getKeyCode() == KeyEvent.VK_P) {
                    lineRasterizer.setColor(Color.PINK);
                    System.out.println("Color: PINK");
                }

                // Ostatní akce
                if (e.getKeyCode() == KeyEvent.VK_SPACE && currentPolygon.size() > 2 && !lineMode) {
                    closePolygon();
                }

                if ((e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) && selectedVertex != null && !lineMode) {
                    System.out.println("Delete/Backspace - pokus o mazání vybraného vrcholu");
                    boolean removed = false;
                    for(Polygon poly : polygons) {
                        if(poly.getVertices().remove(selectedVertex)) {
                            removed = true;
                            break;
                        }
                    }
                    if (!removed) {
                        currentPolygon.getVertices().remove(selectedVertex);
                    }

                    redrawAll();
                    panel.repaint();
                    selectedVertex = null;
                    editing = false;
                    System.out.println("=== Vybraný vrchol smazán ===");
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    shiftPressed = false;
                }
            }
        });
    }

    // Metoda pro přichytávání
    private Point getSnappedPoint(int startX, int startY, int endX, int endY) {
        int dx = endX - startX;
        int dy = endY - startY;
        double angle = Math.toDegrees(Math.atan2(dy, dx));
        angle = (angle + 360) % 360;

        if (angle >= 337.5 || angle < 22.5) return new Point(endX, startY);
        else if (angle >= 22.5 && angle < 67.5) { int dist = Math.min(Math.abs(dx), Math.abs(dy)); return new Point(startX + dist, startY + dist); }
        else if (angle >= 67.5 && angle < 112.5) return new Point(startX, endY);
        else if (angle >= 112.5 && angle < 157.5) { int dist = Math.min(Math.abs(dx), Math.abs(dy)); return new Point(startX - dist, startY + dist); }
        else if (angle >= 157.5 && angle < 202.5) return new Point(endX, startY);
        else if (angle >= 202.5 && angle < 247.5) { int dist = Math.min(Math.abs(dx), Math.abs(dy)); return new Point(startX - dist, startY - dist); }
        else if (angle >= 247.5 && angle < 292.5) return new Point(startX, endY);
        else { int dist = Math.min(Math.abs(dx), Math.abs(dy)); return new Point(startX + dist, startY - dist); }
    }

    // Najde nejbližší vrchol
    private Point findNearestVertex(int x, int y) {
        Point nearest = null;
        double minDistance = Double.MAX_VALUE;
        int threshold = 15;

        for (Polygon poly : polygons) {
            for (Point vertex : poly.getVertices()) {
                double distance = Math.sqrt(Math.pow(vertex.x - x, 2) + Math.pow(vertex.y - y, 2));
                if (distance < minDistance && distance < threshold) {
                    minDistance = distance;
                    nearest = vertex;
                }
            }
        }
        for (Point vertex : currentPolygon.getVertices()) {
            double distance = Math.sqrt(Math.pow(vertex.x - x, 2) + Math.pow(vertex.y - y, 2));
            if (distance < minDistance && distance < threshold) {
                minDistance = distance;
                nearest = vertex;
            }
        }

        if (nearest != null) System.out.println("Nalezen nejbližší vrchol: [" + nearest.x + ", " + nearest.y + "]");
        else System.out.println("Žádný vrchol nebyl nalezen v dosahu");

        return nearest;
    }

    // Najde nejbližší hranu
    private int findNearestEdge(int x, int y, Polygon poly) {
        if (poly.size() < 2) return -1;
        int nearestEdgeIndex = -1;
        double minDistance = Double.MAX_VALUE;
        int threshold = 20;

        for (int i = 0; i < poly.size(); i++) {
            Point p1 = poly.getVertices().get(i);
            Point p2 = poly.getVertices().get((i + 1) % poly.size());
            double distance = pointToLineDistance(x, y, p1.x, p1.y, p2.x, p2.y);
            if (distance < minDistance && distance < threshold) {
                minDistance = distance;
                nearestEdgeIndex = i + 1;
            }
        }

        if (nearestEdgeIndex != -1) System.out.println("Nalezena nejbližší hrana, index pro vložení: " + nearestEdgeIndex);
        else System.out.println("Žádná hrana nebyla nalezena v dosahu");

        return nearestEdgeIndex;
    }

    // Vzdálenost bodu od úsečky
    private double pointToLineDistance(int px, int py, int x1, int y1, int x2, int y2) {
        double A = px - x1; double B = py - y1;
        double C = x2 - x1; double D = y2 - y1;
        double dot = A * C + B * D;
        double len_sq = C * C + D * D;
        double param = (len_sq != 0) ? dot / len_sq : -1;
        double xx, yy;
        if (param < 0) { xx = x1; yy = y1; }
        else if (param > 1) { xx = x2; yy = y2; }
        else { xx = x1 + param * C; yy = y1 + param * D; }
        double dx = px - xx; double dy = py - yy;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Kreslení čáry
    private void drawLine(int x1, int y1, int x2, int y2) {
        if (gradientMode) drawGradientLine(x1, y1, x2, y2);
        else lineRasterizer.rasterize(x1, y1, x2, y2);
    }

    // Kreslení gradientní čáry
    private void drawGradientLine(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x2 - x1); int dy = Math.abs(y2 - y1);
        int sx = (x1 < x2) ? 1 : -1; int sy = (y1 < y2) ? 1 : -1;
        int err = dx - dy;
        int r1 = (currentColor1 >> 16) & 0xFF; int g1 = (currentColor1 >> 8) & 0xFF; int b1 = currentColor1 & 0xFF;
        int r2 = (currentColor2 >> 16) & 0xFF; int g2 = (currentColor2 >> 8) & 0xFF; int b2 = (currentColor2 >> 8) & 0xFF;
        double totalLength = Math.sqrt(dx * dx + dy * dy);
        int x = x1; int y = y1;
        while (true) {
            if (x >= 0 && x < raster.getWidth() && y >= 0 && y < raster.getHeight()) {
                double currentLength = Math.sqrt((x - x1) * (x - x1) + (y - y1) * (y - y1));
                double ratio = (totalLength > 0) ? currentLength / totalLength : 0;
                ratio = Math.max(0, Math.min(1, ratio));
                int r = (int) (r1 + (r2 - r1) * ratio);
                int g = (int) (g1 + (g2 - g1) * ratio);
                int b = (int) (b1 + (b2 - b1) * ratio);
                int color = (0xFF << 24) | (r << 16) | (g << 8) | b;
                raster.setPixel(x, y, color);
            }
            if (x == x2 && y == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x += sx; }
            if (e2 < dx) { err += dx; y += sy; }
        }
    }

    // --- FINÁLNÍ OPRAVENÁ METODA REDRAWALL (s podporou VZORU a OPRAVOU ZOBRAZENÍ) ---
    /**
     * Překreslí vše ve správném pořadí.
     */
    private void redrawAll() {
        raster.clear();

        // 1. NEJDŘÍVE kreslíme běžné polygony (červené)
        drawAllPolygons();

        // 2. PAK kreslíme aktuálně kreslený polygon
        drawCurrentPolygon();

        // 3. PAK VYPLNÍME ořezané polygony (žlutě NEBO vzorem)
        if (!clippedPolygons.isEmpty()) {
            if (patternMode) {
                // Vyplňování vzorem
                System.out.println("ScanLine vyplňování vzorem...");
                for (Polygon poly : clippedPolygons) {
                    if (poly != null && poly.size() >= 3) {
                        scanLineFiller.fill(poly, raster, activePattern);
                    }
                }
            } else {
                // Normální vyplňování barvou
                System.out.println("ScanLine vyplňování žlutou barvou...");
                int fillColor = Color.YELLOW.getRGB(); // Barva výplně
                for (Polygon poly : clippedPolygons) {
                    if (poly != null && poly.size() >= 3) {
                        scanLineFiller.fill(poly, raster, fillColor);
                    }
                }
            }
        }

        // 4. PAK kreslíme ořezávací polygon (modrý) - POUZE POKUD JE AKTIVNÍ REŽIM 'K'
        if (clippingPolygon != null && clipMode) { // <-- OPRAVA ZDE
            Color oldColor = lineRasterizer.getColor();
            lineRasterizer.setColor(Color.CYAN); // Světle modrá
            for (int i = 0; i < clippingPolygon.size(); i++) {
                Point p1 = clippingPolygon.getVertices().get(i);
                Point p2 = clippingPolygon.getVertices().get((i + 1) % clippingPolygon.size());
                drawLine(p1.x, p1.y, p2.x, p2.y);
            }
            lineRasterizer.setColor(oldColor);
        }

        // 5. NAKONEC kreslíme obrysy ořezaných polygonů (zelené) - PŘES VÝPLŇ
        if (!clippedPolygons.isEmpty()) {
            Color oldColor = lineRasterizer.getColor();
            lineRasterizer.setColor(Color.GREEN); // Zelená
            for (Polygon poly : clippedPolygons) {
                if (poly != null && poly.size() >= 2) {
                    for (int i = 0; i < poly.size(); i++) {
                        Point p1 = poly.getVertices().get(i);
                        Point p2 = poly.getVertices().get((i + 1) % poly.size());
                        drawLine(p1.x, p1.y, p2.x, p2.y);
                    }
                }
            }
            lineRasterizer.setColor(oldColor);
        }
    }
    // --- KONEC FINÁLNÍ METODY REDRAWALL ---

    // Vykreslí všechny hotové polygony (nesmaže rastr)
    private void drawAllPolygons() {
        for (Polygon poly : polygons) {
            if (poly.size() < 2) continue;
            if (poly.size() == 2 && lineMode) {
                Point p1 = poly.getVertices().get(0);
                Point p2 = poly.getVertices().get(1);
                drawLine(p1.x, p1.y, p2.x, p2.y);
            } else {
                for (int i = 0; i < poly.size(); i++) {
                    Point p1 = poly.getVertices().get(i);
                    Point p2 = poly.getVertices().get((i + 1) % poly.size());
                    drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }
        }
    }

    // Vykreslí aktuálně kreslený polygon
    private void drawCurrentPolygon() {
        if (currentPolygon.size() < 2) return;

        for (int i = 0; i < currentPolygon.size() - 1; i++) {
            Point p1 = currentPolygon.getVertices().get(i);
            Point p2 = currentPolygon.getVertices().get(i + 1);
            drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }

    // Uzavření polygonu
    private void closePolygon() {
        if (currentPolygon.size() > 2) {
            Point first = currentPolygon.getVertices().get(0);
            Point last = currentPolygon.getVertices().get(currentPolygon.size() - 1);
            drawLine(last.x, last.y, first.x, first.y);

            polygons.add(currentPolygon);
            currentPolygon = new Polygon();

            panel.repaint();
            System.out.println("=== POLYGON UZAVŘEN A ULOŽEN ===");
            System.out.println("Počet uložených tvarů: " + polygons.size());

            drawing = false;
            System.out.println("Režim kreslení ukončen");
        }
    }

    // Smazání plátna
    private void clearCanvas() {
        raster.clear();
        System.out.println("=== SMAZÁNÍ PLÁTNA ===");
        currentPolygon.clear();
        polygons.clear();
        clippedPolygons.clear();

        drawing = false;
        editing = false;
        selectedVertex = null;

        // Resetování všech režimů
        seedFillBackgroundMode = false;
        seedFillBoundaryMode = false;
        rectangleMode = false;
        lineMode = false;
        clipMode = false;
        patternMode = false; // Resetování režimu vzoru
        rectP1 = null;
        rectP2 = null;

        panel.repaint();
        System.out.println("Všechny datové struktury vyčištěny");
    }
}