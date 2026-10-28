# Filius-Erweiterung: Smartphone, Drucker, Access Point (WLAN)

Erweiterung von Filius 2.14.0 um drei neue Geräte für den Unterricht.

- Branch: `feature/smartphone-drucker-wlan`
- GitHub: https://github.com/R00deboy/filius/tree/feature/smartphone-drucker-wlan (Remote `github`)
- Original-Projekt: https://gitlab.com/filius1/filius (Remote `origin`, dorthin wird nicht gepusht)
- Commits: `9092d0c` (Geräte), `85b2e15` (Umlaute in Sprachdateien, Layout des Druckfensters)

## Die neuen Geräte

### Smartphone
- Vollwertiges Endgerät wie Rechner/Notebook: IP-Konfiguration, Desktop, installierbare Anwendungen.
- Eigener Typ und eigenes Symbol (`filius.hardware.knoten.Smartphone`).
- Die Netzwerkkarte startet im WLAN-Modus. Ein Kabel ist trotzdem möglich.

### Drucker
- Endgerät mit IP-Adresse (`filius.hardware.knoten.Drucker`), ohne Desktop.
- Vorinstallierter, aktiver Druckdienst `DruckServer`, "Raw Printing" auf **TCP-Port 9100**
  (wie JetDirect/AppSocket). Jede empfangene Nachricht ist ein Druckauftrag. Der Drucker antwortet nicht.
- Aktionsmodus: Klick auf den Drucker (oder Kontextmenü "Druckaufträge anzeigen") öffnet die Liste der
  Aufträge mit Uhrzeit, Absender-IP und Text.
- Druckaufträge werden absichtlich **nicht** im Projekt gespeichert.

### Anwendung "Drucken"
- Auf jedem Rechner/Notebook/Smartphone über die Software-Installation installierbar (`DruckClient`).
- Drucker-IP und Text eingeben, dann "Drucken": TCP-Verbindung zu Port 9100, Text senden, Verbindung abbauen.
- Die zuletzt verwendete Drucker-Adresse wird im Projekt gespeichert.

### Access Point (WLAN)
- `filius.hardware.knoten.AccessPoint`, Unterklasse von `Switch`: arbeitet auf Schicht 2 wie ein Switch
  (Bridge zwischen Funk und Kabel), 16 Anschlüsse, hat eine SSID.
- Neu ist eine **Funkreichweite** (Standard 200 Bildpunkte, einstellbar in der Konfiguration),
  angezeigt als gestrichelter blauer Kreis.
- Ein WLAN-Gerät mit ausgewählter SSID wird nur **innerhalb der Reichweite** verbunden. Schiebt man es
  heraus, wird die Verbindung getrennt. Die SSID bleibt gespeichert, und das Gerät verbindet sich wieder,
  sobald es zurück in Reichweite ist. Die Logik steckt in `filius.gui.netzwerksicht.WlanVerwaltung`.
- Funkverbindungen sind gestrichelt gezeichnet, Kabel durchgezogen.

## Designentscheidungen

- Filius 2.14 hatte schon WLAN-Grundlagen (SSID am Switch, WLAN-Option bei Hosts, Funkverbindungen).
  Der Access Point baut darauf auf, statt ein zweites WLAN-System einzuführen.
- Der alte "Switch / WLAN" bleibt unverändert und hat **keine** Reichweitenbegrenzung, damit bestehende
  Projektdateien genauso funktionieren wie vorher.
- Wird eine Funkverbindung von Hand mit dem Kabelwerkzeug gezogen, übernimmt das Gerät die SSID des
  Access Points.
- Alle neuen Geräte starten wie in Filius üblich mit der IP 192.168.0.10. Im Unterricht also eigene
  Adressen vergeben.

## Symbole und Übersetzungen

- Symbole für alle drei Geräte in allen drei Filius-Stilen (`gfx/hardware/<name>.png`, `<name>E.png`,
  `<name>S.png`) und `gfx/desktop/icon_drucken.png`. Sie wurden mit Java2D erzeugt.
- Texte in Deutsch, Englisch und Französisch. Französisch ist nötig, sonst stürzt Filius mit französischer
  Oberfläche ab.
- **Achtung:** Die Sprachdateien `MessagesBundle_*.properties` sind ISO-8859-1-kodiert. Umlaute in neuen
  Zeilen immer als `\uXXXX`-Escape schreiben, sonst erscheinen sie als "Ã¼". Ein Test in
  `NeueGeraeteTest` prüft das.

## Bauen und Starten

Voraussetzungen: Java 17 (Temurin, per winget installiert) und Maven 3.9.11 unter
`C:\Users\CC-Student\tools\apache-maven-3.9.11`. Maven gibt es nicht bei winget, es ist nicht im PATH.

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
$env:Path = "$env:JAVA_HOME\bin;$env:USERPROFILE\tools\apache-maven-3.9.11\bin;$env:Path"
mvn package
```

Starten: `target\Filius.exe` oder `java -jar target\filius.jar`.

## Tests

`mvn package` führt 204 Tests aus, alle grün. Neu sind:
- `filius.hardware.knoten.NeueGeraeteTest`: Gerätetypen, Druckdienst, Reichweite, Umlaute
- `filius.software.drucken.DruckServerTest`: Speichern und Löschen von Druckaufträgen
- `filius.software.drucken.WlanUndDruckenIntegrationTest`: Ping vom Smartphone über den AP zum Rechner,
  Drucken vom Rechner zum Drucker
- `filius.rahmenprogramm.SzenarioVerwaltungNeueGeraeteTest`: Speichern/Laden der neuen Geräte,
  Laden eines bestehenden Projekts

Zusätzlich wurde alles einmal in der laufenden Oberfläche getestet: platzieren, verbinden, Reichweite,
Ping, Drucken, Speichern und Laden.

## Mögliche nächste Schritte

- Hinweis in der Host-Konfiguration, wenn das gewählte WLAN außer Reichweite ist.
- Eine Datei aus dem Dateisystem des Rechners drucken, nicht nur eingegebenen Text.
- Druckaufträge optional im Projekt speichern.
