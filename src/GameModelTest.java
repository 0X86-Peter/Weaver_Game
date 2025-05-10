import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameModelTest {
    private GameModel model;

    @BeforeEach
    void setUp() {
        model = new GameModel();
        model.setFlags(false, false, false); // Fixed words: "east" → "west"
    }

    /**
     * Scenario 1: Valid word submission with one-letter difference.
     * @requires currentWord = "east", input = "vast" (differs by one letter, valid in dictionary)
     * @ensures submitWord("vast") → true, currentWord becomes "vast"
     */
    @Test
    void submitWord_WhenWordDiffersByOneLetter_UpdatesCurrentPath() {
        // Precondition check
        assertEquals("east", model.getCurrentWord(), "Initial word must be 'east'");
        assertTrue(model.getDictionary().contains("vast"), "'vast' must exist in dictionary");

        // Action & Assertion
        assertTrue(model.submitWord("vast"), "Valid submission should return true");
        assertEquals("vast", model.getCurrentWord(), "Current word should update to 'vast'");
        assertEquals(2, model.getCurrentPath().size(), "Path length should increase by 1");
    }

    /**
     * Scenario 2: Invalid submission with multiple letter changes.
     * @requires currentWord = "east", input = "nest" (differs by two letters)
     * @ensures submitWord("nest") → false, currentPath remains unchanged
     */
    @Test
    void submitWord_WhenWordDiffersByMultipleLetters_RejectsSubmission() {
        // Precondition check
        assertEquals("east", model.getCurrentWord(), "Initial word must be 'east'");
        assertTrue(model.getDictionary().contains("nest"), "'nest' must exist in dictionary");

        // Action & Assertion
        assertFalse(model.submitWord("nest"), "Invalid submission should return false");
        assertEquals("east", model.getCurrentWord(), "Current word should remain 'east'");
        assertEquals(1, model.getCurrentPath().size(), "Path length should not change");
    }

    /**
     * Scenario 3: Enabling random words flag.
     * @requires randomWords flag is false initially
     * @ensures setRandomWords(true) → startWord not "east", targetWord not "west"
     */
    @Test
    void setRandomWords_WhenEnabled_GeneratesNewStartAndTargetWords() {
        // Precondition check
        assertFalse(model.isRandomWords(), "randomWords flag should be false initially");

        // Action
        model.setFlags(false, false, true);

        // Postcondition assertion
        assertNotEquals("east", model.getStartWord(), "Start word should be randomized");
        assertNotEquals("west", model.getTargetWord(), "Target word should be randomized");
        assertTrue(model.getDictionary().contains(model.getStartWord()), "Start word must be valid");
        assertTrue(model.getDictionary().contains(model.getTargetWord()), "Target word must be valid");
    }
}