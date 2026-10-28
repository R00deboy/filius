package filius.software.drucken;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class DruckServerTest {

    @Test
    public void testDrucken_AuftragWirdGespeichertUndGemeldet() {
        DruckServer server = new DruckServer();
        List<Object> meldungen = new ArrayList<>();
        server.hinzuBeobachter((o, arg) -> meldungen.add(arg));

        server.drucken("192.168.0.10", "Hallo Drucker");

        List<DruckAuftrag> auftraege = server.holeAuftraege();
        assertEquals(1, auftraege.size());
        assertEquals("192.168.0.10", auftraege.get(0).holeAbsender());
        assertEquals("Hallo Drucker", auftraege.get(0).holeText());
        assertEquals(1, meldungen.size());
        assertTrue(meldungen.get(0) instanceof DruckAuftrag);
    }

    @Test
    public void testHoleAuftraege_LiefertKopie() {
        DruckServer server = new DruckServer();
        server.drucken("192.168.0.10", "A");

        server.holeAuftraege().clear();

        assertEquals(1, server.holeAuftraege().size());
    }

    @Test
    public void testLoescheAuftraege() {
        DruckServer server = new DruckServer();
        server.drucken("192.168.0.10", "A");
        server.drucken("192.168.0.11", "B");

        server.loescheAuftraege();

        assertTrue(server.holeAuftraege().isEmpty());
    }

    @Test
    public void testStandardPort() {
        assertEquals(DruckServer.RAW_PRINT_PORT, new DruckServer().getPort());
        assertEquals(9100, DruckServer.RAW_PRINT_PORT);
    }
}
