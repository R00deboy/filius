package filius.gui.schichtensicht;
import javax.swing.JScrollPane;

/**
 * @author Christoph Irniger
 * 
 */
public class GlobalLayerPathPanel extends LayerPathPanel {

	public GlobalLayerPathPanel(LayerPathDialog layerPathDialog) {
		super(layerPathDialog);

		// set visualisation on top half of the SplitPane
		splitPane.setTopComponent(new JScrollPane(new GlobalLayerPathVisualisation(this)));
	}
}
