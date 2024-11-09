package filius.gui.schichtensicht;

import java.awt.BorderLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;

import filius.rahmenprogramm.I18n;
import filius.software.system.SystemSoftware;

/**
 * Super class for local and global view of the path through the layers.
 * 
 * @author Christoph Irniger
 */
public class LocalLayerPathDialog extends JDialog implements I18n {
	private final SystemSoftware systemSoftware;

	private final String interfaceId;

	/** frame number which was selected by mouse click, starts with 1 */
	private final int selectedFrameNumber;

	public LocalLayerPathDialog(JFrame owner, SystemSoftware systemSoftware, String interfaceId, int selectedFrameNumber) {
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

		// Panel is in the center of the BorderLayout
		add(new LocalLayerPathPanel(this), BorderLayout.CENTER);
		setVisible(true);
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
