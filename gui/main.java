import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {
    // Pola klasy i komponenty UI
    private Graph graph = new Graph();
    private FileParser parser = new FileParser();
    private CalculationEngine engine = new CalculationEngine();
    private GraphPanel graphPanel;
    private JPanel rightContainer;
    private JButton toggleBtn;
    
    private JLabel statusLabel = new JLabel("Stan: Oczekiwanie | Wierzchołki: 0 | Krawędzie: 0");
    private JTextField xField = new JTextField(5);
    private JTextField yField = new JTextField(5);
    private JLabel idLabel = new JLabel("ID: -");
    private JComboBox<String> algoCombo = new JComboBox<>(new String[]{"Fruchterman-Reingold", "Tutte"});
    private JComboBox<String> formatCombo = new JComboBox<>(new String[]{"Tekstowy", "Binarny"});
    private JTextField iterField = new JTextField("100");
    private JSlider zoomSlider = new JSlider(5, 200, 50);
    private JPanel toolPanel;
    private String currentInputPath = "";
    private String cleanedInputPath = "";
    private Timer statusTimer;

    public Main() {
        // Konfiguracja okna głównego
        setTitle("Wizualizacja grafu planarnego");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 800);
        setLocationRelativeTo(null);

        graphPanel = new GraphPanel(graph, this);

        setJMenuBar(createMenuBar());

        // Inicjalizacja układu z przyciskiem chowania paska
        setLayout(new BorderLayout(5, 5));
        
        toolPanel = createToolPanel();
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
        
        setPanelEnabled(toolPanel, false);
        
        // Wykonaj autofit po załadowaniu GUI, aby graf był od razu widoczny
        SwingUtilities.invokeLater(() -> {
            graphPanel.autofit();
            zoomSlider.setValue((int)(graphPanel.getZoom() * 100));
        });
        
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);
    }

    // Tworzenie menu górnego
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Plik");

        JMenuItem loadTxt = new JMenuItem("Wczytaj graf");
        loadTxt.addActionListener(e -> openFile(false));

        JMenuItem saveResTxt = new JMenuItem("Zapisz wyniki (Tekstowo)");
        saveResTxt.addActionListener(e -> saveAction(false));

        JMenuItem saveResBin = new JMenuItem("Zapisz wyniki (Binarnie)");
        saveResBin.addActionListener(e -> saveAction(true));

        fileMenu.add(loadTxt);
        fileMenu.addSeparator();
        fileMenu.add(saveResTxt);
        fileMenu.add(saveResBin);
        
        menuBar.add(fileMenu);
        
        // --- NOWA ZAKŁADKA POMOCY ---
        JMenu helpMenu = new JMenu("Pomoc");
        JMenuItem infoItem = new JMenuItem("Instrukcja i dokumentacja");
        infoItem.addActionListener(e -> {
            String helpText = "Wizualizacja Grafu Planarnego\n\n"
                            + "Skrócona instrukcja:\n"
                            + "1. Wczytaj graf z pliku krawędzi w formacie wejściowym.\n"
                            + "2. W menu po prawej wybierz algorytm (np. Fruchterman-Reingold) i uruchom go.\n"
                            + "3. Nawiguj po obszarze przybliżając ekran lub używając trybu przesuwania (Pan).\n\n"
                            + "Uwaga: Po więcej informacji i dokładny opis działania poszczególnych "
                            + "modułów odsyłamy do dokumentacji końcowej projektu.";
            JOptionPane.showMessageDialog(this, helpText, "Pomoc / O programie", JOptionPane.INFORMATION_MESSAGE);
        });
        helpMenu.add(infoItem);
        menuBar.add(helpMenu);
        
        return menuBar;
    }

    // Budowa panelu narzędzi bocznych
    private JPanel createToolPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(220, 0));

        JLabel title1 = new JLabel("USTAWIENIA SILNIKA");
        title1.setFont(new Font("SansSerif", Font.BOLD, 12));
        title1.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title1);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel algoLabel = new JLabel("Algorytm:");
        algoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(algoLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        algoCombo.setMaximumSize(new Dimension(180, 25));
        algoCombo.setAlignmentX(Component.CENTER_ALIGNMENT);
        algoCombo.addActionListener(e -> {
            String selected = (String) algoCombo.getSelectedItem();
            boolean isTutte = "Tutte".equalsIgnoreCase(selected);
            iterField.setEnabled(!isTutte);
            if (isTutte) {
                iterField.setText("-");
            } else if ("-".equals(iterField.getText())) {
                iterField.setText("100");
            }
        });
        panel.add(algoCombo);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel iterLabel = new JLabel("Iteracje:");
        iterLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(iterLabel);
        
        iterField.setMaximumSize(new Dimension(180, 25));
        iterField.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(iterField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JLabel formatLabel = new JLabel("Format danych (C):");
        formatLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(formatLabel);
        
        formatCombo.setMaximumSize(new Dimension(180, 25));
        formatCombo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(formatCombo);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Wywołanie zewnętrznego silnika obliczeniowego
        JButton runBtn = new JButton("Uruchom");
        runBtn.addActionListener(e -> runAlgorithm());
        runBtn.setMaximumSize(new Dimension(180, 30));
        runBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(runBtn);

        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Sekcja widoku
        JLabel viewTitle = new JLabel("WIDOK");
        viewTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        viewTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(viewTitle);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JLabel zoomLabel = new JLabel("Zoom:");
        zoomLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(zoomLabel);
        
        zoomSlider.setAlignmentX(Component.CENTER_ALIGNMENT);
        zoomSlider.addChangeListener(e -> {
            graphPanel.setZoom(zoomSlider.getValue() / 100.0);
        });
        panel.add(zoomSlider);
        graphPanel.setZoom(0.5); // Ustawienie początkowego zooma na 50%
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JButton autofitBtn = new JButton("Autofit");
        autofitBtn.addActionListener(e -> { 
            zoomSlider.setValue(100);
            graphPanel.setZoom(1.0);
            graphPanel.autofit(); 
            repaint(); 
        });
        autofitBtn.setMaximumSize(new Dimension(180, 30));
        autofitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(autofitBtn);

        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Sekcja edycji danych wierzchołka
        JLabel title2 = new JLabel("EDYCJA WIERZCHOŁKA");
        title2.setFont(new Font("SansSerif", Font.BOLD, 11));
        title2.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title2);
        
        idLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(idLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JPanel xPanel = new JPanel(new BorderLayout(5, 0));
        xPanel.setMaximumSize(new Dimension(180, 25));
        xPanel.add(new JLabel("X: "), BorderLayout.WEST);
        xPanel.add(xField, BorderLayout.CENTER);
        panel.add(xPanel);

        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JPanel yPanel = new JPanel(new BorderLayout(5, 0));
        yPanel.setMaximumSize(new Dimension(180, 25));
        yPanel.add(new JLabel("Y: "), BorderLayout.WEST);
        yPanel.add(yField, BorderLayout.CENTER);
        panel.add(yPanel);
        
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JButton applyBtn = new JButton("Zastosuj");
        applyBtn.addActionListener(e -> applyCoords());
        applyBtn.setMaximumSize(new Dimension(180, 30));
        applyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(applyBtn);

        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel displayTitle = new JLabel("WIDOCZNOŚĆ");
        displayTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        displayTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(displayTitle);

        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Sekcja opcji wyświetlania
        JPanel checkPanel = new JPanel();
        checkPanel.setLayout(new BoxLayout(checkPanel, BoxLayout.Y_AXIS));
        checkPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JCheckBox labels = new JCheckBox("Etykiety", true);
        labels.addActionListener(e -> graphPanel.setShowLabels(labels.isSelected()));
        checkPanel.add(labels);

        JCheckBox weights = new JCheckBox("Wagi", false);
        weights.addActionListener(e -> graphPanel.setShowWeights(weights.isSelected()));
        checkPanel.add(weights);

        JCheckBox pan = new JCheckBox("Przesuwanie (Pan)", false);
        pan.addActionListener(e -> graphPanel.setPanMode(pan.isSelected()));
        checkPanel.add(pan);

        panel.add(checkPanel);

        return panel;
    }

    // Obsługa wczytywania plików
    private void openFile(boolean binary) {
        JFileChooser fc = new JFileChooser(new java.io.File(System.getProperty("user.dir")));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                currentInputPath = fc.getSelectedFile().getAbsolutePath();
                graph.clear();
                if (binary) {
                    parser.loadFullGraph(currentInputPath, currentInputPath, graph, true);
                } else {
                    prepareCleanedFile();
                    java.lang.reflect.Method m = FileParser.class.getDeclaredMethod("readEdges", String.class, Graph.class);
                    m.setAccessible(true);
                    m.invoke(parser, cleanedInputPath, graph);
                }
                setPanelEnabled(toolPanel, true);
                
                // Ponowne zablokowanie pola iteracji po masowym odblokowaniu, jeśli wybrany jest Tutte
                if ("Tutte".equalsIgnoreCase((String) algoCombo.getSelectedItem())) {
                    iterField.setEnabled(false);
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

    // Wywołanie zewnętrznego silnika obliczeniowego
    private void runAlgorithm() {
        if (currentInputPath.isEmpty()) {
            showWarning("Najpierw wczytaj plik z listą krawędzi!");
            return;
        }
        if (graph.getVertexCount() == 0) {
            showWarning("Graf jest pusty. Wczytaj poprawne dane przed uruchomieniem algorytmu.");
            return;
        }
        try {
            String selectedAlgo = (String) algoCombo.getSelectedItem();
            String algoParam = "fr"; // domyślnie dla Fruchterman-Reingold
            if ("tutte".equalsIgnoreCase(selectedAlgo)) {
                algoParam = "tutte";
            }
            
            int iterations = 0;
            if (!"tutte".equals(algoParam)) {
                try {
                    iterations = Integer.parseInt(iterField.getText().trim());
                    if (iterations <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    showWarning("Liczba iteracji musi być całkowitą liczbą dodatnią!");
                    return;
                }
            }
            boolean isBinary = "Binarny".equals(formatCombo.getSelectedItem());
            String pathToUse = (cleanedInputPath != null && !cleanedInputPath.isEmpty()) ? cleanedInputPath : currentInputPath;
            engine.runGraphAlgorithm(pathToUse, algoParam, iterations, isBinary, graph);
            
            // engine.runGraphAlgorithm już wywołuje parser.loadFullGraph("wynik.txt", ...) 
            // jeśli exitCode == 0, więc nie dublujemy tego tutaj.
            
            graphPanel.autofit();
            updateUIState("Obliczono układ (" + selectedAlgo + ")");
        } catch (Exception ex) {
            showError("Błąd silnika: " + ex.getMessage());
        }
    }

    // Zastosowanie ręcznych zmian współrzędnych
    private void applyCoords() {
        if (graphPanel.getSelectedVertex() != null) {
            try {
                // Zamieniamy przecinek na kropkę przed parsowaniem
                String xText = xField.getText().replace(',', '.').trim();
                String yText = yField.getText().replace(',', '.').trim();
                
                double nx = Double.parseDouble(xText);
                double ny = Double.parseDouble(yText);
                
                // Walidacja współrzędnych (obszar 5000x5000, margines V_RAD=12)
                if (nx < 12 || nx > 4988 || ny < 12 || ny > 4988) {
                    showWarning("Współrzędne muszą znajdować się w obszarze roboczym (12 - 4988).");
                    return;
                }
                
                if (Double.isNaN(nx) || Double.isInfinite(nx) || Double.isNaN(ny) || Double.isInfinite(ny)) {
                    showWarning("Współrzędne muszą być skończonymi liczbami rzeczywistymi.");
                    return;
                }

                // Ograniczanie współrzędnych do obszaru roboczego (margines 12px na promień wierzchołka)
                boolean clamped = false;
                double minX = 12.0, maxX = 5000.0 - 12.0;
                double minY = 12.0, maxY = 5000.0 - 12.0;

                if (nx < minX) { nx = minX; clamped = true; }
                else if (nx > maxX) { nx = maxX; clamped = true; }

                if (ny < minY) { ny = minY; clamped = true; }
                else if (ny > maxY) { ny = maxY; clamped = true; }

                graphPanel.getSelectedVertex().setX(nx);
                graphPanel.getSelectedVertex().setY(ny);
                
                // Jeśli wartości były poza zakresem, zaktualizuj pola tekstowe i wyświetl komunikat
                if (clamped) {
                    xField.setText(String.format(java.util.Locale.US, "%.2f", nx));
                    yField.setText(String.format(java.util.Locale.US, "%.2f", ny));
                    notifyVertexOutOfBounds();
                }
                
                graphPanel.repaint();
            } catch (NumberFormatException ex) {
                showWarning("Wprowadzona wartość nie jest poprawną liczbą. Użyj formatu np. 123.45");
            }
        }
    }

    // Zapisywanie stanu grafu
    private void saveAction(boolean binary) {
        JFileChooser fc = new JFileChooser(new java.io.File(System.getProperty("user.dir")));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                if (binary) {
                    parser.saveToBinary(fc.getSelectedFile().getAbsolutePath(), graph);
                } else {
                    parser.saveToText(fc.getSelectedFile().getAbsolutePath(), graph);
                }
                statusLabel.setText("Zapisano pomyślnie.");
            } catch (Exception ex) {
                showError("Błąd zapisu: " + ex.getMessage());
            }
        }
    }

    // Aktualizacja informacji o zaznaczonym elemencie
    public void updateSelectedInfo(Vertex v) {
        if (v != null) {
            idLabel.setText("ID: " + v.id);
            xField.setText(String.format("%.2f", v.getX()));
            yField.setText(String.format("%.2f", v.getY()));
        }
    }

    // Odświeżanie paska stanu i widoku
    private void updateUIState(String msg) {
        statusLabel.setText(String.format("Stan: %s | Wierzchołki: %d | Krawędzie: %d", 
                msg, graph.getVertexCount(), graph.getEdgesCount()));
        
        // Automatyczne dopasowanie widoku
        graphPanel.autofit();
        
        // Sprawdzenie czy wierzchołki nie są poza obszarem 5000x5000
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
        
        updateZoomSlider((int)(graphPanel.getZoom() * 100));
        
        graphPanel.repaint();
    }

    // Wyświetlanie dialogu z błędem
    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Błąd", JOptionPane.ERROR_MESSAGE);
    }

    // Wyświetlanie dialogu z ostrzeżeniem
    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Ostrzeżenie", JOptionPane.WARNING_MESSAGE);
    }

    // Powiadomienie o wyjściu poza obszar
    public void notifyVertexOutOfBounds() {
        statusLabel.setText("<html><font color='red'>Uwaga: Próba przesunięcia wierzchołka poza obszar roboczy!</font></html>");
        if (statusTimer != null && statusTimer.isRunning()) {
            statusTimer.restart();
        } else {
            statusTimer = new Timer(3000, e -> {
                statusLabel.setText("Stan: Gotowy | Wierzchołki: " + graph.getVertexCount() + " | Krawędzie: " + graph.getEdgesCount());
            });
            statusTimer.setRepeats(false);
            statusTimer.start();
        }
    }

    public void updateZoomSlider(int value) {
        zoomSlider.setValue(value);
    }

    // Pomocnicza metoda do włączania/wyłączania panelu
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

    // Przygotowanie pliku bez komentarzy dla silnika C
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

    // Punkt startowy aplikacji
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}