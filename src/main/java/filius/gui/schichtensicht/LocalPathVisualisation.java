package filius.gui.schichtensicht;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import filius.rahmenprogramm.I18n;
import filius.software.system.Betriebssystem;
import filius.software.system.GatewayFirmware;
import filius.software.system.SystemSoftware;
import filius.software.system.VermittlungsrechnerBetriebssystem;

public class LocalPathVisualisation extends JPanel implements I18n {
	private final GridBagLayout gridBagLayout;
	private GridBagConstraints c;

	private LocalPathPanel localPathPanel;

	private int pointInTime;
	private int maxPointInTime;
	private int minPointInTime;

	// visualisation is on the sender side (otherwise receiver side)
	private boolean senderSideIsActive = false;
	// for this and all greater points in time visualisation is on the sender side
	// (for smaller on receiver side)
	private int pointInTimeForSenderSide;

	// buttons "vorwärts" and "zurück"
	private final JButton FORTH_IN_TIME;
	private final JButton BACK_IN_TIME;

	public LocalPathVisualisation(LocalPathPanel localPathPanel) {
		super();
		this.localPathPanel = localPathPanel;
		SystemSoftware systemSoftware = localPathPanel.systemSoftware;
		gridBagLayout = new GridBagLayout();
		c = new GridBagConstraints();
		this.setLayout(gridBagLayout);

		// create buttons
		FORTH_IN_TIME = new JButton(messages.getString("schichten_allgemein_msg1"));
		BACK_IN_TIME = new JButton(messages.getString("schichten_allgemein_msg2"));
		BACK_IN_TIME.setEnabled(false);

		if (localPathPanel.isSent()) {
			senderSideIsActive = true;
		}

		// Falls Host (Rechner, Notebook)
		if (systemSoftware instanceof Betriebssystem) {
			minPointInTime = 4 - localPathPanel.getMaxLayer();
			maxPointInTime = 3;
			pointInTime = minPointInTime;
			if (localPathPanel.isSent()) {
				pointInTimeForSenderSide = -1; // should never be reached, since only sent
			} else {
				pointInTimeForSenderSide = 4; // should never be reached, since only received
			}
			hostVisualisation();
		}
		// Falls Router (Vermittlungsrechner, Heimrouter)
		else if (systemSoftware instanceof VermittlungsrechnerBetriebssystem
				|| systemSoftware instanceof GatewayFirmware) {
			if (localPathPanel.isForwarding()) {
				minPointInTime = 0;
				maxPointInTime = 3;
				pointInTime = minPointInTime;
			} else if (localPathPanel.isSent()) {
				minPointInTime = 2;
				maxPointInTime = 3;
				pointInTime = minPointInTime;
			} else {
				minPointInTime = 0;
				maxPointInTime = 1;
				pointInTime = minPointInTime;
			}
			pointInTimeForSenderSide = 2;
			routerVisualisation();
		}

		backAndForth();
	}

	public int getPointInTime() {
		return this.pointInTime;
	}

	public int getMaxPointInTime() {
		return this.maxPointInTime;
	}

	public int getMinPointInTime() {
		return this.minPointInTime;
	}

	/**
	 * Visualisierung eines Hosts (Rechner oder Notebook)
	 */
	private void hostVisualisation() {
		basicConfiguration();
		System.out.println("Created GUI on EDT? " + SwingUtilities.isEventDispatchThread());
	}

	/**
	 * Visualisierung eines Routers (Vermittlungsrechner oder Heimrouter)
	 */
	private void routerVisualisation() {
		basicConfiguration();
	}

	private void basicConfiguration() {
		c.gridx = 0;
		c.gridy = 0;
		this.add(BACK_IN_TIME, c);
		c.gridx = 1;
		c.gridy = 0;
		this.add(FORTH_IN_TIME, c);
	}

	/**
	 * Functionality of the two buttons to go back and forth in time.
	 */
	private void backAndForth() {
		FORTH_IN_TIME.addActionListener(new ActionListener() {
			// forth in time
			@Override
			public void actionPerformed(ActionEvent e) {
				if (pointInTime < maxPointInTime) {
					pointInTime++;
					BACK_IN_TIME.setEnabled(true);
					if (pointInTime == maxPointInTime) {
						FORTH_IN_TIME.setEnabled(false);
					}
					updateSenderSide();
					localPathPanel.updateMessageDetails(senderSideIsActive);

					// hide or show layers
					if (!localPathPanel.isForwarding()) {
						if (localPathPanel.isSent()) {
							localPathPanel.showNextLowerLayer();
						} else {
							localPathPanel.hideNextHigherLayer();
						}
					} else {
						switch (pointInTime) {
						case 1:
							localPathPanel.hideNextHigherLayer();
							break;
						case 3:
							localPathPanel.showNextLowerLayer();
							break;
						}
					}
				}
			}
		});

		BACK_IN_TIME.addActionListener(new ActionListener() {
			// back in time
			@Override
			public void actionPerformed(ActionEvent e) {
				if (pointInTime > minPointInTime) {
					pointInTime--;
					FORTH_IN_TIME.setEnabled(true);
					if (pointInTime == minPointInTime) {
						BACK_IN_TIME.setEnabled(false);
					}
					updateSenderSide();
					localPathPanel.updateMessageDetails(senderSideIsActive);

					// hide or show layers
					if (!localPathPanel.isForwarding()) {
						if (localPathPanel.isSent()) {
							localPathPanel.hideNextHigherLayer();
						} else {
							localPathPanel.showNextLowerLayer();
						}
					} else {
						switch (pointInTime) {
						case 2:
							localPathPanel.hideNextHigherLayer();
							break;
						case 0:
							localPathPanel.showNextLowerLayer();
							break;
						}
					}
				}
			}
		});
	}

	// only has an effect for routers and home routers
	private void updateSenderSide() {
		if (pointInTime >= pointInTimeForSenderSide) {
			senderSideIsActive = true;
		} else {
			senderSideIsActive = false;
		}
	}
}