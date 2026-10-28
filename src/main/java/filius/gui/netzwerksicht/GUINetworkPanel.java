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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

import javax.swing.ImageIcon;

import filius.hardware.knoten.AccessPoint;
import filius.hardware.knoten.Drucker;
import filius.hardware.knoten.Gateway;
import filius.hardware.knoten.InternetKnoten;
import filius.hardware.knoten.Knoten;
import filius.hardware.knoten.Modem;
import filius.hardware.knoten.Notebook;
import filius.hardware.knoten.Rechner;
import filius.hardware.knoten.Smartphone;
import filius.hardware.knoten.Switch;
import filius.hardware.knoten.Vermittlungsrechner;
import filius.rahmenprogramm.IconMode;
import filius.rahmenprogramm.Information;

public class GUINetworkPanel extends GUIMainArea {

    private static final long serialVersionUID = 1L;
    private static final Color FARBE_REICHWEITE_FLAECHE = new Color(70, 140, 220, 22);
    private static final Color FARBE_REICHWEITE_RAND = new Color(70, 140, 220, 140);

    private transient List<GUIKnotenItem> knotenItems;

    public GUINetworkPanel(int width, int height) {
        setLayout(null);
        setPreferredSize(new Dimension(width, height));
        setOpaque(false);
        setBounds(0, 0, width, height);
    }

    /**
     * Zeichnet die Funkreichweite aller Access Points als gestrichelten Kreis hinter die Komponenten. So ist im
     * Unterricht sichtbar, welche WLAN-Geraete sich mit einem Access Point verbinden koennen.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (null == knotenItems) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        float[] dash = { 8.0f, 6.0f };
        for (GUIKnotenItem item : knotenItems) {
            if (item.getKnoten() instanceof AccessPoint && item.getImageLabel() != null) {
                int radius = ((AccessPoint) item.getKnoten()).getReichweite();
                JSidebarButton label = item.getImageLabel();
                int mx = label.getX() + label.getWidth() / 2;
                int my = label.getY() + label.getHeight() / 2;
                g2.setColor(FARBE_REICHWEITE_FLAECHE);
                g2.fillOval(mx - radius, my - radius, 2 * radius, 2 * radius);
                g2.setColor(FARBE_REICHWEITE_RAND);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
                g2.drawOval(mx - radius, my - radius, 2 * radius, 2 * radius);
            }
        }
        g2.dispose();
    }

    public void updateViewport(List<GUIKnotenItem> knoten, List<GUIKabelItem> kabel) {
        removeAll();
        knotenItems = knoten;

        for (GUIKnotenItem tempitem : knoten) {
            Knoten tempKnoten = tempitem.getKnoten();
            JSidebarButton templabel = tempitem.getImageLabel();

            tempKnoten.addObserver(templabel);
            tempKnoten.getSystemSoftware().addObserver(templabel);

            templabel.setSelektiert(false);
            // When the text is changed, the location is recomputed so that the icon does not move (which might happen
            // when the text is wider than the icon)
            templabel.setTextAndUpdateLocation(tempKnoten.holeAnzeigeName());
            templabel.setTyp(tempKnoten.holeHardwareTyp());
            if (tempitem.getKnoten() instanceof InternetKnoten) {
                templabel.updateTooltip((InternetKnoten) tempitem.getKnoten());
            }
            IconMode mode = Information.getInformation().getIconMode();
            if (tempitem.getKnoten() instanceof AccessPoint) {
                templabel.setIcon(
                        new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.ACCESSPOINT + mode.suffix())));
            } else if (tempitem.getKnoten() instanceof Smartphone) {
                templabel.setIcon(
                        new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.SMARTPHONE + mode.suffix())));
            } else if (tempitem.getKnoten() instanceof Drucker) {
                templabel
                        .setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.DRUCKER + mode.suffix())));
            } else if (tempitem.getKnoten() instanceof Switch) {
                if (((Switch) tempitem.getKnoten()).isCloud())
                    templabel.setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.SWITCH_CLOUD)));
                else
                    templabel.setIcon(
                            new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.SWITCH + mode.suffix())));
            } else if (tempitem.getKnoten() instanceof Vermittlungsrechner) {
                templabel.setIcon(new ImageIcon(
                        getClass().getResource("/" + GUIDesignSidebar.VERMITTLUNGSRECHNER + mode.suffix())));
            } else if (tempitem.getKnoten() instanceof Rechner) {
                templabel
                        .setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.RECHNER + mode.suffix())));
            } else if (tempitem.getKnoten() instanceof Notebook) {
                templabel.setIcon(
                        new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.NOTEBOOK + mode.suffix())));
            } else if (tempitem.getKnoten() instanceof Modem) {
                templabel.setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.MODEM + mode.suffix())));
            } else if (tempitem.getKnoten() instanceof Gateway) {
                templabel
                        .setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.GATEWAY + mode.suffix())));
            }

            templabel.setBounds(tempitem.getImageLabel().getBounds());
            add(templabel);
        }

        for (GUIKabelItem tempcable : kabel) {
            add(tempcable.getKabelpanel());
        }
    }
}
