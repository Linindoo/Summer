package ren.yale.java.annotation;

import com.mongodb.lang.NonNull;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;
import ren.yale.java.annotation.impl.AsyncServiceImpl;


public abstract class AsyncService<T> {
    private String registration;

    public abstract Promise<T> get(@NonNull Promise<?> end);


    public String getRegistration() {
        return registration;
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }

    public static <T> AsyncService<T> create(ServiceDiscovery discovery, JsonObject config) {
        return new AsyncServiceImpl<>(discovery, config);
    }
}
