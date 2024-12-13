package filius.gui.schichtensicht;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import filius.gui.nachrichtensicht.AggregatedMessageTable;
import filius.hardware.knoten.Gateway;
import filius.hardware.knoten.Host;
import filius.hardware.knoten.InternetKnoten;
import filius.hardware.knoten.Knoten;
import filius.hardware.knoten.Vermittlungsrechner;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.nachrichten.Lauscher;
import filius.software.system.SystemSoftware;

/**
 * @author Christoph Irniger
 */
public class LocalLayerPathPanel extends JPanel implements I18n {
	private final Knoten NODE;

	private final String INTERFACE_ID;

	/** frame number which was selected by mouse click, starts with 1 */
	private final int SELECTED_FRAME_NUMBER;

	/** if the node sent the frame, otherwise the node received the frame */
	private final boolean SENT;
	/** if the node forwarded an IP or ICMP packet */
	private final boolean FORWARDED;

	private final JSplitPane SPLIT_PANE;
	private static final int DIVIDER_LOCATION = 450;

	/** details of the sent message */
	private String[][] messageDetailsSent = new String[6][8];
	/** details of the received message */
	private String[][] messageDetailsReceived = new String[6][8];

	private MessageDetailsTable tableSent;
	private MessageDetailsTable tableReceived;

	// info about the corresponding frame
	private final Object[] CORR_FRAME_DATA;
	private final String CORR_INTERFACE_ID;

	/**
	 * maximum layer inside the frame (1 = link, 2 = internet, 3 = transport, 4 =
	 * application)
	 */
	private final int MAX_LAYER_INSIDE_FRAME;
	/**
	 * maximum layer on which the node operates (1 = link, 2 = internet, 3 =
	 * transport, 4 = application)
	 */
	private final int MAX_LAYER_OF_OPERATION;

	/**
	 * current number of hidden layers 1 = Link, 2 = Link & Internet, 3 = Link &
	 * Internet & Transport
	 */
	private int hiddenLayers;

	private static final Color BACKGROUND_COLOR = Color.WHITE;

	private static final int MSG_DETAILS_CELL_WIDTH = 150;
	private static final int MSG_DETAILS_CELL_HEIGHT = 30;

	public LocalLayerPathPanel(JFrame owner, String interfaceId, SystemSoftware systemSoftware, int selectedFrameNumber,
			GlobalLayerPath globalLayerPath) {
		super();

		// inherit instance variables from super container (localPathDialog)
		NODE = systemSoftware.getKnoten();
		this.INTERFACE_ID = interfaceId;
		this.SELECTED_FRAME_NUMBER = selectedFrameNumber;
		MAX_LAYER_OF_OPERATION = ((InternetKnoten) getKnoten()).getMaxLayerOfOperation();

		// determine if frame was sent or received
		SENT = isFrameSent(interfaceId, selectedFrameNumber);

		/*
		 * Fill matrices messageDetailsReceived and messageDetailsSent. Both have an
		 * empty second line (e.g. messageDetailsReceived[1][...] is Null), such that it
		 * will match row and column numbers of the MessageDetailsTable, which will be
		 * the visible element.
		 */
		MAX_LAYER_INSIDE_FRAME = fill();

		// determine if the packet (inside the frame) was forwarded
		CORR_FRAME_DATA = Lauscher.getLauscher().getCorrespondingFrame(this.INTERFACE_ID, this.SELECTED_FRAME_NUMBER,
				(InternetKnoten) this.NODE);
		CORR_INTERFACE_ID = (String) CORR_FRAME_DATA[0];
		Integer corrFrameNumber = (Integer) CORR_FRAME_DATA[1];
		FORWARDED = isFrameForwarded(CORR_INTERFACE_ID, corrFrameNumber);
		fillOtherIfForwarded(CORR_INTERFACE_ID, corrFrameNumber);

		// initialise visualisation and tables for message details
		tableSent = new MessageDetailsTable(messageDetailsSent);
		tableReceived = new MessageDetailsTable(messageDetailsReceived);
		setLayersInTablesInvisible();

		// SplitPane (for visualisation in upper and message details in lower part)
		SPLIT_PANE = new JSplitPane();
		SPLIT_PANE.setOrientation(JSplitPane.VERTICAL_SPLIT);
		SPLIT_PANE.setDividerLocation(DIVIDER_LOCATION);

		// ScrollPane for bottom of SplitPane
		setDefaultMessageDetails();

		// layout manager for this panel
		setLayout(new BorderLayout(10, 10));

		// add splitPane to the current JPanel
		add(SPLIT_PANE, BorderLayout.CENTER);

		// set visualisation on top half of the SplitPane
		SPLIT_PANE.setTopComponent(new JScrollPane(new LocalLayerPathVisualisation(this, globalLayerPath)));
	}

