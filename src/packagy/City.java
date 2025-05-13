package packagy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import static packagy.Coord.*;

public class City {

  private final HashMap<String, EntryPoint>             entryPoints;
  private final HashMap<String, Intersection>           intersections;
  private final HashMap<DirectedEdge, DirectedEdgeInfo> directedEdges;

  private final int frequency;
  private final int maxTime;

  private final ArrayList<Vehicle>            vehicles       = new ArrayList<>();
  private final ArrayList<ArrayList<Vehicle>> vehicleHistory = new ArrayList<>();

  private final Random random = new Random ();


  public City(HashMap<String, EntryPoint> entryPoints,
              HashMap<String, Intersection> intersections,
              HashMap<DirectedEdge, DirectedEdgeInfo> directedEdges,
              int frequency, int maxTime){
    this.entryPoints = entryPoints;
    this.intersections = intersections;
    this.frequency = frequency;
    this.maxTime = maxTime;
    this.directedEdges = directedEdges;

  }

  public City(CityDTO cityDTO) {
    this.entryPoints = cityDTO.entryPoints;
    this.intersections = cityDTO.intersections;
    this.frequency = cityDTO.frequency;
    this.maxTime = cityDTO.maxTime;
    this.directedEdges = cityDTO.directedEdges;
  }

  public HashMap<DirectedEdge, DirectedEdgeInfo> getDirectedEdges() {
    return directedEdges;
  }

  public HashMap<String, EntryPoint> getEntryPoints() {
    return entryPoints;
  }

  public HashMap<String, Intersection> getIntersections() {
    return intersections;
  }

  public ArrayList<ArrayList<Vehicle>> getVehicleHistory() {
    return vehicleHistory;
  }

  public int getFrequency() {
    return frequency;
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
    for (Map.Entry<DirectedEdge, DirectedEdgeInfo> entry : directedEdges.entrySet()) {
      DirectedEdgeInfo info = entry.getValue();
      info.updateMaxNum();
    }
  }

  public void simulate(){
      for (int i = 1; i<= maxTime; ++i){
        updateVehicles ();

        for(Map.Entry<String, EntryPoint> entry: entryPoints.entrySet ()){
          if(i % entry.getValue ().freq == 0){
            vehicles.add (createNewVehicle (Helper.getId (), entry.getKey (), entry.getValue ().intersectionName));
          }
        }

        updateDirectedEdgesMaxima();

        if(i % frequency == 0){
          updateVehicleHistory();
        }

      }

  }

}
