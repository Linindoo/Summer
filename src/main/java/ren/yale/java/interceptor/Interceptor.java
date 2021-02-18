package ren.yale.java.interceptor;


import io.vertx.core.Promise;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by yale on 2018/2/1.
 */
public interface Interceptor {
    Promise<Object> handle(RoutingContext routingContext, Object object);
}
