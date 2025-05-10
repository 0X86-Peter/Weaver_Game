import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class GUIView extends JFrame implements Observer {
    private int lastPathSize = 0;
    private final GameModel model;
    private JPanel currentWordPanel, historyPanel, targetWordPanel, inputPanel;
    private JPanel keyboardPanel;
    JButton resetButton;
    private final Color CORRECT_COLOR = new Color(106, 170, 100);
    private final Color WRONG_COLOR = new Color(120, 124, 126);
    private JCheckBox showErrorsCheckbox;
    private JCheckBox showPathCheckbox;
    private JCheckBox randomWordsCheckbox;
    public interface KeyListener { void onKeyPressed(char letter); }
    public interface SubmitListener { void onSubmit(); }
    public interface DeleteListener { void onDelete(); }

    private KeyListener keyListener;
    private SubmitListener submitListener;
    private DeleteListener deleteListener;

    public GUIView(GameModel model) {
        this.model = model;
        this.lastPathSize = model.getCurrentPath().size();
        model.addObserver(this);
        initializeUI();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                model.deleteObservers();
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                char c = e.getKeyChar();
                if (Character.isLetter(c)) {
                    keyListener.onKeyPressed(c);
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    submitListener.onSubmit();
                } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    deleteListener.onDelete();
                }
            }
        });
        setFocusable(true);
    }


    private void initializeUI() {
        setTitle("Word Weaver");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));
        getContentPane().setBackground(Color.WHITE);

        // Main word grid
        JPanel mainGrid = new JPanel(new GridLayout(4, 1, 5, 5));
        mainGrid.setBackground(Color.WHITE);

        currentWordPanel = createWordRow(model.getStartWord());
        mainGrid.add(currentWordPanel);

        inputPanel = createWordRow("");
        mainGrid.add(inputPanel);

        historyPanel = new JPanel();
        historyPanel.setLayout(new BoxLayout(historyPanel, BoxLayout.Y_AXIS));
        historyPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(historyPanel);
        scrollPane.setBorder(null);
        mainGrid.add(scrollPane);

        targetWordPanel = createWordRow(model.getTargetWord());
        mainGrid.add(targetWordPanel);

        add(mainGrid, BorderLayout.CENTER);

        keyboardPanel = new JPanel(new BorderLayout(5, 5));
        keyboardPanel.setBackground(WRONG_COLOR);

        JPanel flagPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        flagPanel.setBackground(new Color(240, 240, 240));

        showErrorsCheckbox = new JCheckBox("Show Errors", model.isShowErrors());
        showErrorsCheckbox.addActionListener(e ->
                model.setShowErrors(showErrorsCheckbox.isSelected()));

        showPathCheckbox = new JCheckBox("Show Path", model.isShowPath());
        showPathCheckbox.addActionListener(e ->
                model.setShowPath(showPathCheckbox.isSelected()));

        randomWordsCheckbox = new JCheckBox("Random Words", model.isRandomWords());
        randomWordsCheckbox.addActionListener(e -> {
            // 统一使用setFlags更新所有标志
            model.setFlags(
                    showErrorsCheckbox.isSelected(),
                    showPathCheckbox.isSelected(),
                    randomWordsCheckbox.isSelected()
            );
        });

        flagPanel.add(showErrorsCheckbox);
        flagPanel.add(showPathCheckbox);
        flagPanel.add(randomWordsCheckbox);

        JPanel letterPanel = new JPanel(new GridLayout(3, 10, 3, 3));
        String[] rows = {"QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM"};
        for (String row : rows) {
            JPanel rowPanel = new JPanel(new GridLayout(1, row.length(), 3, 3));
            for (char c : row.toCharArray()) {
                rowPanel.add(createKeyButton(c));
            }
            letterPanel.add(rowPanel);
        }

        JPanel controlPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        JButton enterButton = createControlButton("ENTER", () -> submitListener.onSubmit());
        JButton deleteButton = createControlButton("⌫", () -> deleteListener.onDelete());
        JButton resetButton = createControlButton("NEW GAME", () -> model.setFlags(
                model.isShowErrors(),
                model.isShowPath(),
                true
        ));

        resetButton = createControlButton("NEW GAME", () -> model.setFlags(
                model.isShowErrors(),
                model.isShowPath(),
                true
        ));
        resetButton.setEnabled(false);

        controlPanel.add(deleteButton);
        controlPanel.add(enterButton);
        controlPanel.add(resetButton);

        keyboardPanel.add(flagPanel, BorderLayout.NORTH);
        keyboardPanel.add(letterPanel, BorderLayout.CENTER);
        keyboardPanel.add(controlPanel, BorderLayout.SOUTH);
        add(keyboardPanel, BorderLayout.SOUTH);

        setSize(600, 700);
        setVisible(true);
    }

    private JPanel createWordRow(String word) {
        JPanel panel = new JPanel(new GridLayout(1, 4, 5, 5));
        panel.setBackground(Color.WHITE);
        for (int i = 0; i < 4; i++) {
            JLabel letter = new JLabel("", SwingConstants.CENTER);
            letter.setOpaque(true);
            letter.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
            letter.setFont(new Font("Arial", Font.BOLD, 24));

            if (i < word.length()) {
                char c = word.charAt(i);
                letter.setText(String.valueOf(c).toUpperCase());
                letter.setBackground(word.equals(model.getTargetWord()) ? CORRECT_COLOR : WRONG_COLOR);
            } else {
                letter.setBackground(WRONG_COLOR);
            }
            panel.add(letter);
        }
        return panel;
    }

    private JButton createKeyButton(char c) {
        JButton button = new JButton(String.valueOf(c));
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBackground(WRONG_COLOR);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.addActionListener(e -> keyListener.onKeyPressed(c));
        return button;
    }



    private JButton createControlButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(200, 200, 200));
        button.addActionListener(e -> action.run());
        return button;
    }

    private void updateWordRow(JPanel panel, String word, String target) {
        panel.removeAll();
        for (int i = 0; i < GameModel.WORD_LENGTH; i++) {
            JLabel letter = new JLabel("", SwingConstants.CENTER);
            letter.setOpaque(true);
            letter.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
            letter.setFont(new Font("Arial", Font.BOLD, 24));

            char wordChar = (i < word.length()) ? word.charAt(i) : ' ';
            char targetChar = (i < target.length()) ? target.charAt(i) : ' ';
            boolean isCorrect = Character.toLowerCase(wordChar) == Character.toLowerCase(targetChar);

            if (i < word.length()) {
                letter.setText(String.valueOf(wordChar).toUpperCase());
                letter.setBackground(panel == inputPanel ? Color.WHITE :
                        (isCorrect ? CORRECT_COLOR : WRONG_COLOR));
            } else {
                letter.setBackground(WRONG_COLOR);
            }
            panel.add(letter);
        }
        panel.revalidate();
        panel.repaint();
    }

    private void addHistoryEntry(String previous, String current) {
        JPanel entry = new JPanel(new BorderLayout());
        entry.setBackground(Color.WHITE);

        List<String> changes = new ArrayList<>();
        for (int i = 0; i < GameModel.WORD_LENGTH; i++) {
            char p = previous.charAt(i);
            char c = current.charAt(i);
            if (p != c) changes.add(String.format("%c→%c@%d", p, c, i+1));
        }

        String changeDesc = "No changes";
        if (!changes.isEmpty()) {
            changeDesc = changes.size() == 1 ?
                    "Changed " + changes.get(0) :
                    "Multiple changes: " + String.join(", ", changes);
        }

        JLabel desc = new JLabel(changeDesc + " → " + current.toUpperCase());
        desc.setFont(new Font("Arial", Font.PLAIN, 12));
        entry.add(desc, BorderLayout.NORTH);

        JPanel wordPanel = createWordRow(current);
        entry.add(wordPanel, BorderLayout.CENTER);

        historyPanel.add(entry);
        historyPanel.revalidate();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof GameModel.GameEvent) {
            GameModel.GameEvent event = (GameModel.GameEvent) arg;
            SwingUtilities.invokeLater(() -> {
                switch (event.type) {
                    case ERROR:
                        JOptionPane.showMessageDialog(this,
                                event.message,
                                "Invalid Move",
                                JOptionPane.ERROR_MESSAGE);
                        break;
                    case WIN:
                        int choice = JOptionPane.showOptionDialog(this,
                                event.message + "\nPlay again?",
                                "Congratulations!",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.INFORMATION_MESSAGE,
                                null,
                                new String[]{"New Game", "Quit"},
                                "New Game");
                        if (choice == JOptionPane.YES_OPTION) {
                            model.setFlags(model.isShowErrors(), model.isShowPath(), true);
                        } else {
                            System.exit(0);
                        }
                        break;
                }
            });
        } else {
            SwingUtilities.invokeLater(() -> {
                updateWordRow(inputPanel, model.getInputBuffer(), model.getTargetWord());

                int currentSize = model.getCurrentPath().size();
                if (currentSize > lastPathSize && currentSize >= 2) {
                    String previous = model.getCurrentPath().get(currentSize-2);
                    String current = model.getCurrentPath().get(currentSize-1);
                    addHistoryEntry(previous, current);
                    lastPathSize = currentSize;
                }

                updateWordRow(currentWordPanel, model.getCurrentWord(), model.getTargetWord());
                updateWordRow(targetWordPanel, model.getTargetWord(), model.getTargetWord());

                // Immediate win check for same start/target words
                if (model.getCurrentWord().equals(model.getTargetWord())) {
                    model.notifyWin();
                }
            });
        }
    }
    public void setKeyListener(KeyListener listener) { keyListener = listener; }
    public void setSubmitListener(SubmitListener listener) { submitListener = listener; }
    public void setDeleteListener(DeleteListener listener) { deleteListener = listener; }
}