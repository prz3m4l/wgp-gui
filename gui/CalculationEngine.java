import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CalculationEngine {
  public void runGraphAlgorithm(String inputPath, String algorithm, int iterations, boolean binary, Graph graph)
      throws Exception {
    List<String> commands = new ArrayList<>();
    commands.add("./graf_planarny");
    commands.add("-i");
    commands.add(inputPath);
    commands.add("-o");
    commands.add("wynik.txt");
    commands.add("-a");
    commands.add(algorithm);
    commands.add("-n");
    commands.add(Integer.toString(iterations));
    if (binary) {
      commands.add("-f");
      commands.add("bin");
    }
    ProcessBuilder command = new ProcessBuilder(commands);
    try {
      Process process = command.start();
      int exitCode = process.waitFor();
      if (exitCode == 0) {
        FileParser parser = new FileParser();
        parser.loadFullGraph("wynik.txt", inputPath, graph, binary);
      } else {
        throw new Exception(getErrorMessage(exitCode));
      }
    } catch (IOException | InterruptedException e) {
      throw new Exception("Nie odnaleziono lub nie można uruchomić pliku wykonywalnego graf_planarny. " +
          "Upewnij się, że plik istnieje i posiada prawo do wykonania (np. chmod +x).");
    }

  }

  private String getErrorMessage(int exitCode) {
    switch (exitCode) {
      case 2:
        return "Silnik C nie może otworzyć pliku wejściowego. Sprawdź, czy plik istnieje i czy masz uprawnienia do odczytu.";
      case 4:
        return "Nie można utworzyć pliku wynikowego przez program C. Sprawdź uprawnienia katalogu docelowego.";
      case 7:
        return "Silnik obliczeniowy zgłosił błąd alokacji pamięci (kod 7). Graf może być zbyt duży. Spróbuj zmniejszyć liczbę wierzchołków.";
      case 8:
        return "Graf wejściowy jest niespójny (zawiera izolowane składowe). Program C wymaga grafu spójnego. Popraw plik wejściowy.";
      case 9:
        return "Wystąpił błąd numeryczny w symulacji (kod 9). Spróbuj uruchomić algorytm ponownie lub zwiększyć liczbę iteracji.";
      case 12:
        return "Plik wejściowy zawiera pętlę własną (krawędź do samego siebie). Algorytmy nie obsługują tego rodzaju połączeń.";
      case 13:
        return "Graf jest zbyt gęsty, by mógł być planarny (E > 3V - 6). Zmniejsz liczbę krawędzi lub użyj innego grafu.";
      default:
        return "Nieoczekiwany błąd silnika obliczeniowego (kod " + exitCode + ").";
    }
  }
}
