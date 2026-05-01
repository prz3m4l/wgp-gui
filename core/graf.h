#ifndef GRAF_H
#define GRAF_H

#define MAX_ID_LEN 16 // maksymalna długość nazwy krawędzi

typedef struct {
    unsigned int id;
    double x;
    double y;

    // przesunięcie współrzędnych wykorzystywane w algorytmie Fruchtermana-Reingolda
    double dx;
    double dy;

    // flaga dla algorytmu Tuttego (1 = zewnętrzny, 0 = wewnętrzny/wolny)
    unsigned int is_fixed;

    // stopień wierzchołka, do obliczania średniej w algorytmie Tuttego
    unsigned int degree;
}Wierzcholek;

typedef struct {
    char nazwa[MAX_ID_LEN];
    Wierzcholek *p;
    Wierzcholek *k;
    double waga;
}Krawedz;

typedef struct {
    int V; // liczba wierzchołków
    int E; // liczba krawędzi
    Wierzcholek *wierzcholki;
    Krawedz *krawedzie;
}Graf;

#endif //GRAF_H
