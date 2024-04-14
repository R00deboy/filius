package filius.gui.schichtensicht;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import filius.hardware.knoten.InternetKnoten;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.nachrichten.Lauscher;
import filius.software.system.GatewayFirmware;
import filius.software.system.SystemSoftware;
import filius.software.system.VermittlungsrechnerBetriebssystem;

/**
 * Panel which is embedded in a JDialog window. Consists of a splitPane
 * (visualisation in the upper part, message details table in the lower part).
 * 
 * @author Christoph Irniger
 */
public class LocalPathPanel extends JPanel implements I18n {
	private final SystemSoftware systemSoftware;

	private final String interfaceId;

	private final int selectedFrameNumber; // beginnt bei 1

	private String[][] messageDetailsSent = new String[5][8]; // für die versendete Nachricht
	private String[][] messageDetailsReceived = new String[5][8]; // für die ankommende Nachricht

	private boolean sent = false;
	private boolean received = false;

	private final LocalPathVisualisation visualisation; // Visualisierung oben
	private final MessageDetailsTable tableSent; // Nachrichtendetails für versendete Daten
	private final MessageDetailsTable tableReceived; // Nachrichtendetails für empfangene Daten

	private static Lauscher lauscher = Lauscher.getLauscher(); // Zugriff auf den Lauscher

	private static final String IP_BROADCAST = "255.255.255.255";
	private static final String IP_ZEROS = "0.0.0.0";

	public LocalPathPanel(LocalPathDialog localPathDialog) {
		super();

		// inherit instance variables from super container (localPathDialog)
		this.systemSoftware = localPathDialog.getSystemSoftware();
		this.interfaceId = localPathDialog.getInterfaceId();
		this.selectedFrameNumber = localPathDialog.getSelectedFrameNumber();

		if (isFrameSent()) {
			sent = true;
		} else {
			received = true;
		}

		// fill messageDetailsReceived and messageDetailsSent
		fill();

		// initialise visualisation and table for message details
		visualisation = new LocalPathVisualisation(localPathDialog.getSystemSoftware());
		tableSent = new MessageDetailsTable(messageDetailsSent);
		tableReceived = new MessageDetailsTable(messageDetailsReceived);

		// scrollPanes
		JScrollPane visualisationScrollPane = new JScrollPane(visualisation);
		JScrollPane messageDetailsScrollPane = determineDefaultMessageDetails();

		// splitPane for visualisation in upper and message details in lower part
		JSplitPane splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane.setTopComponent(visualisationScrollPane);
		splitPane.setBottomComponent(messageDetailsScrollPane);

		// layout manager for this panel
		BorderLayout borderLayout = new BorderLayout(10, 10);
		setLayout(borderLayout);

		// add splitPane to the current JPanel
		add(splitPane, BorderLayout.CENTER);
	}

	public String[][] getMessageDetailsReceived() {
		return this.messageDetailsReceived;
	}

	public String[][] getMessageDetailsSent() {
		return this.messageDetailsSent;
	}

	/*
	 * Bestimmt die Standard-Ansicht der Nachrichtendetails im unteren Teil der
	 * SplitPane: {@code messageDetailsSent} oder {@code messageDetailsReceived}.
	 */
	private JScrollPane determineDefaultMessageDetails() {
		JScrollPane messageDetailsScrollPane = new JScrollPane(tableReceived);
		if (sent && !received) {
			messageDetailsScrollPane = new JScrollPane(tableSent);
		}
		return messageDetailsScrollPane;
	}

	/**
	 * Füllt die Nachrichtendetails {@code messageDetailsSent} und
	 * {@code messageDetailsReceived} mit allen für die Visualisierung benötigten
	 * Daten des/der Frames.
	 */
	private void fill() {
		if (sent) {
			fillTitles(messageDetailsSent);
			fillMessageDetails(messageDetailsSent, this.interfaceId, this.selectedFrameNumber);
			fillOtherIfRouter();
		} else {
			fillTitles(messageDetailsReceived);
			fillMessageDetails(messageDetailsReceived, this.interfaceId, this.selectedFrameNumber);
			fillOtherIfRouter();
		}
		show(25);
	}

	/**
	 * Prüft, ob das ausgewählte Frame empfangen oder versandt (= nicht empfangen)
	 * wurde.
	 */
	private boolean isFrameSent() {
		return this.interfaceId
				.equals(lauscher.getFrame(this.interfaceId, this.selectedFrameNumber).getQuellMacAdresse());
	}

