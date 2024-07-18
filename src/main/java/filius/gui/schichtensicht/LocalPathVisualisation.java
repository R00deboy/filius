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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import filius.gui.nachrichtensicht.AggregatedMessageTable;
import filius.gui.nachrichtensicht.AggregatedMessageTable.LayerColorVar;
import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.InternetKnoten;
import filius.rahmenprogramm.I18n;
import filius.software.system.Betriebssystem;
import filius.software.system.GatewayFirmware;
import filius.software.system.SystemSoftware;
import filius.software.system.VermittlungsrechnerBetriebssystem;

public class LocalPathVisualisation extends JPanel implements I18n {
	private final LocalPathPanel LOCAL_PATH_PANEL;

	private final JPanel MAIN_PANEL = new JPanel();

	// maximum layer involved
	// (1 = link, 2 = internet, 3 = transport, 4 = application)
	private final int MAX_LAYER;

	private int pointInTime;
	private int maxPointInTime;
	private int minPointInTime;

	// visualisation is on the sender side (otherwise receiver side)
	private boolean senderSideIsActive = false;
	// for this and all greater points in time visualisation is on the sender side
	// (for smaller on receiver side)
	private int pointInTimeForSenderSide;

	// width, ,
	private static final int RECTANGLE_WIDTH = 180;
	// ratio of width to height of a standard rectangle
	private static final double RECTANGLE_RATIO = 3;
	// resulting height
	private static final double RECTANGLE_HEIGHT = RECTANGLE_WIDTH / RECTANGLE_RATIO;
	// shrink factor of visualisation of nested messages
	private static final double SHRINK_FACTOR = 0.8;
	// half of height difference after shrinking
	private static final double HEIGHT_DIFF = RECTANGLE_HEIGHT * (1 - SHRINK_FACTOR) * 0.5;
	// side length of squares needed for visualisation of a frame on cable (layer 1)
	private static final double SQUARE_SIDE_LENGTH = RECTANGLE_HEIGHT;
	// client info cell ratio of width to height
	private static final double CLIENT_INFO_CELL_RATIO = (double) 180 / 93;

	// vertical gap between cells with content
	private static final int VGAP_BETWEEN_CELLS = 12;

	// buttons "vorwärts" and "zurück"
	private final JButton FORTH_IN_TIME;
	private final JButton BACK_IN_TIME;

	// constraints for a GridBagLayout
	private GridBagConstraints c;

