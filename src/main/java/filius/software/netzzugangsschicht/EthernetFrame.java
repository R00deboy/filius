/*
 ** This file is part of Filius, a network construction and simulation software.
 ** 
 ** Originally created at the University of Siegen, Institute "Didactics of
 ** Informatics and E-Learning" by a students' project group:
 **     members (2006-2007): 
 **         André Asschoff, Johannes Bade, Carsten Dittich, Thomas Gerding,
 **         Nadja Haßler, Ernst Johannes Klebert, Michell Weyer
 **     supervisors:
 **         Stefan Freischlad (maintainer until 2009), Peer Stechert
 ** Project is maintained since 2010 by Christian Eibl <filius@c.fameibl.de>
 **         and Stefan Freischlad
 ** Filius is free software: you can redistribute it and/or modify
 ** it under the terms of the GNU General Public License as published by
 ** the Free Software Foundation, either version 2 of the License, or
 ** (at your option) version 3.
 ** 
 ** Filius is distributed in the hope that it will be useful,
 ** but WITHOUT ANY WARRANTY; without even the implied
 ** warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 ** PURPOSE. See the GNU General Public License for more details.
 ** 
 ** You should have received a copy of the GNU General Public License
 ** along with Filius.  If not, see <http://www.gnu.org/licenses/>.
 */
package filius.software.netzzugangsschicht;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import filius.software.vermittlungsschicht.ArpPaket;
import filius.software.vermittlungsschicht.IcmpPaket;
import filius.software.vermittlungsschicht.IpPaket;

/**
 * Diese Klasse implementiert einen Frame auf der Netzzugangsschicht.
 */
public class EthernetFrame implements Serializable {
	private static Logger LOG = LoggerFactory.getLogger(EthernetFrame.class);

	private static final long serialVersionUID = 1L;

	/** Protokolltypen der Vermittlungsschicht */
	public static final String IP = "0x800", ARP = "0x806";

	/** Die Ziel-Adresse des Frames */
	private String zielMacAdresse;

	/** Die MAC-Adresse, von dem sendenden Rechner */
	private String quellMacAdresse;

	/** Typ des uebergeordneten Protokolls (ARP oder IP) */
	private String typ;

	/** die Nutzdaten */
	private Object daten;

	/** Rückverfolgung für die Schichten-Visualisierung */
	private EthernetFrame previousFrame;

	/** Rückverfolgung für die Schichten-Visualisierung */
	private EthernetFrame nextFrame;

	private Set<String> readByLauscherForMac = new HashSet<>();

	/** Konstruktor zur Initialisierung der Attribute des Frames */
	public EthernetFrame(Object daten, String quellMacAdresse, String zielMacAdresse, String typ,
			EthernetFrame previousFrame, EthernetFrame nextFrame) {
		LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (EthernetFrame), constr: EthernetFrame(" + daten
				+ "," + quellMacAdresse + "," + zielMacAdresse + "," + typ + ")");
		this.zielMacAdresse = zielMacAdresse;
		this.quellMacAdresse = quellMacAdresse;
		this.typ = typ;
		this.daten = daten;
		this.previousFrame = previousFrame;
		this.nextFrame = nextFrame;
	}

	public void setReadByLauscherForMac(String mac) {
		readByLauscherForMac.add(mac);
	}

	public boolean isReadByLauscherForMac(String mac) {
		return readByLauscherForMac.contains(mac);
	}

	/** Zugriff auf die Daten, die mit dem Frame verschickt werden */
	public Object getDaten() {
		return daten;
	}

	/** Zugriff auf die Absender-MAC-Adresse */
	public String getQuellMacAdresse() {
		return quellMacAdresse;
	}

	/** Zugriff auf den Protokolltyp. Zulaessig sind ARP und IP */
	public String getTyp() {
		return typ;
	}

	/** Methode fuer den Zugriff auf die Ziel-MAC-Adresse */
	public String getZielMacAdresse() {
		return zielMacAdresse;
	}

	public String toString() {
		return "[" + "src=" + quellMacAdresse + ", " + "dest=" + zielMacAdresse + ", " + "type=" + typ
				+ (daten != null ? " | " + daten : "") + "]";
	}

	public void setPreviousFrame(EthernetFrame frame) {
		this.previousFrame = frame;
	}

	public void setNextFrame(EthernetFrame frame) {
		this.nextFrame = frame;
	}

	public EthernetFrame getPreviousFrame() {
		return this.previousFrame;
	}

	public EthernetFrame getNextFrame() {
		return this.nextFrame;
	}
	
    @Override
    public boolean equals(Object o) {
 
        if (o == this) {
            return true;
        }
 
        if (!(o instanceof EthernetFrame)) {
            return false;
        }
        
        EthernetFrame frame = (EthernetFrame) o;
        
        if (frame.zielMacAdresse.equals(this.zielMacAdresse) &&
        		frame.quellMacAdresse.equals(this.quellMacAdresse) &&
        		frame.typ.equals(this.typ)) {
        	
        	Object daten = frame.getDaten();
        	if (frame.getTyp().equals(IP)) {
        		if (!(daten instanceof IcmpPaket)) {
        		
	        		IpPaket ipPacket1 = (IpPaket) frame.getDaten();
	        		IpPaket ipPacket2 = (IpPaket) this.daten;
	        		return ipPacket1.equals(ipPacket2);
        		}
        		else {
        			IcmpPaket icmpPacket1 = (IcmpPaket) frame.getDaten();
	        		IcmpPaket icmpPacket2 = (IcmpPaket) this.daten;
	        		return icmpPacket1.equals(icmpPacket2);
        		}
        	}
        	else if (frame.getTyp().equals(ARP)) {
        		ArpPaket arpPaket1 = (ArpPaket) frame.getDaten();
        		ArpPaket arpPaket2 = (ArpPaket) this.daten;
        		return arpPaket1.equals(arpPaket2);
        	}
        	else {
        		return false;
        	}
        }
        else {
        	return false;
        }
    }
}
