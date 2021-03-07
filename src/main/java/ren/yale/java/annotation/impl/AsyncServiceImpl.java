package ren.yale.java.annotation.impl;

import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
import ren.yale.java.annotation.AsyncService;

public final class AsyncServiceImpl<T> extends AsyncService<T> {
    protected ServiceDiscovery discovery;
    private JsonObject config;

    public AsyncServiceImpl(ServiceDiscovery discovery, JsonObject config) {
        this.discovery = discovery;
        this.config = config;
    }

    @Override
    public Promise<T> get() {
        Promise<T> promise = Promise.promise();
        discovery.getRecord(config).onSuccess(record -> {
            this.setRegistration(record.getRegistration());
            ServiceReference reference = discovery.getReference(record);
            promise.complete(reference.get());
        }).onFailure(promise::fail);
        return promise;
    }
}
