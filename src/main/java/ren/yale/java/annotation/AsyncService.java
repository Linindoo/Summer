package ren.yale.java.annotation;

import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
import ren.yale.java.annotation.impl.AsyncServiceImpl;


public abstract class AsyncService<T> {

    protected ServiceReference serviceReference;

    public abstract Promise<T> get();


    public static <T> AsyncService<T> create(ServiceDiscovery discovery, JsonObject config) {
        return new AsyncServiceImpl<T>(discovery, config);
    }

    public ServiceReference getServiceReference() {
        return serviceReference;
    }

    public void setServiceReference(ServiceReference serviceReference) {
        this.serviceReference = serviceReference;
    }
}
