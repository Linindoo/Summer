package ren.yale.java.annotation.impl;

import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
import io.vertx.servicediscovery.Status;
import ren.yale.java.annotation.AsyncService;


public final class AsyncServiceImpl<T> extends AsyncService<T> {
    private static final Logger logger = LoggerFactory.getLogger(AsyncServiceImpl.class);
    protected ServiceDiscovery discovery;
    private final JsonObject config;

    public AsyncServiceImpl(ServiceDiscovery discovery, JsonObject config) {
        this.discovery = discovery;
        this.config = config;
    }

    @Override
    public Promise<T> get() {
        Promise<T> promise = Promise.promise();
        logger.info("获取服务:" + this.config.toString());
        ServiceReference serviceReference = this.getServiceReference();
        if (serviceReference != null && serviceReference.record().getStatus() == Status.DOWN) {
            promise.complete(serviceReference.get());
        } else {
            if (serviceReference != null) {
                serviceReference.release();
            }
            discovery.getRecord(this.config).onSuccess(record -> {
                if (record == null) {
                    promise.fail("未匹配到合适的服务");
                } else {
                    this.serviceReference = discovery.getReference(record);
                    promise.complete(this.serviceReference.get());
                }
            }).onFailure(promise::fail);
        }
        return promise;
    }
}
