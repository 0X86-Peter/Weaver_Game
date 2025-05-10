import java.util.Scanner;

// CLI.java
public class CLI {
    private GameModel model;

    public CLI(GameModel model) {
        this.model = model;
        startGame();
    }

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