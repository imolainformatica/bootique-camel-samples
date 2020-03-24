package it.imolinfo.cura.tubo;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;

public class ErrorIT extends AbstractIT {

	public ErrorIT() {
	}

	@Test
	public void testError() throws Exception {
		FileUtils.copyFileToDirectory(new File("pom.xml"), OUTBOX_TO_NOWHERE_DIR);
		waitForEmptyDirectory(OUTBOX_TO_NOWHERE_DIR);

		assertFileExists(new File(ERRORS_DIR, "pom.xml"));
	}
}
