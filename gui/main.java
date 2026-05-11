import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {
    // Pola klasy i komponenty UI
    private Graph graph = new Graph();
    private FileParser parser = new FileParser();
    private CalculationEngine engine = new CalculationEngine();
    private GraphPanel graphPanel;
    
    private JLabel statusLabel = new JLabel("Stan: Oczekiwanie | Wierzchołki: 0 | Krawędzie: 0");
    private JTextField xField = new JTextField(5);
    private JTextField yField = new JTextField(5);
    private JLabel idLabel = new JLabel("ID: -");
    private JComboBox<String> algoCombo = new JComboBox<>(new String[]{"fr", "tutte"});
    private String currentInputPath = "";

    public Main() {
        // Konfiguracja okna głównego
        setTitle("Wizualizacja grafu planarnego");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 800);
        setLocationRelativeTo(null);

        graphPanel = new GraphPanel(graph, this);

        setJMenuBar(createMenuBar());

        // Inicjalizacja układu i paneli
        setLayout(new BorderLayout(5, 5));
        add(graphPanel, BorderLayout.CENTER);
        add(createToolPanel(), BorderLayout.EAST);
        
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);
    }

    // Tworzenie menu górnego
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Plik");

        JMenuItem loadTxt = new JMenuItem("Wczytaj z pliku tekstowego");
        loadTxt.addActionListener(e -> openFile(false));
        
        JMenuItem loadBin = new JMenuItem("Wczytaj z pliku binarnego");
        loadBin.addActionListener(e -> openFile(true));

        JMenuItem saveRes = new JMenuItem("Zapisz wyniki");
        saveRes.addActionListener(e -> saveAction(true));

        fileMenu.add(loadTxt);
        fileMenu.add(loadBin);
        fileMenu.addSeparator();
        fileMenu.add(saveRes);
        
        menuBar.add(fileMenu);
        menuBar.add(new JMenu("Pomoc"));
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

        algoCombo.setMaximumSize(new Dimension(180, 25));
        algoCombo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(algoCombo);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        // Sekcja algorytmu
        JButton runBtn = new JButton("Uruchom");
        runBtn.addActionListener(e -> runAlgorithm());
        runBtn.setMaximumSize(new Dimension(180, 30));
        runBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(runBtn);

        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton autofitBtn = new JButton("Autofit");
        autofitBtn.addActionListener(e -> { graphPanel.autofit(); repaint(); });
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
                    java.lang.reflect.Method m = parser.getClass().getDeclaredMethod("readEdges", String.class, Graph.class);
                    m.setAccessible(true);
                    m.invoke(parser, currentInputPath, graph);
                }
                updateUIState("Wczytano plik");
            } catch (Exception ex) {
                showError("Błąd wczytywania: " + ex.getCause().getMessage());
            }
        }
    }

    // Wywołanie zewnętrznego silnika obliczeniowego
    private void runAlgorithm() {
        if (currentInputPath.isEmpty()) {
            showError("Najpierw wczytaj plik z listą krawędzi!");
            return;
        }
        try {
            String algo = (String) algoCombo.getSelectedItem();
            engine.runGraphAlgorithm(currentInputPath, algo, 600, false, graph);
            graphPanel.autofit();
            updateUIState("Obliczono układ");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    // Zastosowanie ręcznych zmian współrzędnych
    private void applyCoords() {
        if (graphPanel.getSelectedVertex() != null) {
            try {
                // Zamieniamy przecinek na kropkę przed parsowaniem
                String xText = xField.getText().replace(',', '.');
                String yText = yField.getText().replace(',', '.');
                
                double nx = Double.parseDouble(xText);
                double ny = Double.parseDouble(yText);
                
                graphPanel.getSelectedVertex().setX(nx);
                graphPanel.getSelectedVertex().setY(ny);
                graphPanel.repaint();
            } catch (NumberFormatException ex) {
                showError("Niepoprawna wartość. Współrzędna musi być prawidłową liczbą.");
            }
        }
    }

    // Zapisywanie stanu grafu
    private void saveAction(boolean resultsOnly) {
        JFileChooser fc = new JFileChooser(new java.io.File(System.getProperty("user.dir")));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                parser.saveToText(fc.getSelectedFile().getAbsolutePath(), graph);
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
        graphPanel.repaint();
    }

    // Wyświetlanie dialogu z błędem
    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Błąd", JOptionPane.ERROR_MESSAGE);
    }

    // Punkt startowy aplikacji
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}