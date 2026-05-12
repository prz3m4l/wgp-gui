# Wizualizacja Grafu Planarnego

System do wizualizacji i optymalizacji układu grafów płaskich przy pomocy algorytmów siłowych.

## Struktura Projektu
- `core/` - Silnik obliczeniowy napisany w języku C (Fruchterman-Reingold, Tutte).
- `gui/` - Aplikacja kliencka Java Swing do interaktywnej wizualizacji.
- `data/` - Przykładowe pliki z definjami grafów.

## Funkcje
- **Algorytmy:** Implementacja algorytmu Fruchtermana-Reingolda oraz Tutte'a.
- **Interakcja:** Przeciąganie wierzchołków, ręczna edycja współrzędnych, zoom, przesuwanie widoku (Pan).
- **Obsługa danych:** Wczytywanie i zapisywanie grafów w formacie tekstowym oraz binarnym (Big/Little Endian).
- **Obszar roboczy:** Wirtualna przestrzeń 5000x5000 z systemem Autofit i walidacją współrzędnych.

## Instrukcja Uruchomienia

### 1. Kompilacja i uruchomienie (Metoda uproszczona)
W folderze głównym:
```bash
make
```
To polecenie skompiluje pliki Java i uruchomi GUI.

### 2. Kompilacja silnika C
Jeśli silnik w folderze `core/` nie był jeszcze budowany:
```bash
cd core
make
cd ..
```

### 3. Sposób użycia
1. Wczytaj graf (`Plik` -> `Wczytaj graf`).
2. Wybierz algorytm w panelu bocznym i kliknij `Uruchom`.
3. Użyj `Autofit`, aby wycentrować widok.
4. Nawiguj używając myszki (LPM - zaznaczanie/przeciąganie, tryb Pan - przesuwanie kamery).
