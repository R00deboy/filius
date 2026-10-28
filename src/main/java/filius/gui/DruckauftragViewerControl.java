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
package filius.gui;

import java.util.HashMap;
import java.util.Map;

import filius.gui.anwendungssicht.DruckauftragViewer;
import filius.hardware.knoten.Drucker;

/** Verwaltet die Fenster mit den Druckauftraegen der Drucker (analog zu {@link SatViewerControl}). */
public class DruckauftragViewerControl {

    private Map<Drucker, DruckauftragViewer> viewer = new HashMap<>();
    private static DruckauftragViewerControl singleton;

    private DruckauftragViewerControl() {}

    public static DruckauftragViewerControl getInstance() {
        if (null == singleton) {
            singleton = new DruckauftragViewerControl();
        }
        return singleton;
    }

    public void showViewer(Drucker drucker) {
        DruckauftragViewer druckerViewer = viewer.get(drucker);
        if (null == druckerViewer) {
            druckerViewer = new DruckauftragViewer(drucker);
            viewer.put(drucker, druckerViewer);
        } else {
            druckerViewer.resetSizeAndPosition();
        }
        druckerViewer.setVisible(true);
    }

    public void hideViewer() {
        for (DruckauftragViewer druckerViewer : viewer.values()) {
            druckerViewer.setVisible(false);
        }
    }
}
