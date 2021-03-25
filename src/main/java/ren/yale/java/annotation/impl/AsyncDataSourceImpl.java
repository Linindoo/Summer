package ren.yale.java.annotation.impl;

import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
import ren.yale.java.annotation.AsyncDataSource;

public class AsyncDataSourceImpl<T> extends AsyncDataSource<T> {
    private static final Logger logger = LoggerFactory.getLogger(AsyncServiceImpl.class);
    protected ServiceDiscovery discovery;
    private final JsonObject config;

    public AsyncDataSourceImpl(ServiceDiscovery discovery, JsonObject config) {
        this.discovery = discovery;
        this.config = config;
    }

    @Override
    public Promise<T> get() {
        Promise<T> promise = Promise.promise();
        logger.info("获取服务:" + this.config.toString());
        discovery.getRecord(this.config).onSuccess(record -> {
            if (record == null) {
                promise.fail("未匹配到合适的服务");
            } else {
                ServiceReference reference = discovery.getReference(record);
                promise.complete(reference.get());
            }
        }).onFailure(promise::fail);
        return promise;
    }
}
