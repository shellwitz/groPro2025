package packagy;

import java.io.*;
import java.util.*;

public class TextFileReader {

  public static City readFromFile(String filePath) throws IOException {
    HashMap<String, EntryPoint> entryPoints = new HashMap<>();
    HashMap<String, Intersection> intersections = new HashMap<>();
    HashMap<DirectedEdge, DirectedEdgeInfo> directedEdges = new HashMap<>();

    int frequency = 0;
    int maxTime = 0;

    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      String line;
      String section = "";
      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("#")) continue;

        if (line.equalsIgnoreCase("Zeitraum:") || line.equalsIgnoreCase("Einfallspunkte:") || line.equalsIgnoreCase("Kreuzungen:")) {
          section = line;
          continue;
        }

        switch (section) {
          case "Zeitraum:":
            String[] timeParts = line.split("\\s+");
            maxTime = Integer.parseInt(timeParts[0]);
            frequency = Integer.parseInt(timeParts[1]);
            break;

          case "Einfallspunkte:":
            parseEntryPoint(line, entryPoints);
            break;

          case "Kreuzungen:":
            parseIntersection(line, intersections, directedEdges);
            break;
        }
      }
    }

    return new City(entryPoints, intersections, directedEdges, frequency, maxTime);
  }

  private static void parseEntryPoint(String line, Map<String, EntryPoint> entryPoints) {
    // Format: Name X Y Ziel Takt
    String[] parts = line.split("\\s+");
    String name = parts[0];
    double x = Double.parseDouble(parts[1]);
    double y = Double.parseDouble(parts[2]);
    String ziel = parts[3];
    int takt = Integer.parseInt(parts[4]);

    EntryPoint ep = new EntryPoint();
    ep.coord = new Coord(x, y);
    ep.freq = takt;
    ep.intersectionName = ziel;

    entryPoints.put(name, ep);
  }

  private static void parseIntersection(String line, Map<String, Intersection> intersections,
                                        Map<DirectedEdge, DirectedEdgeInfo> directedEdges) {
    // Format: Name X Y Ziel1 Anteil1 Ziel2 Anteil2 ...
    String[] parts = line.split("\\s+");
    String name = parts[0];
    double x = Double.parseDouble(parts[1]);
    double y = Double.parseDouble(parts[2]);

    List<String> dests = new ArrayList<>();
    List<Double> probs = new ArrayList<>();

    for (int i = 3; i < parts.length; i += 2) {
      String dest = parts[i];
      double relProb = Double.parseDouble(parts[i + 1]);
      dests.add(dest);
      probs.add(relProb);

      // DirectedEdge in beide Richtungen hinzufügen
      DirectedEdge forward = new DirectedEdge(name, dest);
      DirectedEdge reverse = new DirectedEdge(dest, name);

      directedEdges.putIfAbsent(forward, new DirectedEdgeInfo());
      directedEdges.putIfAbsent(reverse, new DirectedEdgeInfo());
    }

    // Wahrscheinlichkeiten normalisieren
    double total = probs.stream().mapToDouble(Double::doubleValue).sum();
    for (int i = 0; i < probs.size(); i++) {
      probs.set(i, probs.get(i) / total);
    }

    Intersection inter = new Intersection();
    inter.coord = new Coord(x, y);
    inter.namesAndProbabilities = new NamesAndProbabilities(
        dests.toArray(new String[0]),
        probs.stream().mapToDouble(Double::doubleValue).toArray()
    );

    intersections.put(name, inter);
  }
}
