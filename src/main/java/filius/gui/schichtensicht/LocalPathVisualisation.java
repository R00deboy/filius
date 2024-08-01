package filius.gui.schichtensicht;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.InternetKnoten;
import filius.rahmenprogramm.I18n;

public class LocalPathVisualisation extends JPanel implements I18n {
	private final LocalPathPanel LOCAL_PATH_PANEL;

	private final JPanel CENTER_PANEL = new JPanel();
	private final GridBagLayout GB_LAYOUT_CENTER_PANEL = new GridBagLayout();
	private GridBagConstraints c = new GridBagConstraints();
	// x offset of center visualisation in GridBag layout
	private final static int X_OFFSET = 2;

	private int pointInTime;
	private int minPointInTime;
	private int maxPointInTime;

	// visualisation is on the sender side (otherwise receiver side)
	private boolean senderSideIsActive;
	// for this and all greater points in time visualisation is on the sender side
	// (for smaller on receiver side)
	private int pointInTimeForSenderSide;

	// vertical and horizontal gap between GridBag cells with content
	// (must be divisible by 2)
	private static final int GAP_BETWEEN_CELLS = 12;

	// width of rectangle including border (must be divisible by 2 *
	// RECTANGLE_RATIO)
	private static final int RECTANGLE_WIDTH = 180;
	// ratio of width to height of a rectangle including border
	private static final int RECTANGLE_RATIO = 3;
	// resulting height of a rectangle including border (must be divisible by 2)
	private static final int RECTANGLE_HEIGHT = RECTANGLE_WIDTH / RECTANGLE_RATIO;

	// instance variables for nested messages visualisation:
	// border width of standard border around nested messages
	private static final int MAIN_BORDER_WIDTH = 2;
	// border width of active border around nested messages
//	private static final int ACTIVE_BORDER_WIDTH = 2;
	// left gap around a nested message
	private static final int INDENT_NESTED_MSGS = 20;
	// upper, lower and right gap around a nested message
	private static final int BORDER_NESTED_MSGS = 5;
	// side length of squares needed for visualisation of a frame on cable (layer 1)
	private static final int SQUARE_SIDE_LENGTH = RECTANGLE_HEIGHT;
	// node name cell height
	private static final int NODE_NAME_CELL_HEIGHT = (int) (0.3 * RECTANGLE_HEIGHT);
	// node info cell ratio of width to height
	private static final int NODE_INFO_CELL_HEIGHT = (int) Math.round(RECTANGLE_WIDTH / 1.935);

	// buttons "vorwärts" and "zurück"
	private final JButton FORTH_IN_TIME;
	private final JButton BACK_IN_TIME;

	public LocalPathVisualisation(LocalPathPanel localPathPanel) {
		super();
		LOCAL_PATH_PANEL = localPathPanel;

		// set BorderLayout
		BorderLayout borderLayout = new BorderLayout();
		setLayout(borderLayout);
		setBackground(LOCAL_PATH_PANEL.getBackgroundColor());

		// set GridBagLayout for CENTER_PANEL
		CENTER_PANEL.setLayout(GB_LAYOUT_CENTER_PANEL);
		CENTER_PANEL.setBackground(LOCAL_PATH_PANEL.getBackgroundColor());
		c.anchor = GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.NONE;

		// create buttons
		FORTH_IN_TIME = new JButton(messages.getString("schichten_allgemein_msg1"));
		BACK_IN_TIME = new JButton(messages.getString("schichten_allgemein_msg2"));
		FORTH_IN_TIME.setPreferredSize(new Dimension(RECTANGLE_WIDTH, NODE_INFO_CELL_HEIGHT));
		BACK_IN_TIME.setPreferredSize(new Dimension(RECTANGLE_WIDTH, NODE_INFO_CELL_HEIGHT));
		// decativate "zurück" button if the frame was received
		if (!LOCAL_PATH_PANEL.isSent()) {
			BACK_IN_TIME.setEnabled(false);
		}

		// set initial value for senderSideIsActive
		senderSideIsActive = LOCAL_PATH_PANEL.isSent();

		// if the packet was not forwarded (i.e. sent or received)
		if (!LOCAL_PATH_PANEL.isForwarded()) {
			// configure left and right side (indication of layers and back and forth
			// buttons)
			addSidePanel(BACK_IN_TIME, 0); // left side
			addSidePanel(FORTH_IN_TIME, 7); // right side
			if (LOCAL_PATH_PANEL.isSent()) {
				minPointInTime = 4 - LOCAL_PATH_PANEL.getMaxLayerInsideFrame();
				maxPointInTime = 4;
				pointInTimeForSenderSide = -1; // should never be reached, since only sent
				LOCAL_PATH_PANEL.hideNextHigherLayer();
				LOCAL_PATH_PANEL.hideNextHigherLayer();
				LOCAL_PATH_PANEL.hideNextHigherLayer();
			} else {
				minPointInTime = 0;
				maxPointInTime = Math.min(LOCAL_PATH_PANEL.getMaxLayerInsideFrame(),
						LOCAL_PATH_PANEL.getMaxLayerOfOperation());
				pointInTimeForSenderSide = 5; // should never be reached, since only received
			}
			pointInTime = minPointInTime;
			visualisationSentOrReceived();
		}
		// if the packet was forwarded (only for routers)
		else {
			// configure left and right side (indication of layers and back and forth
			// buttons)
			addSidePanel(BACK_IN_TIME, 0); // left side
			addSidePanel(FORTH_IN_TIME, X_OFFSET + 10); // right side
			minPointInTime = 0;
			maxPointInTime = 5;
			if (LOCAL_PATH_PANEL.isSent()) {
				pointInTime = minPointInTime + 3;
				LOCAL_PATH_PANEL.hideNextHigherLayer();
			} else {
				pointInTime = minPointInTime;
			}
			pointInTimeForSenderSide = minPointInTime + 3;
			visualisationForwarded();
		}
		backAndForth();
	}

