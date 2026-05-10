package filius.software.dhcp;

public class IpConfig {
    private String ipAddress;
    private String gateway;
    private String subnetMask;
    private String dnsServer;
    private String dhcpServer;

    IpConfig(String ipAddress, String gateway, String subnetMask, String dnsServer, String dhcpServer) {
        this.ipAddress = ipAddress;
        this.gateway = gateway;
        this.subnetMask = subnetMask;
        this.dnsServer = dnsServer;
        this.dhcpServer = dhcpServer;
    }

    IpConfig(String ipAddress, String subnetMask) {
        this.ipAddress = ipAddress;
        this.subnetMask = subnetMask;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getGateway() {
        return gateway;
    }

    public String getSubnetMask() {
        return subnetMask;
    }

    public String getDnsServer() {
        return dnsServer;
    }

    public String getDhcpServer() {
        return dhcpServer;
    }
}
