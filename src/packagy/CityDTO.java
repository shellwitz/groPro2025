package packagy;

import java.util.HashMap;

public class CityDTO {
    public HashMap<String, EntryPoint> entryPoints;
    public HashMap<String, Intersection> intersections;
    public HashMap<DirectedEdge, DirectedEdgeInfo> directedEdges;
    public int frequency;
    public int maxTime;

    public CityDTO(HashMap<String, EntryPoint> entryPoints,
                   HashMap<String, Intersection> intersections,
                   HashMap<DirectedEdge, DirectedEdgeInfo> directedEdges,
                   int frequency, int maxTime) {
        this.entryPoints = entryPoints;
        this.intersections = intersections;
        this.directedEdges = directedEdges;
        this.frequency = frequency;
        this.maxTime = maxTime;
    }
}
