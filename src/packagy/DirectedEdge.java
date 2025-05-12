package packagy;

import java.util.Objects;

public class DirectedEdge {
  String from;
  String to;

  public DirectedEdge (String from,
                       String to) {
    this.from = from;
    this.to = to;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    DirectedEdge that = (DirectedEdge) obj;
    return from.equals(that.from) && to.equals(that.to);
  }

  @Override
  public int hashCode() {
    return Objects.hash(from, to);
  }
}
