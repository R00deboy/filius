package filius.gui.schichtensicht;

import javax.swing.JDialog;
import javax.swing.JFrame;

import filius.hardware.knoten.Host;
import filius.rahmenprogramm.I18n;
import filius.software.system.SystemSoftware;

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

public class LocalPathThroughLayersDialog extends JDialog implements I18n {
	
	/** 
	 * Diese Ansicht muss auf der Position des Maus-Rechtsklicks in der Wireshark-Ansicht
	 * basieren. Denn sie bestimmt das Datenpaket, das untersucht werden soll.
	 * 
	 */
	
	/* Zuerst wird gecheckt, ob Quell- UND Ziel-IP von einem Host sind.
	 * (Pings von Routern (von einem Host aus) werden also vorläufig nicht visualisiert.)
	 * Dann muss gecheckt werden, ob das gewählte IP-Paket wirklich angekommen ist.
	 */
	
	private SystemSoftware systemSoftware;
	
	private int selectedFrameNumber;

    public LocalPathThroughLayersDialog(JFrame owner, SystemSoftware systemSoftware, int selectedFrameNumber) {
        super(owner);
        this.systemSoftware = systemSoftware;
        this.selectedFrameNumber = selectedFrameNumber;
    }
    
	/*
	 * VISUALISIERUNG
	 */
}
