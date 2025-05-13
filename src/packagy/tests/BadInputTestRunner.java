package packagy.tests;

import packagy.TextFileReader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static packagy.TextFileReader.*;

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
        testCases.put("empty.txt", ERROR_NO_ENTRY_POINTS);
        testCases.put("ERROR_NO_INTERSECTIONS.txt", ERROR_NO_INTERSECTIONS);
        testCases.put("ERROR_NO_ENTRY_POINTS.txt", ERROR_NO_ENTRY_POINTS);
        testCases.put("ERROR_COORDINATES_TOO_CLOSE.txt", ERROR_COORDINATES_TOO_CLOSE);
        testCases.put("ERROR_DUPLICATE_ENTRY_POINT_NAME.txt", ERROR_DUPLICATE_ENTRY_POINT_NAME);
        testCases.put("ERROR_DUPLICATE_INTERSECTION_NAME.txt", ERROR_DUPLICATE_INTERSECTION_NAME);
        testCases.put("ERROR_DUPLICATE_LOCATIONS.txt", ERROR_DUPLICATE_LOCATIONS);
        testCases.put("ERROR_ENTRY_POINT_NAME_TOO_LONG.txt", ERROR_ENTRY_POINT_NAME_TOO_LONG);
        testCases.put("ERROR_INTERSECTION_NAME_TOO_LONG.txt", ERROR_INTERSECTION_NAME_TOO_LONG);
        testCases.put("ERROR_INVALID_COORDINATE_COMPONENT.txt", ERROR_INVALID_COORDINATE_COMPONENT);
        testCases.put("ERROR_INVALID_ENTRY_POINT_REFERENCE.txt", ERROR_INVALID_ENTRY_POINT_REFERENCE);
        testCases.put("ERROR_INVALID_GENERAL_FREQUENCY.txt", ERROR_INVALID_GENERAL_FREQUENCY);
        testCases.put("ERROR_INVALID_INTERSECTION_FORMAT.txt", ERROR_INVALID_INTERSECTION_FORMAT);
        testCases.put("ERROR_INVALID_INTERSECTION_REFERENCE.txt", ERROR_INVALID_INTERSECTION_REFERENCE);
        testCases.put("ERROR_INVALID_MAX_TIME.txt", ERROR_INVALID_MAX_TIME);
        testCases.put("ERROR_INVALID_PROBABILITY.txt", ERROR_INVALID_PROBABILITY);
        testCases.put("ERROR_INVALID_PROBABILITY_SUM.txt", ERROR_INVALID_PROBABILITY_SUM);
        testCases.put("ERROR_MULTIPLE_TIMESPAN_SECTIONS.txt", ERROR_MULTIPLE_TIMESPAN_SECTIONS);
        testCases.put("ERROR_NOT_UTF8.jpeg", ERROR_NOT_UTF8);

        boolean allTestsPassed = true;

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
                    allTestsPassed = false;
                    System.err.println("FAILED: Exception message does not match for file: " + fileName);
                    System.err.println("Expected: " + expectedMessage);
                    System.err.println("Actual: " + e.getMessage());
                }
            } catch (IOException e) {
                System.err.println("ERROR: IOException occurred for file: " + fileName);
                e.printStackTrace();
            }
        }

        if (allTestsPassed) {
            System.out.println("\n All tests passed!");
        } else {
            System.err.println("Some tests failed. Please check the output.");
        }
    }
}