package filius.gui.schichtensicht;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JFrame;

import filius.rahmenprogramm.I18n;
import filius.software.system.SystemSoftware;

/**
 * This class is used to visualise the path of a layer 2 packet (in Filius: IP
 * or ARP packet) through the TCP/IP layers on the selected node (host or
 * router). The idea was taken from the following website and with permission of
 * the creator: https://oinf.ch/interactive/tcp-ip-visualisierung/
 * 
 * This view corresponds to reality. In packet-switching networks like the
 * internet a single node in general cannot track the end-to-end path of a
 * packet. This follows immediately from the properties of the packet-switching
 * network model. The node can only reproduce the receiving, processing and
 * forwarding of a packet.
 * 
 * @author Christoph Irniger
 */
public class LocalLayerPathDialog extends JDialog implements I18n {
	/** the corresponding JPanel which is contained in this JDialog */
	private final LocalLayerPathPanel localLayerPathPanel;

	private final SystemSoftware systemSoftware;

	private final String interfaceId;

	/** frame number which was selected by mouse click, starts with 1 */
	private final int selectedFrameNumber;

	private final GlobalLayerPath globalLayerPath;

	public LocalLayerPathDialog(JFrame owner, String interfaceId, SystemSoftware systemSoftware,
			int selectedFrameNumber, GlobalLayerPath globalLayerPath) {
		super(owner, true);
		this.systemSoftware = systemSoftware;
		this.interfaceId = interfaceId;
		this.selectedFrameNumber = selectedFrameNumber;
		this.globalLayerPath = globalLayerPath;

		// user can resize the JDialog
		setResizable(true);

		// initialise BorderLayout
		BorderLayout borderLayout = new BorderLayout(10, 10);
		setLayout(borderLayout);

		// title "Lokaler Weg durch die Schichten"
		setTitle(messages.getString("schichten_lokaler_weg_msg1"));

		localLayerPathPanel = new LocalLayerPathPanel(this);

		// set size and positioning properties of outer JDialog
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(screenSize.width / 6, screenSize.height / 6);
		int dialogWidth = localLayerPathPanel.getSizeOfMessageDetailsTable().width;
		int dialogHeight = localLayerPathPanel.getSizeOfMessageDetailsTable().height
				+ LocalLayerPathPanel.DIVIDER_LOCATION + 100;
		setPreferredSize(new Dimension(dialogWidth, dialogHeight));
		setMinimumSize(new Dimension(dialogWidth, dialogHeight));

		// Panel is in the center of the BorderLayout
		add(localLayerPathPanel, BorderLayout.CENTER);
		setVisible(true);
	}

	protected LocalLayerPathPanel getLocalLayerPathPanel() {
		return this.localLayerPathPanel;
	}

	public SystemSoftware getSystemSoftware() {
		return this.systemSoftware;
	}

	public String getInterfaceId() {
		return this.interfaceId;
	}

	public int getSelectedFrameNumber() {
		return this.selectedFrameNumber;
	}

	public GlobalLayerPath getGlobalLayerPath() {
		return this.globalLayerPath;
	}
}
