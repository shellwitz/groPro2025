package trafficsimulation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class PathValidator {

    public static final String PROMPT_ENTER_PATH = "Bitte geben Sie einen gültigen Dateipfad zu einer existierenden Datei ein: ";
    public static final String MESSAGE_PATH_VALID = "Der eingegebene Pfad ist gültig und verweist auf eine Datei.";
    public static final String MESSAGE_PATH_INVALID = "Ungültiger Pfad oder der Pfad verweist nicht auf eine Datei. Bitte versuchen Sie es erneut.";

    public static String getValidPath() {
        String inputPath;

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print(PROMPT_ENTER_PATH);
                inputPath = scanner.nextLine();

                if (Files.exists(Path.of(inputPath)) && Files.isRegularFile(Path.of(inputPath))) {
                    System.out.println(MESSAGE_PATH_VALID);
                    break;
                } else {
                    System.out.println(MESSAGE_PATH_INVALID);
                }
            }
        }

        return inputPath;
    }
}
