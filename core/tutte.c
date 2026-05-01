#include "tutte.h"
#include <math.h>
#include <stdlib.h>

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

// Funkcja pomocnicza realizująca rekurencyjne przeszukiwanie
int szukaj_cyklu_dfs(Graf *g, int u, int rodzic, int *odwiedzone, int *przodkowie, int *na_stosie) {
    odwiedzone[u] = 1;
    na_stosie[u] = 1;

    for (int i = 0; i < g->E; i++) {
        Wierzcholek *sasiad = NULL;
        
        // Identyfikacja sąsiada
        if (g->krawedzie[i].p->id == g->wierzcholki[u].id) sasiad = g->krawedzie[i].k;
        else if (g->krawedzie[i].k->id == g->wierzcholki[u].id) sasiad = g->krawedzie[i].p;
        
        if (!sasiad) continue;

        // Znalezienie indeksu sąsiada w tablicy
        int v_idx = -1;
        for(int j=0; j < g->V; j++) {
            if(g->wierzcholki[j].id == sasiad->id) { v_idx = j; break; }
        }

       	if (v_idx == -1) continue;
        if (v_idx == rodzic) continue; // Pomiń powrót bezpośrednio do rodzica

        if (na_stosie[v_idx]) {
            // Gdy wykryto cykl, znaczamy wierzchołki jako stałe (is_fixed)
            int obecny = u;
            g->wierzcholki[v_idx].is_fixed = 1;
            while (obecny != v_idx && obecny != -1) {
                g->wierzcholki[obecny].is_fixed = 1;
                obecny = przodkowie[obecny];
            }
            return 1;
        }

        if (!odwiedzone[v_idx]) {
            przodkowie[v_idx] = u;
            if (szukaj_cyklu_dfs(g, v_idx, u, odwiedzone, przodkowie, na_stosie)) return 1;
        }
    }

    na_stosie[u] = 0;
    return 0;
}

int wyznacz_zewnetrzny_cykl(Graf *g) {
    int *odwiedzone = (int*)calloc(g->V, sizeof(int));
    int *przodkowie = (int*)malloc(g->V * sizeof(int));
    int *na_stosie = (int*)calloc(g->V, sizeof(int));

    if (!odwiedzone || !przodkowie || !na_stosie) {
        if (odwiedzone) free(odwiedzone);
        if (przodkowie) free(przodkowie);
        if (na_stosie) free(na_stosie);
        return 7;
    }

    for (int i = 0; i < g->V; i++) przodkowie[i] = -1;

    // Szukamy pierwszego napotkanego cyklu
    for (int i = 0; i < g->V; i++) {
        if (!odwiedzone[i]) {
            if (szukaj_cyklu_dfs(g, i, -1, odwiedzone, przodkowie, na_stosie)) break;
        }
    }

    free(odwiedzone);
    free(przodkowie);
    free(na_stosie);

    return 0;
}

void ustaw_zewnetrzne_na_kole(Graf *g, double promien) {
    int licznik_zew = 0;
    for (int i = 0; i < g->V; i++) {
        if (g->wierzcholki[i].is_fixed) licznik_zew++;
    }

    if (licznik_zew == 0) return;

    int j = 0;
    for (int i = 0; i < g->V; i++) {
        if (g->wierzcholki[i].is_fixed) {
            double kat = 2.0 * M_PI * j / licznik_zew;
            g->wierzcholki[i].x = promien + promien * cos(kat);
            g->wierzcholki[i].y = promien + promien * sin(kat);
            j++;
        }
    }
}

int oblicz_tutte(Graf *g, int it) {
  	if (g->V == 1) {
    	g->wierzcholki[0].x = 0.0;
        g->wierzcholki[0].y = 0.0;
        return 0;
   	}

    if (g->V == 2) {
        g->wierzcholki[0].x = 0.0;
        g->wierzcholki[0].y = 0.0;
        g->wierzcholki[1].x = 40.0;
        g->wierzcholki[1].y = 0.0;
        return 0;
    }

    // 1. Znalezienie cyklu
    int kod_alokacji = wyznacz_zewnetrzny_cykl(g);
    if (kod_alokacji == 7) return 7;

    // Jeśli nie znaleziono cyklu, weź pierwsze trzy
    int czy_istnieja_stale = 0;
    for(int i=0; i < g->V; i++) if(g->wierzcholki[i].is_fixed) czy_istnieja_stale = 1;
    
    if (!czy_istnieja_stale) {
        for(int i=0; i < (g->V > 3 ? 3 : g->V); i++) g->wierzcholki[i].is_fixed = 1;
    }

    // Dostosowanie rozstrzału wierzchołków na planszy proporcjonalnie do rozstrzału w f-r.c
    double promien = 25.0 * sqrt(g->V);
    if (promien < 50.0) promien = 50.0;
    // 2. Rozmieszczenie na okręgu
    ustaw_zewnetrzne_na_kole(g, promien);

    // 3. Metoda barycentryczna
    for (int iter = 0; iter < it; iter++) {
        for (int i = 0; i < g->V; i++) {
            if (g->wierzcholki[i].is_fixed) continue;

            double suma_x = 0, suma_y = 0;
            int liczba_sasiadow = 0;

            for (int e = 0; e < g->E; e++) {
                if (g->krawedzie[e].p->id == g->wierzcholki[i].id) {
                    suma_x += g->krawedzie[e].k->x;
                    suma_y += g->krawedzie[e].k->y;
                    liczba_sasiadow++;
                } else if (g->krawedzie[e].k->id == g->wierzcholki[i].id) {
                    suma_x += g->krawedzie[e].p->x;
                    suma_y += g->krawedzie[e].p->y;
                    liczba_sasiadow++;
                }
            }

            if (liczba_sasiadow > 0) {
                g->wierzcholki[i].x = suma_x / liczba_sasiadow;
                g->wierzcholki[i].y = suma_y / liczba_sasiadow;
            }
        }
    }

    return 0;
}
