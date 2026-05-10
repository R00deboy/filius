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
package filius.software.dhcp;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import filius.exception.NoValidDhcpResponseException;
import filius.exception.TimeOutException;
import filius.exception.VerbindungsException;
import filius.gui.GUIContainer;
import filius.gui.netzwerksicht.GUIKnotenItem;
import filius.hardware.Verbindung;
import filius.hardware.knoten.Knoten;
import filius.software.clientserver.ClientAnwendung;
import filius.software.system.Betriebssystem;
import filius.software.system.InternetKnotenBetriebssystem;
import filius.software.system.SystemSoftware;
import filius.software.transportschicht.UDPSocket;
import filius.software.vermittlungsschicht.ARP;

import static filius.software.dhcp.DHCPClient.State.*;

public class DHCPClient extends ClientAnwendung {
    private static Logger LOG = LoggerFactory.getLogger(DHCPClient.class);

    enum State {
        DHCP_ASSIGN_IP, SUCCEED, DHCP_DISCOVER, DHCP_REQUEST, DHCP_VALIDATE, DHCP_DECLINE, DHCP_INIT,
        LINK_LOCAL_INIT, LINK_LOCAL_VALIDATE, LINK_LOCAL_ANNOUNCE
    }

    private static final String IP_ADDRESS_CURRENT_NETWORK = "0.0.0.0";
    private static final int DHCP_MAX_ERROR_COUNT = 5;
    private static final int LINK_LOCAL_MAX_ERROR_COUNT = 5;

    public static final String LINK_LOCAL_ADDRESS_PREFIX = "169.254.1.";
    public static final String LINK_LOCAL_SUBNET_MASK = "255.255.0.0";

