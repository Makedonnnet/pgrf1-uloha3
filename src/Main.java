import controller.Controller3D; // Důležité: Importujeme 3D kontroler
import view.Window;

public class Main {
    public static void main(String[] args) {
        // Vytvoříme okno o velikosti 800x600 pixelů
        Window window = new Window(800, 600);

        // Spustíme 3D kontroler a předáme mu panel z okna
        new Controller3D(window.getPanel());
    }
}