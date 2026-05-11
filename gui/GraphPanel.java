import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GraphPanel extends JPanel {
    // Pola klasy i parametry wizualne
    private Graph graph;
    private Vertex selectedVertex = null;
    private boolean showLabels = true;
    private boolean showWeights = false;
    private static final int V_RAD = 12;
    private double zoom = 1.0;

    public GraphPanel(Graph graph, Main parent) {
        // Konfiguracja panelu i obsługa myszy
        this.graph = graph;
        setBackground(Color.WHITE);

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                double tx = (getWidth() / 2.0) * (1.0 - zoom);
                double ty = (getHeight() / 2.0) * (1.0 - zoom);
                int mouseX = (int) ((e.getX() - tx) / zoom);
                int mouseY = (int) ((e.getY() - ty) / zoom);
                
                selectedVertex = findVertex(mouseX, mouseY);
                parent.updateSelectedInfo(selectedVertex);
                repaint();
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                if (selectedVertex != null) {
                    double tx = (getWidth() / 2.0) * (1.0 - zoom);
                    double ty = (getHeight() / 2.0) * (1.0 - zoom);
                    selectedVertex.setX((e.getX() - tx) / zoom);
                    selectedVertex.setY((e.getY() - ty) / zoom);
                    parent.updateSelectedInfo(selectedVertex);
                    repaint();
                }
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    // Wyszukiwanie wierzchołka pod kursorem
    private Vertex findVertex(int x, int y) {
        for (Vertex v : graph.getVertices().values()) {
            if (Math.hypot(v.getX() - x, v.getY() - y) < V_RAD * 2) return v;
        }
        return null;
    }

    // Skalowanie grafu do rozmiaru okna
    public void autofit() {
        if (graph.getVertexCount() == 0) return;
        graph.calculateBounds();
        double minX = graph.getMinX(), maxX = graph.getMaxX();
        double minY = graph.getMinY(), maxY = graph.getMaxY();
        
        double gWidth = maxX - minX;
        double gHeight = maxY - minY;
        if (gWidth == 0) gWidth = 100; if (gHeight == 0) gHeight = 100;

        double scale = Math.min((getWidth() - 150) / gWidth, (getHeight() - 150) / gHeight);
        if (scale > 2.0) scale = 2.0;

        double offsetX = (getWidth() - gWidth * scale) / 2.0;
        double offsetY = (getHeight() - gHeight * scale) / 2.0;
        
        for (Vertex v : graph.getVertices().values()) {
            v.setX(offsetX + (v.getX() - minX) * scale);
            v.setY(offsetY + (v.getY() - minY) * scale);
        }
    }

    // Renderowanie grafu
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Zoom od środka ekranu
        double tx = (getWidth() / 2.0) * (1.0 - zoom);
        double ty = (getHeight() / 2.0) * (1.0 - zoom);
        g2.translate(tx, ty);
        g2.scale(zoom, zoom);

        // Rysowanie krawędzi
        g2.setColor(Color.DARK_GRAY);
        for (Edge e : graph.edges) {
            int x1 = (int) e.getSource().getX();
            int y1 = (int) e.getSource().getY();
            int x2 = (int) e.getTarget().getX();
            int y2 = (int) e.getTarget().getY();
            
            g2.drawLine(x1, y1, x2, y2);
            
            if (showWeights) {
                String weightStr = String.valueOf(e.getWeight());
                FontMetrics fm = g2.getFontMetrics();
                int midX = (x1 + x2) / 2;
                int midY = (y1 + y2) / 2;
                g2.drawString(weightStr, midX - fm.stringWidth(weightStr) / 2, midY - 5);
            }
        }

        // Rysowanie wierzchołków i etykiet
        for (Vertex v : graph.getVertices().values()) {
            int vx = (int) v.getX();
            int vy = (int) v.getY();
            
            g2.setColor(v == selectedVertex ? Color.WHITE : Color.BLACK);
            g2.fillOval(vx - V_RAD, vy - V_RAD, V_RAD * 2, V_RAD * 2);
            
            g2.setColor(Color.BLACK);
            g2.drawOval(vx - V_RAD, vy - V_RAD, V_RAD * 2, V_RAD * 2);
            
            if (showLabels) {
                String label = String.valueOf(v.id);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label, vx - fm.stringWidth(label) / 2, vy - V_RAD - 5);
            }
        }
    }

    // Zarządzanie opcjami widoku
    public void setShowLabels(boolean s) { this.showLabels = s; repaint(); }
    public void setShowWeights(boolean s) { this.showWeights = s; repaint(); }
    public void setZoom(double z) { this.zoom = z; repaint(); }
    public Vertex getSelectedVertex() { return selectedVertex; }
}