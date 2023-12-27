package filius.gui.schichtensicht;

import javax.swing.JDialog;

import filius.rahmenprogramm.I18n;
import filius.software.vermittlungsschicht.IpPaket;

/**
 * This class is used to visualise the path of a data packet through the TCP/IP layers on the 
 * selected node (host or router). The idea was taken from the following website and with
 * permission of the creator: https://oinf.ch/interactive/tcp-ip-visualisierung/
 * 
 * This view corresponds to reality. In packet-switching networks like the internet a single node
 * in general cannot track the end-to-end path of a message. This follows immediately from the
 * properties of the packet-switching network model. The node can only reproduce the receiving,
 * processing and forwarding of a message.
 * 
 * @author Christoph Irniger
 */

public class LocalViewDialog extends JDialog implements I18n {
	
	/** 
	 * Diese Ansicht muss auf der Position des Maus-Rechtsklicks in der Wireshark-Ansicht
	 * basieren. Denn sie bestimmt das Datenpaket, das untersucht werden soll.
	 * 
	 */
	
	/* Zuerst wird gecheckt, ob Quell- UND Ziel-IP von einem Host sind.
	 * (Pings von Routern (von einem Host aus) werden also vorläufig nicht visualisiert.)
	 * Dann muss gecheckt werden, ob das gewählte IP-Paket wirklich angekommen ist.
	 */
	
	/** Zugehöriges IP-Paket */
	private IpPaket ip_paket;
	
	/*
	 *  Diese Methode soll überprüfen, ob das ausgewählte Ethernet-Frame tatsächlich beim
	 *  Ziel angekommen ist.
	 */
	
	/*
	 * VISUALISIERUNG
	 */
	
	/**  Diese Methode soll die Anzahl Router ("Hops") bestimmen, die auf dem Weg lagen. */
	private int getNumberOfRouters() {
		return 0;
	}
	
	
}
