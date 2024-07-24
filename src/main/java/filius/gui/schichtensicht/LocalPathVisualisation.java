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
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import filius.gui.nachrichtensicht.AggregatedMessageTable;
import filius.gui.nachrichtensicht.AggregatedMessageTable.LayerColorVar;
import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.InternetKnoten;
import filius.rahmenprogramm.I18n;

public class LocalPathVisualisation extends JPanel implements I18n {
	private final LocalPathPanel LOCAL_PATH_PANEL;

	private final JPanel CENTER_PANEL = new JPanel();
	private final GridBagLayout GB_LAYOUT_CENTER_PANEL = new GridBagLayout();
	private GridBagConstraints c;

	// maximum layer involved
	// (1 = link, 2 = internet, 3 = transport, 4 = application)
	private final int MAX_LAYER;

	private int pointInTime;
	private int minPointInTime;
	private int maxPointInTime;

	// visualisation is on the sender side (otherwise receiver side)
	private boolean senderSideIsActive;
	// for this and all greater points in time visualisation is on the sender side
	// (for smaller on receiver side)
	private int pointInTimeForSenderSide;

	// vertical gap between GridBag cells with content
	private static final int VGAP_BETWEEN_CELLS = 12;

	// width of rectangle including border (must be divisible by 3)
	private static final int RECTANGLE_WIDTH = 180;
	// ratio of width to height of a rectangle including border
	private static final int RECTANGLE_RATIO = 3;
	// resulting height of a rectangle including border
	private static final int RECTANGLE_HEIGHT = RECTANGLE_WIDTH / RECTANGLE_RATIO;

	// instance variables for nested messages visualisation:
	// border width around nested messages
	private static final int MAIN_BORDER_WIDTH = 2;
	// left gap around a nested message
	private static final int INDENT_NESTED_MSGS = 20;
	// upper, lower and right gap around a nested message
	private static final int BORDER_NESTED_MSGS = 5;
	// side length of squares needed for visualisation of a frame on cable (layer 1)
	private static final int SQUARE_SIDE_LENGTH = RECTANGLE_HEIGHT;
	// client info cell ratio of width to height
	private static final double CLIENT_INFO_CELL_RATIO = (double) 60 / 31;

	// buttons "vorwärts" and "zurück"
	private final JButton FORTH_IN_TIME;
	private final JButton BACK_IN_TIME;

	public LocalPathVisualisation(LocalPathPanel localPathPanel) {
		super();
		LOCAL_PATH_PANEL = localPathPanel;
		MAX_LAYER = LOCAL_PATH_PANEL.getMaxLayer();

		// set BorderLayout
		BorderLayout borderLayout = new BorderLayout(10, 10);
		setLayout(borderLayout);
		setBackground(Color.WHITE);

		// set GridBagLayout for CENTER_PANEL
		CENTER_PANEL.setLayout(GB_LAYOUT_CENTER_PANEL);
		CENTER_PANEL.setBackground(Color.WHITE);
		c = new GridBagConstraints();

		// create buttons
		FORTH_IN_TIME = new JButton(messages.getString("schichten_allgemein_msg1"));
		BACK_IN_TIME = new JButton(messages.getString("schichten_allgemein_msg2"));
		BACK_IN_TIME.setEnabled(false);

		// set initial value for senderSideIsActive
		senderSideIsActive = LOCAL_PATH_PANEL.isSent();

		// configure left and right side
		sideConfiguration(BACK_IN_TIME, BorderLayout.WEST); // left side
		sideConfiguration(FORTH_IN_TIME, BorderLayout.EAST); // right side

		// if the packet was not forwarded (i.e. sent or received)
		if (!LOCAL_PATH_PANEL.isForwarded()) {
			if (LOCAL_PATH_PANEL.isSent()) {
				minPointInTime = 4 - MAX_LAYER;
				maxPointInTime = 4;
				pointInTimeForSenderSide = -1; // should never be reached, since only sent
				pointInTime = minPointInTime;
				visualisationSentOrReceived();
			} else {
				minPointInTime = 0;
				maxPointInTime = MAX_LAYER;
				pointInTimeForSenderSide = 5; // should never be reached, since only received
				pointInTime = minPointInTime;
				visualisationSentOrReceived();
			}
		}
		// if the packet was forwarded (only for routers)
		else {
			minPointInTime = 0;
			maxPointInTime = 5;
			pointInTime = LOCAL_PATH_PANEL.isSent() ? minPointInTime + 3 : minPointInTime;
			pointInTimeForSenderSide = minPointInTime + 3;
			visualisationForwarded();
		}
		backAndForth();
	}