	public Knoten getKnoten() {
		return NODE;
	}

	public String getInterfaceId() {
		return INTERFACE_ID;
	}

	public int getMaxLayerInsideFrame() {
		return MAX_LAYER_INSIDE_FRAME;
	}

	public int getMaxLayerOfOperation() {
		return MAX_LAYER_OF_OPERATION;
	}

	public String getCorrInterfaceId() {
		return CORR_INTERFACE_ID;
	}

	public Color getBackgroundColor() {
		return BACKGROUND_COLOR;
	}

	public boolean isSent() {
		return SENT;
	}

	public boolean isForwarded() {
		return FORWARDED;
	}
	
	public int getDividerLocation() {
		return DIVIDER_LOCATION;
	}

	public Dimension getSizeOfMessageDetailsTable() {
		return MessageDetailsTable.getSizeOfMessageDetailsTable();
	}

	/**
	 * Checks if the selected frame is sent or received (= not sent).
	 * 
	 * @return true if sent, false if received
	 */
	public static boolean isFrameSent(String interfaceId, int selectedFrameNumber) {
		return interfaceId
				.equals(Lauscher.getLauscher().getFrame(interfaceId, selectedFrameNumber).getQuellMacAdresse());
	}

	/**
	 * Fills {@code messageDetailsSent} and {@code messageDetailsReceived} with all
	 * data of the frame(s) which is needed for the visualisation.
	 * 
	 * @return maximum layer that appears inside the frame
	 */
	private int fill() {
		if (SENT) {
			fillTitles(messageDetailsSent);
			return fillMessageDetails(messageDetailsSent, this.INTERFACE_ID, this.SELECTED_FRAME_NUMBER);
		} else {
			fillTitles(messageDetailsReceived);
			return fillMessageDetails(messageDetailsReceived, this.INTERFACE_ID, this.SELECTED_FRAME_NUMBER);
		}
	}

