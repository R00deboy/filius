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

	private Set<String> readByLauscherForMac = new HashSet<>();

	/** Konstruktor zur Initialisierung der Attribute des Frames */
	public EthernetFrame(Object daten, String quellMacAdresse, String zielMacAdresse, String typ) {
		LOG.trace("INVOKED (" + this.hashCode() + ") " + getClass() + " (EthernetFrame), constr: EthernetFrame(" + daten
				+ "," + quellMacAdresse + "," + zielMacAdresse + "," + typ + ")");
		this.zielMacAdresse = zielMacAdresse;
		this.quellMacAdresse = quellMacAdresse;
		this.typ = typ;
		this.daten = daten;
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

	/**
	 * Diese Methode prüft, ob ein Frame vom Typ IP denselben Payload, aber eine
	 * unterschiedliche TTL hat.
	 * 
	 * @author Christoph Irniger
	 */
	public boolean gleicherInhaltAndereTTL(EthernetFrame frame) {
		boolean[] boolArray = checkPayloadAndTTL(frame);
		return boolArray[0] && !boolArray[1];
	}

	/**
	 * Diese Methode prüft, ob ein Frame des Typs IP senselben Payload bis auf TTL
	 * hat (erster Eintrag) und ob TTL übereinstimmt (zweiter Eintrag).
	 * 
	 * @author Christoph Irniger
	 */
	private boolean[] checkPayloadAndTTL(EthernetFrame frame) {
		Object daten = frame.getDaten();
		boolean[] boolArray = new boolean[2];
		
		if (!(daten instanceof IcmpPaket)) {
			IpPaket ipPacket1 = (IpPaket) daten;
			IpPaket ipPacket2 = (IpPaket) this.getDaten();
			boolArray = ipPacket1.checkContentAndTTL(ipPacket2);
		} else {
			IcmpPaket icmpPacket1 = (IcmpPaket) daten;
			IcmpPaket icmpPacket2 = (IcmpPaket) this.getDaten();
			boolArray = icmpPacket1.checkContentAndTTL(icmpPacket2);
		}
		return boolArray;
	}

	/**
	 * Gleich, wenn Sender, Empfänger, Typ und Daten gleich.
	 * 
	 * @author Christoph Irniger
	 */
	public boolean gleicherInhalt(Object o) {

		if (o == this) {
			return true;
		}

		if (!(o instanceof EthernetFrame)) {
			return false;
		}

		if (o instanceof EthernetFrame) {
			EthernetFrame frame = (EthernetFrame) o;

			// Header checken
			if (this.getZielMacAdresse().equals(frame.getZielMacAdresse()) &&
					this.getQuellMacAdresse().equals(frame.getQuellMacAdresse()) &&
					this.getTyp().equals(frame.getTyp())) {

				// Daten checken
				if (frame.getTyp().equals("0x800")) {
					boolean[] boolArray = checkPayloadAndTTL(frame);
					// Payload und TTL müssen übereinstimmen
					return boolArray[0] && boolArray[1];
				} else if (frame.getTyp().equals("0x806")) {
					ArpPaket arpPaket1 = (ArpPaket) frame.getDaten();
					ArpPaket arpPaket2 = (ArpPaket) this.getDaten();
					return arpPaket1.gleicherInhalt(arpPaket2);
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}
