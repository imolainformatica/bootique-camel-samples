package it.imolinfo.cura.tubo.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public final class Deflate implements Processor {

    public Deflate() {}

	@Override public void process(final Exchange exchange) throws Exception {
        final Message in = exchange.getIn();
        final File file = File.createTempFile("gzip-", null);

        file.deleteOnExit();

		try (OutputStream zip = new GZIPOutputStream(new FileOutputStream(file));
		     InputStream body = in.getBody(InputStream.class)) {
		    IOUtils.copyLarge(body, zip);
        }

        // Il body e' un java.io.File per evitare java.lang.OutOfMemoryError: se
        // usassimo un java.io.FileInputStream, il componente HTTP di Camel a
        // valle di questo Processor si porterebbe in memoria l'intero contenuto
        // dello stream per calcolare l'header HTTP "content-length" (vedere il
        // metodo bufferContent() della classe
        // org.apache.commons.httpclient.methods.InputStreamRequestEntity)
		in.setBody(file);
	}
}
