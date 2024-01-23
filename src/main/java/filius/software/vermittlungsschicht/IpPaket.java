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
package filius.software.vermittlungsschicht;

import java.io.Serializable;

import filius.software.netzzugangsschicht.EthernetFrame;
import filius.software.transportschicht.Segment;
import filius.software.transportschicht.TcpSegment;
import filius.software.transportschicht.UdpSegment;

/**
 * Diese Klasse umfasst die Attribute bzw. Felder eines IP-Pakets
 */
@SuppressWarnings("serial")
public class IpPaket implements Serializable, Cloneable {

    public static final int UDP = 17, TCP = 6;

    /** IP-Adresse des sendenden Knotens */
    private String sender;
    private String empfaenger;
    /** Time-to-Live */
    private int ttl;
    private final int protocol;
    private Segment data;
    
    /** Rückverfolgung für die Schichten-Visualisierung */
    private EthernetFrame unpackedFromFrame;

    public IpPaket(int protocol) {
        this.protocol = protocol;
    }

    @Override
    public IpPaket clone() {
        IpPaket clone = new IpPaket(protocol);
        copyIpPacketAttributes(clone);
        return clone;
    }

    void copyIpPacketAttributes(IpPaket clone) {
        clone.ttl = ttl;
        clone.empfaenger = empfaenger;
        clone.sender = sender;
        clone.data = data;
        clone.unpackedFromFrame = unpackedFromFrame;
    }

    public String getEmpfaenger() {
        return empfaenger;
    }

    public void setEmpfaenger(String empfaenger) {
        this.empfaenger = empfaenger;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public int getProtocol() {
        return protocol;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public Segment getSegment() {
        return data;
    }

    public void setSegment(Segment data) {
        this.data = data;
    }

    public String toString() {
        return "[" + "ttl=" + ttl + ", " + "protocol=" + protocol + ", " + "empfaenger=" + empfaenger + ", " + "sender="
                + sender + "]";
    }

    public void decrementTtl() {
        ttl--;
    }
    
    public void setUnpackedFromFrame(EthernetFrame frame) {
    	this.unpackedFromFrame = frame;
    }
    
    public EthernetFrame getUnpackedFromFrame() {
    	return unpackedFromFrame;
    }
    
	/**
	 * Diese Methode prüft, ob ein IpPaket bis auf TTL dasselbe ist (erster
	 * Eintrag) und ob TTL übereinstimmt (zweiter Eintrag).
	 * 
	 * @author Christoph Irniger
	 */
	public boolean[] checkContentAndTTL(IpPaket packet) {
		boolean[] boolArray = new boolean[2];
		
		// --Payload checken--
		// Header checken
        if (this.getSender().equals(packet.getSender()) &&
        		this.getEmpfaenger().equals(packet.getEmpfaenger()) &&
        		this.getProtocol() == packet.getProtocol()) {
        	
			// Daten checken
			Segment segment1 = packet.getSegment();
			Segment segment2 = this.getSegment();
			if (segment1 instanceof TcpSegment && segment2 instanceof TcpSegment) {
				boolArray[0] = ((TcpSegment) segment1).gleicherInhalt((TcpSegment) segment2);
			}
			else if (segment1 instanceof UdpSegment && segment2 instanceof UdpSegment) {
				boolArray[0] = ((UdpSegment) segment1).gleicherInhalt((UdpSegment) segment2);
			}
			else {
				boolArray[0] = false;
			}
		} else {
			boolArray[0] = false;
		}
		// --TTL checken--
		boolArray[1] = (this.getTtl() == packet.getTtl());

		return boolArray;
	}
	
	/**
	 * Gleich, wenn Sender, Empfänger, TTL, Protokoll und Daten gleich sind.
	 * 
	 * @author Christoph Irniger
	 */
	public boolean gleicherInhalt(Object o) {

		if (o == this) {
			return true;
		}

		if (!(o instanceof IpPaket)) {
			return false;
		}
		IpPaket packet = (IpPaket) o;
        
		boolean[] boolArray = this.checkContentAndTTL(packet);
		return boolArray[0] && boolArray[1];
    }
}
