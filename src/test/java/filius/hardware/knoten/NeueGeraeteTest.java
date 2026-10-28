package filius.hardware.knoten;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.Test;

import filius.software.drucken.DruckServer;
import filius.software.system.Betriebssystem;
import filius.software.system.SwitchFirmware;

public class NeueGeraeteTest {

    @Test
    public void testSmartphone_IstEndgeraetMitWlan() {
        Smartphone smartphone = new Smartphone();

        assertTrue(smartphone instanceof Host);
        assertTrue(smartphone.getSystemSoftware() instanceof Betriebssystem);
        assertTrue(smartphone.getSystemSoftware().wireless());
        assertEquals(1, smartphone.getNetzwerkInterfaces().size());
    }

    @Test
    public void testSmartphone_EigenerTyp() {
        assertNotEquals(Notebook.TYPE, Smartphone.TYPE);
        assertEquals(Smartphone.TYPE, new Smartphone().holeHardwareTyp());
    }

    @Test
    public void testDrucker_DruckdienstIstInstalliertUndAktiv() {
        Drucker drucker = new Drucker();

        DruckServer server = drucker.holeDruckServer();
        assertNotNull(server);
        assertTrue(server.isAktiv());
        assertEquals(9100, server.getPort());
        assertFalse(drucker.getSystemSoftware().wireless());
        assertEquals(Drucker.TYPE, drucker.holeHardwareTyp());
    }

    @Test
    public void testAccessPoint_IstSwitchMitSsid() {
        AccessPoint ap = new AccessPoint();

        assertTrue(ap instanceof Switch);
        assertTrue(ap.getSystemSoftware() instanceof SwitchFirmware);
        assertNotNull(((SwitchFirmware) ap.getSystemSoftware()).getSSID());
        assertNotEquals(Switch.TYPE, AccessPoint.TYPE);
        assertEquals(AccessPoint.STANDARD_REICHWEITE, ap.getReichweite());
        assertEquals(16, ap.holeAnzahlAnschluesse());
    }

    @Test
    public void testAccessPoint_InReichweite() {
        AccessPoint ap = new AccessPoint();
        ap.setReichweite(100);

        assertTrue(ap.inReichweite(0, 0));
        assertTrue(ap.inReichweite(60, 80));
        assertFalse(ap.inReichweite(60, 81));
        assertFalse(ap.inReichweite(-150, 0));
    }

    @Test
    public void testUebersetzungen_UmlauteKorrektKodiert() {
        ResourceBundle de = ResourceBundle.getBundle("filius.messages.MessagesBundle", Locale.GERMANY);
        ResourceBundle fr = ResourceBundle.getBundle("filius.messages.MessagesBundle", Locale.FRANCE);
        ResourceBundle en = ResourceBundle.getBundle("filius.messages.MessagesBundle", Locale.UK);

        assertEquals("Druckauftrag wurde übertragen.", de.getString("sw_druckclient_msg2"));
        assertEquals("Alle Aufträge löschen", de.getString("druckauftragviewer_msg5"));
        assertEquals("Point d'accès (Wi-Fi)", fr.getString("hw_accesspoint_msg1"));
        assertEquals("Printer", en.getString("hw_drucker_msg1"));
    }

    @Test
    public void testAccessPoint_NegativeReichweiteWirdNull() {
        AccessPoint ap = new AccessPoint();
        ap.setReichweite(-5);

        assertEquals(0, ap.getReichweite());
        assertTrue(ap.inReichweite(0, 0));
        assertFalse(ap.inReichweite(1, 0));
    }
}
