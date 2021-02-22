package ren.yale.java;

import io.vertx.ext.web.RoutingContext;

public interface ResponseHandler {

    void successHandler(RoutingContext context, Object result);

    void errorHandler(RoutingContext context, Throwable e);

}
