package trafficsimulation;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class TextFileReader implements Reader {

    public static final String ERROR_NOT_UTF8                     = "Eingabedatei ist nicht UTF-8 kodiert: ";
    public static final String ERROR_MULTIPLE_TIMESPAN_SECTIONS   = "Mehrere 'Zeitraum:' Abschnitte sind nicht erlaubt.";
    public static final String ERROR_MULTIPLE_ENTRYPOINT_SECTIONS = "Mehrere 'Einfallspunkte:' Abschnitte sind nicht erlaubt.";
    public static final String ERROR_MULTIPLE_CROSSING_SECTIONS   = "Mehrere 'Kreuzungen:' Abschnitte sind nicht erlaubt.";
    public static final String ERROR_INVALID_TIMESPAN_FORMAT    = "Ungültiges Format für 'Zeitraum:'. Erwartet werden zwei Werte.";
    public static final String ERROR_NO_ENTRY_POINTS         = "Keine Einfallspunkte gefunden.";
    public static final String ERROR_NO_INTERSECTIONS = "Keine Kreuzungen gefunden.";
    public static final String ERROR_DUPLICATE_LOCATIONS = "Orte können nicht gleichzeitig Einfallspunkte und Kreuzungen sein.";
    public static final String ERROR_INVALID_ENTRY_POINT_REFERENCE = "Nicht alle Referenzen von Einfallspunkten existieren tatsächlich.";
    public static final String ERROR_INVALID_INTERSECTION_REFERENCE = "Nicht alle Referenzen von Kreuzungen existieren tatsächlich.";
    public static final String ERROR_INVALID_MAX_TIME = "Ungültiger Wert für die Simulationszeitspanne: ";
    public static final String ERROR_INVALID_GENERAL_FREQUENCY    = "Ungültiger Wert für allgemeine Taktrate: ";
    public static final String ERROR_COORDINATES_TOO_CLOSE        = "Koordinaten sind zu nah beieinander: ";
    public static final String ERROR_INVALID_COORDINATE_COMPONENT = "Ungültiger Wert für Koordinatenkomponente: ";
    public static final String ERROR_INVALID_PROBABILITY = "Ungültiger Wert für Wahrscheinlichkeit: ";
    public static final String ERROR_INVALID_ENTRY_POINT_FORMAT = "Ungültiges Format für Einfallspunkt: ";
    public static final String ERROR_DUPLICATE_ENTRY_POINT_NAME = "Doppelter Einfallspunktname: ";
    public static final String ERROR_LOCATION_PROBABILITY_PAIRS_EXPECTED = "Ungültiges Format für Kreuzung, alle Ortsangaben werden mit relativen Wahrscheinlichkeiten erwartet: ";
    public static final String ERROR_TOO_FEW_CONNECTED_STREETS   = "Diese Kreuzung hat eventuell zu wenig verbindende Straßen: ";
    public static final String ERROR_DUPLICATE_INTERSECTION_NAME = "Doppelter Kreuzungsname: ";
    public static final String ERROR_ENTRY_POINT_NAME_TOO_LONG = "Einfallspunktname ist zu lang: ";
    public static final String ERROR_INTERSECTION_NAME_TOO_LONG = "Kreuzungsname ist zu lang: ";
    public static final String ERROR_INVALID_FILE_PATH = "Der angegebene Dateipfad existiert nicht oder ist keine gültige Datei: ";
    public static final String ERROR_TOO_MANY_CONNECTED_STREETS = "Diese Kreuzung hat eventuell mehr als 20 verbindende Straßen: ";
    public static final String ERROR_DUPLICATE_TURNOFF_NAME = "Doppelter Abbiegepunktname: ";

    private static final int MAX_CONNECTED_STREETS = 43;
    private static final int MIN_CONNECTED_STREETS = 7;
    private static final int EXPECTED_ODD_PARTS = 1;

    private static final int ENTRY_POINT_PARTS_COUNT = 5;
    private static final int ENTRY_POINT_NAME_MAX_LENGTH = 100;
    private static final int ENTRY_POINT_X_INDEX = 1;
    private static final int ENTRY_POINT_Y_INDEX = 2;
    private static final int ENTRY_POINT_DEST_INDEX = 3;
    private static final int ENTRY_POINT_FREQ_INDEX = 4;

    private final String filePath;

    public TextFileReader(String filePath) {
        validateFilePath(filePath);
        this.filePath = filePath;
    }

    @Override
    public CityDTO read() {
        if (!isUTF8Encoded(filePath)) {
            throw new IllegalArgumentException(ERROR_NOT_UTF8 + filePath);
        }

        HashMap<String, EntryPoint> entryPoints = new HashMap<>();
        HashMap<String, Intersection> intersections = new HashMap<>();
        HashMap<DirectedEdge, DirectedEdgeInfo> directedEdges = new HashMap<>();
        ArrayList<Coord> alreadyExistingCoords = new ArrayList<>();

        HashSet<String> referencesMadeByEntryPoints = new HashSet<>();
        HashSet<String> referencesMadeByIntersections = new HashSet<>();

        int clockrate = 0;
        int maxTime = 0;

        boolean[] hasZeitraumEinfallspunkteKreuzungen = {false, false, false};

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            String section = "";
            while ((line = br.readLine()) != null) {

                line = stripComment(line);
                if (!line.isEmpty()) {

                    if (line.equalsIgnoreCase("Zeitraum:") || line.equalsIgnoreCase("Einfallspunkte:") || line.equalsIgnoreCase("Kreuzungen:")) {
                  
                    checkAndSetZeitraumEinfallspunkteKreuzungenSections(line, hasZeitraumEinfallspunkteKreuzungen);
                    
                    section = line;
                    } else {
                        switch (section) {
                            case "Zeitraum:":
                                String[] timeParts = line.split("\\s+");

                                if (timeParts.length != 2) {
                                    throw new IllegalArgumentException(ERROR_INVALID_TIMESPAN_FORMAT);
                                }

                                maxTime = checkMaxTime(timeParts[0]);
                                clockrate = checkGeneralFrequency(timeParts[1], maxTime);
                                break;

                            case "Einfallspunkte:":
                                parseEntryPoint(line, entryPoints, alreadyExistingCoords, referencesMadeByEntryPoints);
                                break;

                            case "Kreuzungen:":
                                parseIntersection(line, intersections, directedEdges, alreadyExistingCoords, referencesMadeByIntersections);
                                break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading file", e);
        }

        validateCityData(entryPoints, intersections, referencesMadeByEntryPoints, referencesMadeByIntersections);

        return new CityDTO(entryPoints, intersections, directedEdges, clockrate, maxTime);
    }

    private static void checkAndSetZeitraumEinfallspunkteKreuzungenSections(String line, boolean[] hasZeitraumEinfallspunkteKreuzungen){
        checkLine(line, hasZeitraumEinfallspunkteKreuzungen);
        setZeitraumEinfallspunkteKreuzungenIfFound(line, hasZeitraumEinfallspunkteKreuzungen);
    }

    private static void setZeitraumEinfallspunkteKreuzungenIfFound(String line, boolean[] hasZeitraumEinfallspunkteKreuzungen){
        if (line.equalsIgnoreCase("Zeitraum:")) hasZeitraumEinfallspunkteKreuzungen[0] = true;
        if (line.equalsIgnoreCase("Einfallspunkte:")) hasZeitraumEinfallspunkteKreuzungen[1] = true;
        if (line.equalsIgnoreCase("Kreuzungen:")) hasZeitraumEinfallspunkteKreuzungen[2] = true;
    }

    private static void checkLine(String line, boolean[] hasZeitraumEinfallspunkteKreuzungen) {
         if (line.equalsIgnoreCase("Zeitraum:") && hasZeitraumEinfallspunkteKreuzungen[0]) {
                            throw new IllegalArgumentException(ERROR_MULTIPLE_TIMESPAN_SECTIONS);
                        } else if (line.equalsIgnoreCase("Einfallspunkte:") && hasZeitraumEinfallspunkteKreuzungen[1]) {
                            throw new IllegalArgumentException(ERROR_MULTIPLE_ENTRYPOINT_SECTIONS);
                        } else if (line.equalsIgnoreCase("Kreuzungen:") && hasZeitraumEinfallspunkteKreuzungen[2]) {
                            throw new IllegalArgumentException(ERROR_MULTIPLE_CROSSING_SECTIONS);
                        }
    }

    private static void validateFilePath(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException(ERROR_INVALID_FILE_PATH + filePath);
        }
    }

    private static boolean isUTF8Encoded(String filePath) {
        try {
            byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
            Charset.availableCharsets().get(StandardCharsets.UTF_8.name()).newDecoder().decode(java.nio.ByteBuffer.wrap(fileBytes));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static String stripComment(String line) {
        int commentIndex = line.indexOf('#');
        if (commentIndex != -1) {
            return line.substring(0, commentIndex).trim(); // remove everything after # and trim whitespace
        }
        return line.trim(); // return the line as is if no # is found
    }

    private static int checkMaxTime(String potentialMaxTime) {
        if (potentialMaxTime.contains(".")) {
            throw new IllegalArgumentException(ERROR_INVALID_MAX_TIME + potentialMaxTime + ". Dezimalwerte sind nicht erlaubt.");
        }

        try {
            int maxTime = Integer.parseInt(potentialMaxTime);
            if (maxTime <= 0) {
                throw new IllegalArgumentException(ERROR_INVALID_MAX_TIME + potentialMaxTime + ". Erwartet wird eine positive Ganzzahl.");
            }

            if (maxTime > 24 * 60 * 60) {
                throw new IllegalArgumentException(ERROR_INVALID_MAX_TIME + potentialMaxTime + ". Erwartet wird ein Wert kleiner als 24 Stunden in Sekunden.");
            }

            return maxTime;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(ERROR_INVALID_MAX_TIME + potentialMaxTime + ". Erwartet wird eine Ganzzahl.");
        }
    }

    private static void checkWhetherCoordIsFarEnough(List<Coord> alreadyExistingCoords, Coord newCord) {
        for (Coord existingCoord : alreadyExistingCoords) {
            if (Coord.distance(existingCoord, newCord) < 0.1) { //0.1*100m Steets should be apart at least 10m
                throw new IllegalArgumentException(ERROR_COORDINATES_TOO_CLOSE + existingCoord + " und " + newCord);
            }
        }
        alreadyExistingCoords.add(newCord);
    }

    private static int checkGeneralFrequency(String potentialGeneralFrequency, int maxTime) {
        if (potentialGeneralFrequency.contains(".")) {
            throw new IllegalArgumentException(ERROR_INVALID_GENERAL_FREQUENCY + potentialGeneralFrequency + ". Dezimalwerte sind nicht erlaubt.");
        }

        try {
            int generalFrequency = Integer.parseInt(potentialGeneralFrequency);
            if (generalFrequency <= 0) {
                throw new IllegalArgumentException(ERROR_INVALID_GENERAL_FREQUENCY + potentialGeneralFrequency + ". Erwartet wird eine positive Ganzzahl.");
            }

            if (generalFrequency > maxTime) {
                throw new IllegalArgumentException(ERROR_INVALID_GENERAL_FREQUENCY + potentialGeneralFrequency + ". Erwartet wird ein Wert kleiner als die Simulationszeitspanne.");
            }

            return generalFrequency;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(ERROR_INVALID_GENERAL_FREQUENCY + potentialGeneralFrequency + ". Erwartet wird eine Ganzzahl.");
        }
    }

    private static void parseEntryPoint(String line, Map<String, EntryPoint> entryPoints, List<Coord> coordChecker, HashSet<String> referencesMadeByEntryPoints) {
        // Format: Name X Y Destination Frequency
        String[] parts = line.split("\\s+");

        if (parts.length != ENTRY_POINT_PARTS_COUNT) { //if the length is not 5 the format of EntryPoint coordinate x y destination frequency is invalid
            throw new IllegalArgumentException(ERROR_INVALID_ENTRY_POINT_FORMAT + line);
        }

        String name = parts[0];

        if (name.length() > ENTRY_POINT_NAME_MAX_LENGTH) {
            throw new IllegalArgumentException(ERROR_ENTRY_POINT_NAME_TOO_LONG + name);
        }

        if (entryPoints.containsKey(name)) {
            throw new IllegalArgumentException(ERROR_DUPLICATE_ENTRY_POINT_NAME + name);
        }

        double x = checkCoordinateComponent(parts[ENTRY_POINT_X_INDEX]);
        double y = checkCoordinateComponent(parts[ENTRY_POINT_Y_INDEX]);

        Coord epCoord = new Coord(x, y);

        checkWhetherCoordIsFarEnough(coordChecker, epCoord);

        String destination = parts[ENTRY_POINT_DEST_INDEX];

        referencesMadeByEntryPoints.add(destination);

        int freq = Integer.parseInt(parts[ENTRY_POINT_FREQ_INDEX]);

        EntryPoint ep = new EntryPoint();
        ep.coord = new Coord(x, y);
        ep.freq = freq;
        ep.intersectionName = destination;

        entryPoints.put(name, ep);
    }

    private static double checkCoordinateComponent(String potentialCoordinateComponent) {

        try {
            double ret = Double.parseDouble(potentialCoordinateComponent);
            if (Math.abs(ret) > 1000) {
                throw new IllegalArgumentException(ERROR_INVALID_COORDINATE_COMPONENT + potentialCoordinateComponent + ". Erwartet wird eine Zahl zwischen -1000 und 1000.");
            }
            return ret;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(ERROR_INVALID_COORDINATE_COMPONENT + potentialCoordinateComponent + ". Erwartet wird eine Zahl mit einem Dezimalpunkt als Trennzeichen.");
        }
    }

    private static double checkProbability (String potentialProbability) {
        try {
            double ret = Double.parseDouble(potentialProbability);
            if (ret < Math.pow(10, -6) || ret > Math.pow(10, 6)) { //values smaller or greater can lead to a loss of precision
                throw new IllegalArgumentException(ERROR_INVALID_PROBABILITY + potentialProbability + ". Erwartet wird eine Zahl zwischen 0.000001 und 1 000 000.");
            }
            return ret;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(ERROR_INVALID_PROBABILITY + potentialProbability + ". Erwartet wird eine Zahl mit einem Dezimalpunkt als Trennzeichen.");
        }
    }

    private static void parseIntersection(String line, Map<String, Intersection> intersections,
                                          Map<DirectedEdge, DirectedEdgeInfo> directedEdges, List<Coord> coordChecker, HashSet<String> referencesMadeByIntersections) {
        // Format: Name X Y Target1 Probability1 Target2 Probability2 ...
        String[] parts = line.split("\\s+");
        String name = parts[0];

        validateIntersectionInput(name, parts, intersections, line);

        double x = checkCoordinateComponent(parts[1]);
        double y = checkCoordinateComponent(parts[2]);

        List<String> dests = new ArrayList<>();
        List<Double> probs = new ArrayList<>();

        for (int i = 3; i < parts.length; i += 2) {
            String dest = parts[i];
            double relProb = checkProbability (parts[i + 1]);

            //linear search through the line doesnt really matter as only up to 20 destinations are allowed
            if(dests.contains(dest)) {
                throw new IllegalArgumentException(ERROR_DUPLICATE_TURNOFF_NAME + line);
            }
            dests.add(dest);
            probs.add(relProb);

            // adding DirectedEdges in both directions
            DirectedEdge forward = new DirectedEdge(name, dest);
            DirectedEdge reverse = new DirectedEdge(dest, name);

            directedEdges.putIfAbsent(forward, new DirectedEdgeInfo());
            directedEdges.putIfAbsent(reverse, new DirectedEdgeInfo());
        }

        processIntersection(name, x, y, probs, dests, coordChecker, referencesMadeByIntersections, intersections);
    }

    private static void processIntersection(String name, double x, double y, List<Double> probs, List<String> dests,
                                            List<Coord> coordChecker, HashSet<String> referencesMadeByIntersections,
                                            Map<String, Intersection> intersections) {
        final double total = calculateTotalProbability(probs);

        for (int i = 0; i < probs.size(); i++) {
            probs.set(i, probs.get(i) / total);
        }

        Coord intersectionCoord = new Coord(x, y);

        checkWhetherCoordIsFarEnough(coordChecker, intersectionCoord);

        referencesMadeByIntersections.addAll(dests);

        Intersection inter = new Intersection();
        inter.coord = intersectionCoord;

        double[] probabilitiesArray = new double[probs.size()];
        for (int i = 0; i < probs.size(); i++) {
            probabilitiesArray[i] = probs.get(i);
        }

        inter.namesAndProbabilities = new NamesAndProbabilities(
            dests.toArray(new String[0]),
            probabilitiesArray
        );

        intersections.put(name, inter);
    }

    private static double calculateTotalProbability(List<Double> probabilities) {
        double sum = 0;
        for (Double prob : probabilities) {
            sum += prob;
        }
        return sum;
    }

    private static void validateIntersectionInput(String name, String[] parts, Map<String, Intersection> intersections, String line) {
        if (intersections.containsKey(name)) {
            throw new IllegalArgumentException(ERROR_DUPLICATE_INTERSECTION_NAME + name);
        }

        if (name.length() > 100) {
            throw new IllegalArgumentException(ERROR_INTERSECTION_NAME_TOO_LONG + name);
        }

        if (parts.length < MIN_CONNECTED_STREETS) {
            throw new IllegalArgumentException(ERROR_TOO_FEW_CONNECTED_STREETS + line);
        }

        if (parts.length % 2 != EXPECTED_ODD_PARTS) {
            throw new IllegalArgumentException(ERROR_LOCATION_PROBABILITY_PAIRS_EXPECTED + line);
        }

        if (parts.length > MAX_CONNECTED_STREETS) {
            throw new IllegalArgumentException(ERROR_TOO_MANY_CONNECTED_STREETS + line);
        }
    }

    private static void validateCityData(Map<String, EntryPoint> entryPoints, Map<String, Intersection> intersections, Set<String> referencesMadeByEntryPoints, Set<String> referencesMadeByIntersections) {
        if (entryPoints.isEmpty()) {
            throw new IllegalArgumentException(ERROR_NO_ENTRY_POINTS);
        }

        if (intersections.isEmpty()) {
            throw new IllegalArgumentException(ERROR_NO_INTERSECTIONS);
        }

        boolean noLocationsThatAreBothEntryPointAndIntersection = Collections.disjoint(entryPoints.keySet(), intersections.keySet());

        if (!noLocationsThatAreBothEntryPointAndIntersection) {
            throw new IllegalArgumentException(ERROR_DUPLICATE_LOCATIONS);
        }

        if (!intersections.keySet().containsAll(referencesMadeByEntryPoints)) {
            throw new IllegalArgumentException(ERROR_INVALID_ENTRY_POINT_REFERENCE);
        }

        for (String reference : referencesMadeByIntersections) {
            if (!entryPoints.containsKey(reference) && !intersections.containsKey(reference)) {
                throw new IllegalArgumentException(ERROR_INVALID_INTERSECTION_REFERENCE);
            }
        }
    }
}
