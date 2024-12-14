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
	private final LocalLayerPathPanel LOCAL_LAYER_PATH_PANEL;

	public LocalLayerPathDialog(JFrame owner, String interfaceId, SystemSoftware systemSoftware,
			int selectedFrameNumber, GlobalLayerPath globalLayerPath) {
		super(owner, true);

		// user can resize the JDialog
		setResizable(true);

		// initialise BorderLayout
		BorderLayout borderLayout = new BorderLayout(10, 10);
		setLayout(borderLayout);

		// title "Lokaler Weg durch die Schichten"
		setTitle(messages.getString("schichten_lokaler_weg_msg1"));

		LOCAL_LAYER_PATH_PANEL = new LocalLayerPathPanel(owner, interfaceId, systemSoftware, selectedFrameNumber,
				globalLayerPath);

		// set size and positioning properties of outer JDialog
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(screenSize.width / 6, screenSize.height / 6);
		int dialogWidth = LOCAL_LAYER_PATH_PANEL.getSizeOfMessageDetailsTable().width;
		int dialogHeight = LOCAL_LAYER_PATH_PANEL.getSizeOfMessageDetailsTable().height
				+ LOCAL_LAYER_PATH_PANEL.getDividerLocation() + 100;
		setPreferredSize(new Dimension(dialogWidth, dialogHeight));
		setMinimumSize(new Dimension(dialogWidth, dialogHeight));

		// Panel is in the center of the BorderLayout
		add(LOCAL_LAYER_PATH_PANEL, BorderLayout.CENTER);
		setVisible(true);
	}

	protected LocalLayerPathPanel getLocalLayerPathPanel() {
		return this.LOCAL_LAYER_PATH_PANEL;
	}
}
