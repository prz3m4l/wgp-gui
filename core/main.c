#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "f-r.h"
#include "tutte.h"
#include "io.h"
#include "graf.h"
#include "logger.h"

int main(int argc, char* argv[]) {

    if (argc == 1) {
        fprintf(stderr, "Blad: Nie podano zadnych argumentow.\n");
        wypisz_pomoc();
        return 1;
    }

    char *in = NULL;
    char *out = NULL;
    int algorytm = 0; // 0 = F-R, 1 = Tutte
    int format = 0; // 0 = txt, 1 = bin
    int debug = 0; // flaga debugowania; 0 = wyłączona, 1 = włączona
    int iteracje_uzytkownika = 0; // jeśli zostanie 0 - program sam dobiera ilość iteracji

    int i;
    for (i = 1; i < argc; i++) {
        if (strcmp(argv[i], "-h") == 0) {
          	wypisz_pomoc();
            return 0;
        }
        else if (strcmp(argv[i], "-d") == 0) {
          	debug = 1;
        }
        else if (strcmp(argv[i], "-i") == 0 && i + 1 < argc) {
            in = argv[++i];
        }
        else if (strcmp(argv[i], "-o") == 0 && i + 1 < argc) {
        	if (i + 1 < argc && argv[i+1][0] != '-') {
                out = argv[++i];
            } else {
                fprintf(stderr, "Blad: Oczekiwano nazwy pliku po fladze -o.\n");
                wypisz_pomoc(); return 11;
            }
        }
        else if (strcmp(argv[i], "-a") == 0 && i + 1 < argc) {
            i++;
            if (strcmp(argv[i], "tutte") == 0) {
                algorytm = 1;
            }
            else if (strcmp(argv[i], "fr") == 0) {
              	algorytm = 0;
            }
            else {
                fprintf(stderr, "Blad: Nierozpoznany algorytm.\n");
                wypisz_pomoc();
                return 5;
            }
        }
        else if (strcmp(argv[i], "-f") == 0 && i + 1 < argc) {
            i++;
            if (strcmp(argv[i], "bin") == 0) {
                format = 1;
            }
            else if (strcmp(argv[i], "txt") == 0) {
              	format = 0;
            }
            else {
                fprintf(stderr, "Blad: Nierozpoznany format.\n");
                wypisz_pomoc();
                return 6;
            }
        }
        else if (strcmp(argv[i], "-n") == 0 && i + 1 < argc) {
            iteracje_uzytkownika = atoi(argv[++i]);
        }
        else {
            fprintf(stderr, "Blad: Nierozpoznany argument: '%s'\n", argv[i]);
            wypisz_pomoc();
            return 10;
        }
    }

	// Zabezpieczenie, gdyby ktos nie podal nazw plików
    if (in == NULL || out == NULL) {
        fprintf(stderr, "Blad: Nie podano pliku wejsciowego lub wyjsciowego.\n");
        wypisz_pomoc();
        return 1;
    }

    int kod_bledu = 0;
    Graf *g = wczytaj_graf(in, &kod_bledu);
    if (g == NULL) {
     	if (kod_bledu == 2) fprintf(stderr, "Blad: Nie mozna otworzyc pliku wejsciowego.\n");
        if (kod_bledu == 3) fprintf(stderr, "Blad: Nieprawidlowy format danych w pliku.\n");
        if (kod_bledu == 7) fprintf(stderr, "Blad: Nieudana alokacja pamieci.\n");
		if (kod_bledu == 12) fprintf(stderr, "Blad: Wykryto petle wlasna.\n");
        return kod_bledu;
    }

    // Weryfikacja warunku koniecznego planarności (Wzór Eulera)
	if (g->V >= 3 && g->E > 3 * g->V - 6) {
    	fprintf(stderr, "Blad: Graf jest zbyt gesty, by mogl byc planarny (E > 3V - 6).\n");
    	zwolnij_graf(g);
    	return 13;
	}

    // Jeśli włączono flagę -d
    if (debug) wypisz_debug(g);

    // Sprawdzanie spójności
    int spojnosc = sprawdz_spojnosc(g);
    if (spojnosc == -1) {
        fprintf(stderr, "Blad: Nieudana alokacja pamieci.\n");
        zwolnij_graf(g);
        return 7;
    } else if (spojnosc == 0) {
        fprintf(stderr, "Blad: Graf wejsciowy jest niespojny.\n");
        zwolnij_graf(g);
        return 8;
    }

    // Ustawienie ilości iteracji (zależnie od użytkownika lub uruchamianego algorytmu)
    int iteracje = 0;
    if (iteracje_uzytkownika > 0) { // nawet jeśli użytkownik poda ujemną liczbę, nie ma szans na błąd
      	iteracje = iteracje_uzytkownika;
    }
    else {
      	if (algorytm == 0) {
          	iteracje = 100 + (5 * g->V);
    	} else {
      		iteracje = 100 + (10 * g->V);
    	}
    }
    printf("Liczba iteracji do symulacji: %d\n", iteracje);

    // Odpalenie algorytmu
    int kod_algorytmu = (algorytm == 0) ? oblicz_f_r(g, iteracje) : oblicz_tutte(g, iteracje);
    if (kod_algorytmu != 0) {
        if (kod_algorytmu == 9) fprintf(stderr, "Blad: Dzielenie przez zero w symulacji.\n");
        if (kod_algorytmu == 7) fprintf(stderr, "Blad: Nieudana alokacja pamieci.\n");
        zwolnij_graf(g);
        return kod_algorytmu;
    }

    // Jeśli włączono flagę -d
    if (debug) wypisz_wyniki_terminal(g);

    // Zapis wynikow
    int kod_zapisu = 0;
    if (format == 0) {
        kod_zapisu = zapisz_graf_txt(g, out);
        printf("Zapisano wyniki do pliku tekstowego: %s\n", out);
    } else {
        kod_zapisu = zapisz_graf_bin(g, out);
        printf("Zapisano wyniki do pliku binarnego: %s\n", out);
    }

    if (kod_zapisu != 0) {
        fprintf(stderr, "Blad: Nie mozna utworzyc pliku wyjsciowego.\n");
        zwolnij_graf(g);
        return 4;
    }

    // Sprzątanie pamięci
    zwolnij_graf(g);

    return 0;
}
