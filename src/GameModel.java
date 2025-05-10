import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the core logic and state of the Weaver game.
 * @invariant startWord.length() == WORD_LENGTH && targetWord.length() == WORD_LENGTH
 * @invariant dictionary != null && !dictionary.isEmpty()
 * @invariant currentPath != null && currentPath.get(0).equals(startWord)
 */
public class GameModel extends Observable {
    Set<String> getDictionary() { return this.dictionary; }
    public static final int WORD_LENGTH = 4;
    private final Set<String> dictionary;
    private String startWord;
    private String targetWord;
    private final List<String> currentPath = new ArrayList<>();
    private StringBuilder inputBuffer = new StringBuilder();

    private boolean showErrors = true;
    private boolean showPath = false;
    private boolean randomWords = false;
    /**
     * Sets whether to display error messages.
     * @requires true (no specific precondition)
     * @ensures showErrors == newShowErrors
     */
    public void setShowErrors(boolean showErrors) {
        this.showErrors = showErrors;
        notifyStateChange();
    }

    /**
     * Sets whether to show the debug path.
     * @requires true (no specific precondition)
     * @ensures showPath == newShowPath
     */
    public void setShowPath(boolean showPath) {
        this.showPath = showPath;
        notifyStateChange();
    }

    /**
     * Sets whether to use random start/target words.
     * @requires true (no specific precondition)
     * @ensures randomWords == newRandomWords &&
     *          (randomWords changed => words are reinitialized)
     */
    public void setRandomWords(boolean randomWords) {
        if (this.randomWords != randomWords) {
            this.randomWords = randomWords;
            initializeWords();
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

    /**
     * Submits a word to progress the game.
     * @requires word != null && word.length() == WORD_LENGTH
     * @ensures (\result == true) ==> currentPath.contains(word)
     * @ensures (\result == false) ==> currentPath remains unchanged
     * @ensures (\result == true) ==> (word differs by exactly 1 letter from last word)
     */
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

    /**
     * Appends a letter to the input buffer.
     * @requires Character.isLetter(letter) && inputBuffer.length() < WORD_LENGTH
     * @ensures inputBuffer.length() == \old(inputBuffer.length()) + 1
     */
    public void appendToInputBuffer(char letter) {
        if (inputBuffer.length() < WORD_LENGTH) {
            inputBuffer.append(Character.toLowerCase(letter));
            notifyStateChange();
        } else if (showErrors) {
            notifyError("Max 4 letters!");
        }
    }

    /**
     * Deletes the last character from the input buffer.
     * @requires inputBuffer.length() > 0
     * @ensures inputBuffer.length() == \old(inputBuffer.length()) - 1
     */
    public void deleteLastInput() {
        if (inputBuffer.length() > 0) {
            inputBuffer.deleteCharAt(inputBuffer.length()-1);
            notifyStateChange();
        }
    }

    /**
     * Sets multiple flags simultaneously.
     * @requires true (no specific precondition)
     * @ensures showErrors == newShowErrors && showPath == newShowPath
     *          && randomWords == newRandomWords
     * @ensures (randomWords changed => words are reinitialized)
     */
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