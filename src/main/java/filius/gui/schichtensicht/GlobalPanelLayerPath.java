package filius.gui.schichtensicht;
import javax.swing.JScrollPane;

/**
 * @author Christoph Irniger
 * 
 */
public class GlobalPanelLayerPath extends PanelLayerPath {
	private GlobalVisualisationLayerPath path;

	public GlobalPanelLayerPath(DialogLayerPath dialogLayerPath) {
		super(dialogLayerPath);

		// set path on top half of the SplitPane
		path = new GlobalVisualisationLayerPath(this);
		splitPane.setTopComponent(new JScrollPane(path));
	}
}
