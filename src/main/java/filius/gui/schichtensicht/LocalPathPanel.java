package filius.gui.schichtensicht;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import filius.gui.nachrichtensicht.AggregatedMessageTable.LayerColorVar;
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

	private boolean sent = false; // if the node sent the frame, otherwise the node received the frame
	private boolean forwarding = false; // if the node is only forwarding an IP or ICMP packet

	private final LocalPathVisualisation visualisation; // Visualisierung oben
	private final MessageDetailsTable tableSent; // Nachrichtendetails für versendete Daten
	private final MessageDetailsTable tableReceived; // Nachrichtendetails für empfangene Daten

	Object[] correspondingFrameData;

	private static final int PREF_MSG_DETAILS_CELL_WIDTH = 150;
	private static final int PREF_MSG_DETAILS_CELL_HEIGHT = 30;

	private static Lauscher lauscher = Lauscher.getLauscher(); // Zugriff auf den Lauscher

	public LocalPathPanel(LocalPathDialog localPathDialog) {
		super();

		// inherit instance variables from super container (localPathDialog)
		this.systemSoftware = localPathDialog.getSystemSoftware();
		this.interfaceId = localPathDialog.getInterfaceId();
		this.selectedFrameNumber = localPathDialog.getSelectedFrameNumber();

		if (isFrameSent()) {
			sent = true;
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

	/**
	 * Prüft, ob das ausgewählte Frame empfangen oder versandt (= nicht empfangen)
	 * wurde.
	 */
	private boolean isFrameSent() {
		return this.interfaceId
				.equals(lauscher.getFrame(this.interfaceId, this.selectedFrameNumber).getQuellMacAdresse());
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
			setForwardingAndFillOther();
		} else {
			fillTitles(messageDetailsReceived);
			fillMessageDetails(messageDetailsReceived, this.interfaceId, this.selectedFrameNumber);
			setForwardingAndFillOther();
		}
		show(25);
	}

	/*
	 * Füllt die andere Nachrichtentabelle aus, falls der Knoten ein Router ist und
	 * das IP- oder ICMP-Paket von ihm nur weitergeleitet wird.
	 */
	private void setForwardingAndFillOther() {
		correspondingFrameData = lauscher.getCorrespondingFrame(this.interfaceId, this.selectedFrameNumber,
				this.systemSoftware);
		String corrInterfaceId = (String) correspondingFrameData[0];
		Integer corrFrameNumber = (Integer) correspondingFrameData[1];
		setForwarding(corrInterfaceId, corrFrameNumber);
		fillOtherIfForwarding(corrInterfaceId, corrFrameNumber);
	}

	/**
	 * Setzt {@code forwarding} auf wahr, falls das IP- oder ICMP-Paket beim Router
	 * nur weitergeleitet wird.
	 */
	private void setForwarding(String corrInterfaceId, Integer corrFrameNumber) {
		if (systemSoftware instanceof VermittlungsrechnerBetriebssystem || systemSoftware instanceof GatewayFirmware) {
			if (corrInterfaceId != null && corrFrameNumber != null) {
				forwarding = true;
			}
		}
	}

	/**
	 * Falls {@code forwarding} wahr ist, füllt diese Methode diejenigen
	 * {@code messageDetailsSent} aus, welche noch nicht ausgefüllt wurden, da für
	 * die Visualisierung beide benötigt werden.
	 */
	private void fillOtherIfForwarding(String corrInterfaceId, Integer corrFrameNumber) {
		if (forwarding == true) {
			if (sent) {
				fillTitles(messageDetailsReceived);
				fillMessageDetails(messageDetailsReceived, corrInterfaceId, corrFrameNumber);
			} else {
				fillTitles(messageDetailsSent);
				fillMessageDetails(messageDetailsSent, corrInterfaceId, corrFrameNumber);
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
					messageDetails[1][6] = daten[i + 3][4].toString();
					// Daten
					messageDetails[2][6] = daten[i + 3][6].toString();
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
		messageDetails[2][0] = messages.getString("rp_lauscher_msg3") + ":";
		// Ziel (Netzzugang)
		messageDetails[3][0] = messages.getString("rp_lauscher_msg4") + ":";
		// Bemerkungen / Details (Netzzugang)
		messageDetails[4][0] = messages.getString("rp_lauscher_msg7") + ":";
		// Quelle (Vermittlung)
		messageDetails[2][2] = messages.getString("rp_lauscher_msg3") + ":";
		// Ziel (Vermittlung)
		messageDetails[3][2] = messages.getString("rp_lauscher_msg4") + ":";
		// Bemerkungen / Details (Vermittlung)
		messageDetails[4][2] = messages.getString("rp_lauscher_msg7") + ":";
		// Quelle (Transport)
		messageDetails[2][4] = messages.getString("rp_lauscher_msg3") + ":";
		// Ziel (Transport)
		messageDetails[3][4] = messages.getString("rp_lauscher_msg4") + ":";
		// Bemerkungen / Details (Transport)
		messageDetails[4][4] = messages.getString("rp_lauscher_msg7") + ":";
	}

	/*
	 * Bestimmt, welche Nachrichtendetails standardmässig beim erstmaligen Öffnen
	 * des Fensters im unteren Teil der SplitPane zu sehen sind: {@code
	 * messageDetailsSent} oder {@code messageDetailsReceived}.
	 */
	private JScrollPane determineDefaultMessageDetails() {
		JScrollPane messageDetailsScrollPane;
		if (sent) {
			// for the sender a message
			messageDetailsScrollPane = new JScrollPane(tableSent);
		} else {
			// for the receiver of a message
			// for a router, which is only forwarding an IP or ICMP packet
			messageDetailsScrollPane = new JScrollPane(tableReceived);
		}
		return messageDetailsScrollPane;
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

	/*
	 * Class for the table at the bottom of the SplitPane for the message details.
	 */
	private class MessageDetailsTable extends JPanel {
		private final GridBagLayout gridBagLayout;
		private GridBagConstraints c;

		private MessageDetailsTable(String[][] messageDetails) {
			gridBagLayout = new GridBagLayout();
			c = new GridBagConstraints();
			this.setLayout(gridBagLayout);
			JLabel label;
			JTextArea textArea;

			// first line (from left to right)
			for (int j = 0; j < 6; j = j + 2) {
				label = setLabel(messageDetails[0][j], j, 0, 2, 1, 2 / 8, 1 / 5, 1, 1);
				label.setHorizontalAlignment(SwingConstants.CENTER);
				setBold(label);
				if (j == 0 || j == 1) {
					label.setBackground(LayerColorVar.LINK.backgroundColorFine);
				} else if (j == 2 || j == 3) {
					label.setBackground(LayerColorVar.INTERNET.backgroundColorFine);
				} else if (j == 4 || j == 5) {
					label.setBackground(LayerColorVar.TRANSPORT.backgroundColorFine);
				}
			}
			label = setLabel(messageDetails[0][6], 6, 0, 1, 1, 1 / 8, 1 / 5, 2, 1);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setBackground(LayerColorVar.APPLICATION.backgroundColorFine);
			setBold(label);
			label = setLabel(messageDetails[0][7], 7, 0, 1, 1, 1 / 8, 1 / 5, 1, 1);
			label.setBackground(LayerColorVar.DEFAULT.backgroundColor);
			setBold(label);

			// second line (from left to right)
			for (int j = 0; j < 6; j = j + 2) {
				label = setLabel(messageDetails[1][j], j, 1, 2, 1, 2 / 8, 1 / 5, 1, 1);
				label.setHorizontalAlignment(SwingConstants.CENTER);
				if (j == 0 || j == 1) {
					label.setBackground(LayerColorVar.LINK.backgroundColor);
				} else if (j == 2 || j == 3) {
					label.setBackground(LayerColorVar.INTERNET.backgroundColor);
				} else if (j == 4 || j == 5) {
					label.setBackground(LayerColorVar.TRANSPORT.backgroundColor);
				}
			}
			label = setLabel(messageDetails[1][6], 6, 1, 1, 1, 1 / 8, 1 / 5, 2, 1);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setBackground(LayerColorVar.APPLICATION.backgroundColor);
			label = setLabel(messageDetails[1][7], 7, 1, 1, 1, 1 / 8, 1 / 5, 1, 1);
			label.setBackground(LayerColorVar.DEFAULT.backgroundColor);
			setBold(label);

			// third and fourth line, first to sixth columns (from left to right)
			for (int i = 2; i < 4; i++) {
				for (int j = 0; j < 6; j++) {
					label = setLabel(messageDetails[i][j], j, i, 1, 1, 1 / 8, 1 / 5, 1, 1);
					if (j % 2 == 0) {
						label.setHorizontalAlignment(SwingConstants.RIGHT);
					}
					if (j == 0 || j == 1) {
						label.setBackground(LayerColorVar.LINK.backgroundColor);
					} else if (j == 2 || j == 3) {
						label.setBackground(LayerColorVar.INTERNET.backgroundColor);
					} else if (j == 4 || j == 5) {
						label.setBackground(LayerColorVar.TRANSPORT.backgroundColor);
					}
				}
			}

			// fifth line, first to sixth columns (from left to right)
			for (int j = 0; j < 6; j++) {
				if (j % 2 == 0) {
					label = setLabel(messageDetails[4][j], j, 4, 1, 1, 1 / 8, 1 / 5, 1, 7);
					label.setHorizontalAlignment(SwingConstants.RIGHT);
					label.setVerticalAlignment(SwingConstants.TOP);
					if (j == 0) {
						label.setBackground(LayerColorVar.LINK.backgroundColor);
					} else if (j == 2) {
						label.setBackground(LayerColorVar.INTERNET.backgroundColor);
					} else if (j == 4) {
						label.setBackground(LayerColorVar.TRANSPORT.backgroundColor);
					}
				} else {
					textArea = setTextArea(messageDetails[4][j], j, 4, 1, 1, 1 / 8, 1 / 5, 1, 7);
					if (j == 1) {
						textArea.setBackground(LayerColorVar.LINK.backgroundColor);
					} else if (j == 3) {
						textArea.setBackground(LayerColorVar.INTERNET.backgroundColor);
					} else if (j == 5) {
						textArea.setBackground(LayerColorVar.TRANSPORT.backgroundColor);
					}
				}
			}

			// last two cells on the bottom right with height 3
			textArea = setTextArea(messageDetails[2][6], 6, 2, 1, 3, 1 / 8, 3 / 5, 2, 3);
			textArea.setBackground(LayerColorVar.APPLICATION.backgroundColor);
			label = setLabel(messageDetails[2][7], 7, 2, 1, 3, 1 / 8, 1 / 5, 1, 3);
			label.setVerticalAlignment(SwingConstants.TOP);
			label.setBackground(LayerColorVar.DEFAULT.backgroundColor);
			setBold(label);
		}

		// Cells with short content are JLabels
		private JLabel setLabel(String text, int x_pos, int y_pos, int width, int height, double weightx,
				double weighty, int width_multiplier, int height_multiplier) {
			JLabel label = new JLabel(text);

			label.setPreferredSize(new Dimension(width_multiplier * width * PREF_MSG_DETAILS_CELL_WIDTH,
					height_multiplier * height * PREF_MSG_DETAILS_CELL_HEIGHT));
			label.setOpaque(true);

			setPlain(label);

			c.gridx = x_pos;
			c.gridy = y_pos;
			c.gridwidth = width;
			c.gridheight = height;
			c.weightx = weightx;
			c.weighty = weighty;
			Border border = BorderFactory.createLineBorder(Color.BLACK);
			Border margin = new EmptyBorder(5, 5, 5, 5);
			label.setBorder(new CompoundBorder(border, margin));
			add(label, c);

			return label;
		}

		// Cells with potential long content are JTextAreas
		private JTextArea setTextArea(String text, int x_pos, int y_pos, int width, int height, double weightx,
				double weighty, int width_multiplier, int height_multiplier) {
			JTextArea textArea = new JTextArea(text);

			textArea.setPreferredSize(new Dimension(width_multiplier * width * PREF_MSG_DETAILS_CELL_WIDTH,
					height_multiplier * height * PREF_MSG_DETAILS_CELL_HEIGHT));

			textArea.setWrapStyleWord(true);
			textArea.setLineWrap(true);
			textArea.setOpaque(true);
			textArea.setEditable(false);
			textArea.setFocusable(false);

			c.gridx = x_pos;
			c.gridy = y_pos;
			c.gridwidth = width;
			c.gridheight = height;
			c.weightx = weightx;
			c.weighty = weighty;
			Border border = BorderFactory.createLineBorder(Color.BLACK);
			Border margin = new EmptyBorder(5, 5, 5, 5);
			textArea.setBorder(new CompoundBorder(border, margin));
			add(textArea, c);

			return textArea;
		}

		private void setBold(Component comp) {
			Font boldFont = new Font(comp.getFont().getName(), Font.BOLD, comp.getFont().getSize());
			comp.setFont(boldFont);
		}

		private void setPlain(Component comp) {
			Font boldFont = new Font(comp.getFont().getName(), Font.PLAIN, comp.getFont().getSize());
			comp.setFont(boldFont);
		}
	}
}
