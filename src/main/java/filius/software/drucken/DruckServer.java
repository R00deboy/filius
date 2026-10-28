/*
 ** This file is part of Filius, a network construction and simulation software.
 **
 ** Originally created at the University of Siegen, Institute "Didactics of
 ** Informatics and E-Learning" by a students' project group:
 **     members (2006-2007):
 **         André Asschoff, Johannes Bade, Carsten Dittich, Thomas Gerding,
 **         Nadja Haßler, Ernst Johannes Klebert, Michell Weyer
 **     supervisors:
 **         Stefan Freischlad (maintainer until 2009), Peer Stechert
 ** Project is maintained since 2010 by Christian Eibl <filius@c.fameibl.de>
 **         and Stefan Freischlad
 ** Filius is free software: you can redistribute it and/or modify
 ** it under the terms of the GNU General Public License as published by
 ** the Free Software Foundation, either version 2 of the License, or
 ** (at your option) version 3.
 **
 ** Filius is distributed in the hope that it will be useful,
 ** but WITHOUT ANY WARRANTY; without even the implied
 ** warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 ** PURPOSE. See the GNU General Public License for more details.
 **
 ** You should have received a copy of the GNU General Public License
 ** along with Filius.  If not, see <http://www.gnu.org/licenses/>.
 */
package filius.software.drucken;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import filius.software.clientserver.TCPServerAnwendung;
import filius.software.transportschicht.Socket;
import filius.software.transportschicht.TCPSocket;

/**
 * Druckdienst eines Netzwerkdruckers nach dem Vorbild von "Raw Printing" (auch "JetDirect" oder "AppSocket"): Der
 * Drucker wartet auf TCP-Port 9100 auf Verbindungen. Jede empfangene Nachricht wird als Druckauftrag gespeichert und
 * "gedruckt", eine Antwort wird nicht verschickt.
 * <p>
 * Die Druckauftraege werden absichtlich nicht als Bean-Eigenschaft (get/set) angeboten, damit sie nicht in der
 * Projektdatei gespeichert werden.
 * </p>
 */
public class DruckServer extends TCPServerAnwendung {
    private static Logger LOG = LoggerFactory.getLogger(DruckServer.class);

    /** Standard-Port fuer Raw Printing */
    public static final int RAW_PRINT_PORT = 9100;

    private final List<DruckAuftrag> auftraege = new ArrayList<>();

    public DruckServer() {
        super();
        setPort(RAW_PRINT_PORT);
    }

    @Override
    protected void neuerMitarbeiter(Socket socket) {
        LOG.trace("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (DruckServer), neuerMitarbeiter(" + socket + ")");
        if (socket instanceof TCPSocket) {
            DruckServerMitarbeiter neuerMitarbeiter = new DruckServerMitarbeiter(this, socket);
            mitarbeiter.add(neuerMitarbeiter);
            neuerMitarbeiter.starten();
        }
    }

    /** Speichert einen neuen Auftrag und benachrichtigt die Beobachter (z. B. die Druckerwarteschlange). */
    public void drucken(String absender, String text) {
        DruckAuftrag auftrag = new DruckAuftrag(System.currentTimeMillis(), absender, text);
        synchronized (auftraege) {
            auftraege.add(auftrag);
        }
        LOG.debug("print job received from {}", absender);
        benachrichtigeBeobachter(auftrag);
    }

    /** Kopie der Liste aller bisher empfangenen Druckauftraege */
    public List<DruckAuftrag> holeAuftraege() {
        synchronized (auftraege) {
            return new ArrayList<>(auftraege);
        }
    }

    public void loescheAuftraege() {
        synchronized (auftraege) {
            auftraege.clear();
        }
        benachrichtigeBeobachter();
    }
}
