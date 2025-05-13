package packagy;

import java.io.File;
import java.io.IOException;

import static packagy.PathValidator.*;
import static packagy.TextFileReader.*;

public class Main {
  public static void main(String[] args) throws IOException {

    
    String path = getValidPath();

    //City city = readFromFile("C:\\Users\\debel\\Workspace\\groPro2025\\groPro2025\\testy.txt");
    City city = readFromFile(path);
    city.simulate ();

    String outputDirName = "output_" + new File(path).getName();
    File outputDir = new File(outputDirName);
    outputDir.mkdirs();

    packagy.Output.PlanWriter.write(new File(outputDir, "Plan.txt").getPath(), city.getDirectedEdges(), city.getEntryPoints(), city.getIntersections());
    packagy.Output.StatisticWriter.write(new File(outputDir, "Statistik.txt").getPath(), city.getDirectedEdges(), city.getEntryPoints(), city.getIntersections());
    packagy.Output.VehicleWriter.write(new File(outputDir, "Fahrzeuge.txt").getPath(), city.getVehicleHistory(), city.getFrequency());

    System.out.println("Simulation beendet");
  }

}