	public LocalPathVisualisation(LocalPathPanel localPathPanel) {
		super();
		LOCAL_PATH_PANEL = localPathPanel;
		MAX_LAYER = LOCAL_PATH_PANEL.getMaxLayer();
		SystemSoftware systemSoftware = localPathPanel.systemSoftware;
		BorderLayout borderLayout = new BorderLayout(10, 10);
		setLayout(borderLayout);
		setBackground(Color.WHITE);

		// create buttons
		FORTH_IN_TIME = new JButton(messages.getString("schichten_allgemein_msg1"));
		BACK_IN_TIME = new JButton(messages.getString("schichten_allgemein_msg2"));
		BACK_IN_TIME.setEnabled(false);

		if (localPathPanel.isSent()) {
			senderSideIsActive = true;
		}

		// configure left and right side
		sideConfiguration(BACK_IN_TIME, BorderLayout.WEST); // left side
		sideConfiguration(FORTH_IN_TIME, BorderLayout.EAST); // right side

		// if the selected node is a host (Rechner or Notebook)
		if (systemSoftware instanceof Betriebssystem) {
			if (localPathPanel.isSent()) {
				minPointInTime = 4 - MAX_LAYER;
				maxPointInTime = 4;
				pointInTimeForSenderSide = -1; // should never be reached, since only sent
			} else {
				minPointInTime = 0;
				maxPointInTime = MAX_LAYER;
				pointInTimeForSenderSide = 5; // should never be reached, since only received
			}
			pointInTime = minPointInTime;
			if (localPathPanel.isSent()) {
				visualisationSent();
			} else {
				visualisationReceived();
			}
		}

		// if the selected node is a router (Vermittlungsrechner or Heimrouter)
		else if (systemSoftware instanceof VermittlungsrechnerBetriebssystem
				|| systemSoftware instanceof GatewayFirmware) {
			if (localPathPanel.isForwarded()) {
				minPointInTime = 0;
				maxPointInTime = 5;
			} else if (localPathPanel.isSent()) {
				minPointInTime = 3;
				maxPointInTime = 4;
				visualisationSent();
			} else {
				minPointInTime = 1;
				maxPointInTime = 2;
				visualisationReceived();
			}
			pointInTime = minPointInTime;
			pointInTimeForSenderSide = 3;
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
	 * Visualisation of a node if the frame was sent
	 */
	private void visualisationSent() {
		MAIN_PANEL.setLayout(new GridBagLayout());
		MAIN_PANEL.setBackground(Color.WHITE);
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		JLabel label;
		JEditorPane editorPane;

		// client name cell
		label = setEmptyCell(MAIN_PANEL, 0, 0, 1, 1);
		setSize(label, RECTANGLE_WIDTH, VGAP_BETWEEN_CELLS);
		label = setLabel(MAIN_PANEL, LOCAL_PATH_PANEL.systemSoftware.getKnoten().holeAnzeigeName(), 0, 1, 1, 1, true);
		setSize(label, RECTANGLE_WIDTH, (int) (0.3 * RECTANGLE_HEIGHT));
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label = setEmptyCell(MAIN_PANEL, 0, 2, 1, 1);
		setSize(label, RECTANGLE_WIDTH, VGAP_BETWEEN_CELLS);

		// client info cell
		InternetKnoten node = ((InternetKnoten) LOCAL_PATH_PANEL.systemSoftware.getKnoten());
		NetzwerkInterface ni = node.getNetzwerkInterfaceByMac(LOCAL_PATH_PANEL.getInterfaceId());
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
		editorPane = setEditorPane(MAIN_PANEL, clientInfo, 0, 3, 1, 1, true);
		setSize(editorPane, RECTANGLE_WIDTH, (int) (RECTANGLE_WIDTH / CLIENT_INFO_CELL_RATIO));
		label = setEmptyCell(MAIN_PANEL, 0, 4, 1, 1);
		setSize(label, RECTANGLE_WIDTH, VGAP_BETWEEN_CELLS);

		// create visualisations of nested messages
		for (int i = 1; i < 5; i++) {
			label = new NestedMessagesVisualisation(5 - i, RECTANGLE_WIDTH, (int) (RECTANGLE_WIDTH / RECTANGLE_RATIO));
			setFinishedLabel(MAIN_PANEL, label, 0, 5 + 2 * (i - 1), 1, 1);
		}

		// create vertical arrows below the nested messages rectangles
		for (int layer = 2; layer <= MAX_LAYER; layer++) {
			label = new Arrow();
			setFinishedLabel(MAIN_PANEL, label, 0, 10 - 2 * (layer - 2), 1, 1);
		}

		// create first wire
		label = new Wire();
		setFinishedLabel(MAIN_PANEL, label, 1, 11, 1, 1);

		// create square nested messages
		label = new NestedMessagesVisualisation(1, (int) SQUARE_SIDE_LENGTH, (int) SQUARE_SIDE_LENGTH);
		setFinishedLabel(MAIN_PANEL, label, 2, 11, 1, 1);

		// create second wire
		label = new Wire();
		setFinishedLabel(MAIN_PANEL, label, 3, 11, 1, 1);

		add(MAIN_PANEL, BorderLayout.CENTER);
	}

	/**
	 * Visualisation of a node if the frame was received
	 */
	private void visualisationReceived() {
		// System.out.println("Created GUI on EDT? " +
		// SwingUtilities.isEventDispatchThread());
	}

	/**
	 * Visualisation of a router (Vermittlungsrechner oder Heimrouter) which forwarded the frame
	 */
	private void visualisationForwarded() {
		// System.out.println("Created GUI on EDT? " +
		// SwingUtilities.isEventDispatchThread());
	}

	/**
	 * Right and left side of the visualisation, indicating the layers and showing
	 * the buttons "vorwärts" and "zurück".
	 */
	private void sideConfiguration(JButton backOrForthButton, String position) {
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
		add(sidePanel, position);
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
					updateActiveBorderIfForth();
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
					updateActiveBorderIfBack();
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
	 * Hide or show layers in message details if button "vorwärts" was pressed
	 */
	private void hideOrShowLayersIfForth() {
		// if not forwarded (means hosts or routers which have sent or received the frame)
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
	 * Hide or show layers in message details if button "zurück" was pressed
	 */
	private void hideOrShowLayersIfBack() {
		// if not forwarded (means hosts or routers which have sent or received the frame)
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
		if (pointInTime >= pointInTimeForSenderSide) {
			senderSideIsActive = true;
		} else {
			senderSideIsActive = false;
		}
	}

	private void updateActiveBorderIfForth() {

	}

	private void updateActiveBorderIfBack() {

	}

	/**
	 * Class for the visualisation of nested messages.
	 */
	private class NestedMessagesVisualisation extends JLabel {

		private int layerNumber;
		private int width;
		private int height;

		private NestedMessagesVisualisation(int layerNumber, int width, int height) {
			this.layerNumber = layerNumber;
			this.width = width;
			this.height = height;
			setBackground(Color.WHITE);
//			Border lineBorder = BorderFactory.createLineBorder(Color.BLACK);
//			Border marginBorder = new EmptyBorder(5, 5, 5, 5);
//			setBorder(new CompoundBorder(lineBorder, marginBorder));
		}

		public Dimension getPreferredSize() {
			return new Dimension(width, height);
		}

		public void paintComponent(Graphics g) {
			// Let UI Delegate paint first, which includes background filling since this
			// component is opaque.
			super.paintComponent(g);

			// draw origin is at top left
			double xPos = 0;
			double yPos = 0;

			// set basic layer rectangle
			FilledRectangle rect = new FilledRectangle();
			rect.setX((int) xPos);
			rect.setY((int) yPos);

			// Only draw nested messages if the layer is involved. Otherwise the rectangle
			// stays white (not visible).
			if (layerNumber <= MAX_LAYER) {
				setColorAccordingToLayer(layerNumber, rect);
				rect.paintFilledRectangle(g, width, height);
				if (layerNumber < MAX_LAYER) {
					// set other layer rectangles
					int exp = 0;
					for (int i = layerNumber + 1; i <= MAX_LAYER; i++) {
						xPos = Math.round(
								xPos + Math.pow(SHRINK_FACTOR, exp) * (width - (width * SHRINK_FACTOR + HEIGHT_DIFF)));
						yPos = Math.round(yPos + Math.pow(SHRINK_FACTOR, exp) * HEIGHT_DIFF);

						rect.setX((int) xPos);
						rect.setY((int) yPos);
						setColorAccordingToLayer(i, rect);
						rect.paintFilledRectangle(g, (int) Math.round(Math.pow(SHRINK_FACTOR, exp + 1) * width),
								(int) Math.round(Math.pow(SHRINK_FACTOR, exp + 1) * height));
						exp++;
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

		public Dimension getPreferredSize() {
			return new Dimension(RECTANGLE_WIDTH, (int) VGAP_BETWEEN_CELLS);
		}

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
	private class Wire extends JLabel {

		private Wire() {
			setBackground(Color.WHITE);
		}

		public Dimension getPreferredSize() {
			return new Dimension((int) SQUARE_SIDE_LENGTH, (int) SQUARE_SIDE_LENGTH);
		}

		public void paintComponent(Graphics g) {
			// Let UI Delegate paint first, which includes background filling since this
			// component is opaque.
			super.paintComponent(g);

			int halfSquareSideLength = (int) (SQUARE_SIDE_LENGTH / 2);
			int height = 4;
			int headWidth = 6;

			// arrow
			g.fillRect(0, halfSquareSideLength - height / 2, (int) (SQUARE_SIDE_LENGTH - headWidth), height);
			g.fillPolygon(
					new int[] { (int) (SQUARE_SIDE_LENGTH - headWidth), (int) (SQUARE_SIDE_LENGTH - headWidth),
							(int) SQUARE_SIDE_LENGTH },
					new int[] { halfSquareSideLength - headWidth, halfSquareSideLength + headWidth,
							halfSquareSideLength },
					3);

			g.setColor(AggregatedMessageTable.LayerColorVar.LINK.backgroundColor);
			g.fillRect(0, (int) (SQUARE_SIDE_LENGTH - HEIGHT_DIFF) + 1, (int) SQUARE_SIDE_LENGTH, (int) HEIGHT_DIFF);
		}
	}
}