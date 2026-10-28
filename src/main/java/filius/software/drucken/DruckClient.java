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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import filius.rahmenprogramm.I18n;
import filius.software.clientserver.ClientAnwendung;
import filius.software.transportschicht.TCPSocket;

/**
 * Client-Anwendung "Drucken": Baut eine TCP-Verbindung zum Druckdienst eines Druckers (Port 9100) auf, schickt den Text
 * als eine Nachricht und baut die Verbindung wieder ab.
 */
public class DruckClient extends ClientAnwendung implements I18n {
    private static Logger LOG = LoggerFactory.getLogger(DruckClient.class);

    /** Adresse (IP oder Domainname) des zuletzt verwendeten Druckers; wird im Projekt gespeichert */
    private String druckerAdresse;

    public String getDruckerAdresse() {
        return druckerAdresse;
    }

    public void setDruckerAdresse(String druckerAdresse) {
        this.druckerAdresse = druckerAdresse;
    }

    /**
     * Schickt einen Druckauftrag an den Drucker. Diese Methode ist <b>nicht blockierend</b>.
     */
    public void drucken(String drucker, String text) {
        setDruckerAdresse(drucker);
        ausfuehren("sendeDruckauftrag", new Object[] { drucker, text });
    }

    /** Diese Methode ist <b>blockierend</b> und wird ueber ausfuehren() in einem eigenen Thread aufgerufen. */
    public void sendeDruckauftrag(String drucker, String text) {
        LOG.debug("send print job to {}", drucker);
        try {
            benachrichtigeBeobachter(messages.getString("sw_druckclient_msg1") + " " + drucker + ":"
                    + DruckServer.RAW_PRINT_PORT + " ...");
            socket = new TCPSocket(getSystemSoftware(), drucker, DruckServer.RAW_PRINT_PORT);
            socket.verbinden();
            socket.senden(text);
            socket.schliessen();
            benachrichtigeBeobachter(messages.getString("sw_druckclient_msg2"));
        } catch (Exception e) {
            LOG.debug("", e);
            benachrichtigeBeobachter(messages.getString("sw_druckclient_msg3") + " " + e.getMessage());
            if (socket != null) {
                socket.beenden();
            }
        } finally {
            socket = null;
        }
    }
}
