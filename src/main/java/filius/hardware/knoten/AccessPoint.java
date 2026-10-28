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
package filius.hardware.knoten;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import filius.rahmenprogramm.I18n;

/**
 * Ein WLAN Access Point arbeitet auf Schicht 2 wie ein Switch (Bridge zwischen Funk und Kabel). Er hat eine SSID
 * (verwaltet in der Switch-Firmware) und zusaetzlich eine Funkreichweite. WLAN-Geraete (Notebook, Smartphone, ...)
 * werden nur dann mit dem Access Point verbunden, wenn sie die SSID ausgewaehlt haben und sich innerhalb der Reichweite
 * befinden.
 */
@SuppressWarnings("serial")
public class AccessPoint extends Switch implements I18n {
    private static Logger LOG = LoggerFactory.getLogger(AccessPoint.class);

    public static final String TYPE = messages.getString("hw_accesspoint_msg1");

    /** Standard-Reichweite des Funknetzes in Bildpunkten der Entwurfsansicht */
    public static final int STANDARD_REICHWEITE = 200;

    private int reichweite = STANDARD_REICHWEITE;

    @Override
    public String holeHardwareTyp() {
        return TYPE;
    }

    public AccessPoint() {
        super();
        LOG.trace("INVOKED-2 (" + this.hashCode() + ") " + getClass() + " (AccessPoint), constr: AccessPoint()");

        this.setzeAnzahlAnschluesse(16);
        this.setName(TYPE);
    }

    public int getReichweite() {
        return reichweite;
    }

    public void setReichweite(int reichweite) {
        this.reichweite = Math.max(0, reichweite);
    }

    /**
     * Prueft, ob sich ein Geraet innerhalb der Funkreichweite befindet.
     *
     * @param dx
     *            horizontaler Abstand zwischen den Mittelpunkten von Access Point und Geraet
     * @param dy
     *            vertikaler Abstand zwischen den Mittelpunkten von Access Point und Geraet
     */
    public boolean inReichweite(double dx, double dy) {
        return dx * dx + dy * dy <= (double) reichweite * reichweite;
    }
}
