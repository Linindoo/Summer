package ren.yale.java.annotation;

import io.vertx.core.Promise;

public abstract class AsyncService<T> {
    private String registration;
    public abstract Promise<T> get();

    public String getRegistration() {
        return registration;
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }
}
