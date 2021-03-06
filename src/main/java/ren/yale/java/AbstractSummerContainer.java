package ren.yale.java;

import io.netty.util.internal.StringUtil;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
import ren.yale.java.annotation.AsyncService;
import ren.yale.java.annotation.Service;
import ren.yale.java.method.ClassInfo;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class AbstractSummerContainer {
    protected List<ClassInfo> classInfos;
    protected HashMap<String, ServiceReference> serviceReferences;
    protected ServiceDiscovery discovery;
    protected Vertx vertx;


    public AbstractSummerContainer(ServiceDiscovery discovery, Vertx vertx) {
        this.discovery = discovery;
        this.vertx = vertx;
        this.classInfos = new ArrayList<>();
        this.serviceReferences = new HashMap<>();
    }

    protected boolean isRegister(Class clazz){

        for (ClassInfo classInfo:classInfos) {
            if (classInfo.getClazz() == clazz){
                return true;
            }
        }
        return false;
    }
    private Class getGeneratorType(Field declaredField) {
        Type genericType = declaredField.getGenericType();
        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length > 0) {
                return (Class) actualTypeArguments[0];
            }
        }
        return null;
    }

    protected void autoWriedBean(ClassInfo classInfo) throws IllegalAccessException {
        for (Field declaredField : classInfo.getClazz().getDeclaredFields()) {
            if (declaredField.isAnnotationPresent(Service.class)) {
                Service service = declaredField.getAnnotation(Service.class);
                JsonObject config = new JsonObject();
                if (!StringUtil.isNullOrEmpty(service.value())) {
                    config.put("name", service.value());
                }
                config.put("type", service.type());
                if (declaredField.getType() == AsyncService.class) {
                    declaredField.setAccessible(true);
                    declaredField.set(classInfo.getClazzObj(), AsyncService.create(discovery, config));
                }
            }
        }
    }
}
