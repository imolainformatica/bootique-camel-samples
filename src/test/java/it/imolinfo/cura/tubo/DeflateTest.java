package it.imolinfo.cura.tubo;

import it.imolinfo.cura.tubo.processor.Deflate;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class DeflateTest extends CamelTestSupport {

    @Produce(uri = "direct:start")
    ProducerTemplate template;

    @EndpointInject(uri = "mock:result")
    MockEndpoint resultEndpoint;

    public DeflateTest() {}

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("direct:start").streamCaching().process(new Deflate()).to("mock:result");
            }
        };
    }

    @Test
    public void testAvoidOutOfMemoryError() throws InterruptedException {
        resultEndpoint.setExpectedMessageCount(1);

        // L'eventuale OutOfMemoryError generato nel sendBody(...) e' wrappato
        // da una org.apache.camel.CamelExecutionException che non logga nulla
        // dell'OOM nonostante il suo metodo getCause() lo restituisca
        template.sendBody(new InputStream() {

            final Random random = new Random();
            long available = 2 * Runtime.getRuntime().maxMemory();

            @Override
            public int read() throws IOException {
                if (available == 0) {
                    return -1;
                }
                available--;
                return random.nextInt(255);
            }
        });

        resultEndpoint.assertIsSatisfied();
    }
}
