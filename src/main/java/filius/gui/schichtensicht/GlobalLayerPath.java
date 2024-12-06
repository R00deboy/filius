package filius.gui.schichtensicht;

import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import filius.hardware.knoten.InternetKnoten;
import filius.hardware.knoten.Knoten;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.nachrichten.Lauscher;
import filius.software.netzzugangsschicht.EthernetFrame;
import filius.software.system.SystemSoftware;
import filius.software.vermittlungsschicht.ArpPaket;
import filius.software.vermittlungsschicht.IpPaket;

/**
 * This class is used to visualise the end-to-end path of a message through the
 * TCP/IP layers of all the nodes it went through. The idea was taken from the
 * following website and with permission of the creator:
 * https://oinf.ch/interactive/tcp-ip-visualisierung/
 * 
 * I want to emphasize that this view does not correspond to reality. In
 * packet-switching net- works like the internet a single node in general
 * cannnot possible track the end-to-end path of a message. This follows
 * immediately from the properties of the packet-switching network model.
 * 
 * The purpose of this view is to help learners understand the principle of the
 * layers of the TCP/IP model.
 * 
 * @author Christoph Irniger
 */

public class GlobalLayerPath extends JPanel implements I18n {
	private static final Lauscher LAUSCHER = Lauscher.getLauscher();
	private static final String ETHERNET_BROADCAST = "FF:FF:FF:FF:FF:FF";

	private final JFrame owner;
	private final SystemSoftware mainSystemSoftware;
	private final String mainInterfaceId;
	private final int mainFrameNumber;

	/** local view of the selected node */
	private final LocalLayerPathDialog mainLocalDialog;
	private final LocalLayerPathPanel mainLocalPanel;

	/**
	 * List of arrays containing node data for creating the local view
	 * (systemSoftware, interfaceId, selectedFrameNumber). The order of the nodes
	 * represent the global path of the IP packet.
	 */
	ArrayList<Object[]> nodeDataPath = new ArrayList<Object[]>();

	/** list of LocalLayerPathPanels corresponding to the nodes in nodesPath */
	ArrayList<LocalLayerPathPanel> panelsPath = new ArrayList<LocalLayerPathPanel>();

	public GlobalLayerPath(JFrame owner, SystemSoftware systemSoftware, String interfaceId, int frameNumber) {
		this.owner = owner;
		mainSystemSoftware = systemSoftware;
		mainInterfaceId = interfaceId;
		mainFrameNumber = frameNumber;
		mainLocalDialog = new LocalLayerPathDialog(owner, systemSoftware, interfaceId, frameNumber);
		mainLocalPanel = mainLocalDialog.getLocalLayerPathPanel();

		// overwrite title to "Globaler Weg durch die Schichten"
		mainLocalDialog.setTitle(messages.getString("schichten_globaler_weg_msg2"));

		nodeDataPath.add(new Object[] { mainSystemSoftware, mainInterfaceId, mainFrameNumber });
		panelsPath.add(mainLocalPanel);
	}