	public int getPointInTime() {
		return pointInTime;
	}

	public int getMaxPointInTime() {
		return maxPointInTime;
	}

	public int getMinPointInTime() {
		return minPointInTime;
	}

	/*
	 * @Override public Dimension getPreferredSize() { return new Dimension(480,
	 * 300); }
	 * 
	 * @Override public Dimension getMinimumSize() { return new Dimension(480, 300);
	 * }
	 * 
	 * @Override public Dimension getMaximumSize() { return new Dimension(480, 300);
	 * }
	 */

	/**
	 * Visualisation of a node if the packet was sent or received
	 */
	private void visualisationSentOrReceived() {
		c = new GridBagConstraints();
		JLabel label;
		JEditorPane editorPane;
		boolean activeBorder;

		// coordinates for the placement inside the GridBagLayout
		int xCoord0;
		int xCoord1;
		int xCoord2;
		int xCoord3;

		if (LOCAL_PATH_PANEL.isSent()) {
			xCoord0 = X_OFFSET + 0;
			xCoord1 = X_OFFSET + 1;
			xCoord2 = X_OFFSET + 2;
			xCoord3 = X_OFFSET + 3;
			// colored layer indicators
			coloredLayerIndicators(1, SQUARE_SIDE_LENGTH, true);
			coloredLayerIndicators(3, SQUARE_SIDE_LENGTH, false);
			coloredLayerIndicators(4, SQUARE_SIDE_LENGTH, false);
			coloredLayerIndicators(5, SQUARE_SIDE_LENGTH, false);
			coloredLayerIndicators(6, SQUARE_SIDE_LENGTH, true);
		} else {
			xCoord0 = X_OFFSET + 3;
			xCoord1 = X_OFFSET + 2;
			xCoord2 = X_OFFSET + 1;
			xCoord3 = X_OFFSET + 0;
			// colored layer indicators
			coloredLayerIndicators(X_OFFSET - 1, SQUARE_SIDE_LENGTH, true);
			coloredLayerIndicators(X_OFFSET, SQUARE_SIDE_LENGTH, false);
			coloredLayerIndicators(X_OFFSET + 1, SQUARE_SIDE_LENGTH, false);
			coloredLayerIndicators(X_OFFSET + 2, SQUARE_SIDE_LENGTH, false);
			coloredLayerIndicators(X_OFFSET + 4, SQUARE_SIDE_LENGTH, true);
		}

		// node name cell with empty cell above and below
		label = setEmptyCell(CENTER_PANEL, xCoord0, 0, 1, 1);
		setSize(label, RECTANGLE_WIDTH, GAP_BETWEEN_CELLS);
		label = setLabel(CENTER_PANEL, LOCAL_PATH_PANEL.getKnoten().holeAnzeigeName(), xCoord0, 1, 1, 1, true);
		setSize(label, RECTANGLE_WIDTH, NODE_NAME_CELL_HEIGHT);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label = setEmptyCell(CENTER_PANEL, xCoord0, 2, 1, 1);
		setSize(label, RECTANGLE_WIDTH, GAP_BETWEEN_CELLS);

		// node info cell with empty cell below
		String nodeInfo = createNodeInfoByMac(LOCAL_PATH_PANEL.getInterfaceId());
		editorPane = setEditorPane(CENTER_PANEL, nodeInfo, xCoord0, 3, 1, 1, true);
		setSize(editorPane, RECTANGLE_WIDTH, NODE_INFO_CELL_HEIGHT);
		label = setEmptyCell(CENTER_PANEL, xCoord0, 4, 1, 1);
		setSize(label, RECTANGLE_WIDTH, GAP_BETWEEN_CELLS);

		// create visualisations of rectangular nested messages including the arrows in
		// between
		createRectangularNestedMessages(xCoord0, null);

		// create first wire
		label = new WireWithArrow(null, true, SQUARE_SIDE_LENGTH, SQUARE_SIDE_LENGTH);
		setFinishedLabel(CENTER_PANEL, label, xCoord1, 11, 1, 1);

		// create square nested messages and if packet was received, create active (red)
		// border
		activeBorder = !LOCAL_PATH_PANEL.isSent();
		label = new NestedMessagesVisualisation(1, SQUARE_SIDE_LENGTH, 1, activeBorder);
		setFinishedLabel(CENTER_PANEL, label, xCoord2, 11, 1, 1);

		// create second wire
		label = new WireWithArrow(null, true, SQUARE_SIDE_LENGTH, SQUARE_SIDE_LENGTH);
		setFinishedLabel(CENTER_PANEL, label, xCoord3, 11, 1, 1);

		add(CENTER_PANEL, BorderLayout.CENTER);
	}

