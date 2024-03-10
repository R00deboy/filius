package filius.gui.schichtensicht;

import javax.swing.JPanel;

import filius.software.system.Betriebssystem;
import filius.software.system.GatewayFirmware;
import filius.software.system.SystemSoftware;
import filius.software.system.VermittlungsrechnerBetriebssystem;
import net.miginfocom.swing.MigLayout;

/**
 * 
 * @author Christoph Irniger
 */
public class LocalPathThroughLayersPanel extends JPanel {

	MigLayout migLayout;

	public LocalPathThroughLayersPanel(LocalPathThroughLayersDialog localPathThroughLayersDialog,
			SystemSoftware systemSoftware) {
		super();

		migLayout = new MigLayout();
		this.setLayout(migLayout);
		
		// Visualisierung, falls Host (Rechner, Notebook)
		if (systemSoftware instanceof Betriebssystem) {
			hostVisualisation();
		}
		// Visualisierung, falls Router (Vermittlungsrechner, Heimrouter)
		else if (systemSoftware instanceof VermittlungsrechnerBetriebssystem || systemSoftware instanceof GatewayFirmware) {
			routerVisualisation();
		}
	}

	/**
	 * Visualisierung eines Hosts (Rechner oder Notebook)
	 */
	private void hostVisualisation() {

	}

	/**
	 * Visualisierung eines Routers (Vermittlungsrechner oder Heimrouter)
	 */
	private void routerVisualisation() {
		// MigLayout: Es wird für einen Router brauchen: 15 Zeilen, 13 Spalten.
	}
}