	/**
	 * Fills either {@code messageDetailsSent} or {@code messageDetailsReceived}
	 * with the values of the selected interface and frame number.
	 * 
	 * @param messageDetails      Either {@code messageDetailsSent} or
	 *                            {@code messageDetailsReceived}
	 * @param interfaceId
	 * @param selectedFrameNumber
	 * @return maximum layer that appears inside the frame
	 */
	private int fillMessageDetails(String[][] messageDetails, String interfaceId, int selectedFrameNumber) {
		Object[][] daten = Lauscher.getLauscher().getDaten(interfaceId, false, 0);
		String selectedFrameNumberAsString = Integer.toString(selectedFrameNumber);
		int maxLayerInsideFrame = 1;

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
						maxLayerInsideFrame = 2;
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
						maxLayerInsideFrame = 3;
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
						maxLayerInsideFrame = 4;
					}
					// Daten
					messageDetails[3][6] = daten[i + 3][6].toString();
				}
				break;
			}
		}
		return maxLayerInsideFrame;
	}

	/**
	 * Fills either {@code messageDetailsSent} or {@code messageDetailsReceived}
	 * with the titles (column and cell labels) that are the same for both tables.
	 * 
	 * @param messageDetails Either {@code messageDetailsSent} or
	 *                       {@code messageDetailsReceived}
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
	 * Only has an effect if the node is a router (or home router). If the IP or
	 * ICMP packet was forwarded, this method sets {@code forwarded} to true.
	 * 
	 * @param corrInterfaceId corresponding interface ID
	 * @param corrFrameNumber corresponding frame number
	 */
	private boolean isFrameForwarded(String corrInterfaceId, Integer corrFrameNumber) {
		return ((NODE instanceof Vermittlungsrechner || NODE instanceof Gateway)
				&& (corrInterfaceId != null && corrFrameNumber != null));
	}

	/**
	 * Only has an effect if the node is a router (or home router). If
	 * {@code forwarded} is true, sets the message details of the table which is not
	 * yet filled ({@code messageDetailsSent} or {@code messageDetailsReceived}).
	 * 
	 * @param corrInterfaceId
	 * @param corrFrameNumber
	 */
	private void fillOtherIfForwarded(String corrInterfaceId, Integer corrFrameNumber) {
		if (FORWARDED) {
			if (SENT) {
				fillTitles(messageDetailsReceived);
				fillMessageDetails(messageDetailsReceived, corrInterfaceId, corrFrameNumber);
			} else {
				fillTitles(messageDetailsSent);
				fillMessageDetails(messageDetailsSent, corrInterfaceId, corrFrameNumber);
			}
		}
	}

	/**
	 * Determines and sets the message details ({@code messageDetailsSent} or
	 * {@code messageDetailsReceived}) are visible when opening the whole window of
	 * the local path for the first time.
	 */
	private void setDefaultMessageDetails() {
		if (SENT && !FORWARDED) {

			// for the sender of a message
			updateMessageDetails(true);
			if (NODE instanceof Host) {
				for (int i = 1; i < MAX_LAYER_INSIDE_FRAME; i++) {
					hideLayerData(i, tableSent);
					System.out.println("hide layer data " + i);
				}
				hiddenLayers = MAX_LAYER_INSIDE_FRAME - 1;
			} else if (NODE instanceof Vermittlungsrechner || NODE instanceof Gateway) {
				hideLayerData(1, tableSent);
				hiddenLayers = 1;
			}
		} else {

			// for the receiver of a message
			// for a router, which has forwarded an IP or ICMP packet
			updateMessageDetails(false);
		}
	}

	/**
	 * Updates the message details (e.g. if one of the "forth" or "back" button was
	 * pressed).
	 * 
	 * @param senderSideIsActive If true, the visualisation is on the sender side,
	 *                           otherwise on the receiver side.
	 */
	public void updateMessageDetails(boolean senderSideIsActive) {
		if (senderSideIsActive) {
			// save old divider location
			int currentDividerLocation = SPLIT_PANE.getDividerLocation();
			// set new table
			SPLIT_PANE.setBottomComponent(new JScrollPane(tableSent));
			// set old divider location
			SPLIT_PANE.setDividerLocation(currentDividerLocation);
		} else {
			// save old divider location
			int currentDividerLocation = SPLIT_PANE.getDividerLocation();
			// set new table
			SPLIT_PANE.setBottomComponent(new JScrollPane(tableReceived));
			// set old divider location
			SPLIT_PANE.setDividerLocation(currentDividerLocation);
		}
	}

	/**
	 * Sets the layers in {@code tableSent} and {@code tableReceived} which are not
	 * needed invisible according to {@code maxLayer}.
	 */
	private void setLayersInTablesInvisible() {

		// determine columns to deactivate
		Set<Integer> columns = new HashSet<>();
		if (MAX_LAYER_INSIDE_FRAME == 4) {
			return;
		} else if (MAX_LAYER_INSIDE_FRAME == 3) {
			columns.add(6); // application data
		} else if (MAX_LAYER_INSIDE_FRAME == 2) {
			columns.add(4); // transport data left side
			columns.add(5); // transport data right side
			columns.add(6); // application data
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

	/**
	 * Hide data of a layer in the message details. 1 = link, 2 = internet, 3 =
	 * transport.
	 * 
	 * @param layerNumber number for layer
	 * @param table       table in which layer data should be hidden (either the
	 *                    table for the sent or the received message)
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

					// set foreground and background to main background (such that the component
					// gets invisible)
					component.setBackground(table.getBackground());
					component.setForeground(table.getBackground());

					// remove border
					setBorderVisible(component, false);
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

			// determine columns to show
			Set<Integer> columns = new HashSet<>();
			int firstColumn = (layerNumber - 1) * 2;
			columns.add(firstColumn);
			columns.add(firstColumn + 1);

			// show columns
			for (Component component : table.getComponents()) {
				if (columns.contains(table.gridBagLayout.getConstraints(component).gridx)) {
					int gridy = table.gridBagLayout.getConstraints(component).gridy;
					if (gridy > 1) {

						// set colors
						component.setForeground(getLayerColorVar("foregroundColor", layerNumber));
						if (gridy == 0) {
							component.setBackground(getLayerColorVar("backgroundColorFine", layerNumber));
						} else {
							component.setBackground(getLayerColorVar("backgroundColor", layerNumber));

							// create border
							setBorderVisible(component, true);
						}
					}
				}
			}
		}
	}

	/**
	 * If the boolean value is true, creates the border of the component. Otherwise
	 * sets an empty border.
	 * 
	 * @param component the component whose border will be affected
	 * @param isVisible the border is visible (true) or not (false)
	 */
	private void setBorderVisible(Component component, boolean isVisible) {
		if (isVisible) {

			// create border
			Border loweredEtchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			Border marginBorder = new EmptyBorder(5, 5, 5, 5);
			if (component instanceof JTextArea) {
				((JTextArea) component).setBorder(new CompoundBorder(loweredEtchedBorder, marginBorder));
				;
			} else if (component instanceof JLabel) {
				((JLabel) component).setBorder(new CompoundBorder(loweredEtchedBorder, marginBorder));
			}
		} else {

			// remove border (set empty border)
			if (component instanceof JTextArea) {
				((JTextArea) component).setBorder(javax.swing.BorderFactory.createEmptyBorder());
			} else if (component instanceof JLabel) {
				((JLabel) component).setBorder(javax.swing.BorderFactory.createEmptyBorder());
			}
		}
	}

	/**
	 * This method returns the color defined in the enum class LayerColorVar in
	 * class AggregatedMessageTable corresponding to the type of color and the layer
	 * which belongs to the column of the MessageDetailsTable.
	 * 
	 * @param typeOfColor All types of colors defined in the enum class in class
	 *                    AggregatedMessageTable: "foregroundColor",
	 *                    "backgroundColor", "backgroundColorFine",
	 *                    "backgroundColorUltraFine", "borderColor"
	 * @param column      column of the MessageDetailsTable
	 * @return the desired color
	 */
	private Color getColorByColumn(String typeOfColor, int column) {
		Color color = null;
		if (column == 0 || column == 1) {
			color = getLayerColorVar(typeOfColor, 1);
		} else if (column == 2 || column == 3) {
			color = getLayerColorVar(typeOfColor, 2);
		} else if (column == 4 || column == 5) {
			color = getLayerColorVar(typeOfColor, 3);
		} else if (column == 6) {
			color = getLayerColorVar(typeOfColor, 4);
		}
		return color;
	}

	/**
	 * This method returns the color defined in the enum class LayerColorVar in
	 * class AggregatedMessageTable corresponding to the type of color and the layer
	 * number.
	 * 
	 * @param typeOfColor All types of colors defined in the enum class in class
	 *                    AggregatedMessageTable: "foregroundColor",
	 *                    "backgroundColor", "backgroundColorFine",
	 *                    "backgroundColorUltraFine", "borderColor"
	 * @param layerNumber number of the layer (1 = LINK, 2 = INTERNET, 3 =
	 *                    TRANSPORT, 4 = APPLICATION)
	 * @return the desired color
	 */
	public Color getLayerColorVar(String typeOfColor, int layerNumber) {
		return AggregatedMessageTable.LayerColorVar.getLayerColorVar(typeOfColor, layerNumber);
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

			// Storage for creating the Labels (cells with little content) and
			// TextAreas (cells with potential large content)
			JLabel label;
			JTextArea textArea;

			// first row (from left to right)
			for (int column = 0; column < 6; column = column + 2) {
				label = setLabel(messageDetails[0][column], column, 0, 2, 1, 1, 1, false);
				label.setHorizontalAlignment(SwingConstants.CENTER);
				label.setBackground((getColorByColumn("backgroundColorFine", column)));
				label.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
				setBoldAndItalic(label);
			}
			label = setLabel(messageDetails[0][6], 6, 0, 1, 1, 2, 1, false);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setBackground(getLayerColorVar("backgroundColorFine", 4));
			label.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			setBoldAndItalic(label);
			label = setLabel(messageDetails[0][7], 7, 0, 1, 1, 0.5, 1, false);
			label.setHorizontalAlignment(JLabel.CENTER);
			label.setVerticalAlignment(JLabel.CENTER);
			label.setBackground(BACKGROUND_COLOR);
			label.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			setBoldAndItalic(label);

			// second empty row, which is transparent (from left to right)
			for (int column = 0; column < 6; column = column + 2) {
				label = setEmptyCell(column, 1, 2, 1, 1, 0.8);
			}
			label = setEmptyCell(6, 1, 1, 1, 2, 0.8);
			label = setEmptyCell(7, 1, 1, 1, 0.5, 0.8);

			// third row (from left to right)
			for (int column = 0; column < 6; column = column + 2) {
				label = setLabel(messageDetails[2][column], column, 2, 2, 1, 1, 1, true);
				label.setHorizontalAlignment(SwingConstants.CENTER);
				label.setBackground((getColorByColumn("backgroundColor", column)));
			}
			label = setLabel(messageDetails[2][6], 6, 2, 1, 1, 2, 1, true);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setBackground(getLayerColorVar("backgroundColor", 4));
			label = setLabel(messageDetails[2][7], 7, 2, 1, 1, 0.5, 1, false);
			label.setHorizontalAlignment(JLabel.CENTER);
			label.setVerticalAlignment(JLabel.CENTER);
			label.setBackground(BACKGROUND_COLOR);
			label.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			setBoldAndItalic(label);

			// fourth and fifth row, first to sixth columns (from left to right)
			for (int row = 3; row < 5; row++) {
				for (int column = 0; column < 6; column++) {
					label = setLabel(messageDetails[row][column], column, row, 1, 1, 1, 1, true);
					if (column % 2 == 0) {
						label.setHorizontalAlignment(SwingConstants.RIGHT);
					}
					label.setBackground((getColorByColumn("backgroundColor", column)));
				}
			}

			// sixth row, first to sixth columns (from left to right)
			for (int column = 0; column < 6; column++) {
				if (column % 2 == 0) {
					label = setLabel(messageDetails[5][column], column, 5, 1, 1, 1, 3, true);
					label.setHorizontalAlignment(SwingConstants.RIGHT);
					label.setVerticalAlignment(SwingConstants.TOP);
					label.setBackground((getColorByColumn("backgroundColor", column)));
				} else {
					textArea = setTextArea(messageDetails[5][column], column, 5, 1, 1, 1, 3);
					textArea.setBackground(getColorByColumn("backgroundColor", column));
				}
			}

			// last two cells on the bottom right with height 3
			textArea = setTextArea(messageDetails[3][6], 6, 3, 1, 3, 2, 5 / 3.0);
			textArea.setBackground(getLayerColorVar("backgroundColor", 4));
			label = setLabel(messageDetails[3][7], 7, 3, 1, 3, 0.5, 5 / 3.0, false);
			label.setHorizontalAlignment(JLabel.CENTER);
			label.setVerticalAlignment(JLabel.CENTER);
			label.setBackground(BACKGROUND_COLOR);
			label.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			setBoldAndItalic(label);
		}

		@Override
		public Dimension getPreferredSize() {
			return getSizeOfMessageDetailsTable();
		}

		@Override
		public Dimension getMinimumSize() {
			return getSizeOfMessageDetailsTable();
		}

		@Override
		public Dimension getMaximumSize() {
			return getSizeOfMessageDetailsTable();
		}

		/**
		 * size of the whole table
		 * 
		 * @return Dimension of the whole MessageDetailsTable
		 */
		private static Dimension getSizeOfMessageDetailsTable() {
			return new Dimension(9 * LocalLayerPathPanel.MSG_DETAILS_CELL_WIDTH,
					8 * LocalLayerPathPanel.MSG_DETAILS_CELL_HEIGHT);
		}

		/**
		 * Helper method to create a new JLabel in the GridBagLayout. JLabels are needed
		 * for one line content.
		 * 
		 * @return The JLabel
		 */
		private JLabel setLabel(String text, int x_pos, int y_pos, int width, int height, double width_multiplier,
				double height_multiplier, boolean needsBorder) {
			JLabel label = new JLabel(text);

			setStandards(label, x_pos, y_pos, width, height, width_multiplier, height_multiplier);
			label.setOpaque(true);
			setPlain(label);

			if (needsBorder) {
				setBorderVisible(label, true);
			}
			add(label, c);

			return label;
		}

		/**
		 * Helper method to create a new JTextArea in the GridBagLayout. JTextAreas are
		 * needed for multiline content.
		 * 
		 * @return The JTextArea
		 */
		private JTextArea setTextArea(String text, int x_pos, int y_pos, int width, int height, double width_multiplier,
				double height_multiplier) {
			JTextArea textArea = new JTextArea(text);

			setStandards(textArea, x_pos, y_pos, width, height, width_multiplier, height_multiplier);
			textArea.setOpaque(true);
			textArea.setWrapStyleWord(true);
			textArea.setLineWrap(true);
			textArea.setEditable(false);
			textArea.setFocusable(false);

			setBorderVisible(textArea, true);
			add(textArea, c);

			return textArea;
		}

		/**
		 * Helper method to create a visually empty cell, which is a JLabel, into the
		 * GridBagLayout. Empty cells form the gap between the first and the third row.
		 * 
		 * @param x_pos
		 * @param y_pos
		 * @param width
		 * @param height
		 * @param width_multiplier
		 * @param height_multiplier
		 * @return The JLabel
		 */
		private JLabel setEmptyCell(int x_pos, int y_pos, int width, int height, double width_multiplier,
				double height_multiplier) {
			JLabel label = new JLabel();

			setStandards(label, x_pos, y_pos, width, height, width_multiplier, height_multiplier);
			label.setOpaque(false);

			label.setBorder(javax.swing.BorderFactory.createEmptyBorder());
			add(label, c);

			return label;
		}

		/**
		 * Helper method to set standard GridBagLayout constraints.
		 * 
		 * @param component
		 * @param x_pos
		 * @param y_pos
		 * @param width
		 * @param height
		 * @param width_multiplier
		 * @param height_multiplier
		 */
		private void setStandards(Component component, int x_pos, int y_pos, int width, int height,
				double width_multiplier, double height_multiplier) {
			Dimension dimensionOfComponent = new Dimension((int) (width_multiplier * width * MSG_DETAILS_CELL_WIDTH),
					(int) (height_multiplier * height * MSG_DETAILS_CELL_HEIGHT));
			component.setPreferredSize(dimensionOfComponent);
			component.setMinimumSize(dimensionOfComponent);
			component.setMaximumSize(dimensionOfComponent);
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