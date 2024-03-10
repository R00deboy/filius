package filius.gui.schichtensicht;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.nachrichten.Lauscher;
import filius.software.system.GatewayFirmware;
import filius.software.system.SystemSoftware;
import filius.software.system.VermittlungsrechnerBetriebssystem;

/**
 * 
 * @author Christoph Irniger
 */
public class LocalPathPanel extends JPanel implements I18n {
	private final SystemSoftware systemSoftware;

	private final String interfaceId;

	private final int selectedFrameNumber;  // beginnt bei 1

	private JLabel[][] messageDetailsReceived = new JLabel[5][8];  // für die ankommende Nachricht
	private JLabel[][] messageDetailsSent = new JLabel[5][8];  // für die versendete Nachricht

	private boolean received = false;
	private boolean sent = false;

	private final LocalPathVisualisation visualisation;  // Visualisierung oben
	private final MessageDetailsTable messageDetailsTable; // Nachrichtendetails unten

	private static Lauscher lauscher = Lauscher.getLauscher();  // zugriff auf den Lauscher

	public LocalPathPanel(LocalPathDialog localPathDialog) {
		super();

		// inherit instance variables from super container (localPathDialog)
		this.systemSoftware = localPathDialog.getSystemSoftware();
		this.interfaceId = localPathDialog.getInterfaceId();
		this.selectedFrameNumber = localPathDialog.getSelectedFrameNumber();

		// fill messageDetailsReceived and messageDetailsSent
		fill();

		// initialise visualisation and table for message details
		visualisation = new LocalPathVisualisation(localPathDialog.getSystemSoftware());
		messageDetailsTable = new MessageDetailsTable(messageDetailsReceived, messageDetailsSent);

		// scrollPanes
		JScrollPane visualisationScrollPane;
		JScrollPane messageDetailsScrollPane;
		visualisationScrollPane = new JScrollPane(visualisation);
		messageDetailsScrollPane = new JScrollPane(messageDetailsTable);

		// splitPane for visualisation in upper and message details in lower part
		JSplitPane splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane.setTopComponent(visualisationScrollPane);
		splitPane.setBottomComponent(messageDetailsScrollPane);

		// layout manager for this panel
		BorderLayout borderLayout = new BorderLayout(10, 10);
		setLayout(borderLayout);

		// splitPane hier einbetten
		add(splitPane, BorderLayout.CENTER);
	}

	public JLabel[][] getMessageDetailsReceived() {
		return this.messageDetailsReceived;
	}

	public JLabel[][] getMessageDetailsSent() {
		return this.messageDetailsSent;
	}

	/**
	 * Füllt die Nachrichtendetails {@code messageDetailsSent} und
	 * {@code messageDetailsReceived} mit allen für die Visualisierung benötigten
	 * Daten des/der Frames.
	 */
	private void fill() {
		if (isFrameReceived()) {
			received = true;
			fillTitles(messageDetailsReceived);
			fillMessageDetails(messageDetailsReceived, this.interfaceId, this.selectedFrameNumber);
			fillOther();
		} else {
			sent = true;
			fillTitles(messageDetailsSent);
			fillMessageDetails(messageDetailsSent, this.interfaceId, this.selectedFrameNumber);
			fillOther();
		}
		show(25);
	}

	/**
	 * Prüft, ob das ausgewählte Frame empfangen oder versandt (= nicht empfangen)
	 * wurde.
	 */
	private boolean isFrameReceived() {
		return (this.interfaceId
				.equals(lauscher.getFrame(this.interfaceId, this.selectedFrameNumber).getZielMacAdresse()));
	}

