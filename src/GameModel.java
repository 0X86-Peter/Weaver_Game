import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

public class GameModel extends Observable {
    public static final int WORD_LENGTH = 4;
    private final Set<String> dictionary;
    private String startWord;
    private String targetWord;
    private final List<String> currentPath = new ArrayList<>();
    private StringBuilder inputBuffer = new StringBuilder();

    private boolean showErrors = true;
    private boolean showPath = false;
    private boolean randomWords = false;
    public void setShowErrors(boolean showErrors) {
        this.showErrors = showErrors;
        notifyStateChange();
    }

    public void setShowPath(boolean showPath) {
        this.showPath = showPath;
        notifyStateChange();
    }

    public void setRandomWords(boolean randomWords) {
        if (this.randomWords != randomWords) {
            this.randomWords = randomWords;
            initializeWords(); // 重新生成单词
        }
        notifyStateChange();
    }
    private List<String> debugPath = new ArrayList<>();

    public List<String> getDebugPath() {
        return showPath ? debugPath : Collections.emptyList();
    }

    public static class GameEvent {
        public enum Type { ERROR, WIN }
        public final Type type;
        public final String message;

        public GameEvent(Type type, String message) {
            this.type = type;
            this.message = message;
        }
    }

    public GameModel() {
        this.dictionary = DictionaryLoader.load("dictionary.txt");
        initializeWords();
    }

    private void initializeWords() {
        if (randomWords && !dictionary.isEmpty()) {
            List<String> validWords = new ArrayList<>(dictionary).stream()
                    .filter(w -> w.length() == WORD_LENGTH)
                    .collect(Collectors.toList());

            do {
                Collections.shuffle(validWords);
                startWord = validWords.get(0);
                targetWord = validWords.size() > 1 ? validWords.get(1) : startWord;
            } while (startWord.equals(targetWord));
        } else {
            startWord = "east";
            targetWord = "west";
        }
        resetGameState();
    }

    private void resetGameState() {
        currentPath.clear();
        currentPath.add(startWord);
        inputBuffer.setLength(0);
        notifyStateChange();
    }

    public void submitInputBuffer() {
        String word = inputBuffer.toString().toLowerCase();
        inputBuffer.setLength(0);
        notifyStateChange();

        if (word.length() != WORD_LENGTH) {
            if (showErrors) notifyError("Word must be 4 letters!");
            return;
        }
        submitWord(word);
    }

    public boolean submitWord(String word) {
        assert word != null : "Word cannot be null";
        assert word.length() == WORD_LENGTH : "Word must be 4 letters";
        word = word.trim().toLowerCase();

        if (word.length() != WORD_LENGTH) {
            notifyError("4 letters required!");
            return false;
        }
        if (!dictionary.contains(word)) {
            notifyError(word + " not in dictionary!");
            return false;
        }
        if (word.equals(getCurrentWord())) {
            notifyError("Same as current word!");
            return false;
        }
        if (!isOneLetterDifferent(word)) {
            notifyError("Change exactly 1 letter!");
            return false;
        }

        currentPath.add(word);
        if (showPath) debugPath.add(word);
        notifyStateChange();

        if (word.equals(targetWord)) {
            notifyWin();
        }
        return true;
    }

    private boolean isOneLetterDifferent(String newWord) {
        String lastWord = getCurrentWord();
        int diffCount = 0;
        for (int i = 0; i < WORD_LENGTH; i++) {
            if (lastWord.charAt(i) != newWord.charAt(i)) {
                if (++diffCount > 1) return false;
            }
        }
        return diffCount == 1;
    }

    private void notifyError(String message) {
        SwingUtilities.invokeLater(() -> {
            setChanged();
            notifyObservers(new GameEvent(GameEvent.Type.ERROR, message));
        });
    }

    void notifyWin() {
        SwingUtilities.invokeLater(() -> {
            setChanged();
            notifyObservers(new GameEvent(GameEvent.Type.WIN,
                    "You won! Target: " + targetWord.toUpperCase()));
        });
    }

    private void notifyStateChange() {
        SwingUtilities.invokeLater(() -> {
            setChanged();
            notifyObservers();
        });
    }

    // Getters
    public String getStartWord() { return startWord; }
    public String getTargetWord() { return targetWord; }
    public List<String> getCurrentPath() { return Collections.unmodifiableList(currentPath); }
    public String getCurrentWord() { return currentPath.isEmpty() ? "" : currentPath.get(currentPath.size()-1); }
    public String getInputBuffer() { return inputBuffer.toString().toUpperCase(); }

    public void appendToInputBuffer(char letter) {
        if (inputBuffer.length() < WORD_LENGTH) {
            inputBuffer.append(Character.toLowerCase(letter));
            notifyStateChange();
        } else if (showErrors) {
            notifyError("Max 4 letters!");
        }
    }

    public void deleteLastInput() {
        if (inputBuffer.length() > 0) {
            inputBuffer.deleteCharAt(inputBuffer.length()-1);
            notifyStateChange();
        }
    }

    public void setFlags(boolean showErrors, boolean showPath, boolean randomWords) {
        boolean wasRandom = this.randomWords;

        this.showErrors = showErrors;
        this.showPath = showPath;
        this.randomWords = randomWords;
        if (wasRandom != randomWords) {
            initializeWords();
        }
        notifyStateChange();
    }

    public boolean isShowErrors() { return showErrors; }
    public boolean isShowPath() { return showPath; }
    public boolean isRandomWords() { return randomWords; }
}