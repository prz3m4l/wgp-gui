#ifndef IO_H
#define IO_H

#include "graf.h"
// POMOCNICZA: funkcja szukająca w tablicy wierzchołków indeksu, który jest odczytywany z
// pliku wejściowego zwraca indeks wierzchołka, w razie niepowodzenia zapisuje w tablicy nowy indeks
Wierzcholek* pobierz_lub_dodaj(Graf* graf, unsigned int id);

// funkcja wczytuje graf z pliku wejściowego i zwraca wskaźnik na zainicjowany graf
Graf* wczytaj_graf(const char* plik, int* kod_bledu);

// funkcje do zapisu
int zapisz_graf_txt(Graf* graf, const char* plik);
int zapisz_graf_bin(Graf* graf, const char* plik);

// Funkcja sprawdzająca spójność grafu, zwraca 1 jeśli graf jest spójny, 0 jeśli nie jest
int sprawdz_spojnosc(Graf *graf);

// funkcja zwalniająca zaalokowaną pamięć
void zwolnij_graf(Graf* graf);

#endif //IO_H
