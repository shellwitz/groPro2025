package trafficsimulation.tests;

import trafficsimulation.TextFileReader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static trafficsimulation.TextFileReader.*;

public class BadInputTestRunner {

    public static void main(String[] args) {
        String badInputDirPath;
        if(args.length > 0){
            badInputDirPath = args[0];
        }else{
            badInputDirPath = "badInputTests";
        }
        File badInputDir = new File(badInputDirPath);

        if (!badInputDir.exists() || !badInputDir.isDirectory()) {
            System.err.println("Directory does not exist: " + badInputDirPath);
            return;
        }

        //define test files and their expected error messages
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
        testCases.put("ERROR_INVALID_INTERSECTION_REFERENCE.txt", ERROR_INVALID_INTERSECTION_REFERENCE);
        testCases.put("ERROR_INVALID_MAX_TIME.txt", ERROR_INVALID_MAX_TIME);
        testCases.put("ERROR_INVALID_PROBABILITY.txt", ERROR_INVALID_PROBABILITY);
        testCases.put("ERROR_MULTIPLE_TIMESPAN_SECTIONS.txt", ERROR_MULTIPLE_TIMESPAN_SECTIONS);
        testCases.put("ERROR_NOT_UTF8.jpeg", ERROR_NOT_UTF8);
        testCases.put("nonExistentFilePath", ERROR_INVALID_FILE_PATH);
        testCases.put("ERROR_TOO_MANY_CONNECTED_STREETS.txt", ERROR_TOO_MANY_CONNECTED_STREETS);
        testCases.put("ERROR_LOCATION_PROBABILITY_PAIRS_EXPECTED.txt", ERROR_LOCATION_PROBABILITY_PAIRS_EXPECTED);
        testCases.put("ERROR_TOO_FEW_CONNECTED_STREETS.txt", ERROR_TOO_FEW_CONNECTED_STREETS);
        testCases.put("ERROR_DUPLICATE_TURNOFF_NAME.txt", ERROR_DUPLICATE_TURNOFF_NAME);

        boolean allTestsPassed = true;

        for (Map.Entry<String, String> testCase : testCases.entrySet()) {
            String fileName = testCase.getKey();
            String expectedMessage = testCase.getValue();


            System.out.println("Testing file: " + fileName);

            try {
                TextFileReader reader = new TextFileReader(badInputDirPath + File.separator + fileName);
                reader.read();
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
            }
        }

        if (allTestsPassed) {
            System.out.println("\n All tests passed!");
        } else {
            System.err.println("Some tests failed. Please check the output.");
        }
    }
}