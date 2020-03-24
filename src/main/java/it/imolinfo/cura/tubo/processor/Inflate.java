package it.imolinfo.cura.tubo.processor;

import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class Inflate implements Processor {

	@Override public void process(Exchange exchange) throws Exception {
		InputStream				body;
		
		body = exchange.getIn().getBody(InputStream.class);
		exchange.getIn().setBody(new GZIPInputStream(body));
	}

}
