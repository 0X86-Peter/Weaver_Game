import javax.swing.*;

public class GameController {
    private final GameModel model;
    private final GUIView view;

    public GameController(GameModel model, GUIView view) {
        this.model = model;
        this.view = view;
        bindListeners();
    }

    private void bindListeners() {
        view.setKeyListener(model::appendToInputBuffer);
        view.setSubmitListener(() -> {
            if (model.getInputBuffer().length() == 4) {
                model.submitInputBuffer();
            }
        });
        view.setDeleteListener(model::deleteLastInput);
        model.addObserver((o, arg) -> {
            SwingUtilities.invokeLater(() -> updateResetButtonState());
        });
    }
    private void updateResetButtonState() {
        if (view.resetButton != null && !view.resetButton.isDisplayable()) return;

        boolean hasMoves = model.getCurrentPath().size() > 1;
        view.resetButton.setEnabled(hasMoves);
    }
}