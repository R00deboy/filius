package filius.gui.schichtensicht;

import java.util.Hashtable;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import filius.software.vermittlungsschicht.IpPaket;

public class LocalViewPanel extends JTabbedPane {
	
	private Hashtable<String, JPanel> openedTabs = new Hashtable<String, JPanel>();
	private Hashtable<String, IpPaket> packets = new Hashtable<String, IpPaket>();
	
	/*
	 * JSplitPane benutzen: Oben das Modell mit dem Weg durch die Schichten, unten die Details
	 * des Pakets.
	 */
}
