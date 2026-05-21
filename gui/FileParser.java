import java.io.File;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Scanner;

public class FileParser {
  private void readVertices(String path, Graph graph) throws Exception {
    File file = new File(path);
    graph.clear();
    try (Scanner scanner = new Scanner(file)) {
      scanner.useLocale(Locale.US);
      int verticesCount = scanner.nextInt();
      for (int i = 0; i < verticesCount; i++) {
        int id = scanner.nextInt();
        double x = scanner.nextDouble();
        double y = scanner.nextDouble();
        graph.addVertex(id, x, y);
      }
      graph.calculateBounds();
    }
  }

  private void readEdges(String path, Graph graph) throws Exception {
    File file = new File(path);
    try (Scanner scanner = new Scanner(file)) {
      scanner.useLocale(Locale.US);
      while (scanner.hasNext()) {
        String name = scanner.next();
        if (!scanner.hasNextInt()) break; // Zabezpieczenie na koniec pliku
        int sourceId = scanner.nextInt();
        if (!scanner.hasNextInt()) break;
        int targetId = scanner.nextInt();
        if (!scanner.hasNextDouble()) break;
        double weight = scanner.nextDouble();
        
        // Jeśli wierzchołki nie istnieją, stwórz je (domyślna pozycja 0,0)
        if (!graph.getVertices().containsKey(sourceId)) {
          graph.addVertex(sourceId, 0, 0);
        }
        if (!graph.getVertices().containsKey(targetId)) {
          graph.addVertex(targetId, 0, 0);
        }
        
        graph.addEdge(name, sourceId, targetId, weight);
      }
    }
  }

  private void readBinary(String path, Graph graph) throws Exception {
    byte[] allBytes = Files.readAllBytes(Paths.get(path));
    graph.clear();
    
    // Ręczna konwersja Little-Endian do Big-Endian
    // Zmieniamy kolejność bajtów w odczytanej tablicy:
    // int (4 bajty)
    reverseBytes(allBytes, 0, 4);
    int verticesCount = ByteBuffer.wrap(allBytes, 0, 4).getInt();
    
    if (allBytes.length != 4 + (verticesCount * 20)) {
      int expected = 4 + (verticesCount * 20);
      throw new Exception("Plik binarny jest uszkodzony lub niekompletny. " +
          "Oczekiwano " + expected + " bajtów, " +
          "znaleziono " + allBytes.length + " bajtów.");
    }
    
    // Dla każdego wierzchołka: id(int, 4 bajty), x(double, 8 bajtów), y(double, 8 bajtów)
    ByteBuffer buffer = ByteBuffer.wrap(allBytes);
    // Używamy natywnego Big Endian Javy (domyślny), ponieważ ręcznie obrócimy bajty
    buffer.position(4); // pomijamy ilość wierzchołków
    
    for (int i = 0; i < verticesCount; i++) {
        int offset = 4 + (i * 20);
        reverseBytes(allBytes, offset, 4);      // id
        reverseBytes(allBytes, offset + 4, 8);  // x
        reverseBytes(allBytes, offset + 12, 8); // y
        
        int id = buffer.getInt();
        double x = buffer.getDouble();
        double y = buffer.getDouble();
        graph.addVertex(id, x, y);
    }
    graph.calculateBounds();
  }

  // Pomocnicza metoda do zmiany kolejności bajtów (Little Endian -> Big Endian)
  private void reverseBytes(byte[] array, int offset, int length) {
      for (int i = 0; i < length / 2; i++) {
          byte temp = array[offset + i];
          array[offset + i] = array[offset + length - 1 - i];
          array[offset + length - 1 - i] = temp;
      }
  }

  public void loadFullGraph(String nodePath, String edgePath, Graph graph, boolean binary) throws Exception {
    if (binary) {
      readBinary(nodePath, graph);
    } else {
      readVertices(nodePath, graph);
    }
    readEdges(edgePath, graph);
  }

  public void saveToText(String path, Graph graph) throws Exception {
    if (graph.getVertexCount() != 0) {
      try (PrintWriter writer = new PrintWriter(path)) {
        writer.println(graph.getVertexCount());

        graph.getVertices().forEach((key, value) -> {
          writer.printf(Locale.US, "%d %.4f %.4f%n", key, value.getX(), value.getY());
        });
      }
    }
  }

  public void saveToBinary(String path, Graph graph) throws Exception {
    if (graph.getVertexCount() != 0) {
      int vCount = graph.getVertexCount();
      ByteBuffer buffer = ByteBuffer.allocate(4 + (vCount * 20));
      buffer.order(ByteOrder.LITTLE_ENDIAN);

      buffer.putInt(vCount);
      graph.getVertices().forEach((key, value) -> {
        buffer.putInt(key);
        buffer.putDouble(value.getX());
        buffer.putDouble(value.getY());
      });

      Files.write(Paths.get(path), buffer.array());
    }
  }
}
