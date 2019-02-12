package test.jaeger;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

import javax.ejb.Stateless;

@Stateless
public class CustomerService {
    public void get(Span span) {
        Tracer tracer = GlobalTracer.get();
        Span childSpan = tracer.buildSpan("CustomerService's inside get method").asChildOf(span).start();
        // we would do something interesting here
        for (int i = 0; i < 100; i++) {
            System.out.println(i);
        }
        childSpan.finish();
    }
}
