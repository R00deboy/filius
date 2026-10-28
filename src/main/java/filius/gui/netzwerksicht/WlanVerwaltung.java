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
package filius.gui.netzwerksicht;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import filius.gui.GUIContainer;
import filius.gui.GUIEvents;
import filius.hardware.knoten.AccessPoint;
import filius.hardware.knoten.Host;
import filius.hardware.knoten.Knoten;
import filius.hardware.knoten.Switch;
import filius.software.system.Betriebssystem;
import filius.software.system.SwitchFirmware;

/**
 * Verwaltet die Funkverbindungen zwischen WLAN-Geraeten und Access Points in der Entwurfsansicht.
 * <p>
 * Regel: Ein Host mit WLAN-Netzwerkkarte, der eine SSID ausgewaehlt hat, wird mit einem Access Point (oder einem
 * Switch) mit dieser SSID verbunden. Bei einem {@link AccessPoint} muss sich der Host ausserdem innerhalb der
 * Funkreichweite befinden. Wird ein Geraet aus der Reichweite geschoben, wird die Funkverbindung getrennt; die SSID
 * bleibt aber gespeichert, so dass die Verbindung wiederhergestellt wird, sobald das Geraet wieder in Reichweite ist.
 * </p>
 */
public class WlanVerwaltung {
    private static Logger LOG = LoggerFactory.getLogger(WlanVerwaltung.class);

    private WlanVerwaltung() {}

    /** Mittelpunkt-Abstand zweier Knoten in der Entwurfsansicht pruefen */
    public static boolean inReichweite(GUIKnotenItem accessPoint, GUIKnotenItem geraet) {
        if (!(accessPoint.getKnoten() instanceof AccessPoint)) {
            // Ein normaler Switch mit SSID hat (wie bisher in Filius) keine Reichweitenbegrenzung.
            return true;
        }
        JSidebarButton apLabel = accessPoint.getImageLabel();
        JSidebarButton geraetLabel = geraet.getImageLabel();
        double dx = (geraetLabel.getX() + geraetLabel.getWidth() / 2.0) - (apLabel.getX() + apLabel.getWidth() / 2.0);
        double dy = (geraetLabel.getY() + geraetLabel.getHeight() / 2.0)
                - (apLabel.getY() + apLabel.getHeight() / 2.0);
        return ((AccessPoint) accessPoint.getKnoten()).inReichweite(dx, dy);
    }

    /** Ueberprueft die Funkverbindungen aller Hosts. */
    public static void aktualisiereVerbindungen() {
        boolean geaendert = false;
        for (GUIKnotenItem item : new ArrayList<>(GUIContainer.getGUIContainer().getKnotenItems())) {
            if (item.getKnoten() instanceof Host) {
                geaendert = aktualisiereVerbindung(item) || geaendert;
            }
        }
        if (geaendert) {
            GUIContainer.getGUIContainer().updateCables();
        }
    }

    /** Ueberprueft die Funkverbindung eines Hosts. */
    public static void aktualisiereVerbindung(Host host) {
        GUIKnotenItem hostItem = findeItem(host);
        if (null != hostItem && aktualisiereVerbindung(hostItem)) {
            GUIContainer.getGUIContainer().updateCables();
        }
    }

    /**
     * @return ob eine Verbindung entfernt oder hinzugefuegt wurde
     */
    private static boolean aktualisiereVerbindung(GUIKnotenItem hostItem) {
        Betriebssystem os = (Betriebssystem) hostItem.getKnoten().getSystemSoftware();
        String ssid = os.wireless() ? os.getSsid() : null;

        GUIKabelItem funkverbindung = null;
        GUIKnotenItem verbundenerAP = null;
        for (GUIKabelItem cable : GUIContainer.getGUIContainer().getCableItems()) {
            if (cable.getDasKabel().getWireless()) {
                if (cable.getKabelpanel().getZiel1() == hostItem) {
                    funkverbindung = cable;
                    verbundenerAP = cable.getKabelpanel().getZiel2();
                    break;
                } else if (cable.getKabelpanel().getZiel2() == hostItem) {
                    funkverbindung = cable;
                    verbundenerAP = cable.getKabelpanel().getZiel1();
                    break;
                }
            }
        }

        if (null != funkverbindung && passt(verbundenerAP, hostItem, ssid)) {
            return false;
        }
        boolean geaendert = false;
        if (null != funkverbindung) {
            LOG.debug("remove wifi connection of {}", hostItem.getKnoten().getName());
            GUIEvents.getGUIEvents().removeConnection(funkverbindung);
            geaendert = true;
        }
        if (null != ssid && os.wireless() && hostItem.getKnoten().holeFreienPort() != null) {
            for (GUIKnotenItem kandidat : findeAccessPoints(ssid)) {
                if (inReichweite(kandidat, hostItem) && kandidat.getKnoten().holeFreienPort() != null) {
                    LOG.debug("connect {} to wifi {}", hostItem.getKnoten().getName(), ssid);
                    GUIEvents.getGUIEvents().createConnection(hostItem, kandidat);
                    geaendert = true;
                    break;
                }
            }
        }
        return geaendert;
    }

    private static boolean passt(GUIKnotenItem accessPoint, GUIKnotenItem hostItem, String ssid) {
        return null != ssid && null != accessPoint && accessPoint.getKnoten() instanceof Switch
                && ssid.equals(((SwitchFirmware) accessPoint.getKnoten().getSystemSoftware()).getSSID())
                && inReichweite(accessPoint, hostItem);
    }

    private static List<GUIKnotenItem> findeAccessPoints(String ssid) {
        List<GUIKnotenItem> accessPoints = new ArrayList<>();
        for (GUIKnotenItem item : GUIContainer.getGUIContainer().getKnotenItems()) {
            if (item.getKnoten() instanceof Switch
                    && ssid.equals(((SwitchFirmware) item.getKnoten().getSystemSoftware()).getSSID())) {
                accessPoints.add(item);
            }
        }
        return accessPoints;
    }

    private static GUIKnotenItem findeItem(Knoten knoten) {
        for (GUIKnotenItem item : GUIContainer.getGUIContainer().getKnotenItems()) {
            if (item.getKnoten() == knoten) {
                return item;
            }
        }
        return null;
    }
}