	public int getPointInTime() {
		return this.pointInTime;
	}

	public int getMaxPointInTime() {
		return this.maxPointInTime;
	}

	public int getMinPointInTime() {
		return this.minPointInTime;
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
		System.out.println(SwingUtilities.isEventDispatchThread());
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
			xCoord0 = 0;
			xCoord1 = 1;
			xCoord2 = 2;
			xCoord3 = 3;
		} else {
			xCoord0 = 3;
			xCoord1 = 2;
			xCoord2 = 1;
			xCoord3 = 0;
		}

		// node name cell
		label = setEmptyCell(CENTER_PANEL, xCoord0, 0, 1, 1);
		setSize(label, RECTANGLE_WIDTH, VGAP_BETWEEN_CELLS);
		label = setLabel(CENTER_PANEL, LOCAL_PATH_PANEL.systemSoftware.getKnoten().holeAnzeigeName(), xCoord0, 1, 1, 1,
				true);
		setSize(label, RECTANGLE_WIDTH, (int) (0.3 * RECTANGLE_HEIGHT));
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label = setEmptyCell(CENTER_PANEL, xCoord0, 2, 1, 1);
		setSize(label, RECTANGLE_WIDTH, VGAP_BETWEEN_CELLS);

		// node info cell with empty cell below
		String clientInfo = createClientInfoByMac(LOCAL_PATH_PANEL.getInterfaceId());
		editorPane = setEditorPane(CENTER_PANEL, clientInfo, xCoord0, 3, 1, 1, true);
		setSize(editorPane, RECTANGLE_WIDTH, (int) (RECTANGLE_WIDTH / CLIENT_INFO_CELL_RATIO));
		label = setEmptyCell(CENTER_PANEL, xCoord0, 4, 1, 1);
		setSize(label, RECTANGLE_WIDTH, VGAP_BETWEEN_CELLS);

		// create visualisations of rectangular nested messages including the arrows in
		// between
		createRectangularNestedMessages(xCoord0, null);

		// create first wire
		label = new WireWithArrow();
		setFinishedLabel(CENTER_PANEL, label, xCoord1, 11, 1, 1);

		// create square nested messages and if packet was received, create active (red)
		// border
		activeBorder = !LOCAL_PATH_PANEL.isSent();
		label = new NestedMessagesVisualisation(1, SQUARE_SIDE_LENGTH, 1, activeBorder);
		setFinishedLabel(CENTER_PANEL, label, xCoord2, 11, 1, 1);

		// create second wire
		label = new WireWithArrow();
		setFinishedLabel(CENTER_PANEL, label, xCoord3, 11, 1, 1);

