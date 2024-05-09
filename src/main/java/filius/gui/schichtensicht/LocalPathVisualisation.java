package filius.gui.schichtensicht;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import filius.software.system.Betriebssystem;
import filius.software.system.GatewayFirmware;
import filius.software.system.SystemSoftware;
import filius.software.system.VermittlungsrechnerBetriebssystem;

public class LocalPathVisualisation extends JPanel {
	private final GridBagLayout gridBagLayout;
	private GridBagConstraints c;

	private LocalPathPanel localPathPanel;

	private int pointInTime = 0;
	private int maxPointInTime;
	private int minPointInTime;

	// visualisation is on the sender side (otherwise receiver side)
	private boolean senderSideIsActive = false;
	// for this and all greater points in time visualisation is on the sender side
	// (for smaller on receiver side)
	private int pointInTimeForSenderSide;

	private final JButton BACK_IN_TIME = new JButton("zurück");
	private final JButton FORTH_IN_TIME = new JButton("vorwärts");

	public LocalPathVisualisation(LocalPathPanel localPathPanel, boolean sent, boolean forwarding) {
		super();
		this.localPathPanel = localPathPanel;
		SystemSoftware systemSoftware = localPathPanel.systemSoftware;
		gridBagLayout = new GridBagLayout();
		c = new GridBagConstraints();
		this.setLayout(gridBagLayout);
		
		BACK_IN_TIME.setEnabled(false);

		if (sent) {
			senderSideIsActive = true;
		}

		// Falls Host (Rechner, Notebook)
		if (systemSoftware instanceof Betriebssystem) {
			minPointInTime = 0;
			maxPointInTime = 4;
			if (sent) {
				pointInTimeForSenderSide = 0;
			} else {
				pointInTimeForSenderSide = 5;
			}
			hostVisualisation();
		}
		// Falls Router (Vermittlungsrechner, Heimrouter)
		else if (systemSoftware instanceof VermittlungsrechnerBetriebssystem
				|| systemSoftware instanceof GatewayFirmware) {
			if (forwarding) {
				minPointInTime = 0;
				maxPointInTime = 5;
			} else if (sent) {
				minPointInTime = 3;
				maxPointInTime = 5;
			} else {
				minPointInTime = 0;
				maxPointInTime = 2;
			}
			pointInTimeForSenderSide = 3;
			routerVisualisation();
		}

		backAndForth();
	}

	/**
	 * Visualisierung eines Hosts (Rechner oder Notebook)
	 */
	private void hostVisualisation() {
		basicConfiguration();
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

	private void backAndForth() {
		BACK_IN_TIME.addActionListener(new ActionListener() {
			// back in time
			@Override
			public void actionPerformed(ActionEvent e) {
				if (pointInTime > minPointInTime) {
					pointInTime--;
					if (pointInTime == minPointInTime) {
						BACK_IN_TIME.setEnabled(false);
					} else {
						FORTH_IN_TIME.setEnabled(true);
					}
					setSenderSide();
					localPathPanel.updateMessageDetails(senderSideIsActive);
				}
			}
		});

		FORTH_IN_TIME.addActionListener(new ActionListener() {
			// forth in time
			@Override
			public void actionPerformed(ActionEvent e) {
				if (pointInTime < maxPointInTime) {
					pointInTime++;
					if (pointInTime == maxPointInTime) {
						FORTH_IN_TIME.setEnabled(false);
					} else {
						BACK_IN_TIME.setEnabled(true);
					}
					setSenderSide();
					localPathPanel.updateMessageDetails(senderSideIsActive);
				}
			}
		});
	}

	private void setSenderSide() {
		if (pointInTime >= pointInTimeForSenderSide) {
			senderSideIsActive = true;
		} else {
			senderSideIsActive = false;
		}
	}
}
