# Pola
JAVAC = javac
JAVA = java
ENCODING = UTF-8
GUI_DIR = gui
MAIN_CLASS = Main

# Domyślny cel: kompilacja i uruchomienie
all: run

# Kompilacja wszystkich plików .java w folderze gui
compile:
	$(JAVAC) -encoding $(ENCODING) $(GUI_DIR)/*.java

# Uruchomienie aplikacji z głównego folderu
run: compile
	$(JAVA) -cp $(GUI_DIR) $(MAIN_CLASS)

# Czyszczenie skompilowanych plików
clean:
	rm -f $(GUI_DIR)/*.class
	rm -f gui/wynik.txt
	rm -f gui/graph_clean_*.txt

.PHONY: all compile run clean
