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

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;

import filius.Main;
import filius.rahmenprogramm.Information;

@SuppressWarnings("serial")
public class LanguageSelectionDialog extends JDialog {

    private static final String FRANCAIS = "Français";
    private static final String ENGLISH = "English";
    private static final String DEUTSCH = "Deutsch";

    private JLabel selectLanguageLabel = new JLabel();
    private JComboBox<String> languageSelection = new JComboBox<>();
    private JLabel restartHint = new JLabel();
    private JButton confirm = new JButton();
    private JButton confirmAndExit = new JButton();
    private JButton cancel = new JButton();
    private Box buttonBox = Box.createHorizontalBox();

    private LanguageSelectionDialog(boolean restartMode) {
        super();
        this.setModal(true);
        Image image = Toolkit.getDefaultToolkit()
                .getImage(LanguageSelectionDialog.class.getResource("/gfx/hardware/kabel.png"));
        this.setIconImage(image);
        this.setTitle("Sprache / Language / Langue");
        this.setResizable(false);
        initialize(restartMode);
        initializeText(Information.getInformation().getLocaleOrDefault());
    }

    private void initializeText(Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle("filius.messages.MessagesBundle", locale);

        selectLanguageLabel.setText(bundle.getString("languageSelection_msg1"));
        restartHint.setText("<html>" + bundle.getString("languageSelection_msg2") + "</html>");
        confirm.setText(bundle.getString("languageSelection_msg3"));
        confirmAndExit.setText(bundle.getString("languageSelection_msg4"));
        cancel.setText(bundle.getString("languageSelection_msg5"));

        buttonBox.updateUI();
    }

    public static void selectLanguage(boolean restart) {
        LanguageSelectionDialog instance = new LanguageSelectionDialog(restart);
        instance.setVisible(true);
    }

    private void apply() {
        String selectedValue = (String) languageSelection.getSelectedItem();
        Information.getInformation().setLocale(languateToLocale(selectedValue));
    }

    private Locale languateToLocale(String language) {
        Locale locale = Locale.UK;
        if (FRANCAIS.equals(language)) {
            locale = Locale.FRANCE;
        } else if (DEUTSCH.equals(language)) {
            locale = Locale.GERMANY;
        }
        return locale;
    }

    private String localeToLanguage(Locale locale) {
        String language;
        if (null == locale || Locale.UK.getCountry().equals(locale.getCountry())) {
            language = ENGLISH;
        } else if (Locale.FRANCE.getCountry().equals(locale.getCountry())) {
            language = FRANCAIS;
        } else {
            language = DEUTSCH;
        }
        return language;
    }

    private void initialize(boolean restartMode) {
        Box verticalBox = Box.createVerticalBox();
        verticalBox.setPreferredSize(new Dimension(400, 240));
        getContentPane().add(verticalBox);
        pack();
        setLocation((getToolkit().getScreenSize().width - getWidth()) / 2,
                (getToolkit().getScreenSize().height - getHeight()) / 2);

        verticalBox.add(Box.createVerticalStrut(10));

        Box labelBox = Box.createHorizontalBox();
        labelBox.add(Box.createHorizontalStrut(10));
        labelBox.add(selectLanguageLabel);
        labelBox.add(Box.createGlue());
        verticalBox.add(labelBox);

        verticalBox.add(Box.createVerticalStrut(10));

        languageSelection.addItem(DEUTSCH);
        languageSelection.addItem(ENGLISH);
        languageSelection.addItem(FRANCAIS);
        languageSelection.setSelectedItem(localeToLanguage(Information.getInformation().getLocaleOrDefault()));
        languageSelection.setMaximumSize(new Dimension(380, 30));
        languageSelection.setPreferredSize(new Dimension(380, 30));
        languageSelection.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                LanguageSelectionDialog.this
                        .initializeText(languateToLocale((String) languageSelection.getSelectedItem()));
            }
        });

        Box selectBox = Box.createHorizontalBox();
        selectBox.add(Box.createHorizontalStrut(10));
        selectBox.add(languageSelection);
        selectBox.add(Box.createGlue());
        verticalBox.add(selectBox);

        if (restartMode) {
            verticalBox.add(Box.createVerticalStrut(10));

            Box hintBox = Box.createHorizontalBox();
            hintBox.add(Box.createHorizontalStrut(10));
            // hintBox.setBorder(BorderFactory.createEtchedBorder());
            restartHint.setPreferredSize(new Dimension(350, 60));
            restartHint.setSize(new Dimension(350, 60));
            restartHint.setMinimumSize(new Dimension(350, 60));
            hintBox.add(restartHint);
            hintBox.add(Box.createGlue());
            verticalBox.add(hintBox);

            verticalBox.add(Box.createVerticalStrut(10));
        } else {
            verticalBox.add(Box.createVerticalStrut(90));
        }

        // buttonBox.setAlignmentX(Box.RIGHT_ALIGNMENT);
        // buttonBox.setBorder(BorderFactory.createEtchedBorder());
        buttonBox.setSize(new Dimension(390, 50));
        buttonBox.setPreferredSize(new Dimension(390, 50));
        buttonBox.setMinimumSize(new Dimension(390, 50));
        if (restartMode) {
            cancel.setPreferredSize(new Dimension(120, 30));
            cancel.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    LanguageSelectionDialog.this.setVisible(false);
                }
            });
            buttonBox.add(cancel);
            buttonBox.add(Box.createHorizontalStrut(10));

            confirmAndExit.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    LanguageSelectionDialog.this.apply();
                    Main.beenden();
                }
            });
            buttonBox.add(confirmAndExit);
        } else {
            confirm.setPreferredSize(new Dimension(250, 30));
            confirm.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    LanguageSelectionDialog.this.apply();
                    LanguageSelectionDialog.this.setVisible(false);
                }
            });
            buttonBox.add(confirm);
        }

        verticalBox.add(Box.createVerticalGlue());

        verticalBox.add(buttonBox);
    }
}