	/**
	 * Füllt diejenigen Nachrichtendetails ({@code messageDetailsReceived} oder
	 * {@code messageDetailsSent}) aus, welche noch nicht ausgefüllt wurde.
	 */
	private void fillOther() {
		/*
		 * Falls der Knoten ein Router (Vermittlungsrechner oder Heimrouter) ist und das
		 * im ankommenden Frame enthaltene (IP- oder ICMP-)Paket nur "auf dem Vorbeiweg"
		 * (bzw. nicht für den Knoten bestimmt oder von ihm ursprünglich erstellt) ist,
		 * müssen auch die anderen Nachrichtendetails mit den Daten des dazugehörenden
		 * Frames gefüllt werden.
		 */
		if (systemSoftware instanceof VermittlungsrechnerBetriebssystem || systemSoftware instanceof GatewayFirmware) {
			Object[] interfaceIdAndFrameNumber = lauscher.getCorrespondingFrame(this.interfaceId,
					this.selectedFrameNumber, this.systemSoftware);

			String interfaceId = (String) interfaceIdAndFrameNumber[0];
			Integer frameNumber = (Integer) interfaceIdAndFrameNumber[1];
			if (!(interfaceId == null || frameNumber == null)) {
				if (received == true) {
					sent = true;
					fillTitles(messageDetailsSent);
					fillMessageDetails(messageDetailsSent, interfaceId, frameNumber);
				} else if (sent == true) {
					received = true;
					fillTitles(messageDetailsReceived);
					fillMessageDetails(messageDetailsReceived, interfaceId, frameNumber);
				}
			}
		}
	}

	/**
	 * Füllt die Nachrichtendetails, die als Parameter mitgegeben werden, mit den
	 * Werten für das ausgewählte Interface und die frame-Nummer.
	 */
	private void fillMessageDetails(JLabel[][] messageDetailsTable, String interfaceId, int selectedFrameNumber) {
		Object[][] daten = lauscher.getDaten(interfaceId, false, 0);

		String selectedFrameNumberAsString = Integer.toString(selectedFrameNumber);

		for (int i = 0; i < daten.length; i++) {
			if (daten[i][0].equals(selectedFrameNumberAsString)) {
				// -----Netzzugangs-Daten-----
				// Quelle
				messageDetailsTable[2][1] = new JLabel(daten[i][2].toString());
				// Ziel
				messageDetailsTable[3][1] = new JLabel(daten[i][3].toString());
				// Bemerkungen / Details
				messageDetailsTable[4][1] = new JLabel(daten[i][6].toString());

				// -----Vermittlungs-Daten (falls existent)-----
				if (i + 1 < daten.length && daten[i + 1][0].equals(selectedFrameNumberAsString)) {
					// Quelle
					messageDetailsTable[2][3] = new JLabel(daten[i + 1][2].toString());
					// Ziel
					messageDetailsTable[3][3] = new JLabel(daten[i + 1][3].toString());
					// Bemerkungen / Details
					messageDetailsTable[4][3] = new JLabel(daten[i + 1][6].toString());
					// Protokoll
					messageDetailsTable[1][2] = new JLabel(daten[i + 1][4].toString());
				}

				// -----Transport-Daten (falls existent)-----
				if (i + 2 < daten.length && daten[i + 2][0].equals(selectedFrameNumberAsString)) {
					// Quelle
					messageDetailsTable[2][5] = new JLabel(daten[i + 2][2].toString());
					// Ziel
					messageDetailsTable[3][5] = new JLabel(daten[i + 2][3].toString());
					// Bemerkungen / Details
					messageDetailsTable[4][5] = new JLabel(daten[i + 2][6].toString());
					// Protokoll
					messageDetailsTable[1][4] = new JLabel(daten[i + 2][4].toString());
				}

				// -----Anwendungs-Daten (falls existent)-----
				if (i + 3 < daten.length && daten[i + 3][0].equals(selectedFrameNumberAsString)) {
					// Protokoll
					messageDetailsTable[2][6] = new JLabel(daten[i + 3][6].toString());
					// Daten
					messageDetailsTable[1][6] = new JLabel(daten[i + 3][4].toString());
				}
				break;
			}
		}
	}

