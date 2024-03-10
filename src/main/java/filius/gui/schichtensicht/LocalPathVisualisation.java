package filius.gui.schichtensicht;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import filius.software.system.Betriebssystem;
import filius.software.system.GatewayFirmware;
import filius.software.system.SystemSoftware;
import filius.software.system.VermittlungsrechnerBetriebssystem;

public class LocalPathVisualisation extends JPanel {
	private final GridBagLayout gridBagLayout;
	private GridBagConstraints c;

	public LocalPathVisualisation(SystemSoftware systemSoftware) {
		super();
		gridBagLayout = new GridBagLayout();
		c = new GridBagConstraints();
		this.setLayout(gridBagLayout);

		// Visualisierung, falls Host (Rechner, Notebook)
		if (systemSoftware instanceof Betriebssystem) {
			hostVisualisation();
		}
		// Visualisierung, falls Router (Vermittlungsrechner, Heimrouter)
		else if (systemSoftware instanceof VermittlungsrechnerBetriebssystem
				|| systemSoftware instanceof GatewayFirmware) {
			routerVisualisation();
		}
	}

	/**
	 * Visualisierung eines Hosts (Rechner oder Notebook)
	 */
	private void hostVisualisation() {
		// For each component to be added to this container:
		// ...Create the component...
		// ...Set instance variables in the GridBagConstraints instance...
		JButton button = new JButton("HOIIIIII");
		this.add(button, c);
	}

	/**
	 * Visualisierung eines Routers (Vermittlungsrechner oder Heimrouter)
	 */
	private void routerVisualisation() {
		// MigLayout: Es wird für einen Router brauchen: 15 Zeilen, 13 Spalten.
	}
}
