package filius.gui.schichtensicht;

import javax.swing.JScrollPane;

/**
 * Panel which is embedded in a JDialog window. Consists of a splitPane
 * (visualisation in the upper part, message details table in the lower part).
 * 
 * @author Christoph Irniger
 */
public class GlobalLayerPathPanel extends LayerPathPanel {

	public GlobalLayerPathPanel(LayerPathDialog layerPathDialog) {
		super(layerPathDialog);

		// set visualisation on top half of the SplitPane
		splitPane.setTopComponent(new JScrollPane(new GlobalLayerPathVisualisation(this)));
	}
}
