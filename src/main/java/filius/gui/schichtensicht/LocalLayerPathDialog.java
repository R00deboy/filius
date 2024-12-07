package filius.gui.schichtensicht;

import java.awt.BorderLayout;
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
	private final LocalLayerPathPanel localLayerPathPanel;

	private final SystemSoftware systemSoftware;

	private final String interfaceId;

	/** frame number which was selected by mouse click, starts with 1 */
	private final int selectedFrameNumber;

	public LocalLayerPathDialog(JFrame owner, String interfaceId, SystemSoftware systemSoftware,
			int selectedFrameNumber) {
		super(owner, true);
		this.systemSoftware = systemSoftware;
		this.interfaceId = interfaceId;
		this.selectedFrameNumber = selectedFrameNumber;

		// user can resize the JDialog
		setResizable(true);

		// initialise BorderLayout
		BorderLayout borderLayout = new BorderLayout(10, 10);
		setLayout(borderLayout);

		// title "Lokaler Weg durch die Schichten"
		setTitle(messages.getString("schichten_lokaler_weg_msg1"));

		localLayerPathPanel = new LocalLayerPathPanel(this);

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
}
