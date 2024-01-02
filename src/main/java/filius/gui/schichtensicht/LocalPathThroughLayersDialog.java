package filius.gui.schichtensicht;

import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.Gateway;
import filius.hardware.knoten.Vermittlungsrechner;
import filius.rahmenprogramm.I18n;
import filius.software.system.Betriebssystem;
import filius.software.system.GatewayFirmware;
import filius.software.system.SystemSoftware;
import filius.software.system.VermittlungsrechnerBetriebssystem;

/**
 * This class is used to visualise the path of a data packet through the TCP/IP
 * layers on the selected node (host or router). The idea was taken from the
 * following website and with permission of the creator:
 * https://oinf.ch/interactive/tcp-ip-visualisierung/
 * 
 * This view corresponds to reality. In packet-switching networks like the
 * internet a single node in general cannot track the end-to-end path of a
 * message. This follows immediately from the properties of the packet-switching
 * network model. The node can only reproduce the receiving, processing and
 * forwarding of a message.
 * 
 * @author Christoph Irniger
 */

public class LocalPathThroughLayersDialog extends JDialog implements I18n {

	private final SystemSoftware systemSoftware;

	private final int selectedFrameNumber;

	public LocalPathThroughLayersDialog(JFrame owner, SystemSoftware systemSoftware, int selectedFrameNumber) {
		super(owner, true);
		this.systemSoftware = systemSoftware;
		this.selectedFrameNumber = selectedFrameNumber;
		
		JOptionPane optionPane = new JOptionPane();
		String message = "";

		if (systemSoftware instanceof Betriebssystem) {
			String ipAddress = ((Betriebssystem) systemSoftware).primaryIPAdresse();
			String subnetMask = ((Betriebssystem) systemSoftware).primarySubnetMask();
			String macAddress = ((Betriebssystem) systemSoftware).primaryMACAddress();
			String gateway = ((Betriebssystem) systemSoftware).getStandardGateway();
			String dnsServer = ((Betriebssystem) systemSoftware).getDNSServer();
			
			message = message + "Nummer des Frames: " + this.selectedFrameNumber + "\n\n" +
								"IP: " + ipAddress + "\n" + 
								"Netzmaske: " + subnetMask + "\n" +
								"MAC: " + macAddress + "\n" +
								"Gateway: " + gateway + "\n" +
								"DNS-Server: " + dnsServer;
		}

		else if (systemSoftware instanceof VermittlungsrechnerBetriebssystem) {
			Vermittlungsrechner knoten = (Vermittlungsrechner) systemSoftware.getKnoten();
			List<NetzwerkInterface> netzwerkInterfaces = knoten.getNetzwerkInterfaces();
			
			message = message + "Nummer des Frames: " + selectedFrameNumber + "\n";
			
			for (NetzwerkInterface netzwerkInterface : netzwerkInterfaces) {
				message = message + "\n";
				message = message + "IP: " + netzwerkInterface.getIp() + "\n";
				message = message + "Netzmaske: " + netzwerkInterface.getSubnetzMaske() + "\n";
				message = message + "MAC: " + netzwerkInterface.getMac() + "\n";
			}
		}
		
		else if (systemSoftware instanceof GatewayFirmware) {
			Gateway knoten = (Gateway) systemSoftware.getKnoten();
			List<NetzwerkInterface> netzwerkInterfaces = knoten.getNetzwerkInterfaces();
			
			message = message + "Nummer des Frames: " + selectedFrameNumber + "\n";
			
			for (NetzwerkInterface netzwerkInterface : netzwerkInterfaces) {
				message = message + "\n";
				message = message + "IP: " + netzwerkInterface.getIp() + "\n";
				message = message + "Netzmaske: " + netzwerkInterface.getSubnetzMaske() + "\n";
				message = message + "MAC: " + netzwerkInterface.getMac() + "\n";
			}
		}

		optionPane.setMessage(message);
		this.add(optionPane);
	}

	/*
	 * VISUALISIERUNG
	 */
}
