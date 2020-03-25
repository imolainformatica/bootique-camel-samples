package it.imolinfo.cura.tubo;

import io.bootique.BQRuntime;
import io.bootique.di.BQModule;
import io.bootique.test.junit.BQTestFactory;
import it.imolainformatica.bootique.camel.CamelModule;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import static org.junit.Assert.assertTrue;

public abstract class AbstractIT {

	protected static final File ARCHIVE_DIR             = new File("target/archive");
	protected static final File ERRORS_DIR              = new File("target/errors");
	protected static final File INBOX_DIR               = new File("target/inbox");
	protected static final File NOTIFICATION_DIR        = new File("target/notification");
	protected static final File OUTBOX_DIR              = new File("target/outbox");

	protected static final File OUTBOX_TO_NOWHERE_DIR   = new File("target/outbox-to-nowhere");

	private static final Logger LOGGER
			= LoggerFactory.getLogger(AbstractIT.class);

	private BQRuntime runtime;
	@Rule
	public BQTestFactory testFactory = new BQTestFactory().autoLoadModules();

	protected AbstractIT() {
	}

	@Before
	public void clearFolders() throws IOException {
		deleteDirContent(ARCHIVE_DIR);
		deleteDirContent(ERRORS_DIR);
		deleteDirContent(INBOX_DIR);
		deleteDirContent(NOTIFICATION_DIR);
		deleteDirContent(OUTBOX_DIR);

		deleteDirContent(OUTBOX_TO_NOWHERE_DIR);
	}

	private BQRuntime startApp(BQModule module) {
		BQRuntime runtime = testFactory.app("-s")
				.module(module)
				.createRuntime();
		runtime.run();

		return runtime;
	}

	@Before
	public void initAndStartBootique() {
		//System.setProperty("TUBO_CONFIGURATION_PATH","src/test/integrationTests.yml");
		runtime=startApp(new TuboApplication());
	}

	@After
	public void shutdownBootique() {

		if (runtime!=null) {
			runtime.shutdown();
		}
	}

	private static void deleteDirContent(final File dir)
			throws IOException {
		for (File f : listFiles(dir)) {
			FileUtils.forceDelete(f);
		}
	}

	protected static void waitForEmptyDirectory(final File dir)
			throws IOException {
		while (listFiles(dir).length > 0) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				LOGGER.warn(e.getMessage(), e);
			}
		}
	}

	protected static File[] listFiles(final File dir) throws IOException {
		File[] result = dir.listFiles();

		if (result == null) {
			throw new IOException("Errore listando il contenuto di " + dir);
		}
		return result;
	}

	protected static void assertFileExists(final File file) {
		assertTrue("Era atteso il file " + file + " ma non e' stato trovato",
				   file.exists());
	}
}
