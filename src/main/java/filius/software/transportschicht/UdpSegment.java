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
package filius.software.transportschicht;

import java.io.Serializable;

import filius.software.vermittlungsschicht.IcmpPaket;
import filius.software.vermittlungsschicht.IpPaket;

public class UdpSegment extends Segment implements Serializable {

	private static final long serialVersionUID = 1L;

	private int laenge;

	public int getLaenge() {
		return laenge;
	}

	public void setLaenge(int laenge) {
		this.laenge = laenge;
	}
	
	/**
	 * Equal if subjacent segments are equal and if laenge is equal.
	 * 
	 * @author Christoph Irniger
	 */
	@Override
	public boolean sameContent(Object o) {

		if (o == this) {
			return true;
		}

		if (!(o instanceof UdpSegment)) {
			return false;
		}

		// check super class first
		if (o instanceof Segment) {
			Segment segment = (Segment) o;

			if (super.sameContent(segment)) {

				// then rest
				if (o instanceof UdpSegment) {
					UdpSegment udpSegment = (UdpSegment) o;

					return this.getLaenge() == udpSegment.getLaenge();
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
