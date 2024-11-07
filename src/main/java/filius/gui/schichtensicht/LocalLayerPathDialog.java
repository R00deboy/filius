package filius.gui.schichtensicht;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import filius.rahmenprogramm.I18n;
import filius.software.system.SystemSoftware;

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
public class LocalLayerPathDialog extends DialogLayerPath implements I18n {

	public LocalLayerPathDialog(JFrame owner, SystemSoftware systemSoftware, String interfaceId,
			int selectedFrameNumber) {
		super(owner, systemSoftware, interfaceId, selectedFrameNumber);
		
		// title "Lokaler Weg durch die Schichten"
		setTitle(messages.getString("schichten_lokaler_weg_msg1"));

		// Panel is in the center of the BorderLayout
		localLayerPathPanel = new LocalLayerPathPanel(this);
		add(localLayerPathPanel, BorderLayout.CENTER);
		setVisible(true);
	}
}
