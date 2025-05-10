import java.util.Scanner;

/**
 * Command-line interface (CLI) implementation for the Weaver game.
 * @invariant model != null
 */
public class CLI {
    private GameModel model;
    /**
     * Initializes the CLI with the specified game model and starts the game.
     * @requires model != null
     * @ensures this.model == model && game loop is running
     */
    public CLI(GameModel model) {
        this.model = model;
        startGame();
    }
    /**
     * Starts the game loop and handles user input.
     * @requires model.isInitialized()
     * @ensures Game progresses according to user input until win/exit
     */
    private void startGame() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Start Word: " + model.getStartWord().toUpperCase());
        System.out.println("Target Word: " + model.getTargetWord().toUpperCase());

        while (true) {
            System.out.print("Enter next word: ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("exit")) break;

            boolean success = model.submitWord(input);
            if (model.getCurrentWord().equals(model.getTargetWord())) {
                System.out.println("Congratulations! You won!");
                break;
            }
            if (!success && model.isShowErrors()) {
                System.out.println("Invalid move. Try again.");
            }
        }
        scanner.close();
    }
    /**
     * Entry point for CLI version.
     * @requires args may contain valid flags: "-showErrors", "-showPath", "-randomWords"
     * @ensures Game starts with specified flags
     */
    public static void main(String[] args) {
        GameModel model = new GameModel();
        for (String arg : args) {
            if (arg.equals("-showErrors")) model.setShowErrors(true);
            if (arg.equals("-showPath")) model.setShowPath(true);
            if (arg.equals("-randomWords")) model.setRandomWords(true);
        }
        new CLI(model);
    }
}