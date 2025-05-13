package packagy;

public class Coord {
  public double x;
  public double y;

  public Coord (double x,
                double y) {
    this.x = x;
    this.y = y;
  }

  @Override
  protected Coord clone() {
    return new Coord(this.x, this.y);
  }

  public void normalize() {
    double length = Math.sqrt(x * x + y * y);
    if (length != 0) {
      x /= length;
      y /= length;
    }
  }

  public void multiply (double factor) {
    x *= factor;
    y *= factor;
  }

  public void add (Coord other) {
    x += other.x;
    y += other.y;
  }

  public void subtract (Coord other) {
    x -= other.x;
    y -= other.y;
  }


  public static double distance (Coord a,
                          Coord b) {
    return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
  }

  public static Coord add (Coord a,
                      Coord b) {
    return new Coord (a.x + b.x,
                      a.y + b.y);
  }

  public static Coord subtract (Coord a,
                          Coord b) {
    return new Coord (a.x - b.x,
                      a.y - b.y);
  }

  public static Coord multiply (Coord a,
                          double b) {
    return new Coord (a.x * b,
                      a.y * b);
  }


}
