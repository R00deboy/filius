package filius.gui.schichtensicht;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import filius.gui.nachrichtensicht.AggregatedMessageTable;
import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.Gateway;
import filius.hardware.knoten.Vermittlungsrechner;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.nachrichten.Lauscher;
import filius.software.system.Betriebssystem;
import filius.software.system.GatewayFirmware;
import filius.software.system.SystemSoftware;
import filius.software.system.VermittlungsrechnerBetriebssystem;
import net.miginfocom.swing.MigLayout;

/**
 * This class is used to visualise the path of a data packet through the TCP/IP
 * layers on the selected node (host or router). The idea was taken from the
 * following website and with permission of the creator:
 * https://oinf.ch/interactive/tcp-ip-visualisierung/
 * 
 * This view corresponds to reality. In packet-switching networks like the
 * internet a single node in general cannot track the end-to-end path of a
 * datenImport. This follows immediately from the properties of the
 * packet-switching network model. The node can only reproduce the receiving,
 * processing and forwarding of a datenImport.
 * 
 * @author Christoph Irniger
 */

public class LocalPathThroughLayersDialog extends JDialog implements I18n {
	private static Logger LOG = LoggerFactory.getLogger(AggregatedMessageTable.class);

	private final SystemSoftware systemSoftware;
	
	private final String interfaceId;

	private final int selectedFrameNumber;

	private JLabel[][] messageDetailsTable = new JLabel[5][8];

	private BorderLayout borderLayout;

	private JOptionPane optionPane; // Nur für Hilfszwecke

	private String datenImport; // Nur für Hilfszwecke

	public LocalPathThroughLayersDialog(JFrame owner, SystemSoftware systemSoftware,
			String interfaceId, int selectedFrameNumber) {
		super(owner, true);
		this.systemSoftware = systemSoftware;
		this.interfaceId = interfaceId;
		this.selectedFrameNumber = selectedFrameNumber;

		setTitle(messages.getString("schichten_lokaler_weg_msg1"));

		borderLayout = new BorderLayout(10, 10);
		setLayout(borderLayout);

		fillMessageDetailsTableConstants();
		fillMessageDetailsTable();

		// showDaten();
	}

	/**
	 * Diese Methode füllt die Tabelle mit den Zellbezeichnungen. Diese Werte sind
	 * für alle Tabellen gleich, egal welche Nachricht ausgewählt wurde.
	 */
	private void fillMessageDetailsTableConstants() {
		messageDetailsTable[0][7] = new JLabel(messages.getString("schichten_lokaler_weg_msg2"));
		messageDetailsTable[1][7] = new JLabel(messages.getString("schichten_lokaler_weg_msg3"));
		messageDetailsTable[2][7] = new JLabel(messages.getString("schichten_lokaler_weg_msg4"));
		messageDetailsTable[0][0] = new JLabel(messages.getString("rp_lauscher_msg8"));
		messageDetailsTable[0][2] = new JLabel(messages.getString("rp_lauscher_msg9"));
		messageDetailsTable[0][4] = new JLabel(messages.getString("rp_lauscher_msg10"));
		messageDetailsTable[0][6] = new JLabel(messages.getString("rp_lauscher_msg11"));
		messageDetailsTable[1][0] = new JLabel("Ethernet");
		messageDetailsTable[2][0] = new JLabel(messages.getString("rp_lauscher_msg3"));
		messageDetailsTable[3][0] = new JLabel(messages.getString("rp_lauscher_msg4"));
		messageDetailsTable[3][0] = new JLabel(messages.getString("rp_lauscher_msg7"));
		messageDetailsTable[2][2] = new JLabel(messages.getString("rp_lauscher_msg3"));
		messageDetailsTable[3][2] = new JLabel(messages.getString("rp_lauscher_msg4"));
		messageDetailsTable[3][2] = new JLabel(messages.getString("rp_lauscher_msg7"));
		messageDetailsTable[2][4] = new JLabel(messages.getString("rp_lauscher_msg3"));
		messageDetailsTable[3][4] = new JLabel(messages.getString("rp_lauscher_msg4"));
		messageDetailsTable[3][4] = new JLabel(messages.getString("rp_lauscher_msg7"));
	}

