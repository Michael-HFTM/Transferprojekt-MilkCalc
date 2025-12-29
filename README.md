# Transferprojekt: MilkCalc

### Voraussetzungen:
- Java 21
- Maven (3.9)
- Docker (28.3.1)
- Git (2.51.0)

### 1. Projekt klonen

```bash
git clone <repository-url>
cd transferprojekt
```

### 2. Datenbank starten

```bash
# PostgreSQL Datenbank mit Docker Compose starten
docker compose up -d

# Überprüfen, ob die Datenbank läuft
docker ps
```

### 3. Anwendung kompilieren und starten

```bash
# Dependencies installieren und Code kompilieren
mvn clean compile

# CLI Anwendung starten
mvn spring-boot:run -Pterminal
```

### 4. Anwendung testen

**CLI:** Simple interaktionen über Terminalanwendung
- Hinzufügen/Anzeigen/Löschen von Lieferanten
- Automatisches einfügen vordefinerter Testdatensätze
- Leeren der Datentabellen
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
**Datenbank:** PostgreSQL auf localhost:5432
- Database: `transferprojekt`
- Username: `transferprojekt`
- Password: `transferprojekt`