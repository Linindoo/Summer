package ren.yale.java.test;

import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import ren.yale.java.bean.User;
import ren.yale.java.interceptor.Interceptor;

/**
 * Yale
 *
 * create at:  2018-02-01 17:24
 **/
public class ChangeUserInterceptor implements Interceptor {
    @Override
    public Promise handle(RoutingContext routingContext, Object obj) {
        Promise<Object> promise = Promise.promise();
        User user = (User) obj;
        user.setName("Alice");
        routingContext.response()
                .end(JsonObject.mapFrom(user).encodePrettily());
        promise.complete(user);
        return promise;
    }
}
