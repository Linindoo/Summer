package ren.yale.java.response;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import ren.yale.java.ResponseHandler;

import javax.ws.rs.core.Response;

public class BaseResponse implements ResponseHandler {

    @Override
    public void successHandler(RoutingContext context, Object result) {
        if (context.response().ended()) {
            return;
        }
        context.response().setStatusCode(Response.Status.OK.getStatusCode());
        if (result == null) {
            context.response().end();
        } else if (result instanceof String) {
            context.response().end((String) result);
        } else if (result instanceof JsonObject) {
            context.response().end(((JsonObject) result).toBuffer());
        } else if (result instanceof JsonArray) {
            context.response().end(((JsonArray) result).toBuffer());
        } else {
            context.response().end(Json.encodeToBuffer(result));
        }
    }

    @Override
    public void errorHandler(RoutingContext context, Throwable e) {
        if (context.response().ended()) {
            return;
        }
        context.response().setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).end(e.getMessage());
    }
}
