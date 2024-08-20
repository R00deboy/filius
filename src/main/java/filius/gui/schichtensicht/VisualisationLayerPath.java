package filius.gui.schichtensicht;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.MatteBorder;

import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.InternetKnoten;
import filius.rahmenprogramm.I18n;

public class VisualisationLayerPath extends JPanel implements I18n {
	protected final PanelLayerPath LAYER_PATH_PANEL;

	protected final JPanel CENTER_PANEL = new JPanel();
	protected final GridBagLayout GB_LAYOUT_CENTER_PANEL = new GridBagLayout();
	protected GridBagConstraints c = new GridBagConstraints();
	// x coordinate of the left most element in the GridBag layout after the layer
	// descrtiption and the colored gap
	protected final static int X_OFFSET = 2;

	protected int pointInTime;
	protected int minPointInTime;
	protected int maxPointInTime;

	// vertical and horizontal gap between GridBag cells with content
	// (must be divisible by 2)
	protected static final int GAP_BETWEEN_CELLS = 12;

	// width of rectangle including border (must be divisible by 2 *
	// RECTANGLE_RATIO)
	protected static final int RECTANGLE_WIDTH = 180;
	// ratio of width to height of a rectangle including border
	protected static final int RECTANGLE_RATIO = 3;
	// resulting height of a rectangle including border (must be divisible by 2)
	protected static final int RECTANGLE_HEIGHT = RECTANGLE_WIDTH / RECTANGLE_RATIO;

	// instance variables for nested messages visualisation:
	// border width of standard border around nested messages
	protected static final int MAIN_BORDER_WIDTH = 2;
	// left gap around a nested message
	protected static final int INDENT_NESTED_MSGS = 20;
	// upper, lower and right gap between neighbouring nested messages
	protected static final int BORDER_NESTED_MSGS = 5;
	// side length of squares needed for visualisation of a frame on cable (layer 1)
	protected static final int SQUARE_SIDE_LENGTH = RECTANGLE_HEIGHT;
	// node name cell height
	protected static final int NODE_NAME_CELL_HEIGHT = (int) (0.3 * RECTANGLE_HEIGHT);
	// node info cell ratio of width to height
	protected static final int NODE_INFO_CELL_HEIGHT = (int) Math.round(RECTANGLE_WIDTH / 1.935);

	// buttons "forth" and "back"
	protected final JButton FORTH_IN_TIME;
	protected final JButton BACK_IN_TIME;

