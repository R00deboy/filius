package filius.gui.schichtensicht;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
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
import filius.software.system.Betriebssystem;
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
	public final SystemSoftware systemSoftware;

	private final String interfaceId;

	private final int selectedFrameNumber; // beginnt bei 1

	private boolean sent = false; // if the node sent the frame, otherwise the node received the frame
	private boolean forwarding = false; // if the node is only forwarding an IP or ICMP packet

	public JSplitPane splitPane;

	private String[][] messageDetailsSent = new String[6][8]; // für die versendete Nachricht
	private String[][] messageDetailsReceived = new String[6][8]; // für die ankommende Nachricht

	private MessageDetailsTable tableSent;
	private MessageDetailsTable tableReceived;

	private LocalPathVisualisation visualisation;

	private final Object[] correspondingFrameData;

	// number of hidden layers
	// 1 = Link, 2 = Link & Internet, 3 = Link & Internet & Transport
	private int hiddenLayers;

	// highest layer that appears
	// 1 = link, 2 = internet, 3 = transport, 4 = application
	private int maxLayer;

	public static final int MSG_DETAILS_CELL_WIDTH = 150;
	public static final int MSG_DETAILS_CELL_HEIGHT = 30;

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

		/*
		 * Fill matrices messageDetailsReceived and messageDetailsSent. Both have an
		 * empty second line (e.g. messageDetailsReceived[1][...] is empty), such that
		 * it will match row and column numbers of the MessageDetailsTable, which will
		 * be the visible element.
		 */
		fill();
		correspondingFrameData = lauscher.getCorrespondingFrame(this.interfaceId, this.selectedFrameNumber,
				this.systemSoftware);
		setForwardingAndFillOther();

		// initialise visualisation and tables for message details
		visualisation = new LocalPathVisualisation(this);
		tableSent = new MessageDetailsTable(messageDetailsSent);
		tableReceived = new MessageDetailsTable(messageDetailsReceived);
		setLayersInTablesInvisible();

		// SplitPane (for visualisation in upper and message details in lower part)
		splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);

		// ScrollPane for top of SplitPane
		splitPane.setTopComponent(new JScrollPane(visualisation));

		// ScrollPane for bottom of SplitPane
		setDefaultMessageDetails();

		// layout manager for this panel
		BorderLayout borderLayout = new BorderLayout(10, 10);
		setLayout(borderLayout);

		// add splitPane to the current JPanel
		add(splitPane, BorderLayout.CENTER);

		// set size and positioning properties of outer JDialog
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		localPathDialog.setLocation(screenSize.width / 6, screenSize.height / 6);
		localPathDialog.setPreferredSize(
				new Dimension(tableSent.getPreferredSize().width, tableSent.getPreferredSize().height + 300));
		localPathDialog.setMinimumSize(
				new Dimension(tableSent.getMinimumSize().width, tableSent.getMinimumSize().height + 300));
	}

	public String[][] getMessageDetailsReceived() {
		return this.messageDetailsReceived;
	}

	public String[][] getMessageDetailsSent() {
		return this.messageDetailsSent;
	}

	public int getMaxLayer() {
		return this.maxLayer;
	}

	public boolean isSent() {
		return this.sent;
	}

	public boolean isForwarding() {
		return this.forwarding;
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
		} else {
			fillTitles(messageDetailsReceived);
			fillMessageDetails(messageDetailsReceived, this.interfaceId, this.selectedFrameNumber);
		}
		// show(25);
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
				messageDetails[3][1] = daten[i][2].toString();
				// Ziel
				messageDetails[4][1] = daten[i][3].toString();
				// Bemerkungen / Details
				messageDetails[5][1] = daten[i][6].toString();

				// -----Vermittlungs-Daten (falls existent)-----
				if (i + 1 < daten.length && daten[i + 1][0].equals(selectedFrameNumberAsString)) {
					// Protokoll
					messageDetails[2][2] = daten[i + 1][4].toString();
					// set maxLayer, can increase later in this method
					if (messageDetails[2][2] != null) {
						maxLayer = 2;
					}
					// Quelle
					messageDetails[3][3] = daten[i + 1][2].toString();
					// Ziel
					messageDetails[4][3] = daten[i + 1][3].toString();
					// Bemerkungen / Details
					messageDetails[5][3] = daten[i + 1][6].toString();
				}

				// -----Transport-Daten (falls existent)-----
				if (i + 2 < daten.length && daten[i + 2][0].equals(selectedFrameNumberAsString)) {
					// Protokoll
					messageDetails[2][4] = daten[i + 2][4].toString();
					// set maxLayer, can increase later in this method
					if (messageDetails[2][4] != null) {
						maxLayer = 3;
					}
					// Quelle
					messageDetails[3][5] = daten[i + 2][2].toString();
					// Ziel
					messageDetails[4][5] = daten[i + 2][3].toString();
					// Bemerkungen / Details
					messageDetails[5][5] = daten[i + 2][6].toString();
				}

				// -----Anwendungs-Daten (falls existent)-----
				if (i + 3 < daten.length && daten[i + 3][0].equals(selectedFrameNumberAsString)) {
					// Protokoll
					messageDetails[2][6] = daten[i + 3][4].toString();
					// set maxLayer
					if (messageDetails[2][6] != null) {
						maxLayer = 4;
					}
					// Daten
					messageDetails[3][6] = daten[i + 3][6].toString();
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
		messageDetails[2][7] = messages.getString("schichten_lokaler_weg_msg3");
		// Daten
		messageDetails[3][7] = messages.getString("schichten_lokaler_weg_msg4");
		// Netzzugang
		messageDetails[0][0] = messages.getString("rp_lauscher_msg8");
		// Vermittlung
		messageDetails[0][2] = messages.getString("rp_lauscher_msg9");
		// Transport
		messageDetails[0][4] = messages.getString("rp_lauscher_msg10");
		// Anwendung
		messageDetails[0][6] = messages.getString("rp_lauscher_msg11");
		// Ethernet
		messageDetails[2][0] = messages.getString("schichten_lokaler_weg_msg5");
		// Quelle (Netzzugang)
		messageDetails[3][0] = messages.getString("rp_lauscher_msg3") + ":";
		// Ziel (Netzzugang)
		messageDetails[4][0] = messages.getString("rp_lauscher_msg4") + ":";
		// Bemerkungen / Details (Netzzugang)
		messageDetails[5][0] = messages.getString("rp_lauscher_msg7") + ":";
		// Quelle (Vermittlung)
		messageDetails[3][2] = messages.getString("rp_lauscher_msg3") + ":";
		// Ziel (Vermittlung)
		messageDetails[4][2] = messages.getString("rp_lauscher_msg4") + ":";
		// Bemerkungen / Details (Vermittlung)
		messageDetails[5][2] = messages.getString("rp_lauscher_msg7") + ":";
		// Quelle (Transport)
		messageDetails[3][4] = messages.getString("rp_lauscher_msg3") + ":";
		// Ziel (Transport)
		messageDetails[4][4] = messages.getString("rp_lauscher_msg4") + ":";
		// Bemerkungen / Details (Transport)
		messageDetails[5][4] = messages.getString("rp_lauscher_msg7") + ":";
	}

	/**
	 * Füllt die andere Nachrichtentabelle aus, falls der Knoten ein Router ist und
	 * das IP- oder ICMP-Paket von ihm nur weitergeleitet wird.
	 */
	private void setForwardingAndFillOther() {
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
	 * Bestimmt, welche Nachrichtendetails standardmässig beim erstmaligen Öffnen
	 * des Fensters im unteren Teil der SplitPane zu sehen sind: {@code
	 * messageDetailsSent} oder {@code messageDetailsReceived}.
	 */
	private void setDefaultMessageDetails() {
		if (sent && !forwarding) {
			// for the sender of a message
			updateMessageDetails(true);
			if (systemSoftware instanceof Betriebssystem) {
				for (int i = 1; i < maxLayer; i++) {
					hideLayerData(i, tableSent);
				}
				hiddenLayers = maxLayer - 1;
			} else if (systemSoftware instanceof VermittlungsrechnerBetriebssystem
					|| systemSoftware instanceof GatewayFirmware) {
				hideLayerData(1, tableSent);
				hiddenLayers = 1;
			}
		} else {
			// for the receiver of a message
			// for a router, which is only forwarding an IP or ICMP packet
			updateMessageDetails(false);
		}
	}

	/**
	 * Aktualisiert die Nachrichtendetails (z.B. wenn in der Visualisierung "zurück"
	 * oder "vorwärts" gedrückt wurde).
	 */
	public void updateMessageDetails(boolean senderSideIsActive) {
		if (senderSideIsActive) {
			splitPane.setBottomComponent(new JScrollPane(tableSent));
		} else {
			splitPane.setBottomComponent(new JScrollPane(tableReceived));
		}
	}

	/**
	 * Sets the layers in {@code tableSent} and {@code tableReceived} which are not
	 * needed invisible according to {@code maxLayer}.
	 */
	private void setLayersInTablesInvisible() {
		// determine columns to deactivate
		Set<Integer> columns = new HashSet<>();
		if (maxLayer == 4) {
			return;
		} else if (maxLayer == 3) {
			columns.add(6);
		} else if (maxLayer == 2) {
			columns.add(4);
			columns.add(5);
			columns.add(6);
		}
		// deactivate columns
		Set<MessageDetailsTable> tables = new HashSet<>();
		tables.add(tableSent);
		tables.add(tableReceived);
		for (MessageDetailsTable table : tables) {
			for (Component component : table.getComponents()) {
				if (columns.contains(table.gridBagLayout.getConstraints(component).gridx)) {
					component.setVisible(false);
				}
			}
		}
	}

	/**
	 * Hide data of a layer in the message details. 1 = link, 2 = internet, 3 =
	 * transport.
	 * 
	 * @param layerNumber number for layer
	 * @param table       in which layer data should be hidden
	 * @throws NoSuchElementException
	 */
	private void hideLayerData(int layerNumber, MessageDetailsTable table) throws NoSuchElementException {
		if (layerNumber > 3 || layerNumber < 1) {
			throw new NoSuchElementException("layerNumber has to be an integer from 1 to 3.");
		} else {
			// determine columns to hide
			Set<Integer> columns = new HashSet<>();
			int firstColumn = (layerNumber - 1) * 2;
			columns.add(firstColumn);
			columns.add(firstColumn + 1);
			// hide columns
			for (Component component : table.getComponents()) {
				if (columns.contains(table.gridBagLayout.getConstraints(component).gridx)
						&& table.gridBagLayout.getConstraints(component).gridy > 1) {
					component.setBackground(table.getBackground());
					component.setForeground(table.getBackground());
					if (component instanceof JTextArea) {
						((JTextArea) component).setBorder(javax.swing.BorderFactory.createEmptyBorder());
					} else if (component instanceof JLabel) {
						((JLabel) component).setBorder(javax.swing.BorderFactory.createEmptyBorder());
					}
				}
			}
		}
	}

	/**
	 * Show data of a layer in the message details. 1 = link, 2 = internet, 3 =
	 * transport.
	 * 
	 * @param layerNumber number for layer
	 * @param table       in which layer data should be shown
	 * @throws NoSuchElementException
	 */
	private void showLayerData(int layerNumber, MessageDetailsTable table) throws NoSuchElementException {
		if (layerNumber > 3 || layerNumber < 1) {
			throw new NoSuchElementException("layerNumber has to be an integer from 1 to 3.");
		} else {
			// determine columns to hide
			Set<Integer> columns = new HashSet<>();
			int firstColumn = (layerNumber - 1) * 2;
			columns.add(firstColumn);
			columns.add(firstColumn + 1);
			// determine colors
			Color foregroundColor = null;
			Color backgroundColor = null;
			Color backgroundColorFine = null;
			if (layerNumber == 1) {
				foregroundColor = LayerColorVar.LINK.foregroundColor;
				backgroundColor = LayerColorVar.LINK.backgroundColor;
				backgroundColorFine = LayerColorVar.LINK.backgroundColorFine;
			} else if (layerNumber == 2) {
				foregroundColor = LayerColorVar.INTERNET.foregroundColor;
				backgroundColor = LayerColorVar.INTERNET.backgroundColor;
				backgroundColorFine = LayerColorVar.INTERNET.backgroundColorFine;
			} else if (layerNumber == 3) {
				foregroundColor = LayerColorVar.TRANSPORT.foregroundColor;
				backgroundColor = LayerColorVar.TRANSPORT.backgroundColor;
				backgroundColorFine = LayerColorVar.TRANSPORT.backgroundColorFine;
			}
			// hide columns
			for (Component component : table.getComponents()) {
				if (columns.contains(table.gridBagLayout.getConstraints(component).gridx)) {
					int gridy = table.gridBagLayout.getConstraints(component).gridy;
					if (gridy > 1) {
						// set colors
						component.setForeground(foregroundColor);
						if (gridy == 0) {
							component.setBackground(backgroundColorFine);
						} else {
							component.setBackground(backgroundColor);
							// set border
							Border lineBorder = BorderFactory.createLineBorder(Color.BLACK);
							Border marginBorder = new EmptyBorder(5, 5, 5, 5);
							if (component instanceof JTextArea) {
								((JTextArea) component).setBorder(new CompoundBorder(lineBorder, marginBorder));
							} else if (component instanceof JLabel) {
								((JLabel) component).setBorder(new CompoundBorder(lineBorder, marginBorder));
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Hide data of the next higher layer in the message details. Hideable layers
	 * are 1 = link, 2 = internet, 3 = transport.
	 */
	public void hideNextHigherLayer() {
		if (hiddenLayers <= 2 && hiddenLayers >= 0) {
			hideLayerData(hiddenLayers + 1, tableSent);
			hideLayerData(hiddenLayers + 1, tableReceived);
			hiddenLayers++;
		}
	}

	/**
	 * Show data of the next lower layer in the message details. Hideable layers are
	 * 1 = link, 2 = internet, 3 = transport.
	 */
	public void showNextLowerLayer() {
		if (hiddenLayers <= 3 && hiddenLayers >= 1) {
			showLayerData(hiddenLayers, tableSent);
			showLayerData(hiddenLayers, tableReceived);
			hiddenLayers--;
		}
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
			setLayout(gridBagLayout);
			setBackground(Color.WHITE);

			// Storage for creating the Labes and TextAreas
			JLabel label;
			JTextArea textArea;

			// first line (from left to right)
			for (int j = 0; j < 6; j = j + 2) {
				label = setLabel(messageDetails[0][j], j, 0, 2, 1, 1, 1, false);
				label.setHorizontalAlignment(SwingConstants.CENTER);
				setBoldAndItalic(label);
				if (j == 0 || j == 1) {
					label.setBackground(LayerColorVar.LINK.backgroundColorFine);
				} else if (j == 2 || j == 3) {
					label.setBackground(LayerColorVar.INTERNET.backgroundColorFine);
				} else if (j == 4 || j == 5) {
					label.setBackground(LayerColorVar.TRANSPORT.backgroundColorFine);
				}
			}
			label = setLabel(messageDetails[0][6], 6, 0, 1, 1, 2, 1, false);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setBackground(LayerColorVar.APPLICATION.backgroundColorFine);
			setBoldAndItalic(label);
			label = setLabel(messageDetails[0][7], 7, 0, 1, 1, 0.5, 1, false);
			label.setHorizontalAlignment(JLabel.CENTER);
			label.setVerticalAlignment(JLabel.CENTER);
			;
			label.setBackground(LayerColorVar.DEFAULT.backgroundColor);
			setBoldAndItalic(label);

			// second empty line, which is transparent (from left to right)
			for (int j = 0; j < 6; j = j + 2) {
				label = setEmptyCell(j, 1, 2, 1, 1, 0.8);
			}
			label = setEmptyCell(6, 1, 1, 1, 2, 0.8);
			label = setEmptyCell(7, 1, 1, 1, 0.5, 0.8);

			// third line (from left to right)
			for (int j = 0; j < 6; j = j + 2) {
				label = setLabel(messageDetails[2][j], j, 2, 2, 1, 1, 1, true);
				label.setHorizontalAlignment(SwingConstants.CENTER);
				if (j == 0 || j == 1) {
					label.setBackground(LayerColorVar.LINK.backgroundColor);
				} else if (j == 2 || j == 3) {
					label.setBackground(LayerColorVar.INTERNET.backgroundColor);
				} else if (j == 4 || j == 5) {
					label.setBackground(LayerColorVar.TRANSPORT.backgroundColor);
				}
			}
			label = setLabel(messageDetails[2][6], 6, 2, 1, 1, 2, 1, true);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setBackground(LayerColorVar.APPLICATION.backgroundColor);
			label = setLabel(messageDetails[2][7], 7, 2, 1, 1, 0.5, 1, false);
			label.setHorizontalAlignment(JLabel.CENTER);
			label.setVerticalAlignment(JLabel.CENTER);
			label.setBackground(LayerColorVar.DEFAULT.backgroundColor);
			setBoldAndItalic(label);

			// fourth and fifth line, first to sixth columns (from left to right)
			for (int i = 3; i < 5; i++) {
				for (int j = 0; j < 6; j++) {
					label = setLabel(messageDetails[i][j], j, i, 1, 1, 1, 1, true);
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

			// sixth line, first to sixth columns (from left to right)
			for (int j = 0; j < 6; j++) {
				if (j % 2 == 0) {
					label = setLabel(messageDetails[5][j], j, 5, 1, 1, 1, 3, true);
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
					textArea = setTextArea(messageDetails[5][j], j, 5, 1, 1, 1, 3);
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
			textArea = setTextArea(messageDetails[3][6], 6, 3, 1, 3, 2, 5 / 3.0);
			textArea.setBackground(LayerColorVar.APPLICATION.backgroundColor);
			label = setLabel(messageDetails[3][7], 7, 3, 1, 3, 0.5, 5 / 3.0, false);
			label.setHorizontalAlignment(JLabel.CENTER);
			label.setVerticalAlignment(JLabel.CENTER);
			label.setBackground(LayerColorVar.DEFAULT.backgroundColor);
			setBoldAndItalic(label);
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(9 * LocalPathPanel.MSG_DETAILS_CELL_WIDTH, 8 * LocalPathPanel.MSG_DETAILS_CELL_HEIGHT);
		}

		@Override
		public Dimension getMinimumSize() {
			return new Dimension(9 * LocalPathPanel.MSG_DETAILS_CELL_WIDTH, 8 * LocalPathPanel.MSG_DETAILS_CELL_HEIGHT);
		}

		@Override
		public Dimension getMaximumSize() {
			return new Dimension(9 * LocalPathPanel.MSG_DETAILS_CELL_WIDTH, 8 * LocalPathPanel.MSG_DETAILS_CELL_HEIGHT);
		}

		// Cells with short content are JLabels
		private JLabel setLabel(String text, int x_pos, int y_pos, int width, int height, double width_multiplier,
				double height_multiplier, boolean needsBorder) {
			JLabel label = new JLabel(text);

			setStandards(label, x_pos, y_pos, width, height, width_multiplier, height_multiplier);
			label.setOpaque(true);
			setPlain(label);

			if (needsBorder) {
				Border lineBorder = BorderFactory.createLineBorder(Color.BLACK);
				Border marginBorder = new EmptyBorder(5, 5, 5, 5);
				label.setBorder(new CompoundBorder(lineBorder, marginBorder));
			}
			add(label, c);

			return label;
		}

		// Cells with potential long content are JTextAreas
		private JTextArea setTextArea(String text, int x_pos, int y_pos, int width, int height, double width_multiplier,
				double height_multiplier) {
			JTextArea textArea = new JTextArea(text);

			setStandards(textArea, x_pos, y_pos, width, height, width_multiplier, height_multiplier);
			textArea.setOpaque(true);
			textArea.setWrapStyleWord(true);
			textArea.setLineWrap(true);
			textArea.setEditable(false);
			textArea.setFocusable(false);

			Border lineBorder = BorderFactory.createLineBorder(Color.BLACK);
			Border marginBorder = new EmptyBorder(5, 5, 5, 5);
			textArea.setBorder(new CompoundBorder(lineBorder, marginBorder));
			add(textArea, c);

			return textArea;
		}

		// Empty cells form the gap between the first and the third row
		private JLabel setEmptyCell(int x_pos, int y_pos, int width, int height, double width_multiplier,
				double height_multiplier) {
			JLabel label = new JLabel();

			setStandards(label, x_pos, y_pos, width, height, width_multiplier, height_multiplier);
			label.setOpaque(false);

			label.setBorder(javax.swing.BorderFactory.createEmptyBorder());
			add(label, c);

			return label;
		}

		// set standard parameters
		private void setStandards(Component component, int x_pos, int y_pos, int width, int height,
				double width_multiplier, double height_multiplier) {
			component.setPreferredSize(new Dimension((int) (width_multiplier * width * MSG_DETAILS_CELL_WIDTH),
					(int) (height_multiplier * height * MSG_DETAILS_CELL_HEIGHT)));
			c.gridx = x_pos;
			c.gridy = y_pos;
			c.gridwidth = width;
			c.gridheight = height;
		}

		// bold and italic Font
		private void setBoldAndItalic(Component comp) {
			Font boldFont = new Font(comp.getFont().getName(), Font.BOLD + Font.ITALIC, comp.getFont().getSize());
			comp.setFont(boldFont);
		}

		// normal Font
		private void setPlain(Component comp) {
			Font boldFont = new Font(comp.getFont().getName(), Font.PLAIN, comp.getFont().getSize());
			comp.setFont(boldFont);
		}
	}
}