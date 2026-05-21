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
    
    // Zwiększony wirtualny obszar roboczy
    private static final int WORK_WIDTH = 5000;
    private static final int WORK_HEIGHT = 5000;
    
    private double offsetX = 0;
    private double offsetY = 0;
    private boolean panMode = false;
    private Point lastMousePos;
    
    private final Main parent;

    // Ograniczanie przesunięcia kamery (Pan) tak, by ekran nie wyszedł poza obszar roboczy
    private void clampOffsets() {
        if (getWidth() == 0 || getHeight() == 0) return;
        
        double limitX = (WORK_WIDTH / 2.0 * zoom) - (getWidth() / 2.0);
        if (limitX < 0) offsetX = 0; // Jeśli obszar jest mniejszy niż ekran, centrujemy
        else offsetX = Math.max(-limitX, Math.min(offsetX, limitX));

        double limitY = (WORK_HEIGHT / 2.0 * zoom) - (getHeight() / 2.0);
        if (limitY < 0) offsetY = 0;
        else offsetY = Math.max(-limitY, Math.min(offsetY, limitY));
    }

    @SuppressWarnings("unused")
    private void keepParent() { if (parent != null) parent.toString(); }

    public GraphPanel(Graph graph, Main parent) {
        // Konfiguracja panelu i obsługa myszy
        this.graph = graph;
        this.parent = parent;
        setBackground(Color.WHITE);

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (panMode && SwingUtilities.isLeftMouseButton(e)) {
                    lastMousePos = e.getPoint();
                    return;
                }
                
                clampOffsets();
                double tx = (getWidth() / 2.0) - (WORK_WIDTH / 2.0 * zoom) + offsetX;
                double ty = (getHeight() / 2.0) - (WORK_HEIGHT / 2.0 * zoom) + offsetY;

                int mouseX = (int) ((e.getX() - tx) / zoom);
                int mouseY = (int) ((e.getY() - ty) / zoom);
                
                selectedVertex = findVertex(mouseX, mouseY);
                parent.updateSelectedInfo(selectedVertex);
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (panMode && SwingUtilities.isLeftMouseButton(e) && lastMousePos != null) {
                    offsetX += e.getX() - lastMousePos.x;
                    offsetY += e.getY() - lastMousePos.y;
                    lastMousePos = e.getPoint();
                    clampOffsets();
                    repaint();
                    return;
                }

                if (selectedVertex != null) {
                    clampOffsets();
                    double tx = (getWidth() / 2.0) - (WORK_WIDTH / 2.0 * zoom) + offsetX;
                    double ty = (getHeight() / 2.0) - (WORK_HEIGHT / 2.0 * zoom) + offsetY;

                    double rawX = (e.getX() - tx) / zoom;
                    double rawY = (e.getY() - ty) / zoom;

                    // Granice fizycznego panelu w przestrzeni wirtualnej
                    double viewMinX = (0 - tx) / zoom;
                    double viewMaxX = (getWidth() - tx) / zoom;
                    double viewMinY = (0 - ty) / zoom;
                    double viewMaxY = (getHeight() - ty) / zoom;

                    // Finalne granice: nie wychodzimy poza wirtualny obszar ANI poza widoczny ekran
                    double minX = Math.max(V_RAD, viewMinX + V_RAD);
                    double maxX = Math.min(WORK_WIDTH - V_RAD, viewMaxX - V_RAD);
                    double minY = Math.max(V_RAD, viewMinY + V_RAD);
                    double maxY = Math.min(WORK_HEIGHT - V_RAD, viewMaxY - V_RAD);

                    boolean clamped = false;
                    double clampedX = rawX;
                    double clampedY = rawY;

                    if (rawX < minX) { clampedX = minX; clamped = true; }
                    else if (rawX > maxX) { clampedX = maxX; clamped = true; }

                    if (rawY < minY) { clampedY = minY; clamped = true; }
                    else if (rawY > maxY) { clampedY = maxY; clamped = true; }

                    selectedVertex.setX(clampedX);
                    selectedVertex.setY(clampedY);
                    parent.updateSelectedInfo(selectedVertex);
                    repaint();

                    if (clamped) {
                        parent.notifyVertexOutOfBounds();
                    }
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

    // Skalowanie grafu do rozmiaru widocznego obszaru
    public void autofit() {
        if (graph.getVertexCount() == 0) return;
        offsetX = 0;
        offsetY = 0;
        graph.calculateBounds();
        double minX = graph.getMinX(), maxX = graph.getMaxX();
        double minY = graph.getMinY(), maxY = graph.getMaxY();
        
        double gWidth = maxX - minX;
        double gHeight = maxY - minY;
        if (gWidth == 0) gWidth = 100;
        if (gHeight == 0) gHeight = 100;

        // Skalujemy tak, aby graf zajmował ok 80% mniejszego z wymiarów widocznego okna
        double targetW = getWidth() - 100;
        double targetH = getHeight() - 100;
        if (targetW <= 0) targetW = 600;
        if (targetH <= 0) targetH = 400;

        double scale = Math.min(targetW / gWidth, targetH / gHeight);
        if (scale > 5.0) scale = 5.0; // Nie powiększaj małych grafów zbyt mocno
        scale = Math.max(scale, getMinZoom()); // Zabezpieczenie przed zbytnim oddaleniem

        this.zoom = scale; // Synchronizacja zooma
        if (parent != null) {
            parent.updateZoomSlider((int)(scale * 100));
        }

        // Środek wirtualnego obszaru roboczego
        double centerX = WORK_WIDTH / 2.0;
        double centerY = WORK_HEIGHT / 2.0;

        double offsetX = centerX - (gWidth * scale) / 2.0;
        double offsetY = centerY - (gHeight * scale) / 2.0;
        
        for (Vertex v : graph.getVertices().values()) {
            v.setX(offsetX + (v.getX() - minX) * scale);
            v.setY(offsetY + (v.getY() - minY) * scale);
        }
        repaint();
    }

    // Renderowanie grafu
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Upewniamy się, że przy zmianie rozmiaru okna lub zooma, kamera nie wyleci za ekran
        clampOffsets();

        // Centrowanie wirtualnego obszaru roboczego z uwzględnieniem przesunięcia
        double tx = (getWidth() / 2.0) - (WORK_WIDTH / 2.0 * zoom) + offsetX;
        double ty = (getHeight() / 2.0) - (WORK_HEIGHT / 2.0 * zoom) + offsetY;
        
        g2.translate(tx, ty);
        g2.scale(zoom, zoom);

        // Opcjonalne: rysowanie ramki obszaru roboczego
        g2.setColor(new Color(245, 245, 245));
        g2.fillRect(0, 0, WORK_WIDTH, WORK_HEIGHT);
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawRect(0, 0, WORK_WIDTH, WORK_HEIGHT);

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
    public void setPanMode(boolean p) { this.panMode = p; }
    public double getZoom() { return zoom; }
    
    private double getMinZoom() {
        if (getWidth() == 0 || getHeight() == 0) return 0.05;
        double minZoomX = (double) getWidth() / WORK_WIDTH;
        double minZoomY = (double) getHeight() / WORK_HEIGHT;
        return Math.max(minZoomX, minZoomY);
    }
    
    public void setZoom(double z) { 
        this.zoom = Math.max(z, getMinZoom()); 
        clampOffsets();
        repaint(); 
    }

    /*
     * Skaluje współrzędne wszystkich wierzchołków tak, aby zmieściły się w obszarze roboczym 5000x5000
     * z zachowaniem proporcji i marginesem bezpieczeństwa.
     */
    public void normalizeToWorkspace() {
        if (graph.getVertexCount() == 0) return;
        
        graph.calculateBounds();
        double minX = graph.getMinX(), maxX = graph.getMaxX();
        double minY = graph.getMinY(), maxY = graph.getMaxY();
        
        double gWidth = maxX - minX;
        double gHeight = maxY - minY;
        if (gWidth == 0) gWidth = 1;
        if (gHeight == 0) gHeight = 1;

        double margin = V_RAD + 5;
        double availableSize = 5000.0 - 2 * margin;

        double scale = Math.min(availableSize / gWidth, availableSize / gHeight);
        
        // Przesunięcie do środka obszaru 5000x5000
        double targetCenterX = 2500.0;
        double targetCenterY = 2500.0;
        double currentCenterX = (minX + maxX) / 2.0;
        double currentCenterY = (minY + maxY) / 2.0;

        for (Vertex v : graph.getVertices().values()) {
            double newX = targetCenterX + (v.getX() - currentCenterX) * scale;
            double newY = targetCenterY + (v.getY() - currentCenterY) * scale;
            v.setX(newX);
            v.setY(newY);
        }
        
        graph.calculateBounds();
        repaint();
        if (parent != null) {
            parent.updateSelectedInfo(selectedVertex);
        }
    }

    public Vertex getSelectedVertex() { return selectedVertex; }
}