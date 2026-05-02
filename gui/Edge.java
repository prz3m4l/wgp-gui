public class Edge {
  private String name;
  private Vertex source;
  private Vertex target;
  private double weight;

  public Edge(String name, Vertex source, Vertex target, double weight) {
    this.name = name;
    this.source = source;
    this.target = target;
    this.weight = weight;
  }

  public String getName() {
    return this.name;
  }

  public double getWeight() {
    return this.weight;
  }

  public Vertex getSource() {
    return this.source;
  }

  public Vertex getTarget() {
    return this.target;
  }
}
