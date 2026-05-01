#include <math.h>
#include <time.h>
#include <stdlib.h>
#include "f-r.h"

static double bok_planszy = 0.0;

void losuj_wspolrzedne(Graf *g) {
  	srand(time(NULL));

	bok_planszy = 50 * sqrt(g->V);
	for (int i = 0; i < g->V; i++) {
    	g->wierzcholki[i].x = ((double) rand() / (double) RAND_MAX) * bok_planszy;
        g->wierzcholki[i].y = ((double) rand() / (double) RAND_MAX) * bok_planszy;
    }
}

int oblicz_f_r(Graf *g, int it) {
  	if (g->V == 1) {
    	g->wierzcholki[0].x = 0.0;
        g->wierzcholki[0].y = 0.0;
        return 0;
   	}

    losuj_wspolrzedne(g);
    double k = 50; // bo k = sqrt(pole/il. wierzcholkow) = sqrt(2500*(g->V)/(g->V)) = 50
    double temperatura_poczatkowa = bok_planszy / 10; // maksymalny przeskok w pierwszej iteracji
	double temperatura = temperatura_poczatkowa;

    for (int iter = 0; iter < it; iter++) {
      // zerowanie sił
      	for (int i = 0; i < g->V; i++) {
            g->wierzcholki[i].dx = 0.0;
            g->wierzcholki[i].dy = 0.0;
        }
        // 1. ddpychanie - każdy od każdego
      	for (int v = 0; v < g->V; v++) {
          	for (int u = 0; u < g->V; u++) {
            	if (v != u) {
                  	double delta_x = g->wierzcholki[v].x - g->wierzcholki[u].x;
                    double delta_y = g->wierzcholki[v].y - g->wierzcholki[u].y;

                    double dist = sqrt(delta_x * delta_x + delta_y * delta_y);
                    if (dist < EPS) dist = EPS;

                    double sila_odp = k * k / dist;
                    g->wierzcholki[v].dx += (delta_x / dist) * sila_odp; // F_x = cos(a)*F
                    g->wierzcholki[v].dy += (delta_y / dist) * sila_odp; // F_y = sin(a)*F
                }
          	}
      	}

        // 2. przyciąganie - wzdłuż krawędzi dla danego wierzchołka
        for (int e = 0; e < g->E; e++) {
          	Wierzcholek *w_p = g->krawedzie[e].p;
            Wierzcholek *w_k = g->krawedzie[e].k;

            double delta_x = w_k->x - w_p->x;
            double delta_y = w_k->y - w_p->y;
            double dist = sqrt(delta_x * delta_x + delta_y * delta_y);
            if (dist < EPS) dist = EPS;

            double sila_przyciag = dist * dist / k;
            // sumowanie składowych sił przyciągania z siłami odpychania obliczonymi w poprzedniej pętli
            w_k->dx -= (delta_x / dist) * sila_przyciag; // F_x = cos(a)*F
            w_k->dy -= (delta_y / dist) * sila_przyciag; // F_y = sin(a)*F
            w_p->dx += (delta_x / dist) * sila_przyciag;
            w_p->dy += (delta_y / dist) * sila_przyciag;
       	}

        // 3. Ruch i zarządzanie temperaturą (zastyganie układu)
        for (int i = 0; i < g->V; i++) {
          	// jak bardzo siły chcą przesunąć wierzchołek (siłą wypadkowa)
          	double przesuniecie = sqrt(g->wierzcholki[i].dx * g->wierzcholki[i].dx + g->wierzcholki[i].dy * g->wierzcholki[i].dy);

            // gdy przesuniecie wyjdzie mniejsze/równe EPS, nie ruszamy wierzchołka (nieznaczna różnica)
            if (przesuniecie > EPS) {
            	// na wypadek, gdyby siły chciały przesunąć wierzchołek dalej niż pozwala "temperatura"
              	double przesuniecie_koncowe = (przesuniecie < temperatura) ? przesuniecie : temperatura;
           		if (przesuniecie == 0.0) {
                	return 9;
            	}
                // nowe współrzędne
                g->wierzcholki[i].x += (g->wierzcholki[i].dx / przesuniecie) * przesuniecie_koncowe;
                g->wierzcholki[i].y += (g->wierzcholki[i].dy / przesuniecie) * przesuniecie_koncowe;
            }
       	}

        // 4. Zmniejszenie "temperatury" w zależności od ilości iteracji
        temperatura = temperatura_poczatkowa * (1 - (double)iter/it);

        // na wypadek błędów precyzji
        if (temperatura < 0) temperatura = 0;
    }

    return 0;
}
