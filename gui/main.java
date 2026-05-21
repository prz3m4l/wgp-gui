import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Main extends JFrame {
    private Graph graph = new Graph();
    private FileParser parser = new FileParser();
    private CalculationEngine engine = new CalculationEngine();
    private GraphPanel graphPanel;

    private AppToolPanel toolPanel;
    private AppStatusPanel statusPanel;
    private JPanel rightContainer;
    private JButton toggleBtn;

    private String currentInputPath = "";
    private String cleanedInputPath = "";

    public Main() {
        // Podstawowa konfiguracja okna
        setTitle("Wizualizacja grafu planarnego");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 800);
        setLocationRelativeTo(null);

        // Tworzenie głównych instancji i powiązanie z GUI
        graphPanel = new GraphPanel(graph, this);
        setJMenuBar(new AppMenuBar(this));

        setLayout(new BorderLayout(5, 5));

        toolPanel = new AppToolPanel(this, graphPanel);
        statusPanel = new AppStatusPanel();

        rightContainer = new JPanel(new BorderLayout());
        toggleBtn = new JButton("◀");
        toggleBtn.setFocusPainted(false);
        toggleBtn.setMargin(new Insets(0, 0, 0, 0));
        toggleBtn.setToolTipText("Schowaj/Pokaż panel");
        toggleBtn.setPreferredSize(new Dimension(25, 0));
        toggleBtn.addActionListener(e -> {
            boolean visible = toolPanel.isVisible();
            toolPanel.setVisible(!visible);
            toggleBtn.setText(visible ? "▶" : "◀");
            revalidate();
            repaint();
        });

        rightContainer.add(toggleBtn, BorderLayout.WEST);
        rightContainer.add(toolPanel, BorderLayout.CENTER);

        add(graphPanel, BorderLayout.CENTER);
        add(rightContainer, BorderLayout.EAST);
        add(statusPanel, BorderLayout.SOUTH);

        setPanelEnabled(toolPanel, false);

        // Zmuszenie do dopasowania skalowania po wyrenderowaniu UI
        SwingUtilities.invokeLater(() -> {
            graphPanel.autofit();
            toolPanel.updateZoomSlider((int) (graphPanel.getZoom() * 100));
        });
    }

    // Okno dialogowe wczytywania
    public void openFile(boolean binary) {
        JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                currentInputPath = fc.getSelectedFile().getAbsolutePath();
                graph.clear();
                if (binary) {
                    parser.loadFullGraph(currentInputPath, currentInputPath, graph, true);
                } else {
                    prepareCleanedFile();
                    java.lang.reflect.Method m = FileParser.class.getDeclaredMethod("readEdges", String.class,
                            Graph.class);
                    m.setAccessible(true);
                    m.invoke(parser, cleanedInputPath, graph);
                }
                setPanelEnabled(toolPanel, true);

                if ("Tutte".equalsIgnoreCase(toolPanel.getSelectedAlgorithm())) {
                    toolPanel.setIterationsEnabled(false);
                }

                updateUIState("Wczytano plik");
            } catch (Exception ex) {
                Throwable target = ex;
                if (ex instanceof java.lang.reflect.InvocationTargetException) {
                    target = ((java.lang.reflect.InvocationTargetException) ex).getTargetException();
                }
                String msg = target.getMessage() != null ? target.getMessage() : target.toString();
                if (msg.startsWith("Błąd wczytywania pliku!")) {
                    showError(msg);
                } else {
                    showError("Błąd wczytywania: " + msg);
                }
            }
        }
    }

    // Uruchomienie obliczeń w zewnętrznym silniku C
    public void runAlgorithm() {
        if (currentInputPath.isEmpty()) {
            showWarning("Najpierw wczytaj plik z listą krawędzi!");
            return;
        }
        if (graph.getVertexCount() == 0) {
            showWarning("Graf jest pusty. Wczytaj poprawne dane przed uruchomieniem algorytmu.");
            return;
        }
        try {
            String selectedAlgo = toolPanel.getSelectedAlgorithm();
            String algoParam = "tutte".equalsIgnoreCase(selectedAlgo) ? "tutte" : "fr";

            int iterations = 0;
            if (!"tutte".equals(algoParam)) {
                try {
                    iterations = Integer.parseInt(toolPanel.getIterations().trim());
                    if (iterations <= 0)
                        throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    showWarning("Liczba iteracji musi być całkowitą liczbą dodatnią!");
                    return;
                }
            }

            boolean isBinary = toolPanel.isBinaryFormat();
            String pathToUse = (cleanedInputPath != null && !cleanedInputPath.isEmpty()) ? cleanedInputPath
                    : currentInputPath;
            engine.runGraphAlgorithm(pathToUse, algoParam, iterations, isBinary, graph);

            graphPanel.autofit();
            updateUIState("Obliczono układ (" + selectedAlgo + ")");
        } catch (Exception ex) {
            showError("Błąd silnika: " + ex.getMessage());
        }
    }

    // Walidacja i przesuwanie węzła poprzez formularz w UI
    public void applyCoords() {
        if (graphPanel.getSelectedVertex() != null) {
            try {
                String xText = toolPanel.getXText().replace(',', '.').trim();
                String yText = toolPanel.getYText().replace(',', '.').trim();

                double nx = Double.parseDouble(xText);
                double ny = Double.parseDouble(yText);

                if (nx < 12 || nx > 4988 || ny < 12 || ny > 4988) {
                    showWarning("Współrzędne muszą znajdować się w obszarze roboczym (12 - 4988).");
                    return;
                }

                if (Double.isNaN(nx) || Double.isInfinite(nx) || Double.isNaN(ny) || Double.isInfinite(ny)) {
                    showWarning("Współrzędne muszą być skończonymi liczbami rzeczywistymi.");
                    return;
                }

                boolean clamped = false;
                double minX = 12.0, maxX = 5000.0 - 12.0;
                double minY = 12.0, maxY = 5000.0 - 12.0;

                if (nx < minX) {
                    nx = minX;
                    clamped = true;
                } else if (nx > maxX) {
                    nx = maxX;
                    clamped = true;
                }

                if (ny < minY) {
                    ny = minY;
                    clamped = true;
                } else if (ny > maxY) {
                    ny = maxY;
                    clamped = true;
                }

                graphPanel.getSelectedVertex().setX(nx);
                graphPanel.getSelectedVertex().setY(ny);

                if (clamped) {
                    toolPanel.setXText(String.format(java.util.Locale.US, "%.2f", nx));
                    toolPanel.setYText(String.format(java.util.Locale.US, "%.2f", ny));
                    notifyVertexOutOfBounds();
                }

                graphPanel.repaint();
            } catch (NumberFormatException ex) {
                showWarning("Wprowadzona wartość nie jest poprawną liczbą. Użyj formatu np. 123.45");
            }
        }
    }

    // Wybór ścieżki i eksport
    public void saveAction(boolean binary) {
        JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                if (binary) {
                    parser.saveToBinary(fc.getSelectedFile().getAbsolutePath(), graph);
                } else {
                    parser.saveToText(fc.getSelectedFile().getAbsolutePath(), graph);
                }
                statusPanel.setLabel("Zapisano pomyślnie.");
            } catch (Exception ex) {
                showError("Błąd zapisu: " + ex.getMessage());
            }
        }
    }

    // Odświeżenie pól tekstowych gdy wybierzemy kursorem obiekt
    public void updateSelectedInfo(Vertex v) {
        toolPanel.setVertexInfo(v);
    }

    // Wspólna aktualizacja stanu całego UI
    private void updateUIState(String msg) {
        statusPanel.updateState(msg, graph.getVertexCount(), graph.getEdgesCount());

        graphPanel.autofit();

        if (graph.getVertexCount() > 0) {
            graph.calculateBounds();
            if (graph.getMinX() < 12 || graph.getMaxX() > 4988 ||
                    graph.getMinY() < 12 || graph.getMaxY() > 4988) {

                int option = JOptionPane.showConfirmDialog(this,
                        "Wykryto wierzchołki poza obszarem roboczym. Czy chcesz je automatycznie przeskalować do obszaru 5000x5000?",
                        "Wierzchołki poza obszarem", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (option == JOptionPane.YES_OPTION) {
                    graphPanel.normalizeToWorkspace();
                }
            }
        }

        updateZoomSlider((int) (graphPanel.getZoom() * 100));
        graphPanel.repaint();
    }

    // Bezpośrednie wywołanie timera dla statusPanel
    public void notifyVertexOutOfBounds() {
        statusPanel.notifyError("Uwaga: Próba przesunięcia wierzchołka poza obszar roboczy!", graph.getVertexCount(),
                graph.getEdgesCount());
    }

    // Powiadomienie slidera ze spodu GraphPanel
    public void updateZoomSlider(int value) {
        toolPanel.updateZoomSlider(value);
    }

    // Blokada poszczególnych elementów przed wczytaniem pliku
    private void setPanelEnabled(JPanel panel, boolean enabled) {
        panel.setEnabled(enabled);
        for (Component cp : panel.getComponents()) {
            if (cp instanceof JPanel) {
                setPanelEnabled((JPanel) cp, enabled);
            } else {
                cp.setEnabled(enabled);
            }
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Błąd", JOptionPane.ERROR_MESSAGE);
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Ostrzeżenie", JOptionPane.WARNING_MESSAGE);
    }

    // Usunięcie linii zaczynających się od '#' (np. komentarzy dla .txt)
    private void prepareCleanedFile() {
        if (currentInputPath == null || currentInputPath.isEmpty() || currentInputPath.endsWith(".bin")) {
            cleanedInputPath = currentInputPath;
            return;
        }
        try {
            java.util.List<String> lines = java.nio.file.Files.readAllLines(java.nio.file.Paths.get(currentInputPath));
            java.util.List<String> cleanedLines = new java.util.ArrayList<>();
            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    cleanedLines.add(line);
                }
            }
            java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("graph_clean_", ".txt");
            java.nio.file.Files.write(tempFile, cleanedLines);
            cleanedInputPath = tempFile.toAbsolutePath().toString();
            tempFile.toFile().deleteOnExit();
        } catch (Exception e) {
            cleanedInputPath = currentInputPath;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}