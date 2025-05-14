package trafficsimulation;

public class Vehicle {

  public static final double EXPECTED_VELOCITY  = 0.125; //0.125 *100m per s = 45 km/h
  public static final double STANDARD_DEVIATION = (double) 100 / 3600;

  public int id;

  String fromName;
  String toName;

  Coord fromCoord;
  public Coord toCoord;

  public Coord currentPosition;
  Coord direction;

  double velocity;

  @Override
  protected Vehicle clone() {
    Vehicle cloned = new Vehicle();
    cloned.id = this.id;
    cloned.fromName = this.fromName;
    cloned.toName = this.toName;
    cloned.fromCoord = this.fromCoord.clone();
    cloned.toCoord = this.toCoord.clone();
    cloned.currentPosition = this.currentPosition.clone();
    cloned.direction = this.direction.clone();
    cloned.velocity = this.velocity;
    return cloned;
  }
}