    public void starten() {
        LOG.debug(
            "INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass() + " (DHCPClient), starten()");
        super.starten();

        ausfuehren("configure", null);
    }

    /**
     * es muss gewaehrleistet werden, dass der DHCP-Server bereits gestartet worden ist! (sofern denn einer als "aktiv"
     * gekennzeichnet ist!)
     */
    void waitUntilDhcpServerStarted() {
        boolean activeDHCPserversStarted = false;
        try {
            while (!activeDHCPserversStarted && running) {
                activeDHCPserversStarted = true;
                for (DHCPServer dhcpServer : getDHCPServers()) {
                    if (dhcpServer.isAktiv() && !dhcpServer.isStarted()) {
                        activeDHCPserversStarted = false;
                        LOG.debug("DHCP client is waiting for DHCP server on '"
                            + dhcpServer.getSystemSoftware().getKnoten().holeAnzeigeName()
                            + "'. Server has NOT yet started.");
                        break;
                    }
                }
                Thread.sleep(100);
            }
            LOG.debug("DHCP client on '" + getSystemSoftware().getKnoten().holeAnzeigeName()
                + "' finished check for DHCP server.");
        } catch (InterruptedException e) {
            LOG.debug("", e);
        }
    }

    private List<DHCPServer> getDHCPServers() {
        SystemSoftware syssoft;
        List<DHCPServer> activeDHCPServers = new ArrayList<DHCPServer>();
        for (GUIKnotenItem knotenItem : GUIContainer.getGUIContainer().getKnotenItems()) {
            syssoft = knotenItem.getKnoten().getSystemSoftware();
            if (syssoft instanceof Betriebssystem) {
                if (((Betriebssystem) syssoft).getDHCPServer().isAktiv()) {
                    activeDHCPServers.add(((Betriebssystem) syssoft).getDHCPServer());
                }
            }
        }
        return activeDHCPServers;
    }

    public void configure() {
        LOG.trace("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
            + " (DHCPClient), starteDatenaustausch()");
        int failureCount = 0;
        State zustand = DHCP_INIT;

        String oldIpAddress = resetIpConfig();
        updateOberserver();

        IpConfig config = null;
        UDPSocket udpSocket = null;
        InternetKnotenBetriebssystem operatingSystem = (InternetKnotenBetriebssystem) getSystemSoftware();
        while (zustand != SUCCEED && running) {
            try {
                switch (zustand) {
                    case DHCP_INIT:
                        waitUntilDhcpServerStarted();
                        udpSocket = initUdpSocket();
                        zustand = DHCP_DISCOVER;
                        break;
                    case DHCP_DISCOVER:
                        config = discover(udpSocket, operatingSystem.dhcpEnabledMACAddress(), Verbindung.holeRTT());
                        zustand = DHCP_VALIDATE;
                        break;
                    case DHCP_VALIDATE:
                        boolean validAddress = validateOfferedAddress(getSystemSoftware().holeARP(), config.getIpAddress());
                        zustand = validAddress ? DHCP_REQUEST : DHCP_DECLINE;
                        break;
                    case DHCP_DECLINE:
                        decline(udpSocket, operatingSystem.dhcpEnabledMACAddress(), config, Verbindung.holeRTT());
                        zustand = DHCP_DISCOVER;
                        break;
                    case DHCP_REQUEST:
                        boolean acknowledged = request(udpSocket, operatingSystem.dhcpEnabledMACAddress(), config,
                            Verbindung.holeRTT());
                        zustand = acknowledged ? DHCP_ASSIGN_IP : DHCP_DISCOVER;
                        break;
                    case DHCP_ASSIGN_IP:
                        operatingSystem.setzeIPAdresse(config.getIpAddress());
                        operatingSystem.setzeNetzmaske(config.getSubnetMask());
                        operatingSystem.setStandardGateway(config.getGateway());
                        operatingSystem.setDNSServer(config.getDnsServer());
                        zustand = SUCCEED;
                        LOG.info("New ip configuration via DHCP: {} / {}, gateway: {}, dns: {}",
                            config.getIpAddress(), config.getSubnetMask(), config.getGateway(), config.getDnsServer());
                        break;
                    case LINK_LOCAL_INIT:
                        config = selectArbitraryLinkLocalAddress(operatingSystem);
                        zustand = LINK_LOCAL_VALIDATE;
                        break;
                    case LINK_LOCAL_VALIDATE:
                        boolean validLinkLocalAddress = validateOfferedAddress(getSystemSoftware().holeARP(), config.getIpAddress());
                        if (validLinkLocalAddress) {
                            operatingSystem.setzeIPAdresse(config.getIpAddress());
                            operatingSystem.setzeNetzmaske(config.getSubnetMask());
                            operatingSystem.setStandardGateway("");
                            zustand = LINK_LOCAL_ANNOUNCE;
                        } else {
                            failureCount++;
                            zustand = LINK_LOCAL_INIT;
                        }
                        break;
                    case LINK_LOCAL_ANNOUNCE:
                        operatingSystem.holeARP().sendArpReply(operatingSystem.primaryMACAddress(), operatingSystem.primaryIPAdresse(),
                            null, IP_ADDRESS_CURRENT_NETWORK, operatingSystem.primaryNetworkInterface());
                        zustand = SUCCEED;
                        LOG.info("New ip configuration via link local: {} / {}", config.getIpAddress(), config.getSubnetMask());
                        break;
                }
            } catch (NoValidDhcpResponseException | TimeOutException | VerbindungsException e) {
                failureCount++;

                if (failureCount == DHCP_MAX_ERROR_COUNT) {
                    LOG.info("Got no configuration via DHCP. Init Address with link local.");
                    zustand = LINK_LOCAL_INIT;
                } else if (failureCount == DHCP_MAX_ERROR_COUNT + LINK_LOCAL_MAX_ERROR_COUNT) {
                    LOG.info("Could not init link-local ip address.");
                    break;
                }
            }
        }
        udpSocket.schliessen();

        updateOberserver();
    }

    private IpConfig selectArbitraryLinkLocalAddress(InternetKnotenBetriebssystem operatingSystem) {
        long lastByte=0;
        for (int i=0; i<500 && lastByte==0; i++) {
             lastByte = Math.round(Math.random() * 253f) + 1;
             if (!operatingSystem.holeARP().holeARPTabelle(LINK_LOCAL_ADDRESS_PREFIX + lastByte).isEmpty()) {
                 lastByte = 0;
             }
        }
        return new IpConfig(LINK_LOCAL_ADDRESS_PREFIX + lastByte, LINK_LOCAL_SUBNET_MASK);
    }

    private void updateOberserver() {
        InternetKnotenBetriebssystem operatingSystem = (InternetKnotenBetriebssystem) getSystemSoftware();
        Knoten node = operatingSystem.getKnoten();
        node.benachrichtigeBeobachter();
        operatingSystem.benachrichtigeBeobacher(node);
    }

    private String resetIpConfig() {
        InternetKnotenBetriebssystem operatingSystem = (InternetKnotenBetriebssystem) getSystemSoftware();
        String oldIpAddress = operatingSystem.primaryIPAdresse();
        operatingSystem.setzeIPAdresse("0.0.0.0");
        return oldIpAddress;
    }

    UDPSocket initUdpSocket() throws VerbindungsException {
        socket = new UDPSocket(getSystemSoftware(), "255.255.255.255", 67, 68);
        ((UDPSocket) socket).verbinden();
        return (UDPSocket) socket;
    }

    boolean request(UDPSocket socket, String clientMacAddress, IpConfig config, long socketTimeoutMillis)
        throws NoValidDhcpResponseException, TimeOutException {
        socket.sendeBroadcast(IP_ADDRESS_CURRENT_NETWORK, DHCPMessage
            .createRequestMessage(clientMacAddress, config.getIpAddress(), config.getDhcpServer()).toString());

        DHCPMessage result = receiveResponse(socket, socketTimeoutMillis, clientMacAddress, config.getDhcpServer(),
            DHCPMessageType.ACK, DHCPMessageType.NACK);

        return null != result && result.getType() == DHCPMessageType.ACK;
    }

    void decline(UDPSocket socket, String clientMacAddress, IpConfig config, long socketTimeoutMillis)
        throws NoValidDhcpResponseException {
        socket.sendeBroadcast(IP_ADDRESS_CURRENT_NETWORK, DHCPMessage
            .createDeclineMessage(clientMacAddress, config.getIpAddress(), config.getDhcpServer()).toString());
    }

    IpConfig discover(UDPSocket socket, String clientMacAddress, long socketTimeoutMillis)
        throws NoValidDhcpResponseException, TimeOutException {
        socket.sendeBroadcast(IP_ADDRESS_CURRENT_NETWORK,
            DHCPMessage.createDiscoverMessage(clientMacAddress).toString());

        DHCPMessage offer = receiveResponse(socket, socketTimeoutMillis, clientMacAddress, null, DHCPMessageType.OFFER);

        return new IpConfig(offer.getYiaddr(), offer.getRouter(), offer.getSubnetMask(), offer.getDnsServer(),
            offer.getServerIdentifier());
    }

    private DHCPMessage receiveResponse(UDPSocket socket, long socketTimeoutMillis, String clientMacAddress,
                                        String serverIdentifier, DHCPMessageType... messageTypes)
        throws NoValidDhcpResponseException, TimeOutException {
        DHCPMessage responseMessage = null;
        long start = System.currentTimeMillis();
        long duration = 0;
        do {
            String response = socket.empfangen(socketTimeoutMillis - duration);
            if (null == response) {
                LOG.debug("{}: DHCP received empty response", getSystemSoftware().getKnoten().holeAnzeigeName());
                throw new TimeOutException("No response from DHCP server received.");
            }
            responseMessage = DHCPMessage.fromString(response);
            boolean invalidMessageType = !ArrayUtils.contains(messageTypes, responseMessage.getType());
            boolean forOtherClient = !clientMacAddress.equalsIgnoreCase(responseMessage.getChaddr());
            boolean fromOtherServer = null != serverIdentifier
                && !serverIdentifier.equals(responseMessage.getServerIdentifier());
            if (invalidMessageType || forOtherClient || fromOtherServer) {
                responseMessage = null;
            }
            duration = System.currentTimeMillis() - start;
        } while (null == responseMessage && duration < socketTimeoutMillis);
        if (null == responseMessage) {
            throw new NoValidDhcpResponseException("No valid server response received");
        }
        return responseMessage;
    }

    boolean validateOfferedAddress(ARP arp, String offeredAddress) {
        String macAddress = arp.holeARPTabellenEintrag(offeredAddress, 1, false);
        return null == macAddress;
    }
}
