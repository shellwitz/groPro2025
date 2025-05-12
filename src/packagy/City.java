package packagy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static packagy.Coord.*;

public class City {

  private HashMap<String, EntryPoint> entryPoints;
  private HashMap<String, Intersection> intersections;
  private HashMap<DirectedEdge, DirectedEdgeInfo> directedEdges;

  private int frequency;
  private int maxTime;

  private ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
  private ArrayList<ArrayList<Vehicle>> vehicleHistory = new ArrayList<ArrayList<Vehicle>>();

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


  private void updateVehicles(){

    for(Vehicle vehicle: vehicles){

      if(distance(vehicle.currentPosition, vehicle.toCoord) < vehicle.velocity){

        directedEdges.get(new DirectedEdge(vehicle.fromName, vehicle.toName)).currentNum--;
        String newDestination = intersections.get (vehicle.toName).getNewDestinationByProbability(vehicle.fromName, random);
        vehicle.fromName = vehicle.toName;
        vehicle.toName = newDestination;

        if(entryPoints.containsKey(newDestination)){
          vehicles.remove(vehicle);

        }else{
          double rest = vehicle.velocity - distance(vehicle.currentPosition, vehicle.toCoord);

          vehicle.fromCoord = vehicle.toCoord;
          vehicle.toCoord = intersections.get (newDestination).coord;

          vehicle.direction = subtract(vehicle.toCoord, vehicle.fromCoord);

          vehicle.direction.normalize();
          vehicle.direction.multiply(vehicle.velocity);

          vehicle.currentPosition = add(vehicle.fromCoord, multiply (vehicle.direction, rest));
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
          if(i % entry.getValue ().freq == 0){
            vehicles.add (createNewVehicle (i, entry.getKey (), entry.getValue ().intersectionName));
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
