package packagy.tests;

import packagy.TextFileReader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BadInputTestRunner {

    public static void main(String[] args) {
        String badInputDirPath = "badInputTests";
        File badInputDir = new File(badInputDirPath);

        // Ensure the directory exists
        if (!badInputDir.exists() || !badInputDir.isDirectory()) {
            System.err.println("Directory does not exist: " + badInputDirPath);
            return;
        }

        // Define test files and their expected error messages
        Map<String, String> testCases = new HashMap<>();
        testCases.put("NotUnique.txt", "Locations cannot be both entry points and intersections.");


        // Iterate through all test cases
        for (Map.Entry<String, String> testCase : testCases.entrySet()) {
            String fileName = testCase.getKey();
            String expectedMessage = testCase.getValue();

            File testFile = new File(badInputDir, fileName);
            if (!testFile.exists()) {
                System.err.println("ERROR: Test file not found: " + fileName);
                continue;
            }

            System.out.println("Testing file: " + fileName);

            try {
                TextFileReader.readFromFile(testFile.getAbsolutePath());
                System.err.println("ERROR: No exception thrown for file: " + fileName);
            } catch (IllegalArgumentException e) {
                if (e.getMessage().startsWith(expectedMessage)) {
                    System.out.println("PASSED: Exception message matches for file: " + fileName);
                } else {
                    System.err.println("FAILED: Exception message does not match for file: " + fileName);
                    System.err.println("Expected: " + expectedMessage);
                    System.err.println("Actual: " + e.getMessage());
                }
            } catch (IOException e) {
                System.err.println("ERROR: IOException occurred for file: " + fileName);
                e.printStackTrace();
            }
        }
    }
}