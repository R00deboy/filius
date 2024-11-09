package filius.gui.schichtensicht;

import javax.swing.JFrame;
import javax.swing.JPanel;

import filius.rahmenprogramm.I18n;
import filius.software.system.SystemSoftware;

public class GlobalLayerPath extends JPanel implements I18n {

	private LocalLayerPathDialog originalLocalDialog;

	public GlobalLayerPath(JFrame owner, SystemSoftware systemSoftware, String interfaceId,
			int selectedFrameNumber) {
		originalLocalDialog = new LocalLayerPathDialog(owner, systemSoftware, interfaceId, selectedFrameNumber);
		
		// overwrite title to "Globaler Weg durch die Schichten"
		originalLocalDialog.setTitle(messages.getString("schichten_globaler_weg_msg2"));
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