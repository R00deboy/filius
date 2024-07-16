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
import javax.swing.JTextArea;
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
import filius.software.system.Betriebssystem;
import filius.software.system.GatewayFirmware;
import filius.software.system.SystemSoftware;
import filius.software.system.VermittlungsrechnerBetriebssystem;

public class LocalPathVisualisation extends JPanel implements I18n {
	private LocalPathPanel localPathPanel;

	private int pointInTime;
	private int maxPointInTime;
	private int minPointInTime;

	// visualisation is on the sender side (otherwise receiver side)
	private boolean senderSideIsActive = false;
	// for this and all greater points in time visualisation is on the sender side
	// (for smaller on receiver side)
	private int pointInTimeForSenderSide;

	// width, ratio of width to height of a standard rectangle, resulting height
	private static final int RECTANGLE_WIDTH = 180;
	private static final double RECTANGLE_RATIO = 3;
	private static final double RECTANGLE_HEIGHT = RECTANGLE_WIDTH / RECTANGLE_RATIO;

	// vertical gap between cells with content
	private static final int VGAP_BETWEEN_CELLS = 12;

	// shrink factor for nested messages
	private static final double SHRINK_FACTOR = 0.85;

	// buttons "vorwärts" and "zurück"
	private final JButton FORTH_IN_TIME;
	private final JButton BACK_IN_TIME;

	// constraints for a GridBagLayout
	private GridBagConstraints c;

