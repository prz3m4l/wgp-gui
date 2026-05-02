import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Graph {
  HashMap<Integer, Vertex> vertices = new HashMap<Integer, Vertex>();
  List<Edge> edges = new ArrayList<Edge>();
  private double minX, maxX, minY, maxY;

  public void addVertex(int id, double x, double y) {
    Vertex newVertex = new Vertex(id, x, y);
    vertices.put(id, newVertex);
  }

  public void addEdge(String name, int sourceId, int targetId, double weight) {
    Vertex source = vertices.get(sourceId);
    Vertex target = vertices.get(targetId);
    if (source != null && target != null) {
      Edge newEdge = new Edge(name, source, target, weight);
      edges.add(newEdge);
    }
  }

  public int getVertexCount() {
    return vertices.size();
  }

  public int getEdgesCount() {
    return edges.size();
  }

  public void calculateBounds() {
    resetBounds();
    for (Vertex current : vertices.values()) {
      maxX = Math.max(maxX, current.getX());
      maxY = Math.max(maxY, current.getY());
      minX = Math.min(minX, current.getX());
      minY = Math.min(minY, current.getY());
    }
  }

  public double getMaxX() {
    return maxX;
  }

  public double getMaxY() {
    return maxY;
  }

  public double getMinX() {
    return minX;
  }

  public double getMinY() {
    return minY;
  }

  private void resetBounds() {
    minX = Double.POSITIVE_INFINITY;
    maxX = Double.NEGATIVE_INFINITY;
    minY = Double.POSITIVE_INFINITY;
    maxY = Double.NEGATIVE_INFINITY;
  }

  public void clear() {
    vertices.clear();
    edges.clear();
    resetBounds();
  }
}
