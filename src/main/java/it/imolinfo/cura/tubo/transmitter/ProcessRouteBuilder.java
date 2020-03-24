package it.imolinfo.cura.tubo.transmitter;

import it.imolinfo.cura.tubo.Configuration;
import it.imolinfo.cura.tubo.processor.Deflate;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.dataformat.Base64DataFormat;

public final class ProcessRouteBuilder extends RouteBuilder {

	private static final String TMP_BODY = "Camel.Tubo.tmpBody";

	private Configuration.TransmitterConfiguration	configuration;

	public ProcessRouteBuilder(Configuration.TransmitterConfiguration receiverConfiguration) {
		this.configuration = receiverConfiguration;
	}

	@Override public void configure() throws Exception {
		Base64DataFormat	base64 = new Base64DataFormat();
        ProcessorDefinition route;

        base64.setLineSeparator("");

        route = from("file://" + configuration.localFolder + "/"
				+ "?delete=false"
				+ "&initialDelay=1000"			//	!   [default:1000]
				+ "&delay=500"					//	!   [default:500]
				+ "&readLock=changed"			//	!	[default:none]
				+ "&readLockCheckInterval=1000"	//	!   [default:1000]
				+ "&readLockTimeout=10000"		//	!   [default:10000]
				+ "&readLockMinLength=1"		//	!   [default:1]
				+ "&readLockMinAge=3000"		//	!   [default:0]
				+ "&recursive=true"
				+ "&startingDirectoryMustExist=true"
				+ "&move="			+ configuration.archiveFolder	+ "/$simple{file:name}"
				+ "&moveFailed="	+ configuration.errorFolder		+ "/$simple{file:name}"
		)
		.streamCaching()
		.setHeader(Exchange.HTTP_METHOD, constant("POST"))
		.setHeader(Configuration.Headers.FILE_NAME, simple("${file:name}"))
		.setProperty(TMP_BODY, body())
		.marshal(base64)
		.setBody(exchangeProperty(TMP_BODY))
		.process(new Deflate())
		.to(configuration.targetUrl)
        .removeProperty(TMP_BODY);
        if (configuration.deliveryNotification != null) {
            route = route
                    .setBody(constant(""))
                    .to("file://" + configuration.deliveryNotification + "?fileName=$simple{file:onlyname}");
        }
        route.log(LoggingLevel.INFO, configuration.logger, "transmitter:\"" + configuration.name + "\", "
                                                         + "filename=\"$simple{file:name}\", "
                                                         + "archive=\"" + configuration.archiveFolder + "/$simple{file:name}\"");
	}
}
