package packagy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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

  private Random random = new Random ();


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


  private void updateVehicles() {
    Iterator<Vehicle> iterator = vehicles.iterator();

    while (iterator.hasNext()) {
      Vehicle vehicle = iterator.next();

      if (distance(vehicle.currentPosition, vehicle.toCoord) < vehicle.velocity) {

        directedEdges.get(new DirectedEdge(vehicle.fromName, vehicle.toName)).currentNum--;
        if(entryPoints.containsKey(vehicle.toName)){
          iterator.remove(); // Safely remove the vehicle from the list

        }else{
          String newDestination = intersections.get(vehicle.toName).getNewDestinationByProbability(vehicle.fromName, random);
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
        }
      } else {
        vehicle.currentPosition = add(vehicle.currentPosition, vehicle.direction);
      }
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

    directedEdges.get(fromTo).currentNum++;
    directedEdges.get(fromTo).totalCount++;

    if(directedEdges.get(fromTo).currentNum > directedEdges.get(fromTo).maxNum){
      directedEdges.get(fromTo).maxNum = directedEdges.get(fromTo).currentNum;
    }

    return vehicle;
  }

  public void simulate(){
      for (int i = 1; i<= maxTime; ++i){
        updateVehicles ();

        for(Map.Entry<String, EntryPoint> entry: entryPoints.entrySet ()){
          if(entry.getValue ().freq != 0 && i % entry.getValue ().freq == 0){ //TODO think if zero fequencies are even allowed, just for test purposes
            vehicles.add (createNewVehicle (Helper.getId (), entry.getKey (), entry.getValue ().intersectionName));
          }
        }

        if(i % frequency == 0){
          ArrayList<Vehicle> deepCopiedVehicles = new ArrayList<>();
          for (Vehicle vehicle : vehicles) {
            deepCopiedVehicles.add(vehicle.clone());
          }
          vehicleHistory.add(deepCopiedVehicles);
        }

      }

  }

}