	/**
	 * Hat nur eine Wirkung bei Routern, bei denen das IpPaket im Frame "auf der
	 * Durchreise" ist. Füllt diejenigen Nachrichtendetails
	 * ({@code messageDetailsReceived} oder {@code messageDetailsSent}) aus, welche
	 * noch nicht ausgefüllt wurde.
	 */
	private void fillOtherIfRouter() {
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
	private void fillMessageDetails(String[][] messageDetails, String interfaceId, int selectedFrameNumber) {
		Object[][] daten = lauscher.getDaten(interfaceId, false, 0);

		String selectedFrameNumberAsString = Integer.toString(selectedFrameNumber);

		for (int i = 0; i < daten.length; i++) {
			if (daten[i][0].equals(selectedFrameNumberAsString)) {
				// -----Netzzugangs-Daten-----
				// Quelle
				messageDetails[2][1] = daten[i][2].toString();
				// Ziel
				messageDetails[3][1] = daten[i][3].toString();
				// Bemerkungen / Details
				messageDetails[4][1] = daten[i][6].toString();

				// -----Vermittlungs-Daten (falls existent)-----
				if (i + 1 < daten.length && daten[i + 1][0].equals(selectedFrameNumberAsString)) {
					// Quelle
					messageDetails[2][3] = daten[i + 1][2].toString();
					// Ziel
					messageDetails[3][3] = daten[i + 1][3].toString();
					// Bemerkungen / Details
					messageDetails[4][3] = daten[i + 1][6].toString();
					// Protokoll
					messageDetails[1][2] = daten[i + 1][4].toString();
				}

				// -----Transport-Daten (falls existent)-----
				if (i + 2 < daten.length && daten[i + 2][0].equals(selectedFrameNumberAsString)) {
					// Quelle
					messageDetails[2][5] = daten[i + 2][2].toString();
					// Ziel
					messageDetails[3][5] = daten[i + 2][3].toString();
					// Bemerkungen / Details
					messageDetails[4][5] = daten[i + 2][6].toString();
					// Protokoll
					messageDetails[1][4] = daten[i + 2][4].toString();
				}

				// -----Anwendungs-Daten (falls existent)-----
				if (i + 3 < daten.length && daten[i + 3][0].equals(selectedFrameNumberAsString)) {
					// Protokoll
					messageDetails[2][6] = daten[i + 3][6].toString();
					// Daten
					messageDetails[1][6] = daten[i + 3][4].toString();
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
	private void fillTitles(String[][] messageDetails) {
		// Schicht
		messageDetails[0][7] = messages.getString("schichten_lokaler_weg_msg2");
		// Protokoll
		messageDetails[1][7] = messages.getString("schichten_lokaler_weg_msg3");
		// Daten
		messageDetails[2][7] = messages.getString("schichten_lokaler_weg_msg4");
		// Netzzugang
		messageDetails[0][0] = messages.getString("rp_lauscher_msg8");
		// Vermittlung
		messageDetails[0][2] = messages.getString("rp_lauscher_msg9");
		// Transport
		messageDetails[0][4] = messages.getString("rp_lauscher_msg10");
		// Anwendung
		messageDetails[0][6] = messages.getString("rp_lauscher_msg11");
		// Ethernet
		messageDetails[1][0] = messages.getString("schichten_lokaler_weg_msg5");
		// Quelle (Netzzugang)
		messageDetails[2][0] = messages.getString("rp_lauscher_msg3");
		// Ziel (Netzzugang)
		messageDetails[3][0] = messages.getString("rp_lauscher_msg4");
		// Bemerkungen / Details (Netzzugang)
		messageDetails[4][0] = messages.getString("rp_lauscher_msg7");
		// Quelle (Vermittlung)
		messageDetails[2][2] = messages.getString("rp_lauscher_msg3");
		// Ziel (Vermittlung)
		messageDetails[3][2] = messages.getString("rp_lauscher_msg4");
		// Bemerkungen / Details (Vermittlung)
		messageDetails[4][2] = messages.getString("rp_lauscher_msg7");
		// Quelle (Transport)
		messageDetails[2][4] = messages.getString("rp_lauscher_msg3");
		// Ziel (Transport)
		messageDetails[3][4] = messages.getString("rp_lauscher_msg4");
		// Bemerkungen / Details (Transport)
		messageDetails[4][4] = messages.getString("rp_lauscher_msg7");
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
	private void showDetails(int breite, String[][] table) {
		for (int i = 0; i < 5; i++) {
			String line = "";
			for (int j = 0; j < 8; j++) {
				String text = table[i][j];
				if (!(text == null)) {
					int wordlength = text.length();
					if (wordlength < breite) {
						line = line.concat(text);
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

		private MessageDetailsTable(String[][] messageDetails) {
			gridBagLayout = new GridBagLayout();
			c = new GridBagConstraints();
			this.setLayout(gridBagLayout);

			setLabel("hoi", 0, 0, 1);
		}

		private void setLabel(String text, int x_pos, int y_pos, int width) {
			JLabel label;

			label = new JLabel(text);
			c.gridx = x_pos;
			c.gridy = y_pos;
			c.gridwidth = width;
			add(label, c);
		}
	}
}
