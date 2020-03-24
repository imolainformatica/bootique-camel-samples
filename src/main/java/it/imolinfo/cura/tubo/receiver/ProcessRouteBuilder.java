package it.imolinfo.cura.tubo.receiver;

import it.imolinfo.cura.tubo.Configuration;
import it.imolinfo.cura.tubo.processor.Inflate;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.Base64DataFormat;

public class ProcessRouteBuilder extends RouteBuilder {

	private static final String TMP_BODY = "Camel.Tubo.tmpBody";
	private static final String TARGET_FOLDER = "Camel.Tubo.targetFolder";

	private Configuration.ReceiverConfiguration configuration;

	public ProcessRouteBuilder(Configuration.ReceiverConfiguration receiverConfiguration) {
		this.configuration = receiverConfiguration;
	}

	@Override
	public void configure() throws Exception {
	Base64DataFormat base64 = new Base64DataFormat();

	from("direct:" + configuration.routeName).streamCaching()
		.setHeader(Exchange.FILE_NAME, header(Configuration.Headers.FILE_NAME))
		.process(new Inflate())
		.setProperty(TMP_BODY, body())
		.setProperty(TARGET_FOLDER, constant(configuration.targetFolder))
		.unmarshal(base64)
		.setBody(exchangeProperty(TMP_BODY)).removeProperty(TMP_BODY)
		.to("file://" + configuration.targetFolder + "?fileName=$simple{file:name}")
		.log(LoggingLevel.INFO, configuration.logger, "receiver:\"" + configuration.name + "\", "
			+ "filename=\"$simple{file:name}\", "
			+ "file=\" ${property[" + TARGET_FOLDER + "]}/$simple{file:name}\",");
	}
}
