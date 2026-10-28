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
import java.util.Observable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import filius.software.drucken.DruckClient;

/** Benutzungsoberflaeche der Anwendung "Drucken": Druckeradresse eingeben, Text schreiben, drucken. */
public class GUIApplicationDruckClientWindow extends GUIApplicationWindow {

    private static final long serialVersionUID = 1L;

    private JTextField tfDrucker;
    private JTextArea taText;
    private JTextArea taAusgabe;

    public GUIApplicationDruckClientWindow(GUIDesktopPanel desktop, String appName) {
        super(desktop, appName);
        initialisiereKomponenten();
    }

    private void initialisiereKomponenten() {
        Box hauptBox = Box.createVerticalBox();
        hauptBox.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        hauptBox.add(Box.createVerticalStrut(5));

        Box hBox = Box.createHorizontalBox();
        JLabel label = new JLabel(messages.getString("druckclient_msg1"));
        label.setPreferredSize(new Dimension(140, label.getHeight()));
        hBox.add(label);
        tfDrucker = new JTextField();
        tfDrucker.setPreferredSize(new Dimension(100, 20));
        String letzterDrucker = ((DruckClient) holeAnwendung()).getDruckerAdresse();
        tfDrucker.setText(letzterDrucker == null ? "" : letzterDrucker);
        hBox.add(tfDrucker);
        hauptBox.add(hBox);
        hauptBox.add(Box.createVerticalStrut(5));

        label = new JLabel(messages.getString("druckclient_msg2"));
        hauptBox.add(label);
        taText = new JTextArea();
        taText.setLineWrap(true);
        taText.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(taText);
        scrollPane.setPreferredSize(new Dimension(400, 180));
        hauptBox.add(scrollPane);
        hauptBox.add(Box.createVerticalStrut(5));

        JButton btDrucken = new JButton(messages.getString("druckclient_msg3"));
        btDrucken.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String drucker = tfDrucker.getText().trim();
                if (drucker.isEmpty()) {
                    taAusgabe.append(messages.getString("druckclient_msg4") + "\n");
                } else {
                    ((DruckClient) holeAnwendung()).drucken(drucker, taText.getText());
                }
            }
        });
        hauptBox.add(btDrucken);
        hauptBox.add(Box.createVerticalStrut(5));

        taAusgabe = new JTextArea();
        taAusgabe.setEditable(false);
        scrollPane = new JScrollPane(taAusgabe);
        scrollPane.setPreferredSize(new Dimension(400, 80));
        hauptBox.add(scrollPane);
        hauptBox.add(Box.createVerticalStrut(5));

        add(hauptBox, BorderLayout.CENTER);
    }

    public void update(Observable o, Object arg) {
        if (arg != null) {
            taAusgabe.append(arg.toString() + "\n");
        }
    }
}