	private void fillPath() {
		// number of the frame in the data exchange window (starts with 1)
		int frameNumber;
		String interfaceId;
		// the current node in this method
		SystemSoftware systemSoftware;
		EthernetFrame frame;

		if (frame.getDaten() instanceof IpPaket) {
			// if packet was received via ethernet broadcast, add node and Panel of source
			// MAC
			// (sent via MAC broadcast not possible, since excluded prior to creating this
			// object)
			if (frame.getZielMacAdresse().equals(ETHERNET_BROADCAST)
					&& !frame.getQuellMacAdresse().equals(mainInterfaceId)) {
				systemSoftware = LAUSCHER.getSystemSoftwareByID(interfaceId);

				Object[] interfaceIdAndFrameNumber = LAUSCHER.getNextOrPreviousFrame(true, false, frame);
				interfaceId = (String) interfaceIdAndFrameNumber[0];
				frameNumber = (int) interfaceIdAndFrameNumber[1];

				addToLists(true, systemSoftware, interfaceId, frameNumber);
			}
			// if the packet was not received via ethernet broadcast, e.g. the interface ID
			// is either the source or the destination
			else {
				// if false, interface is destination
				boolean interfaceIsSource = frame.getQuellMacAdresse().equals(interfaceId);
				
				if (interfaceIsSource) {
					
					String destinationIP = ((IpPaket) frame.getDaten()).getEmpfaenger();
					while (!destinationIP.equals(LAUSCHER.getInterfaceByID(frame.getZielMacAdresse()).getIp())) {
						// TTL of the next frame is 1 less than before, this means the interface ID is
						// its source MAC address
						Object[] interfaceIdAndFrameNumber = LAUSCHER.getNextOrPreviousFrame(false, true, frame);
						interfaceId = (String) interfaceIdAndFrameNumber[0];
						frameNumber = (int) interfaceIdAndFrameNumber[1];

						systemSoftware = LAUSCHER.getSystemSoftwareByID(interfaceId);

						frame = LAUSCHER.getFrame(interfaceId, frameNumber);
						destinationIP = ((IpPaket) frame.getDaten()).getEmpfaenger();

						addToLists(true, systemSoftware, interfaceId, frameNumber);
					}
					// add end node
				}
				
				// when a node (and a panel) is added to the lists, it corresponds to the one
				// which the source ID of the frame belongs to
				interfaceId = (String) frame.getQuellMacAdresse();
				frameNumber = LAUSCHER.getFrameNumber(interfaceId, frame);
				systemSoftware = LAUSCHER.getSystemSoftwareByID(interfaceId);
				addToLists(true, systemSoftware, interfaceId, frameNumber);

				// --- ADD THE SOURCE AND DESTINATION NODES ---
				// if false, interface is destination
				boolean interfaceIsSource = frame.getQuellMacAdresse().equals(interfaceId);

				// if interface is destination / source, add destination / source node and panel
				// to lists
				Object[] interfaceIdAndFrameNumber = LAUSCHER.getNextOrPreviousFrame(true, false, frame);
				interfaceId = (String) frame.getQuellMacAdresse();
				frameNumber = (int) interfaceIdAndFrameNumber[1];
				systemSoftware = LAUSCHER.getSystemSoftwareByID(interfaceId);
				addToLists(true, systemSoftware, interfaceId, frameNumber);

				// --- SEARCH FORWARD ---
				// while the IP packet is not determined for the this interface, which means
				// that the message will be delivered to at least one more interface, get the
				// next node and add it to the lists
				String destinationIP = ((IpPaket) frame.getDaten()).getEmpfaenger();
				interfaceId = frame.getZielMacAdresse();
				while (!destinationIP.equals(LAUSCHER.getInterfaceByID(frame.getZielMacAdresse()).getIp())) {
					// TTL of the next frame is 1 less than before, this means the interface ID is
					// its source MAC address
					Object[] interfaceIdAndFrameNumber = LAUSCHER.getNextOrPreviousFrame(false, true, frame);
					interfaceId = (String) interfaceIdAndFrameNumber[0];
					frameNumber = (int) interfaceIdAndFrameNumber[1];

					systemSoftware = LAUSCHER.getSystemSoftwareByID(interfaceId);

					frame = LAUSCHER.getFrame(interfaceId, frameNumber);
					destinationIP = ((IpPaket) frame.getDaten()).getEmpfaenger();

					addToLists(true, systemSoftware, interfaceId, frameNumber);
				}

				// after all the router nodes which appear after the original node have been
				// added, the destination has to be added
				Object[] interfaceIdAndFrameNumber = LAUSCHER.getNextOrPreviousFrame(true, false, frame);
				interfaceId = (String) interfaceIdAndFrameNumber[0];
				frameNumber = (int) interfaceIdAndFrameNumber[1];

				systemSoftware = LAUSCHER.getSystemSoftwareByID(interfaceId);

				addToLists(true, systemSoftware, interfaceId, frameNumber);

				/// --- SEARCH BACKWARD ---
				// while the IP packet was not originally by this interface, which means
				// that the message was forwarded at least once before, get the previous node
				/// and
				// add it to the lists
			}
		} else if (frame.getDaten() instanceof ArpPaket)

		{

			/*
			 * So lange Quell-IP nicht die IP von der versendenden Netzwerkkarte ist:
			 * interfaceId zu Beginn Schluss des Arrays zu path hinzufügen
			 */
		}
	}

	/**
	 * Add node data and panel to the respective lists.
	 * 
	 * @param addAtTheEnd    true if adding at the end of the lists, false if adding
	 *                       at the beginning
	 * @param systemSoftware system software of the node
	 * @param interfaceId    ID of the interface of the node
	 * @param FrameNumber    number of the frame in the data exchange window (starts
	 *                       with 1)
	 */
	private void addToLists(boolean addAtTheEnd, SystemSoftware systemSoftware, String interfaceId, int FrameNumber) {
		if (addAtTheEnd) {
			nodeDataPath.add(new Object[] { systemSoftware, interfaceId, FrameNumber });
			panelsPath.add(
					new LocalLayerPathPanel(new LocalLayerPathDialog(owner, systemSoftware, interfaceId, FrameNumber)));
		} else {
			nodeDataPath.add(0, new Object[] { systemSoftware, interfaceId, FrameNumber });
			panelsPath.add(0,
					new LocalLayerPathPanel(new LocalLayerPathDialog(owner, systemSoftware, interfaceId, FrameNumber)));
		}

	}

	private void searchBack() {

	}

	private void searchForward() {

	}

//	@Override
//	public Dimension getPreferredSize() {
//		return new Dimension(480, 300);
//	}
//
//	@Override
//	public Dimension getMinimumSize() {
//		return new Dimension(480, 300);
//	}
//
//	@Override
//	public Dimension getMaximumSize() {
//		return new Dimension(480, 300);
//	}

	private void updateBackButtonActivity() {

	}

	private void updateForthButtonActivity() {

	}
}