import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

public class FileParser {
  private void readVertices(String path, Graph graph) throws Exception {
    graph.clear();
    int lineNumber = 1;
    try (BufferedReader br = new BufferedReader(new FileReader(path))) {
      String line;
      // Odczyt pierwszej linii z liczbą wierzchołków
      while ((line = br.readLine()) != null && line.trim().isEmpty()) {
        lineNumber++;
      }
      if (line == null) return;
      
      int verticesCount;
      try {
        verticesCount = Integer.parseInt(line.trim());
      } catch (NumberFormatException e) {
        throw new Exception("Błąd wczytywania pliku! Oczekiwano danych w formacie: <węzeł> <x> <y>. Odnaleziono niedozwolone znaki w linii " + lineNumber + ".");
      }
      lineNumber++;
      
      int count = 0;
      while (count < verticesCount && (line = br.readLine()) != null) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
          lineNumber++;
          continue;
        }
        String[] parts = trimmed.split("\\s+");
        if (parts.length < 3) {
          throw new Exception("Błąd wczytywania pliku! Oczekiwano danych w formacie: <węzeł> <x> <y>. Odnaleziono niedozwolone znaki w linii " + lineNumber + ".");
        }
        try {
          int id = Integer.parseInt(parts[0]);
          double x = Double.parseDouble(parts[1].replace(',', '.'));
          double y = Double.parseDouble(parts[2].replace(',', '.'));
          graph.addVertex(id, x, y);
          count++;
        } catch (NumberFormatException e) {
          throw new Exception("Błąd wczytywania pliku! Oczekiwano danych w formacie: <węzeł> <x> <y>. Odnaleziono niedozwolone znaki w linii " + lineNumber + ".");
        }
        lineNumber++;
      }
      graph.calculateBounds();
    }
  }

  private void readEdges(String path, Graph graph) throws Exception {
    int lineNumber = 1;
    try (BufferedReader br = new BufferedReader(new FileReader(path))) {
      String line;
      while ((line = br.readLine()) != null) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
          lineNumber++;
          continue;
        }
        String[] parts = trimmed.split("\\s+");
        // readEdges wczytuje 4 elementy, natomiast by dopasować się do wymaganych 
        // komunikatów błędu logujemy ten sam precyzyjny tekst zgodny z dokumentacją
        if (parts.length < 4) {
          throw new Exception("Błąd wczytywania pliku! Oczekiwano danych w formacie: <węzeł> <x> <y>. Odnaleziono niedozwolone znaki w linii " + lineNumber + ".");
        }
        try {
          String name = parts[0];
          int sourceId = Integer.parseInt(parts[1]);
          int targetId = Integer.parseInt(parts[2]);
          double weight = Double.parseDouble(parts[3].replace(',', '.'));
          
          if (!graph.getVertices().containsKey(sourceId)) {
            graph.addVertex(sourceId, 0, 0);
          }
          if (!graph.getVertices().containsKey(targetId)) {
            graph.addVertex(targetId, 0, 0);
          }
          graph.addEdge(name, sourceId, targetId, weight);
        } catch (NumberFormatException e) {
          throw new Exception("Błąd wczytywania pliku! Oczekiwano danych w formacie: <węzeł> <x> <y>. Odnaleziono niedozwolone znaki w linii " + lineNumber + ".");
        }
        lineNumber++;
      }
    }
  }

  private void readBinary(String path, Graph graph) throws Exception {
    byte[] allBytes = Files.readAllBytes(Paths.get(path));
    graph.clear();
    
    reverseBytes(allBytes, 0, 4);
    int verticesCount = ByteBuffer.wrap(allBytes, 0, 4).getInt();
    
    if (allBytes.length != 4 + (verticesCount * 20)) {
      int expected = 4 + (verticesCount * 20);
      throw new Exception("Plik binarny jest uszkodzony lub niekompletny. " +
          "Oczekiwano " + expected + " bajtów, " +
          "znaleziono " + allBytes.length + " bajtów.");
    }
    
    ByteBuffer buffer = ByteBuffer.wrap(allBytes);
    buffer.position(4); 
    
    for (int i = 0; i < verticesCount; i++) {
        int offset = 4 + (i * 20);
        reverseBytes(allBytes, offset, 4);
        reverseBytes(allBytes, offset + 4, 8);
        reverseBytes(allBytes, offset + 12, 8);
        
        int id = buffer.getInt();
        double x = buffer.getDouble();
        double y = buffer.getDouble();
        graph.addVertex(id, x, y);
    }
    graph.calculateBounds();
  }

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