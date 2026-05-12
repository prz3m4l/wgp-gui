import javax.swing.*;
import java.awt.*;

public class AppStatusPanel extends JPanel {
    private JLabel statusLabel;
    private Timer statusTimer;

    public AppStatusPanel() {
        // Konfiguracja panelu dolnego (Status Bar)
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setBorder(BorderFactory.createEtchedBorder());
        statusLabel = new JLabel("Stan: Oczekiwanie | Wierzchołki: 0 | Krawędzie: 0");
        add(statusLabel);
    }

    // Aktualizacja zwykłego stanu
    public void updateState(String msg, int vCount, int eCount) {
        statusLabel.setText(String.format("Stan: %s | Wierzchołki: %d | Krawędzie: %d", msg, vCount, eCount));
    }

    // Wyświetlanie błędu znikającego po 3 sekundach
    public void notifyError(String msg, int vCount, int eCount) {
        statusLabel.setText("<html><font color='red'>" + msg + "</font></html>");
        if (statusTimer != null && statusTimer.isRunning()) {
            statusTimer.restart();
        } else {
            statusTimer = new Timer(3000, e -> updateState("Gotowy", vCount, eCount));
            statusTimer.setRepeats(false);
            statusTimer.start();
        }
    }

    public void setLabel(String text) {
        statusLabel.setText(text);
    }
}
