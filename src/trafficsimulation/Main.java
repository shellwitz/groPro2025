package trafficsimulation;

import trafficsimulation.Output.PlanWriter;
import trafficsimulation.Output.StatisticWriter;
import trafficsimulation.Output.VehicleWriter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Main {
  public static void main(String[] args) throws IOException {
    if (args.length == 0) {
      String errorMessage = """
          Fehler: Es wurde keine Eingabedatei angegeben.
          Bitte geben Sie den Dateipfad zu einer Eingabedatei an, die einen Stadtplan beschreibt.
          Beispiel-Format:
          # Tunneldorf
          Zeitraum:
          100 1

          Einfallspunkte:
          A 0 0 B 2
          C 0 2 B 5
          D 4 0 E 3
          F 4 2 E 2
          G 5 1 E 3

          Kreuzungen:
          B 0 1 A 20 C 30 E 50
          E 4 1 D 20 F 20 G 10 B 50
          """;
      System.out.print(errorMessage);
      return;
    }

    String path = args[0];

    Reader reader = new TextFileReader(path);
    City city = new City (reader.read ());

    System.out.println("Simulation gestartet");
    city.simulate ();
    System.out.println("Simulation beendet");

    String fileNameWithoutExtension = new File(path).getName().replaceFirst("[.][^.]+$", "");
    String outputDirName = "output_" + fileNameWithoutExtension;
    File outputDir = new File(outputDirName);
    outputDir.mkdirs();

    List<Map.Entry<DirectedEdge, DirectedEdgeInfo>> alphabeticallySortedDirectedEdges = city.getAlphabeticallySortedDirectedEdges();


    PlanWriter.write(new File(outputDir, "Plan.txt").getPath(), alphabeticallySortedDirectedEdges, city.getEntryPoints(), city.getIntersections());
    StatisticWriter.write(new File(outputDir, "Statistik.txt").getPath(), alphabeticallySortedDirectedEdges, city.getEntryPoints(), city.getIntersections());
    VehicleWriter.write(new File(outputDir, "Fahrzeuge.txt").getPath(), city.getVehicleHistory(), city.getClockRate ());
  }

}
