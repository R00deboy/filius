package filius.gui.schichtensicht;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

/**
 * 
 * @author Christoph Irniger
 */
public class LocalPathPanel extends JPanel {

	public LocalPathPanel(LocalPathDialog localPathDialog) {
		super();

		final LocalPathVisualisation visualisation = new LocalPathVisualisation(
				localPathDialog.getSystemSoftware());

		final MessageDetailsTable messageDetailsTable = new MessageDetailsTable();

		JScrollPane visualisationScrollPane;
		JScrollPane messageDetailsScrollPane;
		visualisationScrollPane = new JScrollPane(visualisation);
		messageDetailsScrollPane = new JScrollPane(messageDetailsTable);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane.setTopComponent(visualisationScrollPane);
		splitPane.setBottomComponent(messageDetailsScrollPane);
	}

	private class MessageDetailsTable extends JPanel {
	}
}
