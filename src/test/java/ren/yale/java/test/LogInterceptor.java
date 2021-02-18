package ren.yale.java.test;

import io.vertx.core.Promise;
import io.vertx.ext.web.RoutingContext;
import ren.yale.java.interceptor.Interceptor;

/**
 * Yale
 *
 * create at:  2018-02-01 17:24
 **/
public class LogInterceptor implements Interceptor {
    @Override
    public Promise handle(RoutingContext routingContext, Object obj) {
        System.out.println(routingContext.request().absoluteURI());
        Promise<Object> promise = Promise.promise();
        promise.complete();
        return promise;
    }
}
