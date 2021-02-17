package ren.yale.java;

import io.vertx.core.Vertx;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.serviceproxy.ProxyHelper;
import io.vertx.serviceproxy.ServiceBinder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ren.yale.java.method.ClassInfo;

/**
 * Yale
 *
 * create at:  2018-02-01 14:08
 **/
public class SummerService extends AbstractSummerContainer {
    private final static Logger LOGGER = LogManager.getLogger(SummerService.class.getName());
    private ServiceBinder serviceBinder;

    public SummerService(ServiceDiscovery discovery, Vertx vertx) {
        super(discovery, vertx);
        this.serviceBinder = new ServiceBinder(vertx);
    }


    public void registService(Class clazz, Class interfaceClass){
        if (isRegister(clazz)){
            return;
        }
        if (interfaceClass != Object.class && interfaceClass.isInterface()) {
            ClassInfo classInfo = MethodsProcessor.get(classInfos, clazz);
            try {
                autoWriedBean(classInfo);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            this.serviceBinder.setAddress(clazz.getSimpleName())
                    .register(interfaceClass, classInfo.getClazzObj());
            Record record = EventBusService.createRecord(clazz.getSimpleName(), clazz.getSimpleName(), interfaceClass);
            discovery.publish(record, ar -> {
                if (ar.succeeded()) {
                    LOGGER.info("Service <" + ar.result().getName() + "> published");
                } else {
                    ar.cause().printStackTrace();
                }
            });
        }
    }
}