	/**
	 * Visualisation of a router (Vermittlungsrechner oder Heimrouter) which
	 * forwarded the packet. Because of its symmetric form, the visualisation is
	 * created from the left and right side to the middle at the same time.
	 */
	private void visualisationForwarded() {
		c = new GridBagConstraints();
		JLabel label;
		JEditorPane editorPane;

		// colored layer indicators
		coloredLayerIndicators(X_OFFSET - 1, SQUARE_SIDE_LENGTH, true);
		coloredLayerIndicators(X_OFFSET, SQUARE_SIDE_LENGTH, false);
		coloredLayerIndicators(X_OFFSET + 1, SQUARE_SIDE_LENGTH, false);
		coloredLayerIndicators(X_OFFSET + 2, SQUARE_SIDE_LENGTH, false);
		coloredLayerIndicators(X_OFFSET + 6, SQUARE_SIDE_LENGTH, false);
		coloredLayerIndicators(X_OFFSET + 7, SQUARE_SIDE_LENGTH, false);
		coloredLayerIndicators(X_OFFSET + 8, SQUARE_SIDE_LENGTH, false);
		coloredLayerIndicators(X_OFFSET + 9, SQUARE_SIDE_LENGTH, true);

		// create first wire on the left
		label = new WireWithArrow(null, true, SQUARE_SIDE_LENGTH, SQUARE_SIDE_LENGTH);
		setFinishedLabel(CENTER_PANEL, label, X_OFFSET + 0, 11, 1, 1);
		// create last (fourth) wire on the right
		label = new WireWithArrow(null, true, SQUARE_SIDE_LENGTH, SQUARE_SIDE_LENGTH);
		setFinishedLabel(CENTER_PANEL, label, X_OFFSET + 8, 11, 1, 1);

		// create left side square nested messages and if packet was received, create
		// active (red) border
		label = new NestedMessagesVisualisation(1, SQUARE_SIDE_LENGTH, 1, !LOCAL_PATH_PANEL.isSent());
		setFinishedLabel(CENTER_PANEL, label, X_OFFSET + 1, 11, 1, 1);
		// create right side square nested messages
		label = new NestedMessagesVisualisation(1, SQUARE_SIDE_LENGTH, 1, false);
		setFinishedLabel(CENTER_PANEL, label, X_OFFSET + 7, 11, 1, 1);

		// create second wire on the left
		label = new WireWithArrow(null, true, SQUARE_SIDE_LENGTH, SQUARE_SIDE_LENGTH);
		setFinishedLabel(CENTER_PANEL, label, X_OFFSET + 2, 11, 1, 1);
		// create second last (third) wire on the right
		label = new WireWithArrow(null, true, SQUARE_SIDE_LENGTH, SQUARE_SIDE_LENGTH);
		setFinishedLabel(CENTER_PANEL, label, X_OFFSET + 6, 11, 1, 1);

		// with GridBag width of 3: node name cell with empty cell above and below
		label = setEmptyCell(CENTER_PANEL, X_OFFSET + 3, 0, 3, 1);
		setSize(label, RECTANGLE_WIDTH, GAP_BETWEEN_CELLS);
		label = setLabel(CENTER_PANEL, LOCAL_PATH_PANEL.getKnoten().holeAnzeigeName(), X_OFFSET + 3, 1, 3, 1, true);
		setSize(label, 2 * RECTANGLE_WIDTH + GAP_BETWEEN_CELLS, NODE_NAME_CELL_HEIGHT);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label = setEmptyCell(CENTER_PANEL, X_OFFSET + 3, 2, 3, 1);
		setSize(label, RECTANGLE_WIDTH, GAP_BETWEEN_CELLS);

		// left and right node info cell with empty cell below
		String nodeInfoLeft;
		String nodeInfoRight;
		if (LOCAL_PATH_PANEL.isSent()) {
			nodeInfoLeft = createNodeInfoByMac(LOCAL_PATH_PANEL.getCorrInterfaceId());
			nodeInfoRight = createNodeInfoByMac(LOCAL_PATH_PANEL.getInterfaceId());
		} else {
			nodeInfoLeft = createNodeInfoByMac(LOCAL_PATH_PANEL.getInterfaceId());
			nodeInfoRight = createNodeInfoByMac(LOCAL_PATH_PANEL.getCorrInterfaceId());
		}
		editorPane = setEditorPane(CENTER_PANEL, nodeInfoLeft, X_OFFSET + 3, 3, 1, 1, true);
		setSize(editorPane, RECTANGLE_WIDTH, NODE_INFO_CELL_HEIGHT);
		label = setEmptyCell(CENTER_PANEL, X_OFFSET + 3, 4, 1, 1);
		setSize(label, RECTANGLE_WIDTH, GAP_BETWEEN_CELLS);
		editorPane = setEditorPane(CENTER_PANEL, nodeInfoRight, X_OFFSET + 5, 3, 1, 1, true);
		setSize(editorPane, RECTANGLE_WIDTH, NODE_INFO_CELL_HEIGHT);
		label = setEmptyCell(CENTER_PANEL, X_OFFSET + 5, 4, 1, 1);
		setSize(label, RECTANGLE_WIDTH, GAP_BETWEEN_CELLS);

		// visualisations of rectangular nested messages including the arrows
		// in between
		createRectangularNestedMessages(X_OFFSET + 3, false);
		createRectangularNestedMessages(X_OFFSET + 5, true);

		// set right arrow in the middle (on layer 2)
		label = new Arrow(null, true, GAP_BETWEEN_CELLS, RECTANGLE_HEIGHT);
		setFinishedLabel(CENTER_PANEL, label, X_OFFSET + 4, 9, 1, 1);

		// The right arrow is located at y coordinate 9 (see above). Thus skip i = 9.
		label = setLabel(CENTER_PANEL, "", X_OFFSET + 4, 5, 1, 1, false);
		setSize(label, GAP_BETWEEN_CELLS, RECTANGLE_HEIGHT);
		label.setBackground(LOCAL_PATH_PANEL.getLayerColorVar("backgroundColorUltraFine", 4));
		label = setLabel(CENTER_PANEL, "", X_OFFSET + 4, 7, 1, 1, false);
		setSize(label, GAP_BETWEEN_CELLS, RECTANGLE_HEIGHT);
		label.setBackground(LOCAL_PATH_PANEL.getLayerColorVar("backgroundColorUltraFine", 3));
		label = setLabel(CENTER_PANEL, "", X_OFFSET + 4, 11, 1, 1, false);
		setSize(label, GAP_BETWEEN_CELLS, RECTANGLE_HEIGHT);
		label.setBackground(LOCAL_PATH_PANEL.getLayerColorVar("backgroundColorUltraFine", 1));

		add(CENTER_PANEL, BorderLayout.CENTER);
	}

