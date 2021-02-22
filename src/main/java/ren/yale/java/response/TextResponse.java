package ren.yale.java.response;

import io.vertx.ext.web.RoutingContext;
import ren.yale.java.ResponseHandler;

public class TextResponse implements ResponseHandler {
    @Override
    public void successHandler(RoutingContext context, Object result) {
    }

    @Override
    public void errorHandler(RoutingContext context, Throwable e) {

    }
}
