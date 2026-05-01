#include <stdio.h>
#include "logger.h"

void wypisz_debug(Graf *graf) {
    printf("\n=== TRYB DEBUG: STRUKTURA GRAFU ===\n");
    printf("Liczba wierzcholkow (V): %d\n", graf->V);
    printf("Liczba krawedzi (E): %d\n\n", graf->E);

    int ilosc = graf->V > 20 ? 20 : graf->V;

    printf("--- LISTA WIERZCHOLKOW (Pokazuje max 20) ---\n");
    for (int j = 0; j < ilosc; j++) {
        printf("Indeks [%d] | ID: %d | Stopien: %d | is_fixed: %d | Wsp: (%.1f, %.1f)\n",
        	j,
            graf->wierzcholki[j].id,
            graf->wierzcholki[j].degree,
            graf->wierzcholki[j].is_fixed,
            graf->wierzcholki[j].x,
            graf->wierzcholki[j].y);
    }

    printf("\n--- LISTA KRAWEDZI (Pokazuje max 20) ---\n");
    for (int j = 0; j < ilosc; j++) {
        printf("Krawedz '%s' | Waga: %.1f | Laczy ID: %d z ID: %d\n",
            graf->krawedzie[j].nazwa,
            graf->krawedzie[j].waga,
            graf->krawedzie[j].p->id,
            graf->krawedzie[j].k->id);
    }
    printf("===================================\n\n");
}

void wypisz_wyniki_terminal(Graf *graf) {
    printf("\n=== WYNIKI OBLICZEN (Wspolrzedne) ===\n");
    int ilosc = graf->V > 50 ? 50 : graf->V;
    for(int i = 0; i < ilosc; i++) {
        printf("ID: %d | X: %.2f | Y: %.2f\n",
            graf->wierzcholki[i].id,
            graf->wierzcholki[i].x,
            graf->wierzcholki[i].y);
    }
    if (graf->V > 50) {
        printf("... i %d kolejnych wierzcholkow (zapisano do pliku).\n", graf->V - 50);
    }
    printf("=====================================\n\n");
}

void wypisz_pomoc() {
    printf("\n");
    printf("========================================================\n");
    printf("   GENERATOR UKLADOW GRAFOW (Fruchterman-Reingold / Tutte)\n");
    printf("========================================================\n");
    printf("Uzycie programu:\n");
    printf("  ./graf_planarny -i <plik_wejsciowy> -o <plik_wyjsciowy> [opcje]\n\n");
    printf("Wymagane flagi:\n");
    printf("  -i <sciezka>  : Plik wejsciowy z definicja grafu\n");
    printf("  -o <sciezka>  : Plik wyjsciowy z zapisanymi wspolrzednymi\n");
    printf("Opcjonalne flagi:\n");
    printf("  -a <algorytm> : 'fr' (Fruchterman-Reingold - domyslny) lub 'tutte' (Tutte)\n\n");
    printf("  -f <format>   : Format wyjsciowy: 'txt' (domyslny) lub 'bin' (binarny)\n");
    printf("  -d            : Tryb debug (wypisuje strukture wczytanego grafu i wyniki bezposrednio w konsoli)\n");
    printf("  -h            : Wyswietla ten ekran pomocy\n\n");
    printf("Wazne ograniczenia programu:\n");
    printf("  - Graf musi byc spojny.\n");
    printf("  - Petle wlasne (krawedzie do samego siebie) sa zabronione.\n\n");
    printf("Przyklad:\n");
    printf("  ./graf_planarny -i graf.txt -o wynik.txt -a fr -d\n");
    printf("========================================================\n\n");
}
