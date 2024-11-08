package filius.gui.schichtensicht;
import javax.swing.JScrollPane;

/**
 * @author Christoph Irniger
 * 
 */
public class GlobalLayerPathPanel extends LayerPathPanel {
	private GlobalayerPathVisualisationL l;

	public GlobalLayerPathPanel(LayerPathDialog layerPathDialog) {
		super(layerPathDialog);

		// set l on top half of the SplitPane
		l = new GlobalayerPathVisualisationL(this);
		splitPane.setTopComponent(new JScrollPane(l));
	}
}