	public VisualisationLayerPath(PanelLayerPath panelLayerPath) {
		super();
		LAYER_PATH_PANEL = panelLayerPath;

		// set BorderLayout
		BorderLayout borderLayout = new BorderLayout();
		setLayout(borderLayout);
		setBackground(LAYER_PATH_PANEL.getBackgroundColor());

		// set GridBagLayout for CENTER_PANEL
		CENTER_PANEL.setLayout(GB_LAYOUT_CENTER_PANEL);
		CENTER_PANEL.setBackground(LAYER_PATH_PANEL.getBackgroundColor());
		c.anchor = GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.NONE;

		// create buttons (create BufferedImages first, then resize them to an Image)
		BufferedImage arrowRightBI = null;
		BufferedImage arrowLeftBI = null;
		try {
			arrowRightBI = ImageIO.read(new File("src/main/resources/gfx/allgemein/arrow_right.png"));
			arrowLeftBI = ImageIO.read(new File("src/main/resources/gfx/allgemein/arrow_left.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Image arrowRight = arrowRightBI.getScaledInstance(RECTANGLE_WIDTH, NODE_INFO_CELL_HEIGHT, Image.SCALE_SMOOTH);
		Image arrowLeft = arrowLeftBI.getScaledInstance(RECTANGLE_WIDTH, NODE_INFO_CELL_HEIGHT, Image.SCALE_SMOOTH);
		FORTH_IN_TIME = new JButton(new ImageIcon(arrowRight));
		BACK_IN_TIME = new JButton(new ImageIcon(arrowLeft));
		FORTH_IN_TIME.setPreferredSize(new Dimension(RECTANGLE_WIDTH, NODE_INFO_CELL_HEIGHT));
		BACK_IN_TIME.setPreferredSize(new Dimension(RECTANGLE_WIDTH, NODE_INFO_CELL_HEIGHT));

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
	protected void coloredLayerIndicators(int xCoord, int width, boolean includingLayer1) {
		c = new GridBagConstraints();
		JLabel label;

		// layers 2 to 4
		for (int layerNumber = 4; layerNumber >= 2; layerNumber--) {
			label = setLabel(CENTER_PANEL, "", xCoord + 0, 5 + 2 * (4 - layerNumber), 1, 1, false);
			setSize(label, width, RECTANGLE_HEIGHT);
			label.setBackground(LAYER_PATH_PANEL.getLayerColorVar("backgroundColorUltraFine", layerNumber));
		}
		// layer 1
		if (includingLayer1) {
			label = setLabel(CENTER_PANEL, "", xCoord + 0, 11, 1, 1, false);
			setSize(label, width, RECTANGLE_HEIGHT);
			label.setBackground(LAYER_PATH_PANEL.getLayerColorVar("backgroundColorUltraFine", 1));
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
	protected String createNodeInfoByMac(String interfaceId) {
		InternetKnoten node = ((InternetKnoten) LAYER_PATH_PANEL.getKnoten());
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
	protected void createRectangularNestedMessages(int xCoord, Boolean isSenderSideWhenForwarded) {
		JLabel label;
		boolean activeBorder;
		boolean arrowPointsDown;
		int layerOfActiveBorder = Math.min(LAYER_PATH_PANEL.getMaxLayerInsideFrame(),
				LAYER_PATH_PANEL.getMaxLayerOfOperation());

		for (int layerNumber = 4; layerNumber >= 1; layerNumber--) {
			// visualisation if the packet was sent or received
			if (!LAYER_PATH_PANEL.isForwarded()) {
				if (LAYER_PATH_PANEL.isSent()) {
					// if the packet was sent, the first message (depending on which layers are
					// involved) gets an active border
					activeBorder = (layerNumber == layerOfActiveBorder) ? true : false;
					arrowPointsDown = true;
				} else {
					// active border will not be here (but around the square message for the
					// "received" visualisation)
					activeBorder = false;
					arrowPointsDown = false;
				}
			}
			// visualisation if the packet was forwarded
			else {
				// "sender" side
				if (isSenderSideWhenForwarded) {
					// if the packet was sent, create an active border for the first message
					// (depending on which layers are involved)
					activeBorder = LAYER_PATH_PANEL.isSent() ? ((layerNumber == layerOfActiveBorder) ? true : false)
							: false;
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
			int maxLayerOfOperation = ((InternetKnoten) LAYER_PATH_PANEL.getKnoten()).getMaxLayerOfOperation();
			if (layerNumber <= Math.min(LAYER_PATH_PANEL.getMaxLayerInsideFrame(), maxLayerOfOperation)
					&& layerNumber >= 2) {
				label = new Arrow(arrowPointsDown, null, RECTANGLE_WIDTH, GAP_BETWEEN_CELLS);
				setFinishedLabel(CENTER_PANEL, label, xCoord, 6 + 2 * (4 - layerNumber), 1, 1);
			}
		}
	}

	/**
	 * Right and left side of the visualisation, indicating the layers and showing
	 * the buttons "forth" and "back".
	 * 
	 * @param backOrForthButton "back" or "forth" button
	 * @param xCoord            x coordinate inside the GridBag layout
	 */
	protected void addSidePanel(JButton backOrForthButton, int xCoord) {
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
			label.setBackground(LAYER_PATH_PANEL.getLayerColorVar("backgroundColorFine", layerNumber));
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
	protected JLabel setLabel(JPanel container, String text, int x_pos, int y_pos, int width, int height,
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
	protected JEditorPane setEditorPane(JPanel container, String text, int x_pos, int y_pos, int width, int height,
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
	protected void setFinishedLabel(JPanel container, JLabel label, int x_pos, int y_pos, int width, int height) {
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
	protected JLabel setEmptyCell(JPanel container, int x_pos, int y_pos, int width, int height) {
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
	protected void setStandards(Component component, int x_pos, int y_pos, int width, int height) {
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
	protected void setSize(Component component, int width, int height) {
		Dimension dimensionOfComponent = new Dimension(width, height);
		component.setPreferredSize(dimensionOfComponent);
		component.setMinimumSize(dimensionOfComponent);
		component.setMaximumSize(dimensionOfComponent);
	}

	/**
	 * Functionality of the "back" and "forth" buttons to go back and forth in time.
	 * The buttons can be triggered by clicking or by pressing the left or right
	 * non-numpad arrow key.
	 */
	protected void backAndForth() {
		FORTH_IN_TIME.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				forthButtonPressLogic();
			}
		});

		FORTH_IN_TIME.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				forthButtonPressLogic();
			}
		});

		BACK_IN_TIME.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				backButtonPressLogic();
			}
		});

		BACK_IN_TIME.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				backButtonPressLogic();
			}
		});
	}

	/**
	 * What to do when the forth button is pressed.
	 */
	protected void forthButtonPressLogic() {
		if (pointInTime < maxPointInTime) {
			pointInTime++;
			updateButtonsIfForth();
			hideOrShowLayersIfForth();
			updateActiveBorder(true);
		}
	}

	/**
	 * What to do when the back button is pressed.
	 */
	protected void backButtonPressLogic() {
		if (pointInTime > minPointInTime) {
			pointInTime--;
			updateButtonsIfBack();
			hideOrShowLayersIfBack();
			updateActiveBorder(false);
		}
	}

	/**
	 * Updates "forth" and "back" buttons if button "forth" was pressed
	 */
	protected void updateButtonsIfForth() {
	}

	/**
	 * Updates "forth" and "back" buttons if button "back" was pressed
	 */
	protected void updateButtonsIfBack() {
	}

	/**
	 * Hide or show layers in message details if button "forth" was pressed
	 */
	protected void hideOrShowLayersIfForth() {
	}

	/**
	 * Hide or show layers in message details if button "back" was pressed
	 */
	protected void hideOrShowLayersIfBack() {
	}

	/**
	 * Updates the active border according to {@code pointInTime} and resets the old
	 * one to black.
	 * 
	 * @param isForth true if the "forth" button was pressed, false if the "back"
	 *                button was pressed
	 */
	protected void updateActiveBorder(boolean isForth) {
	}

	/**
	 * Class for a rectangular visualisation of nested messages.
	 */
	protected class NestedMessagesVisualisation extends JLabel {

		private int layerNumber;
		private boolean isSquare;
		private int widthWithBorder;
		private int heightWithBorder;
		private int widthInsideBorder;
		private int heightInsideBorder;

		protected NestedMessagesVisualisation(int layerNumber, int widthWithBorder, int ratio, boolean activeBorder) {
			this.layerNumber = layerNumber;
			this.isSquare = (ratio == 1);
			this.widthWithBorder = widthWithBorder;
			this.heightWithBorder = widthWithBorder / ratio;
			this.widthInsideBorder = widthWithBorder - 2 * MAIN_BORDER_WIDTH;
			this.heightInsideBorder = heightWithBorder - 2 * MAIN_BORDER_WIDTH;

			// create border only if layer message is actually painted
			if (layerNumber <= LAYER_PATH_PANEL.getMaxLayerInsideFrame()
					&& layerNumber <= LAYER_PATH_PANEL.getMaxLayerOfOperation()) {
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
			if (layerNumber <= LAYER_PATH_PANEL.getMaxLayerInsideFrame()
					&& layerNumber <= LAYER_PATH_PANEL.getMaxLayerOfOperation()) {
				rect.setFillColor(LAYER_PATH_PANEL.getLayerColorVar("backgroundColor", layerNumber));
				rect.paintFilledRectangle(g, width, height);
				if (layerNumber < LAYER_PATH_PANEL.getMaxLayerInsideFrame()) {
					// set other layer rectangles
					for (int i = layerNumber + 1; i <= LAYER_PATH_PANEL.getMaxLayerInsideFrame(); i++) {
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
						rect.setFillColor(LAYER_PATH_PANEL.getLayerColorVar("backgroundColor", i));

						rect.paintFilledRectangle(g, width, height);
					}
				}
			} else {
				setOpaque(true);
				setBackground(LAYER_PATH_PANEL.getLayerColorVar("backgroundColorUltraFine", layerNumber));
			}
		}

		protected void createAndSetBorder(boolean activeBorder) {
			// draw the (main or active) border around the message
			Border border;
			if (activeBorder) {
				MatteBorder outside = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.decode("#5AD86C"));
				MatteBorder inside = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.decode("#75DF83"));
				border = BorderFactory.createCompoundBorder(outside, inside);
			} else {
				border = BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK);
			}
			setBorder(border);
		}
	}

	/**
	 * Class for a filled rectangle for painting.
	 */
	protected class FilledRectangle {

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
	protected class Arrow extends JLabel {

		protected int rectangleWidth;
		protected int rectangleHeight;

		private Boolean pointsDown; // if false points up, if null points left or right
		private Boolean pointsRight; // if false points left, if null points down or up

		protected Arrow(Boolean pointsDown, Boolean pointsRight, int rectangleWidth, int rectangleHeight) {
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
				setBackground(LAYER_PATH_PANEL.getLayerColorVar("backgroundColorUltraFine", 2));
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
	protected class WireWithArrow extends Arrow {

		protected WireWithArrow(Boolean pointsDown, Boolean pointsRight, int rectangleWidth, int rectangleHeight) {
			super(pointsDown, pointsRight, rectangleWidth, rectangleHeight);
		}

		@Override
		public void paintComponent(Graphics g) {
			setOpaque(true);
			setBackground(LAYER_PATH_PANEL.getLayerColorVar("backgroundColorUltraFine", 1));
			// Let UI Delegate paint first, which includes background filling since this
			// component is opaque.
			super.paintComponent(g);
			g.setColor(Color.BLACK);

			// wire (ground)
			g.setColor(LAYER_PATH_PANEL.getLayerColorVar("backgroundColor", 1));
			g.fillRect(0, this.rectangleHeight - 3 * MAIN_BORDER_WIDTH, this.rectangleWidth, 3 * MAIN_BORDER_WIDTH);
		}
	}
}