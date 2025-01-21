package filius.gui.schichtensicht;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import filius.gui.nachrichtensicht.MessageDetailsTable;
import filius.hardware.knoten.Gateway;
import filius.hardware.knoten.Host;
import filius.hardware.knoten.InternetKnoten;
import filius.hardware.knoten.Knoten;
import filius.hardware.knoten.Vermittlungsrechner;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.nachrichten.Lauscher;
import filius.software.system.SystemSoftware;

/**
 * This class is used to visualise the path of a layer 2 packet (in Filius: IP
 * or ARP packet) through the TCP/IP layers on the selected node (host or
 * router). The idea was taken from the following website and with permission of
 * the creator: https://oinf.ch/interactive/tcp-ip-visualisierung/
 * 
 * This view corresponds to reality. In packet-switching networks like the
 * internet a single node in general cannot track the end-to-end path of a
 * packet. This follows immediately from the properties of the packet-switching
 * network model. The node can only reproduce the receiving, processing and
 * sending of a packet.
 * 
 * @author Christoph Irniger
 */
public class LocalLayerPathPanel extends JPanel implements I18n {
	private final Knoten NODE;

	private final String INTERFACE_ID;

	/** frame number which was selected by mouse click, starts with 1 */
	private final int SELECTED_FRAME_NUMBER;

	/** if null, we are in the local layer path view */
	private final GlobalLayerPath globalLayerPath;

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
	 * current number of hidden layers (0 = none, 1 = Link, 2 = Link & Internet, 3 =
	 * Link & Internet & Transport)
	 */
	private int hiddenLayers;

	private final Color GREYED_OUT_COLOR = Color.decode("#A6A6A6");
	
	private static final Color BACKGROUND_COLOR = Color.WHITE;

