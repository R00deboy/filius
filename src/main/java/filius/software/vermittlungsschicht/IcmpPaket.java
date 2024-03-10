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

/** Diese Klasse umfasst die Attribute eines ICMP-Pakets */
@SuppressWarnings("serial")
public class IcmpPaket extends IpPaket {

    public static final int ICMP_PROTOCOL = 1;
    /**
     * sequence number of Echo Request packet: The sequence number can be used by the client to associate each echo
     * request with its reply.
     */
    private int seqNr;
    /**
     * 0: ICMP Echo Reply (pong) <br />
     * 3: see {@link #icmpType} <br />
     * 8: ICMP Echo Request (ping)<br />
     * 11: ICMP Time Exeeded (poof) <br />
     * else: ICMP unknown
     */
    private int icmpType;
    /**
     * 0: "ICMP Network Unreachable <br />
     * 1: ICMP Host Unreachable <br />
     * other: ICMP Destination Unreachable
     */
    private int icmpCode;

    public IcmpPaket() {
        super(ICMP_PROTOCOL);
    }

    @Override
    public IcmpPaket clone() {
        IcmpPaket clone = new IcmpPaket();
        copyIpPacketAttributes(clone);
        copyIcmpAttributes(clone);
        return clone;
    }

    private void copyIcmpAttributes(IcmpPaket clone) {
        clone.seqNr = seqNr;
        clone.icmpType = icmpType;
        clone.icmpCode = icmpCode;
    }

    public void setIcmpType(int type) {
        this.icmpType = type;
    }

    public void setIcmpCode(int code) {
        this.icmpCode = code;
    }

    public int getIcmpType() {
        return icmpType;
    }

    public int getIcmpCode() {
        return icmpCode;
    }

    public boolean isEchoResponse() {
        return icmpType == ICMP.TYPE_ECHO_REPLY && icmpCode == ICMP.CODE_ECHO;
    }

    public boolean isEchoRequest() {
        return icmpType == ICMP.TYPE_ECHO_REQUEST && icmpCode == ICMP.CODE_ECHO;
    }

    public int getSeqNr() {
        return this.seqNr;
    }

    public void setSeqNr(int seqNr) {
        this.seqNr = seqNr;
    }

    public String toString() {
        return "[" + "sender=" + getSender() + "; " + "recipient=" + getEmpfaenger() + "; " + "ttl=" + getTtl() + "; "
                + "seqNr=" + seqNr + "; " + "icmpType=" + icmpType + "; " + "icmpCode=" + icmpCode + "]";
    }
    
	/**
	 * Diese Methode prüft, ob ein IcmpPaket bis auf TTL dasselbe ist (erster
	 * Eintrag) und ob TTL übereinstimmt (zweiter Eintrag).
	 * 
	 * @author Christoph Irniger
	 */
	public boolean[] checkContentAndTTL(IcmpPaket packet) {
		boolean[] boolArray = new boolean[2];
		
		// Inhalt checken
        boolArray[0] = (this.getSender().equals(packet.getSender()) &&
        		this.getEmpfaenger().equals(packet.getEmpfaenger()) &&
        		this.getProtocol() == packet.getProtocol() &&
				this.getSeqNr() == packet.getSeqNr() &&
				this.getIcmpType() == packet.getIcmpType() &&
				this.getIcmpCode() == packet.getIcmpCode());
		// TTL checken
		boolArray[1] = (this.getTtl() == packet.getTtl());
		return boolArray;
	}
	
	/**
	 * Gleich, wenn darunterliegende IpPakete gleich sind und wenn seqNr, icmpType
	 * und icmpCode gleich sind.
	 * 
	 * @author Christoph Irniger
	 */
	@Override
	public boolean gleicherInhalt(Object o) {

		if (o == this) {
			return true;
		}

		if (!(o instanceof IcmpPaket)) {
			return false;
		}

		// Zuerst Oberklasse checken
		if (o instanceof IpPaket) {
			IpPaket ipPacket = (IpPaket) o;

        	if (super.gleicherInhalt(ipPacket)) {
        		
        		// Dann Rest
        		if (o instanceof IcmpPaket) {
        			IcmpPaket icmpPacket = (IcmpPaket) o;
        			
        			return this.getSeqNr() == icmpPacket.getSeqNr() &&
        					this.getIcmpType() == icmpPacket.getIcmpType() &&
							this.getIcmpCode() == icmpPacket.getIcmpCode();
                	}
        		else {
        			return false;
        		}
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