	/**
	 * This method colors the empty space between the (nested) messages and the
	 * layer indicators on the right and left in the corresponding backround color
	 * (the ultra fine alternative).
	 * 
	 * @param xCoord          x coordinate inside the GridBag layout
	 * @param width           width of the labels
	 * @param includingLayer1 indicates if layer 1 should be drawn (true) or not
	 *                        (false)
	 */
	private void coloredLayerIndicators(int xCoord, int width, boolean includingLayer1) {
		c = new GridBagConstraints();
		JLabel label;

		// layers 2 to 4
		for (int layerNumber = 4; layerNumber >= 2; layerNumber--) {
			label = setLabel(CENTER_PANEL, "", xCoord + 0, 5 + 2 * (4 - layerNumber), 1, 1, false);
			setSize(label, width, RECTANGLE_HEIGHT);
			label.setBackground(LOCAL_PATH_PANEL.getLayerColorVar("backgroundColorUltraFine", layerNumber));
		}
		// layer 1
		if (includingLayer1) {
			label = setLabel(CENTER_PANEL, "", xCoord + 0, 11, 1, 1, false);
			setSize(label, width, RECTANGLE_HEIGHT);
			label.setBackground(LOCAL_PATH_PANEL.getLayerColorVar("backgroundColorUltraFine", 1));
		}
	}

	/**
	 * This method creates a string with all the node info belonging to a MAC
	 * address (i.e. interface ID). In the visualisation this string will be
	 * displayed inside an EditorPane.
	 * 
	 * @param interfaceId MAC address of interface
	 * @return String with the resulting node info
	 */
	private String createNodeInfoByMac(String interfaceId) {
		InternetKnoten node = ((InternetKnoten) LOCAL_PATH_PANEL.getKnoten());
		NetzwerkInterface ni = node.getNetzwerkInterfaceByMac(interfaceId);
		String[] nodeInfos = new String[5];
		nodeInfos[0] = "<b>" + messages.getString("schichten_lokaler_weg_msg6") + "</b> " + ni.getIp() + "<br />";
		nodeInfos[1] = "<b>" + messages.getString("schichten_lokaler_weg_msg7") + "</b> " + ni.getSubnetzMaske()
				+ "<br />";
		nodeInfos[2] = "<b>" + messages.getString("schichten_lokaler_weg_msg8") + "</b> " + ni.getMac() + "<br />";
		nodeInfos[3] = "<b>" + messages.getString("schichten_lokaler_weg_msg9") + "</b> " + ni.getGateway() + "<br />";
		nodeInfos[4] = "<b>" + messages.getString("schichten_lokaler_weg_msg10") + "</b> " + ni.getDns();
		String nodeInfo = "";
		for (int i = 0; i < nodeInfos.length; i++) {
			nodeInfo = nodeInfo + nodeInfos[i];
		}
		return nodeInfo;
	}

	/**
	 * This method creates the visualisation of the rectangular nested messages.
	 * 
	 * @param xCoord                    x coordinate of the position in the
	 *                                  GridBagLayout
	 * @param isSenderSideWhenForwarded Indicates if it is the "receiver" (false) or
	 *                                  the "sender" (true) side of the
	 *                                  visualisation. Has no effect if the packet
	 *                                  was just sent or received.
	 */
	private void createRectangularNestedMessages(int xCoord, Boolean isSenderSideWhenForwarded) {
		JLabel label;
		boolean activeBorder;
		boolean arrowPointsDown;
		int layerOfActiveBorder = Math.min(LOCAL_PATH_PANEL.getMaxLayerInsideFrame(),
				LOCAL_PATH_PANEL.getMaxLayerOfOperation());

		for (int layerNumber = 4; layerNumber >= 1; layerNumber--) {
			// visualisation if the packet was sent or received
			if (!LOCAL_PATH_PANEL.isForwarded()) {
				if (LOCAL_PATH_PANEL.isSent()) {
					// if the packet was sent, the first message (depending on which layers are
					// involved) gets an active (red) border
					activeBorder = (layerNumber == layerOfActiveBorder) ? true : false;
					arrowPointsDown = true;
				} else {
					// active (red) border will not be here (but around the square message for the
					// "received" visualisation)
					activeBorder = false;
					arrowPointsDown = false;
				}
			}
			// visualisation if the packet was forwarded
			else {
				// "sender" side
				if (isSenderSideWhenForwarded) {
					// if the packet was sent, create an active (red) border for the first message
					// (depending on which layers are involved)
					activeBorder = LOCAL_PATH_PANEL.isSent() ? ((layerNumber == layerOfActiveBorder) ? true : false) : false;
					arrowPointsDown = true;
				}
				// "receiver" side
				else {
					activeBorder = false;
					arrowPointsDown = false;
				}
			}
			label = new NestedMessagesVisualisation(layerNumber, RECTANGLE_WIDTH, RECTANGLE_RATIO, activeBorder);
			setFinishedLabel(CENTER_PANEL, label, xCoord, 5 + 2 * (4 - layerNumber), 1, 1);
			// create vertical arrows below every nested messages rectangle
			int maxLayerOfOperation = ((InternetKnoten) LOCAL_PATH_PANEL.getKnoten()).getMaxLayerOfOperation();
			if (layerNumber <= Math.min(LOCAL_PATH_PANEL.getMaxLayerInsideFrame(), maxLayerOfOperation) && layerNumber >= 2) {
				label = new Arrow(arrowPointsDown, null, RECTANGLE_WIDTH, GAP_BETWEEN_CELLS);
				setFinishedLabel(CENTER_PANEL, label, xCoord, 6 + 2 * (4 - layerNumber), 1, 1);
			}
		}
	}