	public LocalLayerPathPanel(JFrame owner, String interfaceId, SystemSoftware systemSoftware, int selectedFrameNumber,
			GlobalLayerPath globalLayerPath) {
		super();

		// inherit instance variables from super container (localPathDialog)
		NODE = systemSoftware.getKnoten();
		this.INTERFACE_ID = interfaceId;
		this.SELECTED_FRAME_NUMBER = selectedFrameNumber;
		this.globalLayerPath = globalLayerPath;
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

		// initialise tables for message details
		tableSent = new MessageDetailsTable(messageDetailsSent, BACKGROUND_COLOR);
		tableReceived = new MessageDetailsTable(messageDetailsReceived, BACKGROUND_COLOR);
		setLayersInTablesInvisible(tableSent);
		setLayersInTablesInvisible(tableReceived);

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

	public boolean isSent() {
		return SENT;
	}

	public boolean isForwarded() {
		return FORWARDED;
	}

	public int getDividerLocation() {
		return DIVIDER_LOCATION;
	}
	
	public static Color getBackgroundColor() {
		return BACKGROUND_COLOR;
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
	 * Determines if the frame was forwarded.
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
	 * {@code messageDetailsReceived}) which are visible when opening the whole
	 * window of the local path for the first time.
	 */
	private void setDefaultMessageDetails() {
		// for the sender of a message
		if (SENT) {
			updateMessageDetails(true);
			if (NODE instanceof Host) {
				// if in local view or if in global view and active border does not start at end
				// hide all layers except the maximum layer inside the frame (else show all
				// layers)
				if (globalLayerPath == null || !globalLayerPath.isActiveBorderOfNextPanelAtEnd()) {
					for (int i = 1; i < MAX_LAYER_INSIDE_FRAME; i++) {
						hideLayerData(i, tableSent);
					}
					hiddenLayers = MAX_LAYER_INSIDE_FRAME - 1;
				} else {
					hiddenLayers = 0;
				}
			} else if (NODE instanceof Vermittlungsrechner || NODE instanceof Gateway) {
				hideLayerData(1, tableSent);
				hiddenLayers = 1;
			}
		}
		// for the receiver of a message
		else {
			if (globalLayerPath != null && globalLayerPath.isActiveBorderOfNextPanelAtEnd()) {
				updateMessageDetails(true);
			} else {
				updateMessageDetails(false);
			}
		}
		updatedGreyedOutCells(tableSent);
		updatedGreyedOutCells(tableReceived);
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
	 * Sets the layers in the table which are not needed invisible according to
	 * {@code MAX_LAYER_INSIDE_FRAME}.
	 * 
	 * @param table the table whose layers should be made invisible
	 */
	private void setLayersInTablesInvisible(MessageDetailsTable table) {
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
		for (Component component : table.getComponents()) {
			if (columns.contains(table.getGridBagLayout().getConstraints(component).gridx)) {
				component.setVisible(false);
			}
		}
	}

	/**
	 * Hide data of the next higher layer in the message details. Hideable layers
	 * are 1 = link, 2 = internet, 3 = transport.
	 */
	public void hideNextHigherLayer() {
		if (hiddenLayers <= 2 && hiddenLayers >= 0) {
			hiddenLayers++;
			updatedGreyedOutCells(tableSent);
			updatedGreyedOutCells(tableReceived);
			hideLayerData(hiddenLayers, tableSent);
			hideLayerData(hiddenLayers, tableReceived);
		}
	}

	/**
	 * Show data of the next lower layer in the message details. Hideable layers are
	 * 1 = link, 2 = internet, 3 = transport.
	 */
	public void showNextLowerLayer() {
		if (hiddenLayers <= 3 && hiddenLayers >= 1) {
			hiddenLayers--;
			updatedGreyedOutCells(tableSent);
			updatedGreyedOutCells(tableReceived);
			showLayerData(hiddenLayers + 1, tableSent);
			showLayerData(hiddenLayers + 1, tableReceived);
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
				if (columns.contains(table.getGridBagLayout().getConstraints(component).gridx)
						&& table.getGridBagLayout().getConstraints(component).gridy > 1) {

					// set foreground and background to main background (such that the component
					// gets invisible)
					component.setBackground(table.getBackground());
					component.setForeground(table.getBackground());

					// remove border
					table.setBorderVisible(component, false);
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
				if (columns.contains(table.getGridBagLayout().getConstraints(component).gridx)) {
					int gridy = table.getGridBagLayout().getConstraints(component).gridy;
					if (gridy > 1) {

						// set colors
						component.setForeground(MessageDetailsTable.getLayerColorVar("foregroundColor", layerNumber));
						if (gridy == 0) {
							component.setBackground(
									MessageDetailsTable.getLayerColorVar("backgroundColorFine", layerNumber));
						} else {
							component.setBackground(
									MessageDetailsTable.getLayerColorVar("backgroundColor", layerNumber));

							// create border
							table.setBorderVisible(component, true);
						}
					}
				}
			}
		}
	}

	/**
	 * Set all cells which represent content of the message (i.e. the ones with y
	 * coordinate greater than 1) greyed out according to {@code hiddenlayers} and
	 * {@code MAX_LAYER_INSIDE_FRAME}.
	 * 
	 * @param table the table whose cells should be updated
	 */
	public void updatedGreyedOutCells(MessageDetailsTable table) {
		// determine columns to be greyed out
		Set<Integer> columns = new HashSet<>();
		for (int columnNo = (hiddenLayers + 1) * 2; columnNo <= 6; columnNo++) {
			columns.add(columnNo);
		}

		// grey out those components
		for (Component component : table.getComponents()) {
			int xCoord = table.getGridBagLayout().getConstraints(component).gridx;
			int yCoord = table.getGridBagLayout().getConstraints(component).gridy;
			if (yCoord >= 2) {
				if (columns.contains(xCoord)) {
					// if content is in the internet layer
					if (xCoord >= 2 && xCoord <= 3) {
						setComponentGreyedOut(component, 2);
					}
					// if content is in the transport layer
					else if (xCoord >= 4 && xCoord <= 5) {
						setComponentGreyedOut(component, 3);
					}
					// if content is in the application layer
					else if (xCoord == 6) {
						setComponentGreyedOut(component, 4);
					}
				}
				// if it is not the last column
				else if (xCoord != 7) {
					// if content is in the internet layer
					if (xCoord >= 2 && xCoord <= 3) {
						resetComponentFromGreyedOut(component, 2);
					}
					// if content is in the transport layer
					else if (xCoord >= 4 && xCoord <= 5) {
						resetComponentFromGreyedOut(component, 3);
					}
					// if content is in the application layer
					else if (xCoord == 6) {
						resetComponentFromGreyedOut(component, 4);
					}
				}
			}
		}
	}

	/**
	 * Set a components' foreground color (font color) to "greyed out" and the
	 * background color to the corresponding background color in ultra fine variant.
	 * 
	 * @param comp
	 * @param layerNumber
	 */
	private void setComponentGreyedOut(Component comp, int layerNumber) {
		comp.setForeground(GREYED_OUT_COLOR);
		comp.setBackground(MessageDetailsTable.getLayerColorVar("backgroundColorUltraFine", layerNumber));
	}

	/**
	 * Reset a components' foreground color (font color) to black and the background
	 * color to the corresponding background color.
	 * 
	 * @param comp
	 * @param layerNumber
	 */
	private void resetComponentFromGreyedOut(Component comp, int layerNumber) {
		// only reset if not hidden
		if (hiddenLayers < layerNumber) {
			comp.setForeground(Color.BLACK);
			comp.setBackground(MessageDetailsTable.getLayerColorVar("backgroundColor", layerNumber));
		}
	}
}