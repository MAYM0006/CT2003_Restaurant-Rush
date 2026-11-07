import restaurantrush.ui.RestaurantRushCLI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Regression checks for end-game messages and Stage 3 CLI prompt sequencing. */
public class Stage3OutputChecks {
    public static void main(String[] args) throws Exception {
        String defeat = runScript("tests/defeat-stage3-input.txt");
        String defeatResult = finalResult(defeat);
        require(defeat.contains("TURN 18 / 18"), "Defeat run did not reach turn 18");
        require(defeatResult.contains("Average Satisfaction: N/A (Target: 60.0)"),
                "Defeat result must display N/A when nobody has paid");
        require(defeatResult.contains("DEFEAT!"), "Defeat result missing");

        String victory = runScript("tests/victory-stage3-input.txt");
        String victoryResult = finalResult(victory);
        require(victory.contains("TURN 18 / 18"), "Victory run did not reach turn 18");
        require(victoryResult.contains("Average Satisfaction: 80.3 (Target: 60.0)"),
                "Victory satisfaction changed unexpectedly");
        require(victoryResult.contains("VICTORY!"), "Victory result missing");
        require(victory.contains("Order placed at Table"),
                "CLI must announce the chosen dish in the order-placement turn");

        int host = victory.indexOf("Host Action [");
        int waiter = victory.indexOf("Waiter Action [");
        int chef = victory.indexOf("Chef Action [");
        require(host >= 0 && host < waiter && waiter < chef,
                "Stage 3 prompts must be Host, then Waiter, then Chef");

        System.out.println("Stage 3 output checks: PASS (end results, order announcement, prompt order)");
    }

    private static String runScript(String filename) throws Exception {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        try {
            String input = Files.readString(Path.of(filename), StandardCharsets.UTF_8);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            RestaurantRushCLI.main(new String[0]);
            return output.toString(StandardCharsets.UTF_8);
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    private static String finalResult(String output) {
        int index = output.lastIndexOf("GAME OVER RESULTS");
        require(index >= 0, "Missing game-over results screen");
        return output.substring(index);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
