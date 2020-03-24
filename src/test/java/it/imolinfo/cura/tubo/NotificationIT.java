package it.imolinfo.cura.tubo;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NotificationIT extends AbstractIT {

    private final File cartella = new File(OUTBOX_DIR, "cartella");

    public NotificationIT() {
    }

    @Before
    public void mkDirCartella() throws IOException {
        if (!cartella.exists() && !cartella.mkdir()) {
            throw new IOException("Impossibile creare la directory " + cartella);
        }
    }

    @Test
    public void testNotification() throws IOException {
        final File notification = new File(NOTIFICATION_DIR, "testNotification.bin");

        try (FileOutputStream file = new FileOutputStream(new File(cartella, "testNotification.bin"))) {
            final byte[] content = new byte[10_000_000];

            new Random().nextBytes(content);
            file.write(content);
        }
        waitForEmptyDirectory(cartella);

        assertTrue("File di notifica " + notification + " non trovato", notification.exists());
        assertEquals("File di notifica non vuoto", 0, notification.length());

        // XXX Queste verifiche andrebbero in un altro test, dedicato
        assertFileExists(new File(INBOX_DIR, "cartella/testNotification.bin"));
        assertFileExists(new File(ARCHIVE_DIR, "cartella/testNotification.bin"));
        assertEquals("La directory " + ERRORS_DIR + " non e' vuota", 0,
                     listFiles(ERRORS_DIR).length);
    }
}
