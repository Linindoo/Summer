package ren.yale.java.annotation;

import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;
import ren.yale.java.annotation.impl.AsyncDataSourceImpl;

public abstract class AsyncDataSource<T> {
    public abstract Promise<T> get();

    public static <T> AsyncDataSource<T> create(ServiceDiscovery discovery, JsonObject config) {
        return new AsyncDataSourceImpl<>(discovery, config);
    }
}
