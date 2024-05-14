package filius.gui.schichtensicht;

import java.awt.BorderLayout;
import javax.swing.JDialog;
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
public class LocalPathDialog extends JDialog implements I18n {
	private final SystemSoftware systemSoftware;

	private final String interfaceId;

	private final int selectedFrameNumber;  // beginnt bei 1

	private LocalPathPanel localPathPanel;

	public LocalPathDialog(JFrame owner, SystemSoftware systemSoftware, String interfaceId,
			int selectedFrameNumber) {
		super(owner, true);
		this.systemSoftware = systemSoftware;
		this.interfaceId = interfaceId;
		this.selectedFrameNumber = selectedFrameNumber;
		
		// "Lokaler Weg durch die Schichten"
		setTitle(messages.getString("schichten_lokaler_weg_msg1"));
		setResizable(true);

		// BorderLayout initialisieren
		BorderLayout borderLayout = new BorderLayout(10, 10);
		setLayout(borderLayout);

		// Panel in die Mitte
		localPathPanel = new LocalPathPanel(this);
		add(localPathPanel, BorderLayout.CENTER);
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