	/**
	 * Right and left side of the visualisation, indicating the layers and showing
	 * the buttons "vorwärts" and "zurück".
	 * 
	 * @param backOrForthButton "back" or "forth" button
	 * @param xCoord            x coordinate inside the GridBag layout
	 */
	private void addSidePanel(JButton backOrForthButton, int xCoord) {
		JLabel label;

		setStandards(backOrForthButton, xCoord + 0, 3, 1, 1);
		CENTER_PANEL.add(backOrForthButton, c);

		String[] layers = { messages.getString("rp_lauscher_msg11"), messages.getString("rp_lauscher_msg10"),
				messages.getString("rp_lauscher_msg9"), messages.getString("rp_lauscher_msg8") };
		for (int layerNumber = 4; layerNumber >= 1; layerNumber--) {
			label = setEmptyCell(CENTER_PANEL, xCoord + 0, 4 + 2 * (4 - layerNumber), 1, 1);
			setSize(label, RECTANGLE_WIDTH, GAP_BETWEEN_CELLS);
			label = setLabel(CENTER_PANEL, layers[4 - layerNumber], xCoord + 0, 5 + 2 * (4 - layerNumber), 1, 1, false);
			setSize(label, RECTANGLE_WIDTH, RECTANGLE_HEIGHT);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setBackground(LOCAL_PATH_PANEL.getLayerColorVar("backgroundColor", layerNumber));
			label.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		}
	}

	/**
	 * Helper method to create a new JLabel in the GridBagLayout. JLabels are needed
	 * for one line content.
	 * 
	 * @param container
	 * @param text
	 * @param x_pos
	 * @param y_pos
	 * @param width
	 * @param height
	 * @param needsBorder
	 * @return The JLabel
	 */
	private JLabel setLabel(JPanel container, String text, int x_pos, int y_pos, int width, int height,
			boolean needsBorder) {
		JLabel label = new JLabel(text);

		setStandards(label, x_pos, y_pos, width, height);
		label.setOpaque(true);

		if (needsBorder) {
			Border lineBorder = BorderFactory.createLineBorder(Color.BLACK);
			Border marginBorder = new EmptyBorder(5, 5, 5, 5);
			label.setBorder(new CompoundBorder(lineBorder, marginBorder));
		}
		container.add(label, c);

		return label;
	}

	/**
	 * Helper method to create a new JEditorPane in the GridBagLayout. JEditorPanes
	 * are needed for multiline content.
	 * 
	 * @param container
	 * @param text
	 * @param x_pos
	 * @param y_pos
	 * @param width
	 * @param height
	 * @param needsBorder
	 * @return The JEditorPane
	 */
	private JEditorPane setEditorPane(JPanel container, String text, int x_pos, int y_pos, int width, int height,
			boolean needsBorder) {
		JEditorPane editorPane = new JEditorPane("text/html", text);

		setStandards(editorPane, x_pos, y_pos, width, height);
		editorPane.setOpaque(true);
		editorPane.setEditable(false);
		editorPane.setFocusable(false);

		// set font like other labels or textAreas
		editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		editorPane.setFont(new Font("Dialog", Font.PLAIN, 12));

		if (needsBorder) {
			Border lineBorder = BorderFactory.createLineBorder(Color.BLACK);
			Border marginBorder = new EmptyBorder(5, 5, 5, 5);
			editorPane.setBorder(new CompoundBorder(lineBorder, marginBorder));
		}
		container.add(editorPane, c);

		return editorPane;
	}

	/**
	 * Helper method to include a JLabel into the GridBagLayout.
	 * 
	 * @param container
	 * @param label
	 * @param x_pos
	 * @param y_pos
	 * @param width
	 * @param height
	 */
	private void setFinishedLabel(JPanel container, JLabel label, int x_pos, int y_pos, int width, int height) {
		setStandards(label, x_pos, y_pos, width, height);
		label.setOpaque(false);

		container.add(label, c);
	}

