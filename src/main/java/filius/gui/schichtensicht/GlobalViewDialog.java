package filius.gui.schichtensicht;

import javax.swing.JDialog;

import filius.rahmenprogramm.I18n;
import filius.software.vermittlungsschicht.IpPaket;

/**
 * This class is used to visualise the end-to-end path of a message through all the TCP/IP layers.
 * The idea was taken from the following website and with permission of the creater Nicolas Ruh:
 * https://oinf.ch/interactive/tcp-ip-visualisierung/
 * 
 * @author Christoph Irniger
 */

public class GlobalViewDialog extends JDialog implements I18n {
	
	/** 
	 * Diese Ansicht muss auf der Position des Maus-Rechtsklicks in der Wireshark-Ansicht
	 * basieren. Denn sie bestimmt das Datenpaket, das untersucht werden soll.
	 * 
	 */
	
	/* Zuerst wird gecheckt, ob Quell- UND Ziel-IP von einem Host sind.
	 * (Pings von Routern werden also vorläufig nicht visualisiert)
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
