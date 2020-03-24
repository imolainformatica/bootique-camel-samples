package it.imolinfo.cura.tubo;

import it.imolinfo.cura.tubo.receiver.ProcessRouteBuilder;
import it.imolinfo.cura.tubo.receiver.RestRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.rest.RestBindingMode;

public final class TuboRouteBuilder extends RouteBuilder {

    public TuboRouteBuilder() {
    }

    @Override
    public void configure() throws Exception {
        final Configuration configuration = new Configuration(System.getProperty("TUBO_CONFIGURATION_PATH"), true);
        final ModelCamelContext context;

        restConfiguration().bindingMode(RestBindingMode.off).component("servlet");

        context = getContext();
        for (Configuration.ReceiverConfiguration rc : configuration.receivers()) {
            context.addRoutes(new RestRouteBuilder(rc));
            context.addRoutes(new ProcessRouteBuilder(rc));
        }
        for (Configuration.TransmitterConfiguration tc: configuration.transmitters()) {
            context.addRoutes(new it.imolinfo.cura.tubo.transmitter.ProcessRouteBuilder(tc));
        }
    }
}
