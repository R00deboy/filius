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
package filius.gui.anwendungssicht;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;

import filius.gui.JMainFrame;
import filius.gui.netzwerksicht.GUIDesignSidebar;
import filius.hardware.knoten.Drucker;
import filius.rahmenprogramm.I18n;
import filius.software.drucken.DruckAuftrag;
import filius.software.drucken.DruckServer;

/**
 * Fenster eines Druckers im Aktionsmodus: oben die Liste der empfangenen Druckauftraege, unten der Text ("Ausdruck")
 * des ausgewaehlten Auftrags.
 */
@SuppressWarnings("serial")
public class DruckauftragViewer extends JDialog implements I18n, Observer {

    private final Drucker drucker;
    private DefaultTableModel dtm;
    private JTable tabelle;
    private JTextArea ausdruck;
    private List<DruckAuftrag> auftraege;

    public DruckauftragViewer(Drucker drucker) {
        super(JMainFrame.getJMainFrame(), messages.getString("druckauftragviewer_msg1") + " " + drucker.holeAnzeigeName());
        this.drucker = drucker;
        init();
        DruckServer server = drucker.holeDruckServer();
        if (server != null) {
            server.hinzuBeobachter(this);
        }
        aktualisieren();
    }

    private void init() {
        resetSizeAndPosition();

        ImageIcon icon = new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.DRUCKER + ".png"));
        setIconImage(icon.getImage());

        dtm = new DefaultTableModel(0, 3) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelle = new JTable(dtm);
        tabelle.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        DefaultTableColumnModel dtcm = (DefaultTableColumnModel) tabelle.getColumnModel();
        dtcm.getColumn(0).setHeaderValue(messages.getString("druckauftragviewer_msg2"));
        dtcm.getColumn(0).setPreferredWidth(70);
        dtcm.getColumn(1).setHeaderValue(messages.getString("druckauftragviewer_msg3"));
        dtcm.getColumn(1).setPreferredWidth(110);
        dtcm.getColumn(2).setHeaderValue(messages.getString("druckauftragviewer_msg4"));
        dtcm.getColumn(2).setPreferredWidth(220);
        tabelle.getSelectionModel().addListSelectionListener(e -> zeigeAusdruck());

        ausdruck = new JTextArea();
        ausdruck.setEditable(false);
        ausdruck.setLineWrap(true);
        ausdruck.setWrapStyleWord(true);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tabelle),
                new JScrollPane(ausdruck));
        split.setResizeWeight(0.4);
        getContentPane().add(split, BorderLayout.CENTER);

        JButton loeschen = new JButton(messages.getString("druckauftragviewer_msg5"));
        loeschen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DruckServer server = drucker.holeDruckServer();
                if (server != null) {
                    server.loescheAuftraege();
                }
            }
        });
        getContentPane().add(loeschen, BorderLayout.SOUTH);
    }

    public void resetSizeAndPosition() {
        setSize(new Dimension(420, 360));
        setLocation(getParent().getX() + (getParent().getWidth() - getWidth()) / 2,
                getParent().getY() + (getParent().getHeight() - getHeight()) / 2);
    }

    public Drucker getDrucker() {
        return drucker;
    }

    private void aktualisieren() {
        DruckServer server = drucker.holeDruckServer();
        auftraege = server != null ? server.holeAuftraege() : List.of();
        SimpleDateFormat zeitFormat = new SimpleDateFormat("HH:mm:ss");
        dtm.setRowCount(0);
        for (DruckAuftrag auftrag : auftraege) {
            String vorschau = auftrag.holeText().replaceAll("\\s+", " ");
            dtm.addRow(new Object[] { zeitFormat.format(new Date(auftrag.holeZeitpunkt())), auftrag.holeAbsender(),
                    vorschau });
        }
        if (!auftraege.isEmpty()) {
            tabelle.setRowSelectionInterval(auftraege.size() - 1, auftraege.size() - 1);
        } else {
            ausdruck.setText("");
        }
    }

    private void zeigeAusdruck() {
        int zeile = tabelle.getSelectedRow();
        if (zeile >= 0 && zeile < auftraege.size()) {
            ausdruck.setText(auftraege.get(zeile).holeText());
            ausdruck.setCaretPosition(0);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg == null || arg instanceof DruckAuftrag) {
            SwingUtilities.invokeLater(this::aktualisieren);
        }
    }
}