	/**
	 * Füllt die Nachrichtendetails, die als Parameter mitgegeben werden, mit
	 * denjenigen Titeln (Spalten- und Zellbezeichnungen), die für alle gleich
	 * (konstant) sind.
	 */
	private void fillTitles(JLabel[][] messageDetailsTable) {
		// Schicht
		messageDetailsTable[0][7] = new JLabel(messages.getString("schichten_lokaler_weg_msg2"));
		// Protokoll
		messageDetailsTable[1][7] = new JLabel(messages.getString("schichten_lokaler_weg_msg3"));
		// Daten
		messageDetailsTable[2][7] = new JLabel(messages.getString("schichten_lokaler_weg_msg4"));
		// Netzzugang
		messageDetailsTable[0][0] = new JLabel(messages.getString("rp_lauscher_msg8"));
		// Vermittlung
		messageDetailsTable[0][2] = new JLabel(messages.getString("rp_lauscher_msg9"));
		// Transport
		messageDetailsTable[0][4] = new JLabel(messages.getString("rp_lauscher_msg10"));
		// Anwendung
		messageDetailsTable[0][6] = new JLabel(messages.getString("rp_lauscher_msg11"));
		// Ethernet
		messageDetailsTable[1][0] = new JLabel(messages.getString("schichten_lokaler_weg_msg5"));
		// Quelle (Netzzugang)
		messageDetailsTable[2][0] = new JLabel(messages.getString("rp_lauscher_msg3"));
		// Ziel (Netzzugang)
		messageDetailsTable[3][0] = new JLabel(messages.getString("rp_lauscher_msg4"));
		// Bemerkungen / Details (Netzzugang)
		messageDetailsTable[4][0] = new JLabel(messages.getString("rp_lauscher_msg7"));
		// Quelle (Vermittlung)
		messageDetailsTable[2][2] = new JLabel(messages.getString("rp_lauscher_msg3"));
		// Ziel (Vermittlung)
		messageDetailsTable[3][2] = new JLabel(messages.getString("rp_lauscher_msg4"));
		// Bemerkungen / Details (Vermittlung)
		messageDetailsTable[4][2] = new JLabel(messages.getString("rp_lauscher_msg7"));
		// Quelle (Transport)
		messageDetailsTable[2][4] = new JLabel(messages.getString("rp_lauscher_msg3"));
		// Ziel (Transport)
		messageDetailsTable[3][4] = new JLabel(messages.getString("rp_lauscher_msg4"));
		// Bemerkungen / Details (Transport)
		messageDetailsTable[4][4] = new JLabel(messages.getString("rp_lauscher_msg7"));
	}

	/**
	 * Nur zu Testzwecken (um auf der Programmausgabe beide Nachrichtendetails
	 * anzuzeigen).
	 * 
	 * @param breite Anzahl Zeichen pro Zelle
	 */
	private void show(int breite) {
		System.out.println("-----messageDetailsReceived-----");
		showDetails(breite, messageDetailsReceived);
		System.out.println("-------messageDetailsSent-------");
		showDetails(breite, messageDetailsSent);
	}

	/**
	 * Nur zu Testzwecken (siehe show())
	 * 
	 * @param breite Anzahl Zeichen pro Zelle
	 * @param table  Die zu füllenden Nachrichtendetails
	 */
	private void showDetails(int breite, JLabel[][] table) {
		for (int i = 0; i < 5; i++) {
			String line = "";
			for (int j = 0; j < 8; j++) {
				JLabel cell = table[i][j];
				if (!(cell == null)) {
					String cellContent = cell.getText();
					int wordlength = cellContent.length();
					if (wordlength < breite) {
						line = line.concat(cellContent);
						for (int k = 0; k < breite - wordlength; k++) {
							line = line.concat(" ");
						}
					} else {
						for (int k = 0; k < breite; k++) {
							line = line.concat(" ");
						}
					}
				} else {
					for (int k = 0; k < breite; k++) {
						line = line.concat(" ");
					}
				}

			}
			System.out.println(line);
		}
		System.out.println("-------------------------------------");
	}

	private class MessageDetailsTable extends JPanel {
		private final GridBagLayout gridBagLayout;
		private GridBagConstraints c;
		
		public MessageDetailsTable(JLabel[][] messageDetailsReceived, JLabel[][] messageDetailsSent) {
			gridBagLayout = new GridBagLayout();
			c = new GridBagConstraints();
			this.setLayout(gridBagLayout);
			
			// For each component to be added to this container:
			// ...Create the component...
			// ...Set instance variables in the GridBagConstraints instance...
			JButton button = new JButton("HOIIIIII");
			this.add(button, c);
		}
	}
}
