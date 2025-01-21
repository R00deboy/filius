package filius.gui.nachrichtensicht;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

/**
 * Class for the message details table at the bottom of the data exchange and
 * the layer views.
 */
public class MessageDetailsTable extends JPanel {
	private final GridBagLayout GRID_BAG_LAYOUT;
	private GridBagConstraints c;

	private static final int MSG_DETAILS_CELL_WIDTH = 150;
	private static final int MSG_DETAILS_CELL_HEIGHT = 30;

	/**
	 * constructor of {@link MessageDetailsTable}
	 * 
	 * @param messageDetails 6x8 matrix with the info about the message (i.e. frame)
	 */
	public MessageDetailsTable(String[][] messageDetails, Color backgroundColor) {
		GRID_BAG_LAYOUT = new GridBagLayout();
		c = new GridBagConstraints();
		setLayout(GRID_BAG_LAYOUT);
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
		label.setBackground(backgroundColor);
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
		label.setBackground(backgroundColor);
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
		label.setBackground(backgroundColor);
		label.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		setBoldAndItalic(label);
	}

	public GridBagLayout getGridBagLayout() {
		return this.GRID_BAG_LAYOUT;
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
	public static Dimension getSizeOfMessageDetailsTable() {
		return new Dimension(9 * MSG_DETAILS_CELL_WIDTH, 8 * MSG_DETAILS_CELL_HEIGHT);
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
	public static Color getLayerColorVar(String typeOfColor, int layerNumber) {
		return AggregatedMessageTable.LayerColorVar.getLayerColorVar(typeOfColor, layerNumber);
	}

	/**
	 * This method returns the color corresponding to the type of color and the
	 * layer which belongs to the column of the MessageDetailsTable.
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
	private void setStandards(Component component, int x_pos, int y_pos, int width, int height, double width_multiplier,
			double height_multiplier) {
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

	/**
	 * Set the font to bold and italic for a component.
	 * 
	 * @param comp
	 */
	private void setBoldAndItalic(Component comp) {
		Font boldFont = new Font(comp.getFont().getName(), Font.BOLD + Font.ITALIC, comp.getFont().getSize());
		comp.setFont(boldFont);
	}

	/**
	 * Set the font to plain (normal) for a component.
	 * 
	 * @param comp
	 */
	private void setPlain(Component comp) {
		Font boldFont = new Font(comp.getFont().getName(), Font.PLAIN, comp.getFont().getSize());
		comp.setFont(boldFont);
	}

	/**
	 * If the boolean value is true, creates the border of the component. Otherwise
	 * sets an empty border.
	 * 
	 * @param component the component whose border will be affected
	 * @param isVisible the border is visible (true) or not (false)
	 */
	public void setBorderVisible(Component component, boolean isVisible) {
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
}