	public LocalPathVisualisation(LocalPathPanel localPathPanel) {
		super();
		this.localPathPanel = localPathPanel;
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

		// Falls Host (Rechner, Notebook)
		if (systemSoftware instanceof Betriebssystem) {
			minPointInTime = 4 - localPathPanel.getMaxLayer();
			maxPointInTime = 3;
			pointInTime = minPointInTime;
			if (localPathPanel.isSent()) {
				pointInTimeForSenderSide = -1; // should never be reached, since only sent
			} else {
				pointInTimeForSenderSide = 4; // should never be reached, since only received
			}
			if (localPathPanel.isSent()) {
				hostVisualisationSent();
			} else {
				hostVisualisationReceived();
			}
		}
		// Falls Router (Vermittlungsrechner, Heimrouter)
		else if (systemSoftware instanceof VermittlungsrechnerBetriebssystem
				|| systemSoftware instanceof GatewayFirmware) {
			if (localPathPanel.isForwarding()) {
				minPointInTime = 0;
				maxPointInTime = 3;
				pointInTime = minPointInTime;
			} else if (localPathPanel.isSent()) {
				minPointInTime = 2;
				maxPointInTime = 3;
				pointInTime = minPointInTime;
			} else {
				minPointInTime = 0;
				maxPointInTime = 1;
				pointInTime = minPointInTime;
			}
			pointInTimeForSenderSide = 2;
			routerVisualisation();
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
	 * Visualisation of a host (Rechner or Notebook) if the frame was sent
	 */
	private void hostVisualisationSent() {
		System.out.println("Created GUI on EDT? " + SwingUtilities.isEventDispatchThread());
		JPanel mainPanel = new JPanel();

		mainPanel.setLayout(new GridBagLayout());
		mainPanel.setBackground(Color.WHITE);
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		JLabel label;
		JEditorPane editorPane;

		// client name cell
		label = setEmptyCell(mainPanel, 0, 0, 1, 1);
		setSize(label, RECTANGLE_WIDTH, VGAP_BETWEEN_CELLS);
		label = setLabel(mainPanel, localPathPanel.systemSoftware.getKnoten().holeAnzeigeName(), 0, 1, 1, 1, true);
		setSize(label, RECTANGLE_WIDTH, (int) (0.3 * RECTANGLE_HEIGHT));
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label = setEmptyCell(mainPanel, 0, 2, 1, 1);
		setSize(label, RECTANGLE_WIDTH, VGAP_BETWEEN_CELLS);

		// client info cell
		InternetKnoten node = ((InternetKnoten) localPathPanel.systemSoftware.getKnoten());
		NetzwerkInterface ni = node.getNetzwerkInterfaceByMac(localPathPanel.getInterfaceId());
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
		editorPane = setEditorPane(mainPanel, clientInfo, 0, 3, 1, 1, true);
		setSize(editorPane, RECTANGLE_WIDTH, (int) (1.55 * RECTANGLE_HEIGHT));
		label = setEmptyCell(mainPanel, 0, 4, 1, 1);
		setSize(label, RECTANGLE_WIDTH, VGAP_BETWEEN_CELLS);

		// create visualisation of nested messages
		for (int i = 1; i < 5; i++) {
			label = new NestedMessagesVisualisation(5 - i);
			setFinishedLabel(mainPanel, label, 0, 5 + 2 * (i - 1), 1, 1);
		}

		// create vertical arrows
		for (int i = 1; i < 4; i++) {
			label = new Arrow();
			setFinishedLabel(mainPanel, label, 0, 6 + 2 * (i - 1), 1, 1);
		}

		add(mainPanel, BorderLayout.CENTER);
	}

	/**
	 * Visualisation of a host (Rechner or Notebook) if the frame was received
	 */
	private void hostVisualisationReceived() {
		System.out.println("Created GUI on EDT? " + SwingUtilities.isEventDispatchThread());
	}

	/**
	 * Visualisierung eines Routers (Vermittlungsrechner oder Heimrouter)
	 */
	private void routerVisualisation() {
		System.out.println("Created GUI on EDT? " + SwingUtilities.isEventDispatchThread());
	}

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
	 * Functionality of the two buttons to go back and forth in time.
	 */
	private void backAndForth() {
		FORTH_IN_TIME.addActionListener(new ActionListener() {
			// forth in time
			@Override
			public void actionPerformed(ActionEvent e) {
				if (pointInTime < maxPointInTime) {
					pointInTime++;
					BACK_IN_TIME.setEnabled(true);
					if (pointInTime == maxPointInTime) {
						FORTH_IN_TIME.setEnabled(false);
					}
					updateSenderSide();
					localPathPanel.updateMessageDetails(senderSideIsActive);

					// hide or show layers
					if (!localPathPanel.isForwarding()) {
						if (localPathPanel.isSent()) {
							localPathPanel.showNextLowerLayer();
						} else {
							localPathPanel.hideNextHigherLayer();
						}
					} else {
						switch (pointInTime) {
						case 1:
							localPathPanel.hideNextHigherLayer();
							break;
						case 3:
							localPathPanel.showNextLowerLayer();
							break;
						}
					}
				}
			}
		});

		BACK_IN_TIME.addActionListener(new ActionListener() {
			// back in time
			@Override
			public void actionPerformed(ActionEvent e) {
				if (pointInTime > minPointInTime) {
					pointInTime--;
					FORTH_IN_TIME.setEnabled(true);
					if (pointInTime == minPointInTime) {
						BACK_IN_TIME.setEnabled(false);
					}
					updateSenderSide();
					localPathPanel.updateMessageDetails(senderSideIsActive);

					// hide or show layers
					if (!localPathPanel.isForwarding()) {
						if (localPathPanel.isSent()) {
							localPathPanel.hideNextHigherLayer();
						} else {
							localPathPanel.showNextLowerLayer();
						}
					} else {
						switch (pointInTime) {
						case 2:
							localPathPanel.hideNextHigherLayer();
							break;
						case 0:
							localPathPanel.showNextLowerLayer();
							break;
						}
					}
				}
			}
		});
	}

	// only has an effect for routers and home routers
	private void updateSenderSide() {
		if (pointInTime >= pointInTimeForSenderSide) {
			senderSideIsActive = true;
		} else {
			senderSideIsActive = false;
		}
	}

	// Cells with one line content are JLabels
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

	// Cells with multiline content are JEditorPanes
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

	// Cells with multiline content are JTextAreas
	private JTextArea setTextArea(JPanel container, String text, int x_pos, int y_pos, int width, int height,
			boolean needsBorder) {
		JTextArea textArea = new JTextArea(text);

		setStandards(textArea, x_pos, y_pos, width, height);
		textArea.setOpaque(true);
		textArea.setEditable(false);
		textArea.setFocusable(false);

		if (needsBorder) {
			Border lineBorder = BorderFactory.createLineBorder(Color.BLACK);
			Border marginBorder = new EmptyBorder(5, 5, 5, 5);
			textArea.setBorder(new CompoundBorder(lineBorder, marginBorder));
		}
		container.add(textArea, c);

		return textArea;
	}

	// Already finished JLabel
	private void setFinishedLabel(JPanel container, JLabel label, int x_pos, int y_pos, int width, int height) {
		setStandards(label, x_pos, y_pos, width, height);
		label.setOpaque(false);

		label.setBorder(javax.swing.BorderFactory.createEmptyBorder());
		container.add(label, c);
	}

	// empty cell
	private JLabel setEmptyCell(JPanel container, int x_pos, int y_pos, int width, int height) {
		JLabel label = new JLabel();

		setStandards(label, x_pos, y_pos, width, height);
		label.setOpaque(false);

		label.setBorder(javax.swing.BorderFactory.createEmptyBorder());
		container.add(label, c);

		return label;
	}

	// set standard GridBagConstraints parameters
	private void setStandards(Component component, int x_pos, int y_pos, int width, int height) {
		c.gridx = x_pos;
		c.gridy = y_pos;
		c.gridwidth = width;
		c.gridheight = height;
	}

	private void setSize(Component component, int width, int height) {
		Dimension dimensionOfComponent = new Dimension(width, height);
		component.setPreferredSize(dimensionOfComponent);
		component.setMinimumSize(dimensionOfComponent);
		component.setMaximumSize(dimensionOfComponent);
	}

	/**
	 * class for the visualisation of nested messages
	 */
	private class NestedMessagesVisualisation extends JLabel {

		private int layerNumber;

		private NestedMessagesVisualisation(int layerNumber) {
			this.layerNumber = layerNumber;
			setBackground(Color.WHITE);
		}

		public Dimension getPreferredSize() {
			return new Dimension(RECTANGLE_WIDTH, (int) RECTANGLE_HEIGHT);
		}

		public void paintComponent(Graphics g) {
			// Let UI Delegate paint first, which
			// includes background filling since
			// this component is opaque.
			super.paintComponent(g);

			double xPos = 0;
			double yPos = 0;

			// set basic layer rectangle
			FilledRectangle rect = new FilledRectangle();
			rect.setX((int) xPos);
			rect.setY((int) yPos);
			setColorAccordingToLayer(layerNumber, rect);
			rect.paintFilledRectangle(g, RECTANGLE_WIDTH);

			// set other layer rectangles
			int exp = 0;
			for (int i = layerNumber + 1; i <= 4; i++) {
				double reducedHeight = RECTANGLE_HEIGHT * (1 - SHRINK_FACTOR) * 0.5;
				xPos = Math.round(xPos + Math.pow(SHRINK_FACTOR, exp)
						* (RECTANGLE_WIDTH - (RECTANGLE_WIDTH * SHRINK_FACTOR + reducedHeight)));
				yPos = Math.round(yPos + Math.pow(SHRINK_FACTOR, exp) * reducedHeight);

				rect.setX((int) xPos);
				rect.setY((int) yPos);
				setColorAccordingToLayer(i, rect);
				rect.paintFilledRectangle(g, (int) Math.round(RECTANGLE_WIDTH * Math.pow(SHRINK_FACTOR, exp + 1)));
				exp++;
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
	 * filled rectangle for painting.
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

		public void paintFilledRectangle(Graphics g, int width) {
			int height = (int) Math.round(width / RECTANGLE_RATIO);
			g.setColor(fillColor);
			g.fillRect(xPos, yPos, width, height);
		}
	}

	/**
	 * class for the arrows between the visualisations of nested messages
	 */
	private class Arrow extends JLabel {

		private Arrow() {
			setBackground(Color.WHITE);
		}

		public Dimension getPreferredSize() {
			return new Dimension(RECTANGLE_WIDTH, (int) VGAP_BETWEEN_CELLS);
		}

		public void paintComponent(Graphics g) {
			// Let UI Delegate paint first, which
			// includes background filling since
			// this component is opaque.
			super.paintComponent(g);

			int xPos = (int) (RECTANGLE_WIDTH / 2.0);
			int length = (int) (VGAP_BETWEEN_CELLS);
			int lineWidth = 4;
			int headHeight = 6;
			g.fillRect(xPos - lineWidth / 2, 0, lineWidth, length - headHeight);
			g.fillPolygon(new int[] { xPos - headHeight, xPos + headHeight, xPos },
					new int[] { length - headHeight, length - headHeight, length }, 3);
		}
	}
}