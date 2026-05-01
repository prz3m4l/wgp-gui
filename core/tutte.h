#ifndef TUTTE_H
#define TUTTE_H

#include "graf.h"

// Funkcja pomocnicza realizująca rekurencyjne przeszukiwanie (do "wyznacz_zewnetrzny_cykl")
int szukaj_cyklu_dfs(Graf *g, int u, int rodzic, int *odwiedzone, int *przodkowie, int *na_stosie);

// Funkcja wyszukująca zewnętrzny cykl
int wyznacz_zewnetrzny_cykl(Graf *g);

// Funkcja główna algorytmu Tutte'a
int oblicz_tutte(Graf *g, int it);

// Funkcja pomocnicza do rozmieszczenia wierzchołków na okręgu
void ustaw_zewnetrzne_na_kole(Graf *g, double promien);

#endif //TUTTE_H
