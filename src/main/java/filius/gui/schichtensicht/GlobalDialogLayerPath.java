package filius.gui.schichtensicht;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import filius.rahmenprogramm.I18n;
import filius.software.system.SystemSoftware;

/**
 * This class is used to visualise the end-to-end path of a message through the TCP/IP layers
 * of all the nodes it went through. The idea was taken from the following website and with
 * permission of the creator: https://oinf.ch/interactive/tcp-ip-visualisierung/
 * 
 * I want to emphasize that this view does not correspond to reality. In packet-switching net-
 * works like the internet a single node in general cannnot possible track the end-to-end path
 * of a message. This follows immediately from the properties of the packet-switching network
 * model.
 * 
 * The purpose of this view is to help learners understand the principle of the layers of the
 * TCP/IP model.
 * 
 * @author Christoph Irniger
 */

public class GlobalDialogLayerPath extends DialogLayerPath implements I18n {
	
	public GlobalDialogLayerPath(JFrame owner, SystemSoftware systemSoftware, String interfaceId,
			int selectedFrameNumber) {
		super(owner, systemSoftware, interfaceId, selectedFrameNumber);
		
		// title "Globaler Weg durch die Schichten"
		setTitle(messages.getString("schichten_lokaler_weg_msg2"));

		// Panel is in the center of the BorderLayout
		localPanelLayerPath = new LocalPanelLayerPath(this);
		add(localPanelLayerPath, BorderLayout.CENTER);
		setVisible(true);
	}
	
	/** 
	 * Diese Ansicht muss auf der Position des Maus-Rechtsklicks in der Wireshark-Ansicht
	 * basieren. Denn sie bestimmt das Datenpaket, das untersucht werden soll.
	 */
	
	/** 
	 * Zuerst wird gecheckt, ob Quell- UND Ziel-IP von einem Host sind.
	 * (Pings von Routern (von einem Host aus) werden also vorläufig nicht visualisiert.)
	 * Dann muss gecheckt werden, ob das gewählte IP-Paket wirklich angekommen ist.
	 */
	
	/*
	 * VISUALISIERUNG
	 */
	
	/**  Diese Methode soll die Anzahl Router ("Hops") bestimmen, die auf dem Weg lagen. */
	private int getNumberOfRouters() {
		return 0;
	}
	
	
}
