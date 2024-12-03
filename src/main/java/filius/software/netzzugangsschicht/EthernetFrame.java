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
	 * This method checks wheter both frames have the same payload. If the second
	 * argument {@code TTLsHaveToBeDifferent} is true, it also checks, if the TTLs
	 * are different. Only works if this frame and the frame in the argument are of
	 * type IP ({@code typ} {@code IP}).
	 * 
	 * @param frame frame to be compared to this frame
	 * @return true if this frame and the frame in the argument have the same
	 *         payload and a different TTL, false otherwise
	 * @author Christoph Irniger
	 */
	public boolean samePayload(EthernetFrame frame, boolean TTLsHaveToBeDifferent) {
		boolean[] boolArray = checkPayloadAndTTL(frame);
		if (TTLsHaveToBeDifferent) {
			return boolArray[0] && !boolArray[1];
		} else {
			return boolArray[0];
		}
	}

	/**
	 * This method checks if a frame has the same payload except for TTL (first
	 * entry) and if the TTL is the same (second entry). Returns false if this frame
	 * or the frame in the argument is not of type IP ({@code typ} {@code IP}).
	 * 
	 * @param frame frame to be compared to this frame
	 * @return Array of boolean of length 2. If this frame or the frame in the
	 *         argument is not of type IP ({@code typ} {@code IP}), both entries are
	 *         false. Otherwise the first entry is true if both have the same
	 *         payload except for TTL and the second entry is true if both frames
	 *         have the same TTL.
	 * @author Christoph Irniger
	 */
	private boolean[] checkPayloadAndTTL(EthernetFrame frame) {
		Object daten = frame.getDaten();
		boolean[] boolArray = new boolean[2];

		if (daten instanceof IpPaket && getDaten() instanceof IpPaket) {
			if (!(daten instanceof IcmpPaket) && !(getDaten() instanceof IcmpPaket)) {
				IpPaket ipPacket1 = (IpPaket) daten;
				IpPaket ipPacket2 = (IpPaket) getDaten();
				boolArray = ipPacket1.checkPayloadAndTTL(ipPacket2);
			} else if (daten instanceof IcmpPaket && getDaten() instanceof IcmpPaket) {
				IcmpPaket icmpPacket1 = (IcmpPaket) daten;
				IcmpPaket icmpPacket2 = (IcmpPaket) this.getDaten();
				boolArray = icmpPacket1.checkPayloadAndTTL(icmpPacket2);
			} else {
				boolArray[0] = false;
				boolArray[1] = false;
			}
		} else {
			boolArray[0] = false;
			boolArray[1] = false;
		}
		return boolArray;
	}

	/**
	 * Checks if this frame is equal to the frame in the argument (generally given
	 * as an Object) regarding sender, receiver and data.
	 * 
	 * @param o frame that needs to be compared to this frame
	 * @return true if sender, receiver and data are equal
	 * @author Christoph Irniger
	 */
	public boolean sameContent(Object o) {

		if (o == this) {
			return true;
		}

		if (!(o instanceof EthernetFrame)) {
			return false;
		}

		if (o instanceof EthernetFrame) {
			EthernetFrame frame = (EthernetFrame) o;

			// check header
			if (this.getZielMacAdresse().equals(frame.getZielMacAdresse())
					&& this.getQuellMacAdresse().equals(frame.getQuellMacAdresse())
					&& this.getTyp().equals(frame.getTyp())) {

				// check data
				if (frame.getTyp().equals("0x800")) {
					boolean[] boolArray = checkPayloadAndTTL(frame);
					// payload and TTL have to be the same
					return boolArray[0] && boolArray[1];
				} else if (frame.getTyp().equals("0x806")) {
					ArpPaket arpPaket1 = (ArpPaket) frame.getDaten();
					ArpPaket arpPaket2 = (ArpPaket) this.getDaten();
					return arpPaket1.sameContent(arpPaket2);
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
