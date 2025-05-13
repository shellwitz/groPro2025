package packagy;

import java.io.File;
import java.io.IOException;

import static packagy.TextFileReader.*;

public class Main {
  public static void main(String[] args) throws IOException {

    System.out.println("Hello World");

    City city = readFromFile("C:\\Users\\debel\\Workspace\\groPro2025\\groPro2025\\testy.txt");
    city.simulate ();

    File file = new File("output/Plan.txt");
    file.getParentFile().mkdirs();

    packagy.Output.PlanWriter.write("output/Plan.txt", city.getDirectedEdges(), city.getEntryPoints(), city.getIntersections());
    packagy.Output.StatisticWriter.write("output/Statistik.txt", city.getDirectedEdges(), city.getEntryPoints(), city.getIntersections());
    packagy.Output.VehicleWriter.write("output/Fahrzeuge.txt", city.getVehicleHistory(), city.getFrequency());
  }

}
