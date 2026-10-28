package filius.rahmenprogramm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import filius.gui.netzwerksicht.GUIDocuItem;
import filius.gui.netzwerksicht.GUIKabelItem;
import filius.gui.netzwerksicht.GUIKnotenItem;
import filius.gui.netzwerksicht.JSidebarButton;
import filius.hardware.Kabel;
import filius.hardware.knoten.AccessPoint;
import filius.hardware.knoten.Drucker;
import filius.hardware.knoten.Knoten;
import filius.hardware.knoten.Rechner;
import filius.hardware.knoten.Smartphone;
import filius.software.drucken.DruckClient;
import filius.software.drucken.DruckServer;
import filius.software.system.Betriebssystem;
import filius.software.system.SwitchFirmware;

/** Speichern und Laden von Projekten mit Smartphone, Drucker und Access Point */
public class SzenarioVerwaltungNeueGeraeteTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void testSpeichernUndLaden_NeueGeraete() throws Exception {
        Smartphone smartphone = new Smartphone();
        ((Betriebssystem) smartphone.getSystemSoftware()).setzeIPAdresse("192.168.0.10");
        ((Betriebssystem) smartphone.getSystemSoftware()).setSsid("Schule");
        Drucker drucker = new Drucker();
        ((Betriebssystem) drucker.getSystemSoftware()).setzeIPAdresse("192.168.0.20");
        AccessPoint ap = new AccessPoint();
        ap.setReichweite(321);
        ((SwitchFirmware) ap.getSystemSoftware()).setSSID("Schule");
        Rechner rechner = new Rechner();
        Betriebssystem rechnerOs = (Betriebssystem) rechner.getSystemSoftware();
        rechnerOs.installApp(DruckClient.class.getCanonicalName(), false);
        ((DruckClient) rechnerOs.holeSoftware(DruckClient.class.getCanonicalName()))
                .setDruckerAdresse("192.168.0.20");

        List<GUIKnotenItem> knoten = new ArrayList<>();
        GUIKnotenItem smartphoneItem = item(smartphone, 10, 10);
        GUIKnotenItem apItem = item(ap, 100, 10);
        knoten.add(smartphoneItem);
        knoten.add(item(drucker, 200, 10));
        knoten.add(apItem);
        knoten.add(item(rechner, 300, 10));

        Kabel funk = new Kabel(smartphone.getNetzwerkInterfaces().get(0).getPort(), ap.holeFreienPort());
        funk.setWireless(true);
        GUIKabelItem kabelItem = new GUIKabelItem();
        kabelItem.getKabelpanel().setZiel1(smartphoneItem);
        kabelItem.getKabelpanel().setZiel2(apItem);
        kabelItem.setDasKabel(funk);
        List<GUIKabelItem> kabel = new ArrayList<>();
        kabel.add(kabelItem);

        String datei = new File(tmp.getRoot(), "neue_geraete.fls").getAbsolutePath();
        assertTrue(SzenarioVerwaltung.getInstance().speichern(datei, knoten, kabel, new ArrayList<GUIDocuItem>()));

        List<GUIKnotenItem> geladeneKnoten = new ArrayList<>();
        List<GUIKabelItem> geladeneKabel = new ArrayList<>();
        assertTrue(SzenarioVerwaltung.getInstance().laden(datei, geladeneKnoten, geladeneKabel,
                new ArrayList<GUIDocuItem>()));

        assertEquals(4, geladeneKnoten.size());
        Smartphone geladenesSmartphone = finde(geladeneKnoten, Smartphone.class);
        Drucker geladenerDrucker = finde(geladeneKnoten, Drucker.class);
        AccessPoint geladenerAp = finde(geladeneKnoten, AccessPoint.class);
        Rechner geladenerRechner = finde(geladeneKnoten, Rechner.class);

        Betriebssystem smartphoneOs = (Betriebssystem) geladenesSmartphone.getSystemSoftware();
        assertEquals("192.168.0.10", smartphoneOs.primaryIPAdresse());
        assertTrue(smartphoneOs.wireless());
        assertEquals("Schule", smartphoneOs.getSsid());

        DruckServer server = geladenerDrucker.holeDruckServer();
        assertNotNull(server);
        assertTrue(server.isAktiv());
        assertEquals(9100, server.getPort());
        assertEquals("192.168.0.20", ((Betriebssystem) geladenerDrucker.getSystemSoftware()).primaryIPAdresse());

        assertEquals(321, geladenerAp.getReichweite());
        assertEquals("Schule", ((SwitchFirmware) geladenerAp.getSystemSoftware()).getSSID());
        assertEquals(AccessPoint.TYPE, geladeneKnoten.get(2).getImageLabel().getTyp());

        DruckClient client = (DruckClient) ((Betriebssystem) geladenerRechner.getSystemSoftware())
                .holeSoftware(DruckClient.class.getCanonicalName());
        assertNotNull(client);
        assertEquals("192.168.0.20", client.getDruckerAdresse());

        assertEquals(1, geladeneKabel.size());
        assertTrue(geladeneKabel.get(0).getDasKabel().getWireless());
    }

    @Test
    public void testSpeichernUndLaden_SmartphoneMitKabel() throws Exception {
        Smartphone smartphone = new Smartphone();
        ((Betriebssystem) smartphone.getSystemSoftware()).nicWireless(false);
        List<GUIKnotenItem> knoten = new ArrayList<>();
        knoten.add(item(smartphone, 10, 10));

        String datei = new File(tmp.getRoot(), "smartphone_lan.fls").getAbsolutePath();
        assertTrue(SzenarioVerwaltung.getInstance().speichern(datei, knoten, new ArrayList<GUIKabelItem>(),
                new ArrayList<GUIDocuItem>()));
        List<GUIKnotenItem> geladeneKnoten = new ArrayList<>();
        assertTrue(SzenarioVerwaltung.getInstance().laden(datei, geladeneKnoten, new ArrayList<GUIKabelItem>(),
                new ArrayList<GUIDocuItem>()));

        assertFalse(finde(geladeneKnoten, Smartphone.class).getSystemSoftware().wireless());
    }

    @Test
    public void testLaden_BestehendesProjekt() throws Exception {
        URL resource = getClass().getResource("/einfaches_rechnernetz_komplett.fls");
        List<GUIKnotenItem> geladeneKnoten = new ArrayList<>();
        List<GUIKabelItem> geladeneKabel = new ArrayList<>();

        assertTrue(SzenarioVerwaltung.getInstance().laden(new File(resource.toURI()).getAbsolutePath(),
                geladeneKnoten, geladeneKabel, new ArrayList<GUIDocuItem>()));

        assertFalse(geladeneKnoten.isEmpty());
        assertFalse(geladeneKabel.isEmpty());
    }

    private static GUIKnotenItem item(Knoten knoten, int x, int y) {
        GUIKnotenItem item = new GUIKnotenItem();
        item.setKnoten(knoten);
        JSidebarButton label = new JSidebarButton();
        label.setTyp(knoten.holeHardwareTyp());
        label.setBounds(x, y, 60, 60);
        item.setImageLabel(label);
        return item;
    }

    private static <T extends Knoten> T finde(List<GUIKnotenItem> items, Class<T> typ) {
        for (GUIKnotenItem item : items) {
            if (typ.isInstance(item.getKnoten())) {
                return typ.cast(item.getKnoten());
            }
        }
        throw new AssertionError("Kein Knoten vom Typ " + typ.getSimpleName());
    }
}
