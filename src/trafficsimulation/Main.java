package trafficsimulation;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;



public class Main {
  public static void main(String[] args) throws IOException {
    String path = args[0];

    Reader reader = new TextFileReader(path);
    City city = new City (reader.read ());
    city.simulate ();

    String fileNameWithoutExtension = new File(path).getName().replaceFirst("[.][^.]+$", "");
    String outputDirName = "output_" + fileNameWithoutExtension;
    File outputDir = new File(outputDirName);
    outputDir.mkdirs();

    List<Map.Entry<DirectedEdge, DirectedEdgeInfo>> alphabeticallySortedDirectedEdges = city.getAlphabeticallySortedDirectedEdges();


    trafficsimulation.Output.PlanWriter.write(new File(outputDir, "Plan.txt").getPath(), alphabeticallySortedDirectedEdges, city.getEntryPoints(), city.getIntersections());
    trafficsimulation.Output.StatisticWriter.write(new File(outputDir, "Statistik.txt").getPath(), alphabeticallySortedDirectedEdges, city.getEntryPoints(), city.getIntersections());
    trafficsimulation.Output.VehicleWriter.write(new File(outputDir, "Fahrzeuge.txt").getPath(), city.getVehicleHistory(), city.getClockRate ());

    System.out.println("Simulation beendet");
  }

}
