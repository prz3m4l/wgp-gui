import javax.swing.*;
import java.awt.*;

public class AppToolPanel extends JPanel {
    private Main parent;
    private GraphPanel graphPanel;
    
    private JComboBox<String> algoCombo = new JComboBox<>(new String[]{"Fruchterman-Reingold", "Tutte"});
    private JComboBox<String> formatCombo = new JComboBox<>(new String[]{"Tekstowy", "Binarny"});
    private JTextField iterField = new JTextField("100");
    private JSlider zoomSlider = new JSlider(5, 200, 50);
    private JTextField xField = new JTextField(5);
    private JTextField yField = new JTextField(5);
    private JLabel idLabel = new JLabel("ID: -");

    public AppToolPanel(Main parent, GraphPanel graphPanel) {
        this.parent = parent;
        this.graphPanel = graphPanel;
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setPreferredSize(new Dimension(220, 0));

        buildAlgorithmSection();
        add(Box.createRigidArea(new Dimension(0, 20)));
        buildViewSection();
        add(Box.createRigidArea(new Dimension(0, 20)));
        buildEditSection();
        add(Box.createRigidArea(new Dimension(0, 20)));
        buildVisibilitySection();
    }

    // --- Sekcja wyboru i konfiguracji algolrytmu ---
    private void buildAlgorithmSection() {
        JLabel title1 = new JLabel("USTAWIENIA SILNIKA");
        title1.setFont(new Font("SansSerif", Font.BOLD, 12));
        title1.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(title1);
        add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel algoLabel = new JLabel("Algorytm:");
        algoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(algoLabel);
        add(Box.createRigidArea(new Dimension(0, 5)));

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
        add(algoCombo);
        add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel iterLabel = new JLabel("Iteracje:");
        iterLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(iterLabel);
        
        iterField.setMaximumSize(new Dimension(180, 25));
        iterField.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(iterField);
        add(Box.createRigidArea(new Dimension(0, 10)));
        
        JLabel formatLabel = new JLabel("Format danych (C):");
        formatLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(formatLabel);
        
        formatCombo.setMaximumSize(new Dimension(180, 25));
        formatCombo.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(formatCombo);
        add(Box.createRigidArea(new Dimension(0, 10)));
        
        JButton runBtn = new JButton("Uruchom");
        runBtn.addActionListener(e -> parent.runAlgorithm());
        runBtn.setMaximumSize(new Dimension(180, 30));
        runBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(runBtn);
    }

    // --- Sekcja sterowania widokiem ---
    private void buildViewSection() {
        JLabel viewTitle = new JLabel("WIDOK");
        viewTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        viewTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(viewTitle);
        add(Box.createRigidArea(new Dimension(0, 5)));

        JLabel zoomLabel = new JLabel("Zoom:");
        zoomLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(zoomLabel);
        
        zoomSlider.setAlignmentX(Component.CENTER_ALIGNMENT);
        zoomSlider.addChangeListener(e -> graphPanel.setZoom(zoomSlider.getValue() / 100.0));
        add(zoomSlider);
        graphPanel.setZoom(0.5); 
        add(Box.createRigidArea(new Dimension(0, 5)));

        JButton autofitBtn = new JButton("Autofit");
        autofitBtn.addActionListener(e -> { 
            zoomSlider.setValue(100);
            graphPanel.setZoom(1.0);
            graphPanel.autofit(); 
            parent.repaint(); 
        });
        autofitBtn.setMaximumSize(new Dimension(180, 30));
        autofitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(autofitBtn);
    }

    // --- Sekcja edycji parametrów zaznaczonego wierzchołka ---
    private void buildEditSection() {
        JLabel title2 = new JLabel("EDYCJA WIERZCHOŁKA");
        title2.setFont(new Font("SansSerif", Font.BOLD, 11));
        title2.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(title2);
        
        idLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(idLabel);
        add(Box.createRigidArea(new Dimension(0, 5)));

        JPanel xPanel = new JPanel(new BorderLayout(5, 0));
        xPanel.setMaximumSize(new Dimension(180, 25));
        xPanel.add(new JLabel("X: "), BorderLayout.WEST);
        xPanel.add(xField, BorderLayout.CENTER);
        add(xPanel);

        add(Box.createRigidArea(new Dimension(0, 5)));

        JPanel yPanel = new JPanel(new BorderLayout(5, 0));
        yPanel.setMaximumSize(new Dimension(180, 25));
        yPanel.add(new JLabel("Y: "), BorderLayout.WEST);
        yPanel.add(yField, BorderLayout.CENTER);
        add(yPanel);
        
        add(Box.createRigidArea(new Dimension(0, 10)));
        
        JButton applyBtn = new JButton("Zastosuj");
        applyBtn.addActionListener(e -> parent.applyCoords());
        applyBtn.setMaximumSize(new Dimension(180, 30));
        applyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(applyBtn);
    }

    // --- Sekcja włączania warstw rysowania ---
    private void buildVisibilitySection() {
        JLabel displayTitle = new JLabel("WIDOCZNOŚĆ");
        displayTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        displayTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(displayTitle);

        add(Box.createRigidArea(new Dimension(0, 5)));

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

        add(checkPanel);
    }

    // Metody dostępowe dla klasy Main
    public String getSelectedAlgorithm() { return (String) algoCombo.getSelectedItem(); }
    public boolean isBinaryFormat() { return "Binarny".equals(formatCombo.getSelectedItem()); }
    public String getIterations() { return iterField.getText(); }
    public void setIterationsEnabled(boolean enabled) { iterField.setEnabled(enabled); }
    
    public void updateZoomSlider(int value) { zoomSlider.setValue(value); }
    
    public void setVertexInfo(Vertex v) {
        if (v != null) {
            idLabel.setText("ID: " + v.id);
            xField.setText(String.format(java.util.Locale.US, "%.2f", v.getX()));
            yField.setText(String.format(java.util.Locale.US, "%.2f", v.getY()));
        } else {
            idLabel.setText("ID: -");
            xField.setText("");
            yField.setText("");
        }
    }
    
    public String getXText() { return xField.getText(); }
    public String getYText() { return yField.getText(); }
    public void setXText(String text) { xField.setText(text); }
    public void setYText(String text) { yField.setText(text); }
}
