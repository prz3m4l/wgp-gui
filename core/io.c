#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "io.h"

#define BUFSIZE 128

Wierzcholek* pobierz_lub_dodaj(Graf* graf, unsigned int id) {
    int i;
    for (i = 0; i < graf->V; i++) {
		if(graf->wierzcholki[i].id == id) {
        	return &graf->wierzcholki[i];
        }
	}

    // gdy nie znaleziono indeksu, tworzony jest nowy
    int index = graf->V;
    graf->wierzcholki[index].id = id;
    graf->wierzcholki[index].x = 0;
    graf->wierzcholki[index].y = 0;
    graf->wierzcholki[index].dx = 0;
    graf->wierzcholki[index].dy = 0;
    graf->wierzcholki[index].degree = 0;
    graf->wierzcholki[index].is_fixed= 0;
    graf->V++;

   	return &graf->wierzcholki[index];
}

Graf* wczytaj_graf(const char* plik, int *kod_bledu) {
	FILE* f = fopen(plik, "r");
    if (f == NULL) {
      	*kod_bledu = 2;
      	return NULL;
	}

    int l_krawedzi = 0;
    char buf[BUFSIZE];
    while (fgets(buf, BUFSIZE, f)) {
    	l_krawedzi++;
    }

    Graf* graf = (Graf*)malloc(sizeof(Graf));
    if (graf == NULL) {
      	*kod_bledu = 8;
        fclose(f);
        return NULL;
    }
    graf->E = l_krawedzi;
    graf->V = 0;

    graf->krawedzie = (Krawedz*)malloc(graf->E * sizeof(Krawedz));
    graf->wierzcholki = (Wierzcholek*)malloc(2 * graf->E * sizeof(Wierzcholek));

    if (graf->krawedzie == NULL || graf->wierzcholki == NULL) {
      	*kod_bledu = 8;
    	if (graf->krawedzie != NULL) free(graf->krawedzie);
        if (graf->wierzcholki != NULL) free(graf->wierzcholki);
        free(graf);
        return NULL;
    }

    rewind(f);

    int i = 0;
    char nazwa_kr[MAX_ID_LEN];
    unsigned int id_p, id_k;
    double waga;
    int przeczytano;

    while((przeczytano = fscanf(f, "%15s %d %d %lf", nazwa_kr, &id_p, &id_k, &waga)) != EOF) {
      	if (przeczytano != 4) {
            *kod_bledu = 3;
            fclose(f);
            zwolnij_graf(graf);
            return NULL;
        }
		if (id_p == id_k) {
            *kod_bledu = 12;
            fclose(f);
            zwolnij_graf(graf);
            return NULL;
        }

    	strcpy(graf->krawedzie[i].nazwa, nazwa_kr);
        graf->krawedzie[i].waga = waga;
        graf->krawedzie[i].p = pobierz_lub_dodaj(graf, id_p);
        graf->krawedzie[i].k = pobierz_lub_dodaj(graf, id_k);
        graf->krawedzie[i].p->degree++;
        graf->krawedzie[i].k->degree++;
        i++;
    }

    if (graf->V == 0) {
      	*kod_bledu = 3;
        fclose(f);
        zwolnij_graf(graf);
       	return NULL;
    }

    fclose(f);
    *kod_bledu = 0;
    return graf;
}

int sprawdz_spojnosc(Graf *graf) {
	if (graf->V == 1) return 1;

    int *odwiedzone = (int*)calloc(graf->V, sizeof(int));
    if (odwiedzone == NULL) return -1;
    odwiedzone[0] = 1;

    int dodany;
    do {
    	dodany = 0;
    	for (int i = 0; i < graf->E; i++) {
			int p = graf->krawedzie[i].p - graf->wierzcholki;
   	        int k = graf->krawedzie[i].k - graf->wierzcholki;

	        if (odwiedzone[p] == 1 && odwiedzone[k] == 0) {
	        	odwiedzone[k] = 1;
	        	dodany = 1;
        	}
        	else if (odwiedzone[p] == 0 && odwiedzone[k] == 1) {
          		odwiedzone[p] = 1;
           		dodany = 1;
        	}
   		}
	} while (dodany == 1);

    int spojny = 1;
    for (int i = 0; i < graf->V; i++) {
      	if (!odwiedzone[i]) {
          	spojny = 0;
            break;
      	}
    }
    free(odwiedzone);
    return spojny;
}

int zapisz_graf_txt(Graf *graf, const char *plik) {
	FILE *f = fopen(plik, "w");
    if (f == NULL) return 4;

    fprintf(f, "%d\n", graf->V);
	for(int i = 0; i < graf->V; i++) {
        fprintf(f, "%d %.4f %.4f\n", graf->wierzcholki[i].id, graf->wierzcholki[i].x, graf->wierzcholki[i].y);
    }
    fclose(f);
    return 0;
}

int zapisz_graf_bin(Graf *graf, const char *plik) {
  	FILE *f = fopen(plik, "wb");
    if (f == NULL) return 4;

    fwrite(&(graf->V), sizeof(int), 1, f);
    for(int i = 0; i < graf->V; i++) {
      	fwrite(&(graf->wierzcholki[i].id), sizeof(unsigned int), 1, f);
        fwrite(&graf->wierzcholki[i].x, sizeof(double), 1, f);
        fwrite(&graf->wierzcholki[i].y, sizeof(double), 1, f);
    }

    fclose(f);
    return 0;
}

void zwolnij_graf(Graf *graf) {
  	if (graf != NULL) {
    	if (graf->wierzcholki != NULL) free(graf->wierzcholki);
        if (graf->krawedzie != NULL) free(graf->krawedzie);
        free(graf);
    }
}

