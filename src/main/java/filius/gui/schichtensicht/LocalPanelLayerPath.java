package filius.gui.schichtensicht;

import javax.swing.JScrollPane;

import filius.rahmenprogramm.I18n;

/**
 * Panel which is embedded in a JDialog window. Consists of a splitPane
 * (path in the upper part, message details table in the lower part).
 * 
 * @author Christoph Irniger
 */
public class LocalPanelLayerPath extends PanelLayerPath implements I18n {
	private LocalVisualisationLayerPath path;

	public LocalPanelLayerPath(DialogLayerPath dialogLayerPath) {
		super(dialogLayerPath);

		// set path on top half of the SplitPane
		path = new LocalVisualisationLayerPath(this);
		splitPane.setTopComponent(new JScrollPane(path));
	}
}