package filius.gui.schichtensicht;

import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPanel;

import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.nachrichten.Lauscher;
import filius.software.netzzugangsschicht.EthernetFrame;
import filius.software.system.SystemSoftware;
import filius.software.vermittlungsschicht.IpPaket;

/**
 * This class is used to visualise the end-to-end path of of an IP packet layer
 * 2 packet (in Filius: IP or ARP packet) through the TCP/IP layers of all the
 * nodes it went through. The idea was taken from the following website and with
 * permission of the creator: https://oinf.ch/interactive/tcp-ip-visualisierung/
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
	private static final int MAX_TTL = 64;

	private final JFrame owner;
	private final String mainInterfaceId;
	private final int mainFrameNumber;
	private final EthernetFrame mainFrame;

	/** local path view of the selected node */
	private final LocalLayerPathDialog mainLocalDialog;
	private final LocalLayerPathPanel mainLocalPanel;

	private int currentLocalPanelPathIndex = 0;

	/**
	 * Path of a packet represented as list of ethernet frames encapsulationg this
	 * packet in chronological order of creation.
	 */
	ArrayList<EthernetFrame> framePath = new ArrayList<EthernetFrame>();

	/**
	 * list of LocalLayerPathPanels corresponding to the nodes involved in the path
	 * of a packet in chronological order (i.e. sender node comes first, then all
	 * the routers and at last the receiver node).
	 */
	ArrayList<LocalLayerPathPanel> localPanelPath = new ArrayList<LocalLayerPathPanel>();

	public GlobalLayerPath(JFrame owner, String interfaceId, SystemSoftware systemSoftware, int frameNumber) {
		this.owner = owner;
		mainInterfaceId = interfaceId;
		mainFrameNumber = frameNumber;
		mainFrame = LAUSCHER.getFrame(mainInterfaceId, mainFrameNumber);
		mainLocalDialog = new LocalLayerPathDialog(owner, interfaceId, systemSoftware, frameNumber);
		mainLocalPanel = mainLocalDialog.getLocalLayerPathPanel();

		// overwrite title to "Globaler Weg durch die Schichten"
		mainLocalDialog.setTitle(messages.getString("schichten_globaler_weg_msg2"));

		fillFramePath();
		fillLocalPanelsPath();

		currentLocalPanelPathIndex = localPanelPath.indexOf(mainLocalPanel);
	}

	/**
	 * Fills the frame path list with all the frames involved in delivering the IP
	 * packet.
	 */
	private void fillFramePath() {
		// frame was received via ethernet broadcast (e.g. ARP, DHCP)
		if (mainFrame.getZielMacAdresse().equals(ETHERNET_BROADCAST)
				&& !mainFrame.getQuellMacAdresse().equals(mainInterfaceId)) {
			addToFramePath(mainFrame);
		} else if (mainFrame.getDaten() instanceof IpPaket) {
			EthernetFrame originalframe = LAUSCHER.getFrame(mainInterfaceId, mainFrameNumber);
			for (int ttl = MAX_TTL; ttl > 0; ttl--) {
				EthernetFrame frame = LAUSCHER.getFrameWithSameContentAndSpecificTTL(originalframe, ttl);
				if (frame != null) {
					addToFramePath(frame);
				} else {
					break;
				}
			}
		}
	}

	/**
	 * Fills the LocalPanels path list with all the LocalPanels involved in
	 * delivering the IP packet.
	 */
	private void fillLocalPanelsPath() {
		// frame was received via ethernet broadcast
		if (mainFrame.getZielMacAdresse().equals(ETHERNET_BROADCAST)
				&& !mainFrame.getQuellMacAdresse().equals(mainInterfaceId)) {
			// add sender
			String interfaceId = mainFrame.getQuellMacAdresse();
			SystemSoftware systemSoftware = LAUSCHER.getSystemSoftwareByID(interfaceId);
			int frameNumber = LAUSCHER.getFrameNumber(interfaceId, mainFrame);
			addToLocalPanelPath(interfaceId, systemSoftware, frameNumber);

			// add receiver
			interfaceId = mainFrame.getZielMacAdresse();
			systemSoftware = LAUSCHER.getSystemSoftwareByID(interfaceId);
			frameNumber = LAUSCHER.getFrameNumber(interfaceId, mainFrame);
			addToLocalPanelPath(interfaceId, systemSoftware, frameNumber);
		} else {
			EthernetFrame firstFrame = framePath.getFirst();

			// add sender
			String interfaceId = firstFrame.getQuellMacAdresse();
			SystemSoftware systemSoftware = LAUSCHER.getSystemSoftwareByID(interfaceId);
			int frameNumber = LAUSCHER.getFrameNumber(interfaceId, firstFrame);
			addToLocalPanelPath(interfaceId, systemSoftware, frameNumber);

			// add routers and receiver
			for (EthernetFrame frame : framePath) {
				interfaceId = frame.getZielMacAdresse();
				systemSoftware = LAUSCHER.getSystemSoftwareByID(interfaceId);
				frameNumber = LAUSCHER.getFrameNumber(interfaceId, frame);
				addToLocalPanelPath(interfaceId, systemSoftware, frameNumber);
			}
		}
	}

	/**
	 * Adds the frame to the frame path.
	 * 
	 * @param frame frame to be added to {@code framePath}
	 */
	private void addToFramePath(EthernetFrame frame) {
		// if frame is exactly the same (incl. TTL) as the main frame
		if (frame.sameContent(mainFrame)) {
			framePath.add(mainFrame);
		} else {
			framePath.add(frame);
		}
	}

	/**
	 * Adds the panel of the local view corresponding to the parameters to the local
	 * panels path.
	 * 
	 * @param interfaceId    ID of the network interface
	 * @param systemSoftware system software running on the node
	 * @param frameNumber    number of the frame in the data exchange window (starts
	 *                       with 1)
	 */
	private void addToLocalPanelPath(String interfaceId, SystemSoftware systemSoftware, int frameNumber) {
		// if frame is exactly the same (incl. TTL) as the main frame
		if (interfaceId.equals(mainInterfaceId) && frameNumber == mainFrameNumber) {
			localPanelPath.add(mainLocalPanel);
		} else {
			localPanelPath.add(
					new LocalLayerPathPanel(new LocalLayerPathDialog(owner, interfaceId, systemSoftware, frameNumber)));
		}
	}

	public void switchPanel(Boolean switchForward) {
		currentLocalPanelPathIndex = switchForward ? currentLocalPanelPathIndex + 1 : currentLocalPanelPathIndex - 1;
		// TO DO
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
}