		add(CENTER_PANEL, BorderLayout.CENTER);
	}

	/**
	 * Visualisation of a router (Vermittlungsrechner oder Heimrouter) which
	 * forwarded the packet. Because of its symmetric form, the visualisation is
	 * created from the left and right side to the middle at the same time.
	 */
	private void visualisationForwarded() {
		System.out.println(SwingUtilities.isEventDispatchThread());
		c = new GridBagConstraints();
		JLabel label;
		JEditorPane editorPane;

		// create first wire on the left
		label = new WireWithArrow();
		setFinishedLabel(CENTER_PANEL, label, 0, 11, 1, 1);
		// create last (fourth) wire on the right
		label = new WireWithArrow();
		setFinishedLabel(CENTER_PANEL, label, 8, 11, 1, 1);

		// create left side square nested messages and if packet was received, create
		// active (red) border
		label = new NestedMessagesVisualisation(1, SQUARE_SIDE_LENGTH, 1, !LOCAL_PATH_PANEL.isSent());
		setFinishedLabel(CENTER_PANEL, label, 1, 11, 1, 1);
		// create right side square nested messages
		label = new NestedMessagesVisualisation(1, SQUARE_SIDE_LENGTH, 1, false);
		setFinishedLabel(CENTER_PANEL, label, 7, 11, 1, 1);

		// create second wire on the left
		label = new WireWithArrow();
		setFinishedLabel(CENTER_PANEL, label, 2, 11, 1, 1);
		// create second last (third) wire on the right
		label = new WireWithArrow();
		setFinishedLabel(CENTER_PANEL, label, 6, 11, 1, 1);

		// with GridBag width of 3: node name cell with empty cell above and below
		label = setEmptyCell(CENTER_PANEL, 3, 0, 3, 1);
		setSize(label, RECTANGLE_WIDTH, VGAP_BETWEEN_CELLS);
		label = setLabel(CENTER_PANEL, LOCAL_PATH_PANEL.systemSoftware.getKnoten().holeAnzeigeName(), 3, 1, 3, 1, true);
		setSize(label, 2 * RECTANGLE_WIDTH + VGAP_BETWEEN_CELLS, (int) (0.3 * RECTANGLE_HEIGHT));
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label = setEmptyCell(CENTER_PANEL, 3, 2, 3, 1);
		setSize(label, RECTANGLE_WIDTH, VGAP_BETWEEN_CELLS);

		// left and right node info cell with empty cell below
		String clientInfoLeft;
		String clientInfoRight;
		if (LOCAL_PATH_PANEL.isSent()) {
			clientInfoLeft = createClientInfoByMac(LOCAL_PATH_PANEL.getCorrInterfaceId());
			clientInfoRight = createClientInfoByMac(LOCAL_PATH_PANEL.getInterfaceId());
		} else {
			clientInfoLeft = createClientInfoByMac(LOCAL_PATH_PANEL.getInterfaceId());
			clientInfoRight = createClientInfoByMac(LOCAL_PATH_PANEL.getCorrInterfaceId());
		}
		editorPane = setEditorPane(CENTER_PANEL, clientInfoLeft, 3, 3, 1, 1, true);
		setSize(editorPane, RECTANGLE_WIDTH, (int) (RECTANGLE_WIDTH / CLIENT_INFO_CELL_RATIO));
		label = setEmptyCell(CENTER_PANEL, 3, 4, 1, 1);
		setSize(label, RECTANGLE_WIDTH, VGAP_BETWEEN_CELLS);
		editorPane = setEditorPane(CENTER_PANEL, clientInfoRight, 5, 3, 1, 1, true);
		setSize(editorPane, RECTANGLE_WIDTH, (int) (RECTANGLE_WIDTH / CLIENT_INFO_CELL_RATIO));
		label = setEmptyCell(CENTER_PANEL, 5, 4, 1, 1);
		setSize(label, RECTANGLE_WIDTH, VGAP_BETWEEN_CELLS);

		// visualisations of rectangular nested messages including the arrows
		// in between
		createRectangularNestedMessages(3, false);
		createRectangularNestedMessages(5, true);

		// set empty cells in the exact middle (x coordinate 4)
		// first the ones with even y coordinates
		for (int i = 4; i <= 10; i = i + 2) {
			label = setEmptyCell(CENTER_PANEL, 4, i, 1, 1);
			setSize(label, VGAP_BETWEEN_CELLS, VGAP_BETWEEN_CELLS);
			label.setBackground(Color.BLUE);
		}
		// then the rest (for y Coordinates 0, 1, 2 no empty cells are required because
		// they are already occupied by the labels with width 3 (see above)
		label = setEmptyCell(CENTER_PANEL, 4, 3, 1, 1);
		setSize(label, VGAP_BETWEEN_CELLS, (int) (RECTANGLE_WIDTH / CLIENT_INFO_CELL_RATIO));
		label.setBackground(Color.BLUE);
		for (int i = 5; i <= 11; i = i + 2) {
			label = setEmptyCell(CENTER_PANEL, 4, i, 1, 1);
			setSize(label, VGAP_BETWEEN_CELLS, (int) RECTANGLE_HEIGHT);
			label.setBackground(Color.BLUE);
		}

		add(CENTER_PANEL, BorderLayout.CENTER);
	}

	/**
	 * This method creates a string with all the client info belonging to a MAC
	 * address (i.e. interface ID). In the visualisation this string will be
	 * displayed inside an EditorPane.
	 * 
	 * @param interfaceId MAC address of interface
	 * @return String with the resulting client info
	 */
	private String createClientInfoByMac(String interfaceId) {
		InternetKnoten node = ((InternetKnoten) LOCAL_PATH_PANEL.systemSoftware.getKnoten());
		NetzwerkInterface ni = node.getNetzwerkInterfaceByMac(interfaceId);
		String[] clientInfos = new String[5];
		clientInfos[0] = "<b>" + messages.getString("schichten_lokaler_weg_msg6") + "</b> " + ni.getIp() + "<br />";
		clientInfos[1] = "<b>" + messages.getString("schichten_lokaler_weg_msg7") + "</b> " + ni.getSubnetzMaske()
				+ "<br />";
		clientInfos[2] = "<b>" + messages.getString("schichten_lokaler_weg_msg8") + "</b> " + ni.getMac() + "<br />";
		clientInfos[3] = "<b>" + messages.getString("schichten_lokaler_weg_msg9") + "</b> " + ni.getGateway()
				+ "<br />";
		clientInfos[4] = "<b>" + messages.getString("schichten_lokaler_weg_msg10") + "</b> " + ni.getDns();
		String clientInfo = "";
		for (int i = 0; i < clientInfos.length; i++) {
			clientInfo = clientInfo + clientInfos[i];
		}
		return clientInfo;
	}

	/**
	 * This method creates the visualisation of the rectangular nested messages.
	 * 
	 * @param xCoord                  x coordinate of the position in the
	 *                                GridBagLayout
	 * @param isSenderSideOfForwarded Indicates if it is the "receiver" (false) or
	 *                                the "sender" (true) side of the visualisation.
	 *                                Has no effect if the packet was just sent or
	 *                                received.
	 */
	private void createRectangularNestedMessages(int xCoord, Boolean isSenderSideOfForwarded) {
		JLabel label;
		boolean activeBorder;
		for (int i = 0; i < 4; i++) {
			// visualisation if the packet was sent or received
			if (!LOCAL_PATH_PANEL.isForwarded()) {
				if (LOCAL_PATH_PANEL.isSent()) {
					// if the packet was sent, the first message (depending on which layers are
					// involved) gets an active (red) border
					activeBorder = (i == 4 - MAX_LAYER) ? true : false;
				} else {
					// active (red) border will not be here (but around the square message for the
					// "received" visualisation)
					activeBorder = false;
				}
			}
			// visualisation if the packet was forwarded
			else {
				// "receiver" side
				if (!isSenderSideOfForwarded) {
					activeBorder = false;
				}
				// "sender" side
				else {
					// if the packet was sent, create an active (red) border for the first message
					// (depending on which layers are involved)
					activeBorder = LOCAL_PATH_PANEL.isSent() ? ((i == 4 - MAX_LAYER) ? true : false) : false;
				}
			}
			label = new NestedMessagesVisualisation(4 - i, RECTANGLE_WIDTH, RECTANGLE_RATIO, activeBorder);
			setFinishedLabel(CENTER_PANEL, label, xCoord, 5 + 2 * i, 1, 1);
		}

		// create vertical arrows below every nested messages rectangle
		for (int layer = 2; layer <= MAX_LAYER; layer++) {
			label = new Arrow();
			setFinishedLabel(CENTER_PANEL, label, xCoord, 10 - 2 * (layer - 2), 1, 1);
		}
	}

	/**
	 * Right and left side of the visualisation, indicating the layers and showing
	 * the buttons "vorwärts" and "zurück".
	 */
	/**
	 * Right and left side of the visualisation, indicating the layers and showing
	 * the buttons "vorwärts" and "zurück".
	 * 
	 * @param backOrForthButton "back" or "forth" button
	 * @param position          position in the BorderLayout of the
	 */
	private void sideConfiguration(JButton backOrForthButton, String borderLayoutPosition) {
		JPanel sidePanel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		setStandards(backOrForthButton, 0, 0, 1, 1);
		sidePanel.add(backOrForthButton, c);
		sidePanel.setBackground(Color.WHITE);

		JLabel label;
		String[] layers = { messages.getString("rp_lauscher_msg11"), messages.getString("rp_lauscher_msg10"),
				messages.getString("rp_lauscher_msg9"), messages.getString("rp_lauscher_msg8") };
		for (int i = 0; i < 4; i++) {
			label = setEmptyCell(sidePanel, 0, 1 + i * 2, 1, 1);
			setSize(label, RECTANGLE_WIDTH, VGAP_BETWEEN_CELLS);
			label = setLabel(sidePanel, layers[i], 0, 2 + i * 2, 1, 1, false);
			setSize(label, RECTANGLE_WIDTH, (int) RECTANGLE_HEIGHT);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			if (i == 0) {
				label.setBackground(LayerColorVar.APPLICATION.backgroundColorFine);
			} else if (i == 1) {
				label.setBackground(LayerColorVar.TRANSPORT.backgroundColorFine);
			} else if (i == 2) {
				label.setBackground(LayerColorVar.INTERNET.backgroundColorFine);
			} else if (i == 3) {
				label.setBackground(LayerColorVar.LINK.backgroundColorFine);
			}
		}
		add(sidePanel, borderLayoutPosition);
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
				gridBagCoordsWithBorders = Collections.unmodifiableList(Arrays.asList(new int[] { 0, 5 },
						new int[] { 0, 7 }, new int[] { 0, 9 }, new int[] { 0, 11 }, new int[] { 2, 11 }));
			} else {
				gridBagCoordsWithBorders = Collections.unmodifiableList(Arrays.asList(new int[] { 1, 11 },
						new int[] { 3, 11 }, new int[] { 3, 9 }, new int[] { 3, 7 }, new int[] { 3, 5 }));
			}
		} else {
			gridBagCoordsWithBorders = Collections
					.unmodifiableList(Arrays.asList(new int[] { 1, 11 }, new int[] { 3, 11 }, new int[] { 3, 9 },
							new int[] { 5, 9 }, new int[] { 5, 11 }, new int[] { 7, 11 }));
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
					((NestedMessagesVisualisation) component).setActiveBorder(true);
					((NestedMessagesVisualisation) component).repaint();
				} catch (Exception e) {
					System.out.println("component is not an instance of NestedMessagesVisualisation");
				}
			} else if (gridx == gridBagCoordsWithBorders.get(lastPointInTime)[0]
					&& gridy == gridBagCoordsWithBorders.get(lastPointInTime)[1]) {
				// make previous active border black
				try {
					((NestedMessagesVisualisation) component).setActiveBorder(false);
					((NestedMessagesVisualisation) component).repaint();
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
		private boolean activeBorder;

		private NestedMessagesVisualisation(int layerNumber, int widthWithBorder, int ratio, boolean activeBorder) {
			this.layerNumber = layerNumber;
			this.isSquare = (ratio == 1);
			this.widthWithBorder = widthWithBorder;
			this.heightWithBorder = widthWithBorder / ratio;
			this.widthInsideBorder = widthWithBorder - 2 * MAIN_BORDER_WIDTH;
			this.heightInsideBorder = heightWithBorder - 2 * MAIN_BORDER_WIDTH;
			this.activeBorder = activeBorder;
			setBackground(Color.WHITE);
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(widthWithBorder, heightWithBorder);
		}

		public void setActiveBorder(boolean activeBorder) {
			this.activeBorder = activeBorder;
		}

		@Override
		public void paintComponent(Graphics g) {
			// Let UI Delegate paint first, which includes background filling since this
			// component is opaque.
			super.paintComponent(g);

			// origin with respect to border width
			int xPos = MAIN_BORDER_WIDTH;
			int yPos = MAIN_BORDER_WIDTH;
			int width = this.widthInsideBorder;
			int height = this.heightInsideBorder;

			// set basic layer rectangle
			FilledRectangle rect = new FilledRectangle();
			rect.setX(xPos);
			rect.setY(yPos);

			// Only draw nested messages if the layer is involved. Otherwise the rectangle
			// stays white (not visible).
			if (layerNumber <= MAX_LAYER) {
				if (activeBorder) {
					// draw the active (red) border around the message
					Border lineBorder = BorderFactory.createMatteBorder(MAIN_BORDER_WIDTH, MAIN_BORDER_WIDTH,
							MAIN_BORDER_WIDTH, MAIN_BORDER_WIDTH, Color.RED);
					setBorder(lineBorder);
				} else {
					Border lineBorder = BorderFactory.createMatteBorder(MAIN_BORDER_WIDTH, MAIN_BORDER_WIDTH,
							MAIN_BORDER_WIDTH, MAIN_BORDER_WIDTH, Color.BLACK);
					setBorder(lineBorder);
				}
				setColorAccordingToLayer(layerNumber, rect);
				rect.paintFilledRectangle(g, width, height);
				if (layerNumber < MAX_LAYER) {
					// set other layer rectangles
					for (int i = layerNumber + 1; i <= MAX_LAYER; i++) {
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
						setColorAccordingToLayer(i, rect);

						rect.paintFilledRectangle(g, width, height);
					}
				}
			}
		}

		private void setColorAccordingToLayer(int layerNumber, FilledRectangle rect) {
			if (layerNumber == 1) {
				rect.setFillColor(AggregatedMessageTable.LayerColorVar.LINK.backgroundColor);
			} else if (layerNumber == 2) {
				rect.setFillColor(AggregatedMessageTable.LayerColorVar.INTERNET.backgroundColor);
			} else if (layerNumber == 3) {
				rect.setFillColor(AggregatedMessageTable.LayerColorVar.TRANSPORT.backgroundColor);
			} else if (layerNumber == 4) {
				rect.setFillColor(AggregatedMessageTable.LayerColorVar.APPLICATION.backgroundColor);
			}
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

		private Arrow() {
			setBackground(Color.WHITE);
		}
		
		@Override
		public Dimension getPreferredSize() {
			return new Dimension(RECTANGLE_WIDTH, (int) VGAP_BETWEEN_CELLS);
		}

		@Override
		public void paintComponent(Graphics g) {
			// Let UI Delegate paint first, which includes background filling since this
			// component is opaque.
			super.paintComponent(g);

			int xPos = (int) (RECTANGLE_WIDTH / 2.0);
			int length = (int) (VGAP_BETWEEN_CELLS);
			int width = 4;
			int headHeight = 6;
			g.fillRect(xPos - width / 2, 0, width, length - headHeight);
			g.fillPolygon(new int[] { xPos - headHeight, xPos + headHeight, xPos },
					new int[] { length - headHeight, length - headHeight, length }, 3);
		}
	}

	/**
	 * Visualisation of a grey wire (link layer) and an arrow
	 */
	private class WireWithArrow extends JLabel {

		private WireWithArrow() {
			setBackground(Color.WHITE);
		}

		public Dimension getPreferredSize() {
			return new Dimension(SQUARE_SIDE_LENGTH, SQUARE_SIDE_LENGTH);
		}

		@Override
		public void paintComponent(Graphics g) {
			// Let UI Delegate paint first, which includes background filling since this
			// component is opaque.
			super.paintComponent(g);

			int halfSquareSideLength = (int) Math.round(SQUARE_SIDE_LENGTH / 2.0);
			int height = (int) Math.round(SQUARE_SIDE_LENGTH / 13.0);
			int headWidth = (int) Math.round(SQUARE_SIDE_LENGTH / 9.0);

			// arrow
			g.fillRect(0, halfSquareSideLength - height / 2, SQUARE_SIDE_LENGTH - headWidth, height);
			g.fillPolygon(
					new int[] { SQUARE_SIDE_LENGTH - headWidth, SQUARE_SIDE_LENGTH - headWidth, SQUARE_SIDE_LENGTH },
					new int[] { halfSquareSideLength - headWidth, halfSquareSideLength + headWidth,
							halfSquareSideLength },
					3);

			// wire
			g.setColor(AggregatedMessageTable.LayerColorVar.LINK.backgroundColor);
			g.fillRect(0, SQUARE_SIDE_LENGTH - 3 * MAIN_BORDER_WIDTH, SQUARE_SIDE_LENGTH, 3 * MAIN_BORDER_WIDTH);
		}
	}
}