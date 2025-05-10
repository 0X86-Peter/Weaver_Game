import javax.swing.*;

/**
 * Entry point to launch the Weaver game.
 */
// Main.java
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameModel model = new GameModel();
            GUIView view = new GUIView(model);
            new GameController(model, view);
        });
    }
}