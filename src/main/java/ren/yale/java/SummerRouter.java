package ren.yale.java;

import io.netty.util.internal.StringUtil;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.servicediscovery.ServiceDiscovery;
import ren.yale.java.interceptor.Interceptor;
import ren.yale.java.method.ArgInfo;
import ren.yale.java.method.ClassInfo;
import ren.yale.java.method.MethodInfo;
import ren.yale.java.response.BaseResponse;
import ren.yale.java.tools.PathParamConverter;
import ren.yale.java.tools.StringUtils;

import javax.ws.rs.*;
import java.util.*;

/**
 * Yale
 *
 * create at:  2018-02-01 14:08
 **/
public class SummerRouter extends AbstractSummerContainer{

    private static final Logger logger = LoggerFactory.getLogger(SummerRouter.class);

    private Router router;
    private String contextPath;
    protected Map<Class<? extends Interceptor>, Interceptor> interceptorMap;
    protected Map<Class<? extends ResponseHandler>, ResponseHandler> responseHandlerMap;


    public SummerRouter(Router router, ServiceDiscovery discovery, Vertx vertx, String contextPath) {
        super(discovery, vertx);
        this.router = router;
        this.contextPath = contextPath;
        this.interceptorMap = new HashMap<>();
        this.responseHandlerMap = new HashMap<>();
        this.init();
    }

    public Map<Class<? extends Interceptor>, Interceptor> getInterceptorMap() {
        return interceptorMap;
    }

    public void setInterceptorMap(Map<Class<? extends Interceptor>, Interceptor> interceptorMap) {
        this.interceptorMap = interceptorMap;
    }

    public Map<Class<? extends ResponseHandler>, ResponseHandler> getResponseHandlerMap() {
        return responseHandlerMap;
    }