	/**
	 * Helper method to create a visually empty cell, which is a JLabel, into the
	 * GridBagLayout. Empty cells form the gaps between the rectangles for the
	 * nested messages.
	 * 
	 * @param container
	 * @param x_pos
	 * @param y_pos
	 * @param width
	 * @param height
	 * @return The JLabel
	 */
	private JLabel setEmptyCell(JPanel container, int x_pos, int y_pos, int width, int height) {
		JLabel label = new JLabel();

		setStandards(label, x_pos, y_pos, width, height);
		label.setOpaque(false);

		label.setBorder(javax.swing.BorderFactory.createEmptyBorder());
		container.add(label, c);

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
	 */
	private void setStandards(Component component, int x_pos, int y_pos, int width, int height) {
		c.gridx = x_pos;
		c.gridy = y_pos;
		c.gridwidth = width;
		c.gridheight = height;
	}

	/**
	 * Helper method to set the preferrend, minimum and maximum size of a component.
	 * 
	 * @param component
	 * @param width
	 * @param height
	 */
	private void setSize(Component component, int width, int height) {
		Dimension dimensionOfComponent = new Dimension(width, height);
		component.setPreferredSize(dimensionOfComponent);
		component.setMinimumSize(dimensionOfComponent);
		component.setMaximumSize(dimensionOfComponent);
	}

	/**
	 * Functionality of the two buttons to go back ("zurück") and forth (vorwärts")
	 * in time.
	 */
	private void backAndForth() {
		FORTH_IN_TIME.addActionListener(new ActionListener() {
			// forth in time
			@Override
			public void actionPerformed(ActionEvent e) {
				if (pointInTime < maxPointInTime) {
					pointInTime++;
					updateButtonsIfForth();
					hideOrShowLayersIfForth();
					updateActiveBorder(true);
				}
			}
		});

		BACK_IN_TIME.addActionListener(new ActionListener() {
			// back in time
			@Override
			public void actionPerformed(ActionEvent e) {
				if (pointInTime > minPointInTime) {
					pointInTime--;
					updateButtonsIfBack();
					hideOrShowLayersIfBack();
					updateActiveBorder(false);
				}
			}
		});
	}

	/**
	 * Updates Buttons "vorwärts" and "zurück" if button "vorwärts" was pressed
	 */
	private void updateButtonsIfForth() {
		BACK_IN_TIME.setEnabled(true);
		if (pointInTime == maxPointInTime) {
			FORTH_IN_TIME.setEnabled(false);
		}
		updateSenderSide();
		LOCAL_PATH_PANEL.updateMessageDetails(senderSideIsActive);
	}

	/**
	 * Updates Buttons "vorwärts" and "zurück" if button "zurück" was pressed
	 */
	private void updateButtonsIfBack() {
		FORTH_IN_TIME.setEnabled(true);
		if (pointInTime == minPointInTime) {
			BACK_IN_TIME.setEnabled(false);
		}
		updateSenderSide();
		LOCAL_PATH_PANEL.updateMessageDetails(senderSideIsActive);
	}

	/**
	 * Hide or show layers in message details if button "vorwärts" was pressed
	 */
	private void hideOrShowLayersIfForth() {
		// if not forwarded (means hosts or routers which have sent or received the
		// packet)
		if (!LOCAL_PATH_PANEL.isForwarded()) {
			if (LOCAL_PATH_PANEL.isSent() && pointInTime < maxPointInTime) {
				LOCAL_PATH_PANEL.showNextLowerLayer();
			} else if (!LOCAL_PATH_PANEL.isSent() && pointInTime > 1) {
				LOCAL_PATH_PANEL.hideNextHigherLayer();
			}
			// if forwarded (for routers)
		} else {
			switch (pointInTime) {
			case 2:
				LOCAL_PATH_PANEL.hideNextHigherLayer();
				break;
			case 4:
				LOCAL_PATH_PANEL.showNextLowerLayer();
				break;
			}
		}
	}

	/**
	 * Hide or show layers in message details if button "zurück" was pressed
	 */
	private void hideOrShowLayersIfBack() {
		// if not forwarded (means hosts or routers which have sent or received the
		// packet)
		if (!LOCAL_PATH_PANEL.isForwarded()) {
			if (LOCAL_PATH_PANEL.isSent() && pointInTime < maxPointInTime - 1) {
				LOCAL_PATH_PANEL.hideNextHigherLayer();
			} else if (!LOCAL_PATH_PANEL.isSent() && pointInTime > 0) {
				LOCAL_PATH_PANEL.showNextLowerLayer();
			}
		} else {
			switch (pointInTime) {
			case 3:
				LOCAL_PATH_PANEL.hideNextHigherLayer();
				break;
			case 1:
				LOCAL_PATH_PANEL.showNextLowerLayer();
				break;
			}
		}
	}

	/**
	 * Updates the variable {@code senderSideIsActive}. Only has an effect if the
	 * node is a router (of home router), since they can forward IP packets. Thus
	 * they have a different frame (containing the packet) on the sender and on the
	 * receiver side.
	 */
	private void updateSenderSide() {
		senderSideIsActive = (pointInTime >= pointInTimeForSenderSide);
	}

	/**
	 * Updates the active (red) border according to {@code pointInTime} and resets
	 * the old one to black.
	 * 
	 * @param isForth true if the "forth" button was pressed, false if the "back"
	 *                button was pressed
	 */
	private void updateActiveBorder(boolean isForth) {
		int lastPointInTime = isForth ? pointInTime - 1 : pointInTime + 1;

		// list with all the coordinates of the components with a possible active border
		List<int[]> gridBagCoordsWithBorders;
		if (!LOCAL_PATH_PANEL.isForwarded()) {
			if (LOCAL_PATH_PANEL.isSent()) {
				gridBagCoordsWithBorders = Collections.unmodifiableList(Arrays.asList(new int[] { X_OFFSET + 0, 5 },
						new int[] { X_OFFSET + 0, 7 }, new int[] { X_OFFSET + 0, 9 }, new int[] { X_OFFSET + 0, 11 },
						new int[] { X_OFFSET + 2, 11 }));
			} else {
				gridBagCoordsWithBorders = Collections.unmodifiableList(Arrays.asList(new int[] { X_OFFSET + 1, 11 },
						new int[] { X_OFFSET + 3, 11 }, new int[] { X_OFFSET + 3, 9 }, new int[] { X_OFFSET + 3, 7 },
						new int[] { X_OFFSET + 3, 5 }));
			}
		} else {
			gridBagCoordsWithBorders = Collections.unmodifiableList(Arrays.asList(new int[] { X_OFFSET + 1, 11 },
					new int[] { X_OFFSET + 3, 11 }, new int[] { X_OFFSET + 3, 9 }, new int[] { X_OFFSET + 5, 9 },
					new int[] { X_OFFSET + 5, 11 }, new int[] { X_OFFSET + 7, 11 }));
		}

		// active border --> black, next active border --> red
		// go through all components
		for (Component component : CENTER_PANEL.getComponents()) {
			int gridx = GB_LAYOUT_CENTER_PANEL.getConstraints(component).gridx;
			int gridy = GB_LAYOUT_CENTER_PANEL.getConstraints(component).gridy;

			// compare coordinates with list
			if (gridx == gridBagCoordsWithBorders.get(pointInTime)[0]
					&& gridy == gridBagCoordsWithBorders.get(pointInTime)[1]) {
				// make current active border red
				try {
					((NestedMessagesVisualisation) component).createAndSetBorder(true);
				} catch (Exception e) {
					System.out.println("component is not an instance of NestedMessagesVisualisation");
				}
			} else if (gridx == gridBagCoordsWithBorders.get(lastPointInTime)[0]
					&& gridy == gridBagCoordsWithBorders.get(lastPointInTime)[1]) {
				// make previous active border black
				try {
					((NestedMessagesVisualisation) component).createAndSetBorder(false);
				} catch (Exception e) {
					System.out.println("component is not an instance of NestedMessagesVisualisation");
				}
			}
		}
	}

	/**
	 * Class for a rectangular visualisation of nested messages.
	 */
	private class NestedMessagesVisualisation extends JLabel {

		private int layerNumber;
		private boolean isSquare;
		private int widthWithBorder;
		private int heightWithBorder;
		private int widthInsideBorder;
		private int heightInsideBorder;

		private NestedMessagesVisualisation(int layerNumber, int widthWithBorder, int ratio, boolean activeBorder) {
			this.layerNumber = layerNumber;
			this.isSquare = (ratio == 1);
			this.widthWithBorder = widthWithBorder;
			this.heightWithBorder = widthWithBorder / ratio;
			this.widthInsideBorder = widthWithBorder - 2 * MAIN_BORDER_WIDTH;
			this.heightInsideBorder = heightWithBorder - 2 * MAIN_BORDER_WIDTH;

			// create border only if layer message is actually painted
			if (layerNumber <= LOCAL_PATH_PANEL.getMaxLayerInsideFrame()
					&& layerNumber <= LOCAL_PATH_PANEL.getMaxLayerOfOperation()) {
				createAndSetBorder(activeBorder);
			}
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(widthWithBorder, heightWithBorder);
		}

		@Override
		public void paintComponent(Graphics g) {
			// Let UI Delegate paint first, which includes background filling since this
			// component is opaque.
			super.paintComponent(g);

			// origin with respect to border width
			int xPos = MAIN_BORDER_WIDTH;
			int yPos = MAIN_BORDER_WIDTH;
			;
			int width = this.widthInsideBorder;
			int height = this.heightInsideBorder;

			// set basic layer rectangle
			FilledRectangle rect = new FilledRectangle();
			rect.setX(xPos);
			rect.setY(yPos);

			// Only draw nested messages if a message of the layer is in the frame.
			// Otherwise the rectangle stays insivisble.
			if (layerNumber <= LOCAL_PATH_PANEL.getMaxLayerInsideFrame()
					&& layerNumber <= LOCAL_PATH_PANEL.getMaxLayerOfOperation()) {
				rect.setFillColor(LOCAL_PATH_PANEL.getLayerColorVar("backgroundColor", layerNumber));
				rect.paintFilledRectangle(g, width, height);
				if (layerNumber < LOCAL_PATH_PANEL.getMaxLayerInsideFrame()) {
					// set other layer rectangles
					for (int i = layerNumber + 1; i <= LOCAL_PATH_PANEL.getMaxLayerInsideFrame(); i++) {
						if (!isSquare) {
							width = width - INDENT_NESTED_MSGS - BORDER_NESTED_MSGS;
							height = height - 2 * BORDER_NESTED_MSGS;
							xPos = xPos + INDENT_NESTED_MSGS;
							yPos = yPos + BORDER_NESTED_MSGS;
						} else {
							width = width - 2 * BORDER_NESTED_MSGS;
							height = height - 2 * BORDER_NESTED_MSGS;
							xPos = xPos + BORDER_NESTED_MSGS;
							yPos = yPos + BORDER_NESTED_MSGS;
						}
						rect.setX(xPos);
						rect.setY(yPos);
						rect.setFillColor(LOCAL_PATH_PANEL.getLayerColorVar("backgroundColor", i));

						rect.paintFilledRectangle(g, width, height);
					}
				}
			} else {
				setOpaque(true);
				setBackground(LOCAL_PATH_PANEL.getLayerColorVar("backgroundColorUltraFine", layerNumber));
			}
		}

		private void createAndSetBorder(boolean activeBorder) {
			// draw the (main or active) border around the message
			Border matteBorder;
			if (activeBorder) {
//				matteBorder = BorderFactory.createMatteBorder(ACTIVE_BORDER_WIDTH, ACTIVE_BORDER_WIDTH,
//						ACTIVE_BORDER_WIDTH, ACTIVE_BORDER_WIDTH, Color.RED);
				matteBorder = BorderFactory.createLoweredBevelBorder();
			} else {
//				matteBorder = BorderFactory.createMatteBorder(MAIN_BORDER_WIDTH, MAIN_BORDER_WIDTH, MAIN_BORDER_WIDTH,
//						MAIN_BORDER_WIDTH, Color.BLACK);
				matteBorder = BorderFactory.createRaisedBevelBorder();
				
			}
			setBorder(matteBorder);
		}
	}

	/**
	 * Filled rectangle for painting.
	 */
	private class FilledRectangle {

		private int xPos;
		private int yPos;
		private Color fillColor;

		public void setX(int xPos) {
			this.xPos = xPos;
		}

		public void setY(int yPos) {
			this.yPos = yPos;
		}

		public void setFillColor(Color fillColor) {
			this.fillColor = fillColor;
		}

		public void paintFilledRectangle(Graphics g, int width, int height) {
			g.setColor(fillColor);
			g.fillRect(xPos, yPos, width, height);
		}
	}

	/**
	 * Class for the arrows between the visualisations of nested messages.
	 */
	private class Arrow extends JLabel {

		protected int rectangleWidth;
		protected int rectangleHeight;

		private Boolean pointsDown; // if false points up, if null points left or right
		private Boolean pointsRight; // if false points left, if null points down or up

		private Arrow(Boolean pointsDown, Boolean pointsRight, int rectangleWidth, int rectangleHeight) {
			this.pointsDown = pointsDown;
			this.pointsRight = pointsRight;
			this.rectangleWidth = rectangleWidth;
			this.rectangleHeight = rectangleHeight;
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(rectangleWidth, rectangleHeight);
		}

		@Override
		public void paintComponent(Graphics g) {
			// Let UI Delegate paint first, which includes background filling since this
			// component is opaque.
			super.paintComponent(g);
			g.setColor(Color.BLACK);

			int xMiddle = rectangleWidth / 2;
			int yMiddle = rectangleHeight / 2;
			int overallLength = (pointsDown != null && (pointsDown || !pointsDown)) ? rectangleHeight : rectangleWidth;
			int arrowRectangleWidth = 4; // must be divisible by 2
			int headHeight = 6;
			int halfOfHeadWidth = headHeight;

			if (pointsDown != null && pointsDown) {
				// rectangle
				g.fillRect(xMiddle - arrowRectangleWidth / 2, 0, arrowRectangleWidth, overallLength - headHeight);
				// head
				int x1 = xMiddle - halfOfHeadWidth;
				int x2 = xMiddle + halfOfHeadWidth;
				int x3 = xMiddle;
				int y1 = overallLength - headHeight;
				int y2 = overallLength - headHeight;
				int y3 = overallLength;
				g.fillPolygon(new int[] { x1, x2, x3 }, new int[] { y1, y2, y3 }, 3);
			} else if (pointsDown != null && !pointsDown) {
				int correction = 1;
				// up arrow
				g.fillRect(xMiddle - arrowRectangleWidth / 2, headHeight - 1, arrowRectangleWidth,
						overallLength - headHeight + correction);
				// head
				int x1 = xMiddle - halfOfHeadWidth;
				int x2 = xMiddle + halfOfHeadWidth;
				int x3 = xMiddle;
				int y1 = headHeight - correction;
				int y2 = headHeight - correction;
				int y3 = -1;
				g.fillPolygon(new int[] { x1, x2, x3 }, new int[] { y1, y2, y3 }, 3);
			} else if (pointsRight != null && pointsRight) {
				// right arrows are needed on the internet layer
				setOpaque(true);
				setBackground(LOCAL_PATH_PANEL.getLayerColorVar("backgroundColorUltraFine", 2));
				// right arrow
				g.fillRect(0, yMiddle - arrowRectangleWidth / 2, overallLength - headHeight, arrowRectangleWidth);
				// head
				int x1 = overallLength - headHeight;
				int x2 = overallLength;
				int x3 = overallLength - headHeight;
				int y1 = yMiddle - halfOfHeadWidth;
				int y2 = yMiddle;
				int y3 = yMiddle + halfOfHeadWidth;
				g.fillPolygon(new int[] { x1, x2, x3 }, new int[] { y1, y2, y3 }, 3);
			} else if (pointsRight != null && !pointsRight) {
				// left arrow
				g.fillRect(headHeight, yMiddle - arrowRectangleWidth / 2, overallLength - headHeight,
						arrowRectangleWidth);
				// head
				int x1 = headHeight;
				int x2 = 0;
				int x3 = headHeight;
				int y1 = yMiddle - halfOfHeadWidth;
				int y2 = yMiddle;
				int y3 = yMiddle + halfOfHeadWidth;
				g.fillPolygon(new int[] { x1, x2, x3 }, new int[] { y1, y2, y3 }, 3);
			}
		}
	}

	/**
	 * Visualisation of a grey wire (link layer) and an arrow
	 */
	private class WireWithArrow extends Arrow {

		private WireWithArrow(Boolean pointsDown, Boolean pointsRight, int rectangleWidth, int rectangleHeight) {
			super(pointsDown, pointsRight, rectangleWidth, rectangleHeight);
		}

		@Override
		public void paintComponent(Graphics g) {
			setOpaque(true);
			setBackground(LOCAL_PATH_PANEL.getLayerColorVar("backgroundColorUltraFine", 1));
			// Let UI Delegate paint first, which includes background filling since this
			// component is opaque.
			super.paintComponent(g);
			g.setColor(Color.BLACK);

			// wire (ground)
			g.setColor(LOCAL_PATH_PANEL.getLayerColorVar("backgroundColor", 1));
			g.fillRect(0, this.rectangleHeight - 3 * MAIN_BORDER_WIDTH, this.rectangleWidth, 3 * MAIN_BORDER_WIDTH);
		}
	}
}