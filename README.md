# Transferprojekt: MilkCalc

### Voraussetzungen / getestet mit:
- Java 21
- Maven (3.9)
- Docker (28.3.1)
- Git (2.51.0)

## Details

**Datenbank:** PostgreSQL auf localhost:5432
- Database: `transferprojekt`
- Username: `transferprojekt`
- Password: `transferprojekt`

## Installation

### 1. Projekt klonen

```bash
git clone <repository-url>
# Wechsel in das Projektverzeichniss
cd Transferprojekt-MilkCalc
```

### 2. Datenbank starten

```bash
# PostgreSQL Datenbank mit Docker Compose starten
docker compose up -d

# Überprüfen, ob die Datenbank läuft
docker ps
```

### 3. Anwendung kompilieren
```bash
# Dependencies installieren und Code kompilieren
mvn clean compile
```

### 4. Anwendung testen

**Hinweis:**
Es ist wichtig dass die Datenbank **beim Programmstart** erreichbar ist, ansonsten schlägt die Schemavalidierung fehl und es kommt zu einem Programmabsturtz. Sobald die Applikation erfolgreich gestartet wurde können Verbindunsunterbrüche simuliert werden und sollten entsprechend gehandelt werden. 

#### 4.1 GUI starten
```bash
# Anwendung starten
mvn spring-boot:run
```
Datenbank mit Testdaten befüllen:
- Register Daten > Testdaten einfügen
Datenbank leeren:
- Register Daten > Alle Daten löschen

#### 4.2 CLI starten (nur simple Tests)
- Hinzufügen/Anzeigen/Löschen von Lieferanten
- Automatisches einfügen vordefinerter Testdatensätze
- Leeren der Datentabellen

Wurde für die Abgabe im Modul Realtional Databases umgesetzt<br>
*(Commit vom 23.09.25: 593980d9a81d3e2e22a170e830b4488f1df130c7)*

```bash
# CLI Anwendung starten
mvn spring-boot:run -Pterminal
```

``` 
=================
Select operation:
1. Add supplier
2. List suppliers
3. Delete a supplier (by UUID)
4. Insert test data
5. Flush all data tables
0. Exit
   Selection:
```