	/**
	 * Diese Methode füllt die Tabelle mit den Werten für die ausgewählte Nachricht.
	 */
	private void fillMessageDetailsTable() {

		// Wenn der ausgewählte Knoten ein Host (Rechner oder ein Notebook) ist
		if (systemSoftware instanceof Betriebssystem) {

			// Lauscher-Daten holen
			Object[][] daten = Lauscher.getLauscher().getDaten(interfaceId, false, 0);

			String selectedFrameNumberString = Integer.toString(selectedFrameNumber);

			for (int i = 0; i < daten.length; i++) {
				if (daten[i][0].equals(selectedFrameNumberString)) {
					// Netzzugangs-Daten
					messageDetailsTable[2][1] = new JLabel(daten[i][2].toString());
					messageDetailsTable[3][1] = new JLabel(daten[i][3].toString());
					messageDetailsTable[4][1] = new JLabel(daten[i][6].toString());
					LOG.debug(daten[i][2].toString());
					LOG.debug(daten[i][3].toString());
					LOG.debug(daten[i][6].toString());

					// Vermittlungs-Daten (falls existent)
					if (i + 1 < daten.length && daten[i + 1][0].equals(selectedFrameNumberString)) {
						messageDetailsTable[2][3] = new JLabel(daten[i + 1][2].toString());
						messageDetailsTable[3][3] = new JLabel(daten[i + 1][3].toString());
						messageDetailsTable[4][3] = new JLabel(daten[i + 1][6].toString());
						messageDetailsTable[1][3] = new JLabel(daten[i + 1][4].toString());
						LOG.debug(daten[i + 1][2].toString());
						LOG.debug(daten[i + 1][3].toString());
						LOG.debug(daten[i + 1][6].toString());
						LOG.debug(daten[i + 1][4].toString());
					}

					// Transport-Daten (falls existent)
					if (i + 2 < daten.length && daten[i + 2][0].equals(selectedFrameNumberString)) {
						messageDetailsTable[2][5] = new JLabel(daten[i + 2][2].toString());
						messageDetailsTable[3][5] = new JLabel(daten[i + 2][3].toString());
						messageDetailsTable[4][5] = new JLabel(daten[i + 2][6].toString());
						messageDetailsTable[1][5] = new JLabel(daten[i + 2][4].toString());
						LOG.debug(daten[i + 2][2].toString());
						LOG.debug(daten[i + 2][3].toString());
						LOG.debug(daten[i + 2][6].toString());
						LOG.debug(daten[i + 2][4].toString());
					}

					// Anwendungs-Daten (falls existent)
					if (i + 3 < daten.length && daten[i + 3][0].equals(selectedFrameNumberString)) {
						messageDetailsTable[2][6] = new JLabel(daten[i + 3][6].toString());
						messageDetailsTable[1][6] = new JLabel(daten[i + 3][4].toString());
						LOG.debug(daten[i + 3][6].toString());
						LOG.debug(daten[i + 3][4].toString());
					}
					break;
				}
			}
		}
		
		// Wenn der ausgewählte Knoten ein Vermittlungsrechner ist
		else if (systemSoftware instanceof VermittlungsrechnerBetriebssystem) {
			Vermittlungsrechner knoten = (Vermittlungsrechner) systemSoftware.getKnoten();
			List<NetzwerkInterface> netzwerkInterfaces = knoten.getNetzwerkInterfaces();
			
			
			
			// to be continued...
		}
		
		// Wenn der ausgewählte Knoten ein Heimrouter ist
		else if (systemSoftware instanceof GatewayFirmware) {
			Gateway knoten = (Gateway) systemSoftware.getKnoten();
			List<NetzwerkInterface> netzwerkInterfaces = knoten.getNetzwerkInterfaces();
			
			// to be continued...
		}
	}

	// Methode kann später gelöscht werden. Nur als Hilfe gedacht für den Moment.
	private void showDaten() {
		optionPane = new JOptionPane();
		datenImport = "";

		if (systemSoftware instanceof Betriebssystem) {
			String ipAddress = ((Betriebssystem) systemSoftware).primaryIPAdresse();
			String subnetMask = ((Betriebssystem) systemSoftware).primarySubnetMask();
			String macAddress = ((Betriebssystem) systemSoftware).primaryMACAddress();
			String gateway = ((Betriebssystem) systemSoftware).getStandardGateway();
			String dnsServer = ((Betriebssystem) systemSoftware).getDNSServer();

			datenImport = datenImport + "Nummer des Frames: " + this.selectedFrameNumber + "\n\n" + "IP: " + ipAddress
					+ "\n" + "Netzmaske: " + subnetMask + "\n" + "MAC: " + macAddress + "\n" + "Gateway: " + gateway
					+ "\n" + "DNS-Server: " + dnsServer;
		}

		else if (systemSoftware instanceof VermittlungsrechnerBetriebssystem) {
			Vermittlungsrechner knoten = (Vermittlungsrechner) systemSoftware.getKnoten();
			List<NetzwerkInterface> netzwerkInterfaces = knoten.getNetzwerkInterfaces();

			datenImport = datenImport + "Nummer des Frames: " + selectedFrameNumber + "\n";

			for (NetzwerkInterface netzwerkInterface : netzwerkInterfaces) {
				datenImport = datenImport + "\n";
				datenImport = datenImport + "IP: " + netzwerkInterface.getIp() + "\n";
				datenImport = datenImport + "Netzmaske: " + netzwerkInterface.getSubnetzMaske() + "\n";
				datenImport = datenImport + "MAC: " + netzwerkInterface.getMac() + "\n";
			}
		}

		else if (systemSoftware instanceof GatewayFirmware) {
			Gateway knoten = (Gateway) systemSoftware.getKnoten();
			List<NetzwerkInterface> netzwerkInterfaces = knoten.getNetzwerkInterfaces();

			datenImport = datenImport + "Nummer des Frames: " + selectedFrameNumber + "\n";

			for (NetzwerkInterface netzwerkInterface : netzwerkInterfaces) {
				datenImport = datenImport + "\n";
				datenImport = datenImport + "IP: " + netzwerkInterface.getIp() + "\n";
				datenImport = datenImport + "Netzmaske: " + netzwerkInterface.getSubnetzMaske() + "\n";
				datenImport = datenImport + "MAC: " + netzwerkInterface.getMac() + "\n";
			}
		}

		optionPane.setMessage(datenImport);
		this.add(optionPane);
	}
}
