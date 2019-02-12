package test.jaeger;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import java.util.HashMap;


@Stateless
@Path("/customers")
public class OrderEndpoint {

    @Inject
    CustomerService os;

    @GET
    @Path("/")
    public String getCustomer(@Context HttpHeaders httpHeaders, @QueryParam(value = "name") String name) {
        Tracer tracer = GlobalTracer.get();

        MultivaluedMap<String, String> rawHeaders = httpHeaders.getRequestHeaders();
        final HashMap<String, String> headers = new HashMap<String, String>();
        for (String key : rawHeaders.keySet()) {
            headers.put(key, rawHeaders.get(key).get(0));
        }

        Tracer.SpanBuilder spanBuilder;
        try {
            SpanContext parentSpan = tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapExtractAdapter(headers));
            if (parentSpan == null) {
                spanBuilder = tracer.buildSpan("inside get customers controller");
            } else {
                spanBuilder = tracer.buildSpan("inside get customers controller").asChildOf(parentSpan);
            }
        } catch (IllegalArgumentException e) {
            spanBuilder = tracer.buildSpan("inside get customers controller");
        }
        Span span = spanBuilder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER).start();


        span.setTag("customer name", name);

        os.get(span);

        span.finish();
        return "Customer Name: " + name;
    }

    @GET
    @Path("/noTracing")
    public String nonTracingMethod() {
      return "no tracing";
    }
}