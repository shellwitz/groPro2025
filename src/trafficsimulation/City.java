package trafficsimulation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;

import static trafficsimulation.Coord.*;

public class City {

  private final HashMap<String, EntryPoint>             entryPoints;
  private final HashMap<String, Intersection>           intersections;
  private final HashMap<DirectedEdge, DirectedEdgeInfo> directedEdges;

  private final int clockRate;
  private final int maxTime;

  private final ArrayList<Vehicle>            vehicles       = new ArrayList<>();
  private final ArrayList<ArrayList<Vehicle>> vehicleHistory = new ArrayList<>();

  private final Random random = new Random ();


  public City(HashMap<String, EntryPoint> entryPoints,
              HashMap<String, Intersection> intersections,
              HashMap<DirectedEdge, DirectedEdgeInfo> directedEdges,
              int clockRate, int maxTime){
    this.entryPoints = entryPoints;
    this.intersections = intersections;
    this.clockRate = clockRate;
    this.maxTime = maxTime;
    this.directedEdges = directedEdges;

  }

  public City(CityDTO cityDTO) {
    this.entryPoints = cityDTO.entryPoints;
    this.intersections = cityDTO.intersections;
    this.clockRate = cityDTO.clockRate;
    this.maxTime = cityDTO.maxTime;
    this.directedEdges = cityDTO.directedEdges;
  }

  public Map<DirectedEdge, DirectedEdgeInfo> getDirectedEdges() {
    return directedEdges;
  }

  public Map<String, EntryPoint> getEntryPoints() {
    return entryPoints;
  }

  public Map<String, Intersection> getIntersections() {
    return intersections;
  }

  public List<ArrayList<Vehicle>> getVehicleHistory() {
    return vehicleHistory;
  }

  public int getClockRate () {
    return clockRate;
  }

  private void updateVehicle(Iterator<Vehicle> vehicleIterator){
    Vehicle vehicle = vehicleIterator.next();

    if (distance(vehicle.currentPosition, vehicle.toCoord) < vehicle.velocity) {

      directedEdges.get(new DirectedEdge(vehicle.fromName, vehicle.toName)).decrement();
      if(entryPoints.containsKey(vehicle.toName)){
        vehicleIterator.remove(); // Safely remove the vehicle from the list

      }else{
        String newDestination = intersections.get(vehicle.toName).getNewDestinationByProbability(vehicle.fromName, random);

        assert !Objects.equals (newDestination, vehicle.fromName) : "vehicle is not allowed to turn";

        vehicle.fromName = vehicle.toName;
        vehicle.toName = newDestination;
        double rest = vehicle.velocity - distance(vehicle.currentPosition, vehicle.toCoord);



        Coord toCoord;
        if (entryPoints.containsKey(newDestination)) {
          toCoord = entryPoints.get(newDestination).coord;
        } else {
          toCoord = intersections.get(newDestination).coord;
        }

        vehicle.fromCoord = vehicle.toCoord;
        vehicle.toCoord = toCoord;

        vehicle.direction = subtract(vehicle.toCoord, vehicle.fromCoord);
        vehicle.direction.normalize();
        vehicle.direction.multiply(vehicle.velocity);

        vehicle.currentPosition = add(vehicle.fromCoord, multiply(vehicle.direction, rest));
        directedEdges.get(new DirectedEdge(vehicle.fromName, vehicle.toName)).increment();
      }
    } else {
      vehicle.currentPosition = add(vehicle.currentPosition, vehicle.direction);
    }
  }


  private void updateVehicles() {
    Iterator<Vehicle> iterator = vehicles.iterator();

    while (iterator.hasNext()) {
      updateVehicle(iterator); // the vehicleIterator.next(); is called in the method
    }
  }

  private Vehicle createNewVehicle(int id, String from, String to){
    Vehicle vehicle = new Vehicle();
    vehicle.id = id;
    vehicle.fromName = from;
    vehicle.toName = to;
    vehicle.currentPosition = entryPoints.get(from).coord;
    vehicle.toCoord = intersections.get(to).coord;
    vehicle.fromCoord = entryPoints.get(from).coord;

    vehicle.direction = subtract(vehicle.toCoord, vehicle.fromCoord);
    vehicle.direction.normalize();

    double randomValue = random.nextGaussian() * Vehicle.STANDARD_DEVIATION + Vehicle.EXPECTED_VELOCITY;
    vehicle.direction.multiply(randomValue);
    vehicle.velocity = randomValue;

    DirectedEdge fromTo = new DirectedEdge(from, to);

    directedEdges.get(fromTo).increment();

    return vehicle;
  }

  private void updateVehicleHistory() {
    ArrayList<Vehicle> deepCopiedVehicles = new ArrayList<>();
    for (Vehicle vehicle : vehicles) {
      deepCopiedVehicles.add(vehicle.clone());
    }
    vehicleHistory.add(deepCopiedVehicles);
  }

  private void updateDirectedEdgesMaxima() {
    for (Entry<DirectedEdge, DirectedEdgeInfo> entry : directedEdges.entrySet()) {
      DirectedEdgeInfo info = entry.getValue();
      info.updateMaxNum();
    }
  }

  public List<Entry<DirectedEdge, DirectedEdgeInfo>> getAlphabeticallySortedDirectedEdges() {
    List<Entry<DirectedEdge, DirectedEdgeInfo>> sortedEntries = new ArrayList<>(directedEdges.entrySet());
    // Sort the entries based on the concatenation of from and to with anonynmous inner class
    sortedEntries.sort(new Comparator<Entry<DirectedEdge, DirectedEdgeInfo>>() {
        @Override
        public int compare(Entry<DirectedEdge, DirectedEdgeInfo> entry1, Entry<DirectedEdge, DirectedEdgeInfo> entry2) {
            String key1 = entry1.getKey().from + entry1.getKey().to;
            String key2 = entry2.getKey().from + entry2.getKey().to;
            return key1.compareTo(key2);
        }
    });
    return sortedEntries;
  }

  public void simulate(){
      for (int i = 1; i<= maxTime; ++i){
        updateVehicles ();

        for(Entry<String, EntryPoint> entry: entryPoints.entrySet ()){
          if(i % entry.getValue ().freq == 0){
            vehicles.add (createNewVehicle (Helper.getId (), entry.getKey (), entry.getValue ().intersectionName));
          }
        }

        updateDirectedEdgesMaxima();

        if(i % clockRate == 0){
          updateVehicleHistory();
        }

      }

  }

}
