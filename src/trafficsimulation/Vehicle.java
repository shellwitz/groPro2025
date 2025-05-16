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

  public Vehicle(int id, String fromName, String toName, Coord fromCoord, Coord toCoord, Coord currentPosition, Coord direction, double velocity) {
    this.id = id;
    this.fromName = fromName;
    this.toName = toName;
    this.fromCoord = fromCoord;
    this.toCoord = toCoord;
    this.currentPosition = currentPosition;
    this.direction = direction;
    this.velocity = velocity;
}

  @Override
  protected Vehicle clone() {
    return new Vehicle(
        this.id,
        this.fromName,
        this.toName,
        this.fromCoord.clone(),
        this.toCoord.clone(),
        this.currentPosition.clone(),
        this.direction.clone(),
        this.velocity
    );
  }
}


