package filius.software.drucken;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import filius.hardware.Kabel;
import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.AccessPoint;
import filius.hardware.knoten.Drucker;
import filius.hardware.knoten.Host;
import filius.hardware.knoten.Rechner;
import filius.hardware.knoten.Smartphone;
import filius.software.system.Betriebssystem;
import filius.software.system.InternetKnotenBetriebssystem;
import filius.software.vermittlungsschicht.IcmpPaket;

/**
 * Szenario aus dem Unterricht: Ein Smartphone ist per WLAN mit einem Access Point verbunden, an dem per Kabel ein
 * Rechner und ein Drucker haengen.
 */
public class WlanUndDruckenIntegrationTest {

    private Smartphone smartphone;
    private Rechner rechner;
    private Drucker drucker;
    private AccessPoint accessPoint;

    @Before
    public void aufbauen() {
        smartphone = new Smartphone();
        rechner = new Rechner();
        drucker = new Drucker();
        accessPoint = new AccessPoint();
        konfiguriere(smartphone, "192.168.0.10");
        konfiguriere(rechner, "192.168.0.11");
        konfiguriere(drucker, "192.168.0.20");

        Kabel funk = new Kabel(nic(smartphone).getPort(), accessPoint.holeFreienPort());
        funk.setWireless(true);
        new Kabel(nic(rechner).getPort(), accessPoint.holeFreienPort());
        new Kabel(nic(drucker).getPort(), accessPoint.holeFreienPort());

        accessPoint.getSystemSoftware().starten();
        smartphone.getSystemSoftware().starten();
        rechner.getSystemSoftware().starten();
        drucker.getSystemSoftware().starten();
    }

    @After
    public void abbauen() {
        smartphone.getSystemSoftware().beenden();
        rechner.getSystemSoftware().beenden();
        drucker.getSystemSoftware().beenden();
        accessPoint.getSystemSoftware().beenden();
    }

    @Test(timeout = 60000)
    public void testSmartphonePingtRechnerUeberAccessPoint() throws Exception {
        IcmpPaket antwort = ((InternetKnotenBetriebssystem) smartphone.getSystemSoftware()).holeICMP()
                .startSinglePing("192.168.0.11", 4711, 1);

        assertNotNull(antwort);
        assertEquals("192.168.0.11", antwort.getSender());
    }

    @Test(timeout = 60000)
    public void testRechnerDrucktAufDrucker() throws Exception {
        Betriebssystem os = (Betriebssystem) rechner.getSystemSoftware();
        os.installApp(DruckClient.class.getCanonicalName(), true);
        DruckClient client = (DruckClient) os.holeSoftware(DruckClient.class.getCanonicalName());

        client.sendeDruckauftrag("192.168.0.20", "Arbeitsblatt Netzwerke");

        List<DruckAuftrag> auftraege = warteAufAuftraege(1);
        assertEquals(1, auftraege.size());
        assertEquals("192.168.0.11", auftraege.get(0).holeAbsender());
        assertEquals("Arbeitsblatt Netzwerke", auftraege.get(0).holeText());
    }

    private List<DruckAuftrag> warteAufAuftraege(int anzahl) throws InterruptedException {
        List<DruckAuftrag> auftraege = drucker.holeDruckServer().holeAuftraege();
        for (int i = 0; i < 100 && auftraege.size() < anzahl; i++) {
            Thread.sleep(100);
            auftraege = drucker.holeDruckServer().holeAuftraege();
        }
        return auftraege;
    }

    private static void konfiguriere(Host host, String ip) {
        Betriebssystem os = (Betriebssystem) host.getSystemSoftware();
        os.setzeIPAdresse(ip);
        os.setzeNetzmaske("255.255.255.0");
    }

    private static NetzwerkInterface nic(Host host) {
        return host.getNetzwerkInterfaces().get(0);
    }
}
