package it.imolinfo.cura.tubo.receiver;

import it.imolinfo.cura.tubo.Configuration;
import org.apache.camel.builder.RouteBuilder;

public final class RestRouteBuilder extends RouteBuilder {

	Configuration.ReceiverConfiguration	configuration;

	public RestRouteBuilder(Configuration.ReceiverConfiguration receiverConfiguration) {
		this.configuration = receiverConfiguration;
	}

	@Override public void configure() throws Exception {
		rest(configuration.path).post().route().to("direct:" + this.configuration.routeName);
	}
}
