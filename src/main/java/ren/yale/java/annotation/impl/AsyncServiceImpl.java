package ren.yale.java.annotation.impl;

import com.mongodb.lang.NonNull;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
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
    public Promise<T> get(@NonNull Promise<?> end) {
        Promise<T> promise = Promise.promise();
        logger.info("获取服务:" + this.config.toString());
        discovery.getRecord(this.config).onSuccess(record -> {
            if (record == null) {
                promise.fail("未匹配到合适的服务");
            } else {
                ServiceReference reference = discovery.getReference(record);
                if (!record.getRegistration().equalsIgnoreCase(this.getRegistration())) {
                    logger.info(this.config.toString() + ":服务ID改变了:" + getRegistration() + ">" + record.getRegistration());
                    this.setRegistration(record.getRegistration());
                }
                if (end != null) {
                    end.future().compose(x -> {
                        logger.info("服务释放:" + this.config.toString());
                        reference.release();
                        return Future.succeededFuture(x);
                    }, e -> {
                        logger.info("服务释放:" + this.config.toString());
                        reference.release();
                        return Future.failedFuture(e);
                    });
                } else {
                    logger.warn("当前回调promise为空");
                }
                promise.complete(reference.get());
            }
        }).onFailure(promise::fail);
        return promise;
    }
}
