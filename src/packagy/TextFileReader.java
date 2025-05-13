package packagy;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class TextFileReader {

  private static final String ERROR_NOT_UTF8 = "Eingabedatei ist nicht UTF-8 kodiert: ";
  private static final String ERROR_MULTIPLE_ZEITRAUM = "Mehrere 'Zeitraum:' Abschnitte sind nicht erlaubt.";
  private static final String ERROR_MULTIPLE_EINFALLSPUNKTE = "Mehrere 'Einfallspunkte:' Abschnitte sind nicht erlaubt.";
  private static final String ERROR_MULTIPLE_KREUZUNGEN = "Mehrere 'Kreuzungen:' Abschnitte sind nicht erlaubt.";
  private static final String ERROR_INVALID_ZEITRAUM_FORMAT = "Ungültiges Format für 'Zeitraum:'. Erwartet werden zwei Werte.";
  private static final String ERROR_NO_ENTRY_POINTS = "Keine Einfallspunkte gefunden.";
  private static final String ERROR_NO_INTERSECTIONS = "Keine Kreuzungen gefunden.";
  private static final String ERROR_DUPLICATE_LOCATIONS = "Orte können nicht gleichzeitig Einfallspunkte und Kreuzungen sein.";
  private static final String ERROR_INVALID_ENTRY_POINT_REFERENCE = "Nicht alle Referenzen von Einfallspunkten existieren tatsächlich.";
  private static final String ERROR_INVALID_INTERSECTION_REFERENCE = "Nicht alle Referenzen von Kreuzungen existieren tatsächlich.";
  private static final String ERROR_INVALID_MAX_TIME = "Ungültiger Wert für maxTime: ";
  private static final String ERROR_INVALID_GENERAL_FREQUENCY = "Ungültiger Wert für allgemeine Frequenz: ";
  private static final String ERROR_COORDINATES_TOO_CLOSE = "Koordinaten sind zu nah beieinander: ";
  private static final String ERROR_INVALID_COORDINATE_COMPONENT = "Ungültiger Wert für Koordinatenkomponente: ";
  private static final String ERROR_INVALID_PROBABILITY = "Ungültiger Wert für Wahrscheinlichkeit in Prozent: ";
  private static final String ERROR_INVALID_ENTRY_POINT_FORMAT = "Ungültiges Format für Einfallspunkt: ";
  private static final String ERROR_DUPLICATE_ENTRY_POINT_NAME = "Doppelter Einfallspunktname: ";
  private static final String ERROR_INVALID_INTERSECTION_FORMAT = "Ungültiges Format für Kreuzung: ";
  private static final String ERROR_DUPLICATE_INTERSECTION_NAME = "Doppelter Kreuzungsname: ";
  private static final String ERROR_INVALID_PROBABILITY_SUM = "Wahrscheinlichkeiten müssen 1 ergeben: ";

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
        return line.substring(0, commentIndex).trim(); // Remove everything after # and trim whitespace
    }
    return line.trim(); // Return the line as is if no # is found
}

  public static City readFromFile(String filePath) throws IOException {

      if (!isUTF8Encoded(filePath)) {
        throw new IllegalArgumentException(ERROR_NOT_UTF8 + filePath);
    }

    HashMap<String, EntryPoint> entryPoints = new HashMap<>();
    HashMap<String, Intersection> intersections = new HashMap<>();
    HashMap<DirectedEdge, DirectedEdgeInfo> directedEdges = new HashMap<>();
    ArrayList<Coord> alreadyExistingCoords = new ArrayList<>();

    HashSet<String> referencesMadeByEntryPoints = new HashSet<>();
    HashSet<String> referencesMadeByIntersections = new HashSet<>();

    int frequency = 0;
    int maxTime = 0;

    boolean hasZeitraum = false;
    boolean hasEinfallspunkte = false;
    boolean hasKreuzungen = false;

    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      String line;
      String section = "";
      while ((line = br.readLine()) != null) {
      
        line = stripComment(line);
        if (line.isEmpty()) continue;

        if (line.equalsIgnoreCase("Zeitraum:") || line.equalsIgnoreCase("Einfallspunkte:") || line.equalsIgnoreCase("Kreuzungen:")) {
          if (line.equalsIgnoreCase("Zeitraum:") && hasZeitraum) {
            throw new IllegalArgumentException(ERROR_MULTIPLE_ZEITRAUM);
          } else if (line.equalsIgnoreCase("Einfallspunkte:") && hasEinfallspunkte) {
            throw new IllegalArgumentException(ERROR_MULTIPLE_EINFALLSPUNKTE);
          } else if (line.equalsIgnoreCase("Kreuzungen:") && hasKreuzungen) {
            throw new IllegalArgumentException(ERROR_MULTIPLE_KREUZUNGEN);
          }

          section = line;
          if (line.equalsIgnoreCase("Zeitraum:")) hasZeitraum = true;
          if (line.equalsIgnoreCase("Einfallspunkte:")) hasEinfallspunkte = true;
          if (line.equalsIgnoreCase("Kreuzungen:")) hasKreuzungen = true;
          continue;
        }

        switch (section) {
          case "Zeitraum:":
            String[] timeParts = line.split("\\s+");

            if(timeParts.length != 2) {
              throw new IllegalArgumentException(ERROR_INVALID_ZEITRAUM_FORMAT);
            }

            maxTime = checkMaxTime(timeParts[0]);
            frequency = checkGeneralFrequency(timeParts[1], maxTime);
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

    if(entryPoints.isEmpty()) {
      throw new IllegalArgumentException(ERROR_NO_ENTRY_POINTS);
    }

    if(intersections.isEmpty()) {
      throw new IllegalArgumentException(ERROR_NO_INTERSECTIONS);
    }

    boolean noLocationsThatAreBothEntryPointAndIntersection = Collections.disjoint(entryPoints.keySet(), intersections.keySet());

    if(!noLocationsThatAreBothEntryPointAndIntersection){
      throw new IllegalArgumentException(ERROR_DUPLICATE_LOCATIONS);
    }

    if(!intersections.keySet().containsAll(referencesMadeByEntryPoints)) {
      throw new IllegalArgumentException(ERROR_INVALID_ENTRY_POINT_REFERENCE);
    }

    for(String reference: referencesMadeByIntersections) {
      if(!entryPoints.containsKey (reference) && !intersections.containsKey(reference)) {
        throw new IllegalArgumentException(ERROR_INVALID_INTERSECTION_REFERENCE);
      }
    }

    return new City(entryPoints, intersections, directedEdges, frequency, maxTime);
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

      if(maxTime > 24*60*60) {
        throw new IllegalArgumentException(ERROR_INVALID_MAX_TIME + potentialMaxTime + ". Erwartet wird ein Wert kleiner als 24 Stunden in Sekunden.");
      }

      return maxTime;
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(ERROR_INVALID_MAX_TIME + potentialMaxTime + ". Erwartet wird eine Ganzzahl.");
    }
  }

  private static void checkWhetherCoordIsFarEnough(List<Coord> alreadyExistingCoords, Coord newCord){
    for (Coord existingCoord : alreadyExistingCoords) {
      if (Coord.distance(existingCoord, newCord) < 0.1) { //0.1*100m Straßen sollten mindestens 10m auseinander liegen
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

      if(generalFrequency > maxTime) {
        throw new IllegalArgumentException(ERROR_INVALID_GENERAL_FREQUENCY + potentialGeneralFrequency + ". Erwartet wird ein Wert kleiner als die Simulationszeitspanne.");
      }

      return generalFrequency;
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(ERROR_INVALID_GENERAL_FREQUENCY + potentialGeneralFrequency + ". Erwartet wird eine Ganzzahl.");
    }
  }

  private static void parseEntryPoint(String line, Map<String, EntryPoint> entryPoints, List<Coord> coordChecker, HashSet<String> referencesMadeByEntryPoints) {
    // Format: Name X Y Ziel Takt
    String[] parts = line.split("\\s+");

    if (parts.length != 5) {
      throw new IllegalArgumentException(ERROR_INVALID_ENTRY_POINT_FORMAT + line);
    }

    String name = parts[0];

    if(name.length() > 100) {
      throw new IllegalArgumentException("Einfallspunktname ist zu lang: " + name);
    }

    if (entryPoints.containsKey(name)) {
      throw new IllegalArgumentException(ERROR_DUPLICATE_ENTRY_POINT_NAME + name);
    }

    double x = checkCoordinateComponent(parts[1]);
    double y = checkCoordinateComponent(parts[2]);

    Coord epCoord = new Coord (x, y);

    checkWhetherCoordIsFarEnough(coordChecker, epCoord);

    String destination = parts[3];

    referencesMadeByEntryPoints.add(destination);

    int freq = Integer.parseInt(parts[4]);

    EntryPoint ep = new EntryPoint();
    ep.coord = new Coord(x, y);
    ep.freq = freq;
    ep.intersectionName = destination;

    entryPoints.put(name, ep);
  }

  private static double checkCoordinateComponent(String potentialCoordinateComponent){

    try {
      double ret = Double.parseDouble(potentialCoordinateComponent);
      if(Math.abs(ret) > 1000) {
        throw new IllegalArgumentException(ERROR_INVALID_COORDINATE_COMPONENT + potentialCoordinateComponent + ". Erwartet wird eine Zahl zwischen -1000 und 1000.");
      }
      return ret;
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(ERROR_INVALID_COORDINATE_COMPONENT + potentialCoordinateComponent + ". Erwartet wird eine Zahl mit einem Dezimalpunkt als Trennzeichen.");
    }
  }

  private static double checkProbabilityInPercentage(String potentialProbabilityInPercentage){
    try {
      double ret = Double.parseDouble(potentialProbabilityInPercentage);
      if(ret < 0 || ret > 100) {
        throw new IllegalArgumentException(ERROR_INVALID_PROBABILITY + potentialProbabilityInPercentage + ". Erwartet wird eine Zahl zwischen 0 und 100.");
      }
      return ret/100;
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(ERROR_INVALID_PROBABILITY + potentialProbabilityInPercentage + ". Erwartet wird eine Zahl mit einem Dezimalpunkt als Trennzeichen.");
    }
  }

  private static void parseIntersection(String line, Map<String, Intersection> intersections,
                                        Map<DirectedEdge, DirectedEdgeInfo> directedEdges, List<Coord> coordChecker, HashSet<String> referencesMadeByIntersections) {
    // Format: Name X Y Ziel1 Anteil1 Ziel2 Anteil2 ...
    String[] parts = line.split("\\s+");
    String name = parts[0];

    if (intersections.containsKey(name)) {
      throw new IllegalArgumentException(ERROR_DUPLICATE_INTERSECTION_NAME + name);
    }

    if(name.length() > 100) {
      throw new IllegalArgumentException("Kreuzungsname ist zu lang: " + name);
    }

    if (parts.length <9) {
      throw new IllegalArgumentException(ERROR_INVALID_INTERSECTION_FORMAT + line);
    }

    if(parts.length % 2 == 1) {
      throw new IllegalArgumentException(ERROR_INVALID_INTERSECTION_FORMAT + line);
    }

    double x = checkCoordinateComponent(parts[1]);
    double y = checkCoordinateComponent(parts[2]);

    List<String> dests = new ArrayList<>();
    List<Double> probs = new ArrayList<>();

    for (int i = 3; i < parts.length; i += 2) {
      String dest = parts[i];
      double relProb = checkProbabilityInPercentage(parts[i + 1]);
      dests.add(dest);
      probs.add(relProb);

      // DirectedEdge in beide Richtungen hinzufügen
      DirectedEdge forward = new DirectedEdge(name, dest);
      DirectedEdge reverse = new DirectedEdge(dest, name);

      directedEdges.putIfAbsent(forward, new DirectedEdgeInfo());
      directedEdges.putIfAbsent(reverse, new DirectedEdgeInfo());
    }

    double total = probs.stream().mapToDouble(Double::doubleValue).sum();
    if (Math.abs (total - 1.0) > 0.0001) { //0.0001 is a small tolerance for floating point comparison
      throw new IllegalArgumentException(ERROR_INVALID_PROBABILITY_SUM + line);
    }

    Coord intersectionCoord = new Coord(x, y);

    checkWhetherCoordIsFarEnough(coordChecker, intersectionCoord);

    referencesMadeByIntersections.addAll(dests);

    Intersection inter = new Intersection();
    inter.coord = intersectionCoord;
    inter.namesAndProbabilities = new NamesAndProbabilities(
        dests.toArray(new String[0]),
        probs.stream().mapToDouble(Double::doubleValue).toArray()
    );

    intersections.put(name, inter);
  }
}
