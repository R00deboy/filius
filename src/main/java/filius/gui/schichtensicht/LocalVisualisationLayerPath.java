package filius.gui.schichtensicht;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import filius.rahmenprogramm.I18n;

public class LocalVisualisationLayerPath extends VisualisationLayerPath implements I18n {
	
	// visualisation is on the sender side (otherwise receiver side)
	private boolean senderSideIsActive;
	// for this and all greater points in time visualisation is on the sender side
	// (for smaller on receiver side)
	private int pointInTimeForSenderSide;
	
	public LocalVisualisationLayerPath(PanelLayerPath panelLayerPath) {
		super(panelLayerPath);
		
		// deactivate back button if the frame was received
		if (!LAYER_PATH_PANEL.isSent()) {
			BACK_IN_TIME.setEnabled(false);
		}

		// set initial value for senderSideIsActive
		senderSideIsActive = LAYER_PATH_PANEL.isSent();

		// if the packet was not forwarded (i.e. sent or received)
		if (!LAYER_PATH_PANEL.isForwarded()) {
			// configure left and right side (indication of layers and back and forth
			// buttons)
			addSidePanel(BACK_IN_TIME, 0); // left side
			addSidePanel(FORTH_IN_TIME, 7); // right side
			if (LAYER_PATH_PANEL.isSent()) {
				minPointInTime = 4 - LAYER_PATH_PANEL.getMaxLayerInsideFrame();
				maxPointInTime = 4;
				pointInTimeForSenderSide = -1; // should never be reached, since only sent
			} else {
				minPointInTime = 0;
				maxPointInTime = Math.min(LAYER_PATH_PANEL.getMaxLayerInsideFrame(),
						LAYER_PATH_PANEL.getMaxLayerOfOperation());
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
			if (LAYER_PATH_PANEL.isSent()) {
				pointInTime = minPointInTime + 3;
				LAYER_PATH_PANEL.hideNextHigherLayer();
			} else {
				pointInTime = minPointInTime;
			}
			pointInTimeForSenderSide = minPointInTime + 3;
			visualisationForwarded();
		}
	}

//	@Override
//	public Dimension getPreferredSize() {
//		return new Dimension(480, 300);
//	}
//
//	@Override
//	public Dimension getMinimumSize() {
//		return new Dimension(480, 300);
//	}
//
//	@Override
//	public Dimension getMaximumSize() {
//		return new Dimension(480, 300);
//	}

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

		if (LAYER_PATH_PANEL.isSent()) {
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
		label = setLabel(CENTER_PANEL, LAYER_PATH_PANEL.getKnoten().holeAnzeigeName(), xCoord0, 1, 1, 1, true);
		setSize(label, RECTANGLE_WIDTH, NODE_NAME_CELL_HEIGHT);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label = setEmptyCell(CENTER_PANEL, xCoord0, 2, 1, 1);
		setSize(label, RECTANGLE_WIDTH, GAP_BETWEEN_CELLS);

		// node info cell with empty cell below
		String nodeInfo = createNodeInfoByMac(LAYER_PATH_PANEL.getInterfaceId());
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

		// create square nested messages and if packet was received, create active
		// border
		activeBorder = !LAYER_PATH_PANEL.isSent();
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
		// active border
		label = new NestedMessagesVisualisation(1, SQUARE_SIDE_LENGTH, 1, !LAYER_PATH_PANEL.isSent());
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
		label = setLabel(CENTER_PANEL, LAYER_PATH_PANEL.getKnoten().holeAnzeigeName(), X_OFFSET + 3, 1, 3, 1, true);
		setSize(label, 2 * RECTANGLE_WIDTH + GAP_BETWEEN_CELLS, NODE_NAME_CELL_HEIGHT);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label = setEmptyCell(CENTER_PANEL, X_OFFSET + 3, 2, 3, 1);
		setSize(label, RECTANGLE_WIDTH, GAP_BETWEEN_CELLS);

		// left and right node info cell with empty cell below
		String nodeInfoLeft;
		String nodeInfoRight;
		if (LAYER_PATH_PANEL.isSent()) {
			nodeInfoLeft = createNodeInfoByMac(LAYER_PATH_PANEL.getCorrInterfaceId());
			nodeInfoRight = createNodeInfoByMac(LAYER_PATH_PANEL.getInterfaceId());
		} else {
			nodeInfoLeft = createNodeInfoByMac(LAYER_PATH_PANEL.getInterfaceId());
			nodeInfoRight = createNodeInfoByMac(LAYER_PATH_PANEL.getCorrInterfaceId());
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
		label.setBackground(LAYER_PATH_PANEL.getLayerColorVar("backgroundColorUltraFine", 4));
		label = setLabel(CENTER_PANEL, "", X_OFFSET + 4, 7, 1, 1, false);
		setSize(label, GAP_BETWEEN_CELLS, RECTANGLE_HEIGHT);
		label.setBackground(LAYER_PATH_PANEL.getLayerColorVar("backgroundColorUltraFine", 3));
		label = setLabel(CENTER_PANEL, "", X_OFFSET + 4, 11, 1, 1, false);
		setSize(label, GAP_BETWEEN_CELLS, RECTANGLE_HEIGHT);
		label.setBackground(LAYER_PATH_PANEL.getLayerColorVar("backgroundColorUltraFine", 1));

		add(CENTER_PANEL, BorderLayout.CENTER);
	}

	@Override
	protected void updateButtonsIfForth() {
		BACK_IN_TIME.setEnabled(true);
		if (pointInTime == maxPointInTime) {
			FORTH_IN_TIME.setEnabled(false);
		}
		updateSenderSide();
		LAYER_PATH_PANEL.updateMessageDetails(senderSideIsActive);
	}

	@Override
	protected void updateButtonsIfBack() {
		FORTH_IN_TIME.setEnabled(true);
		if (pointInTime == minPointInTime) {
			BACK_IN_TIME.setEnabled(false);
		}
		updateSenderSide();
		LAYER_PATH_PANEL.updateMessageDetails(senderSideIsActive);
	}

	@Override
	protected void hideOrShowLayersIfForth() {
		// if not forwarded (means hosts or routers which have sent or received the
		// packet)
		if (!LAYER_PATH_PANEL.isForwarded()) {
			if (LAYER_PATH_PANEL.isSent() && pointInTime < maxPointInTime) {
				LAYER_PATH_PANEL.showNextLowerLayer();
			} else if (!LAYER_PATH_PANEL.isSent() && pointInTime > 1) {
				LAYER_PATH_PANEL.hideNextHigherLayer();
			}
			// if forwarded (for routers)
		} else {
			switch (pointInTime) {
			case 2:
				LAYER_PATH_PANEL.hideNextHigherLayer();
				break;
			case 4:
				LAYER_PATH_PANEL.showNextLowerLayer();
				break;
			}
		}
	}

	@Override
	protected void hideOrShowLayersIfBack() {
		// if not forwarded (means hosts or routers which have sent or received the
		// packet)
		if (!LAYER_PATH_PANEL.isForwarded()) {
			if (LAYER_PATH_PANEL.isSent() && pointInTime < maxPointInTime - 1) {
				LAYER_PATH_PANEL.hideNextHigherLayer();
			} else if (!LAYER_PATH_PANEL.isSent() && pointInTime > 0) {
				LAYER_PATH_PANEL.showNextLowerLayer();
			}
		} else {
			switch (pointInTime) {
			case 3:
				LAYER_PATH_PANEL.hideNextHigherLayer();
				break;
			case 1:
				LAYER_PATH_PANEL.showNextLowerLayer();
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

	@Override
	protected void updateActiveBorder(boolean isForth) {
		int lastPointInTime = isForth ? pointInTime - 1 : pointInTime + 1;

		// list with all the coordinates of the components with a possible active border
		List<int[]> gridBagCoordsWithBorders;
		if (!LAYER_PATH_PANEL.isForwarded()) {
			if (LAYER_PATH_PANEL.isSent()) {
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
}