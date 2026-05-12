import javax.swing.*;

public class AppMenuBar extends JMenuBar {
    public AppMenuBar(Main parent) {
        // Menu operacji na plikach
        JMenu fileMenu = new JMenu("Plik");

        JMenuItem loadTxt = new JMenuItem("Wczytaj graf");
        loadTxt.addActionListener(e -> parent.openFile(false));

        JMenuItem saveResTxt = new JMenuItem("Zapisz wyniki (Tekstowo)");
        saveResTxt.addActionListener(e -> parent.saveAction(false));

        JMenuItem saveResBin = new JMenuItem("Zapisz wyniki (Binarnie)");
        saveResBin.addActionListener(e -> parent.saveAction(true));

        fileMenu.add(loadTxt);
        fileMenu.addSeparator();
        fileMenu.add(saveResTxt);
        fileMenu.add(saveResBin);
        
        add(fileMenu);
        
        // Menu pomocy ze skróconą instrukcją
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
        add(helpMenu);
    }
}
