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
	 * Checks if the whole content (which consists of zielMacAdresse,
	 * quellMacAdresse and daten) of <i>this</i> frame is equal to the frame in the
	 * argument (generally given as an Object).
	 * 
	 * @param o frame to be compared to <i>this</i> frame
	 * @return true if zielMacAdresse, quellMacAdresse and daten are equal
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

				// check payload
				if (frame.getTyp().equals("0x800")) {
					return samePayload(frame, true, false, 0);
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

	/**
	 * This method checks wheter the frame in the first argument has the same
	 * payload as <i>this</i> frame and if desired, checks if it has the specified
	 * TTL. Only works for frames of type IP.
	 * 
	 * @param frame              frame to be compared to <i>this</i> frame
	 * @param requireSameTTL     wheter the TTL of <i>this</i> frame in the first
	 *                           argument should be equal to {@code ttl} in the
	 *                           third argument (if true, the next two arguments do
	 *                           not have any effect)
	 * @param requireSpecificTTL wheter the TTL of the frame in the first argument
	 *                           should be equal to {@code ttl} in the third
	 *                           argument (if false, the next argument does not have
	 *                           any effect)
	 * @param ttl                desired TTL of frame in the first argument
	 * @return true if the frame in the argument has the same payload as <i>this</i>
	 *         frame considerung the specified conditions
	 * @author Christoph Irniger
	 */
	public boolean samePayload(EthernetFrame frame, boolean requireSameTTL, boolean requireSpecificTTL, int ttl) {
		Object packet1 = this.getDaten();
		Object packet2 = frame.getDaten();

		if (packet1 instanceof IpPaket && packet2 instanceof IpPaket) {
			IpPaket ipPacket1 = (IpPaket) packet1;
			IpPaket ipPacket2 = (IpPaket) packet2;
			boolean sameContentApartFromTTL;
			if (!(packet1 instanceof IcmpPaket) && !(packet2 instanceof IcmpPaket)) {
				sameContentApartFromTTL = ipPacket1.sameContentApartFromTTL(ipPacket2);
			} else if (packet1 instanceof IcmpPaket && packet2 instanceof IcmpPaket) {
				IcmpPaket icmpPacket1 = (IcmpPaket) packet1;
				IcmpPaket icmpPacket2 = (IcmpPaket) packet2;
				sameContentApartFromTTL = icmpPacket1.sameContentApartFromTTL(icmpPacket2);
			} else {
				return false;
			}
			if (requireSameTTL) {
				return sameContentApartFromTTL && ipPacket1.getTtl() == ipPacket2.getTtl();
			} else if (requireSpecificTTL) {
				return sameContentApartFromTTL && ipPacket2.getTtl() == ttl;
			} else {
				return sameContentApartFromTTL;
			}
		} else {
			return false;
		}
	}
}
