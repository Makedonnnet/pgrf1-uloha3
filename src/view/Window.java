package view;

import javax.swing.*;

public class Window extends JFrame {
    private Panel panel;

    public Window(int width, int height) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("3D Grafika - Projekt Část 3");

        panel = new Panel(width, height);
        add(panel);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public Panel getPanel() {
        return panel;
    }
}