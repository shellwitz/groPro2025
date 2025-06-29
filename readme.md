# Verkehrssimulation README

### IHK-Praktische Prüfung 2025 zum Mathematisch-Technischen Softwareentwickler

Dieses Projekt simuliert Verkehrsflüsse in einem vordefinierten Straßennetz. Die Simulation basiert auf einer Eingabedatei, die Einfallspunkte, Kreuzungen und einen Simulationszeitraum beschreibt. Fahrzeuge werden mit zufälliger Geschwindigkeit erzeugt und bewegen sich nach definierten Wahrscheinlichkeiten durch das Netz.

## Voraussetzungen

- Java 21 oder höher
- Betriebssystem: Windows, Linux oder macOS

## Installation

Keine Installation notwendig. Die Simulation wird über eine ausführbare JAR-Datei gestartet.

## Ausführung

1. Stellen Sie sicher, dass Java installiert ist.
2. Navigieren Sie in das Verzeichnis mit der `trafficSimulation.jar`.
3. Führen Sie das Programm mit folgendem Befehl aus:
   ```bash
   java -jar trafficSimulation.jar <Pfad/zur/Eingabedatei>
   ```
   Beispiel:
   ```bash
   java -jar trafficSimulation.jar input.txt
   ```

## Eingabeformat

Die Eingabedatei muss UTF-8 kodiert sein und drei Abschnitte enthalten:

1. **Zeitraum:**

   - Zwei positive Ganzzahlen: Gesamtdauer der Simulation (maxTime) und Taktfrequenz (clockRate).

2. **Einfallspunkte:**

   - Jede Zeile: `Name X Y Ziel Takt`

3. **Kreuzungen:**

   - Jede Zeile: `Name X Y Ziel1 Anteil1 Ziel2 Anteil2 ...`

## Ausgabe

Die Simulation erzeugt einen neuen Ordner `output_<Dateiname>` mit folgenden Dateien:

- `Plan.txt`: Enthält Straßengeometrie.
- `Statistik.txt`: Statistiken über Streckenauslastungen.
- `Fahrzeuge.txt`: Positionen der Fahrzeuge pro Zeitschritt.

## Tests

Zum Testen stehen verschiedene Eingabedateien in den Verzeichnissen `normalCases/`, `edgeCases/` und `badInputTests/` zur Verfügung. Die Datei `BadInputTestRunner.jar` kann zur Prüfung der Fehlerfälle verwendet werden.

Beispiel für die Testausführung:

```bash
java -jar BadInputTestRunner.jar badInputTests/
```

## Autor

Daniel Ebel