    public void setResponseHandlerMap(Map<Class<? extends ResponseHandler>, ResponseHandler> responseHandlerMap) {
        this.responseHandlerMap = responseHandlerMap;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        if (!StringUtils.isEmpty(contextPath)){
            this.contextPath = contextPath;
        }
    }
    public Router getRouter() {
        return router;
    }
    private void init(){
//        router.route().handler(BodyHandler.create());
//        SessionHandler handler = SessionHandler.create(LocalSessionStore.create(vertx));
//        router.route().handler(handler);
//        handler.setNagHttps(true);
        router.errorHandler(404,x->{
            x.end(x.failure() != null ? x.failure().getMessage() : "404");
        });
        router.errorHandler(500,x->{
            x.end(x.failure() != null ? x.failure().getMessage() : "500");
        });
        router.route().failureHandler(x->{
            x.end(x.failure() != null ? x.failure().getMessage() : "fail");
        });
    }
    public void registerResource(Class clazz){
        if (isRegister(clazz)){
            return;
        }
        ClassInfo classInfo = MethodsProcessor.get(this, clazz);
        try {
            autoWriedBean(classInfo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (classInfo != null) {
            for (MethodInfo methodInfo : classInfo.getMethodInfoList()) {
                String p = classInfo.getClassPath() + methodInfo.getMethodPath();
                if (StringUtil.isNullOrEmpty(p)) {
                    continue;
                }
                p = PathParamConverter.converter(p);
                p = addContextPath(p);
                Route route = null;
                if (methodInfo.getHttpMethod() == null) {
                    route = router.route(p);
                } else if (methodInfo.getHttpMethod() == GET.class) {
                    route = router.get(p);
                } else if (methodInfo.getHttpMethod() == POST.class) {
                    route = router.post(p);
                } else if (methodInfo.getHttpMethod() == PUT.class) {
                    route = router.put(p);
                } else if (methodInfo.getHttpMethod() == DELETE.class) {
                    route = router.delete(p);
                } else if (methodInfo.getHttpMethod() == OPTIONS.class) {
                    route = router.options(p);
                } else if (methodInfo.getHttpMethod() == HEAD.class) {
                    route = router.head(p);
                }
                if (methodInfo.isBlocking()) {
                    route.blockingHandler(getHandler(classInfo, methodInfo));
                } else {
                    route.handler(getHandler(classInfo, methodInfo));
                }
            }
        }

    }

    private String addContextPath(String path) {
        return contextPath + path;
    }

    private Object covertType(Class type,String v) throws Exception{
        String typeName = type.getTypeName();
        if (type == String.class){
            return v;
        }
        if (type == Integer.class||typeName.equals("int")){
            return Integer.parseInt(v);
        }
        if (type == Long.class||typeName.equals("long")){
            return Long.parseLong(v);
        }
        if (type == Float.class||typeName.equals("float")){
            return Float.parseFloat(v);
        }
        if (type == Double.class||typeName.equals("double")){
            return Double.parseDouble(v);
        }
        if (type == JsonObject.class) {
            return new JsonObject(v);
        }
        return null;
    }
    private Object getPathParamArg(RoutingContext routingContext, ArgInfo argInfo){

        try {
            String path = routingContext.request().getParam(argInfo.getPathParam());
            if (!StringUtils.isEmpty(path)){
                return covertType(argInfo.getClazz(),path);
            }
            if (!StringUtils.isEmpty(argInfo.getDefaultValue())){
                return covertType(argInfo.getClazz(),argInfo.getDefaultValue());
            }

        }catch (Exception e){
            logger.error(e.getMessage());
        }

        return null;

    }
    private Object getBeanParamArg(RoutingContext routingContext,ArgInfo argInfo){

        try {
            String q = routingContext.getBodyAsString();
            if (!StringUtils.isEmpty(q)){
                return covertType(argInfo.getClazz(),q);
            }
        }catch (Exception e){
            logger.error(e.getMessage());
        }
        return null;
    }

    private Object getFromParamArg(RoutingContext routingContext,ArgInfo argInfo){

        try {
            String q = routingContext.request().getParam(argInfo.getFormParam());
            if (!StringUtils.isEmpty(q)){
                return covertType(argInfo.getClazz(),q);
            }
            if (!StringUtils.isEmpty(argInfo.getDefaultValue())){
                return covertType(argInfo.getClazz(),argInfo.getDefaultValue());
            }

        }catch (Exception e){
            logger.error(e.getMessage());
        }
        return null;
    }
    private Object getQueryParamArg(RoutingContext routingContext,ArgInfo argInfo){

        try {
            String q = routingContext.request().getParam(argInfo.getQueryParam());
            if (!StringUtils.isEmpty(q)){
                return covertType(argInfo.getClazz(),q);
            }
            if (!StringUtils.isEmpty(argInfo.getDefaultValue())){
                return covertType(argInfo.getClazz(),argInfo.getDefaultValue());
            }

        }catch (Exception e){
            logger.error(e.getMessage());
        }

        return null;
    }

    private Object getContext(RoutingContext routingContext,ArgInfo argInfo){
        Class clz = argInfo.getClazz();
        if (clz ==RoutingContext.class){
            return routingContext;
        }else if (clz == HttpServerRequest.class){
            return routingContext.request();
        }else if (clz == HttpServerResponse.class){
            return routingContext.response();
        }else if (clz == Session.class){
            return routingContext.session();
        }else if (clz == Vertx.class){
            return vertx;
        }
        return null;
    }
    private Object[] getArgs(RoutingContext routingContext,ClassInfo classInfo,MethodInfo methodInfo){
        Object[] objects = new Object[methodInfo.getArgInfoList().size()];
        int i =0;
        for (ArgInfo argInfo : methodInfo.getArgInfoList()) {

            if (argInfo.isContext()) {
                objects[i] = getContext(routingContext, argInfo);
            } else if (argInfo.isQueryParam()) {
                objects[i] = getQueryParamArg(routingContext, argInfo);
            } else if (argInfo.isFormParam()) {
                objects[i] = getFromParamArg(routingContext, argInfo);
            } else if (argInfo.isPathParam()) {
                objects[i] = getPathParamArg(routingContext, argInfo);
            } else if (argInfo.isBeanParam()) {
                objects[i] = getBeanParamArg(routingContext, argInfo);
            } else {
                objects[i] = null;
            }
            i++;
        }
        return objects;
    }

    private String convert2XML(Object object) {
//        JAXBContext context = JAXBContext.newInstance(object.getClass());
//        Marshaller marshaller = context.createMarshaller();
//        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
//        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        marshaller.marshal(object, baos);
        return new String("no logic");
    }

    private Promise<Object> handleBefores(RoutingContext routingContext,ClassInfo classInfo, MethodInfo methodInfo){
        List<Interceptor> beforeList = new ArrayList<>();
        if (classInfo.getBefores()!=null){
            beforeList.addAll(Arrays.asList(classInfo.getBefores()));
        }
        if (methodInfo.getBefores()!=null){
            beforeList.addAll(Arrays.asList(methodInfo.getBefores()));
        }

        Promise<Object> promise = Promise.promise();
        Future<Object> future = null;
        for (Interceptor inter : beforeList) {
            if (future == null) {
                future = inter.handle(routingContext, null).future();
            } else {
                future = future.compose(x -> inter.handle(routingContext, null).future(), Future::failedFuture);
            }
        }
        if (future == null) {
            promise.complete();
        } else {
            future.onSuccess(promise::complete).onFailure(promise::fail);
        }
        return promise;
    }

    private Promise<Object> handleAfters(RoutingContext routingContext, ClassInfo classInfo, MethodInfo methodInfo, Object obj) {
        List<Interceptor> list = new ArrayList<>();
        if (methodInfo.getAfters() != null) {
            list.addAll(Arrays.asList(methodInfo.getAfters()));
        }
        if (classInfo.getAfters() != null) {
            list.addAll(Arrays.asList(classInfo.getAfters()));
        }
        Promise<Object> promise = Promise.promise();
        Future<Object> future = null;
        for (Interceptor inter : list) {
            if (future == null) {
                future = inter.handle(routingContext, obj).future();
            } else {
                future = future.compose(x -> inter.handle(routingContext, obj).future(), Future::failedFuture);
            }
        }
        if (future == null) {
            promise.complete(obj);
        } else {
            future.onSuccess(promise::complete).onFailure(promise::fail);
        }
        return promise;
    }
    private Promise<Object> handlers(ClassInfo classInfo, MethodInfo methodInfo,RoutingContext routingContext){
        Promise<Object> promise = Promise.promise();
        try {
            Object[] args = getArgs(routingContext, classInfo, methodInfo);
            Object result = methodInfo.getMethod().invoke(classInfo.getClazzObj(), args);
            if (result != null) {
                if (!routingContext.response().ended()) {
                    if (!routingContext.response().ended()) {
                        if (result instanceof Promise) {
                            return (Promise<Object>) result;
                        } else {
                            promise.complete(result);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            promise.fail(e);
        }
        return promise;
    }

    private Handler<RoutingContext> getHandler(ClassInfo classInfo, MethodInfo methodInfo){
        return (routingContext -> {
            ResponseHandler responseHandler = getResponseHandler(classInfo, methodInfo);
            routingContext.response().putHeader("content-type", methodInfo.getProducesType());
            handleBefores(routingContext, classInfo, methodInfo).future().onSuccess(x -> {
                Promise<Object> routerHandler = handlers(classInfo, methodInfo, routingContext);
                routerHandler.future().onSuccess(v->{
                    handleAfters(routingContext, classInfo, methodInfo, v).future().onSuccess(y -> {
                        responseHandler.successHandler(routingContext, y);
                    }).onFailure(e -> {
                        responseHandler.errorHandler(routingContext, e);
                    });

                }).onFailure(e->{
                    responseHandler.errorHandler(routingContext, e);
                });
            }).onFailure(e -> {
                responseHandler.errorHandler(routingContext, e);
            });
        });
    }

    private ResponseHandler getResponseHandler(ClassInfo classInfo, MethodInfo methodInfo) {
        ResponseHandler responseHandler = Optional.ofNullable(methodInfo.getResponseHandler()).orElse(classInfo.getResponseHandler());
        return responseHandler == null ? new BaseResponse() : responseHandler;
    }
}

