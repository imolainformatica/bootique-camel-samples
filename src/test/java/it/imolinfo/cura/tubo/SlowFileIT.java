package it.imolinfo.cura.tubo;

import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class SlowFileIT extends AbstractIT {

    private static final long FILE_SIZE = 10;

    public SlowFileIT() {
    }

    @Test
    public void testFileScrittoLentamente()
            throws IOException, InterruptedException {
        try (FileOutputStream file = new FileOutputStream(new File(OUTBOX_DIR, "slow.bin"))) {
            for (int i = 0; i < FILE_SIZE; ++i) {
                file.write(i);
                Thread.sleep(1000);
            }
        }
        waitForEmptyDirectory(OUTBOX_DIR);

        assertEquals(FILE_SIZE, new File(INBOX_DIR, "slow.bin").length());
        assertEquals(FILE_SIZE, new File(ARCHIVE_DIR, "slow.bin").length());
        assertEquals("La directory " + ERRORS_DIR + " non e' vuota", 0,
                     listFiles(ERRORS_DIR).length);
    }
}
