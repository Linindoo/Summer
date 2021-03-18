package ren.yale.java;

import io.netty.util.internal.StringUtil;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import ren.yale.java.annotation.Blocking;
import ren.yale.java.aop.After;
import ren.yale.java.aop.Before;
import ren.yale.java.aop.Response;
import ren.yale.java.interceptor.Interceptor;
import ren.yale.java.method.ArgInfo;
import ren.yale.java.method.ClassInfo;
import ren.yale.java.method.MethodInfo;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Yale
 *
 * create at:  2018-01-31 17:25
 **/
public class MethodsProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SummerRouter.class);


    private MethodsProcessor() {
    }

    public static Object newClass(Class clazz){

        try {
            for (Constructor<?> c : clazz.getDeclaredConstructors()) {
                c.setAccessible(true);
                if (c.getParameterCount() == 0) {
                    return c.newInstance();
                }
            }
        }catch (Exception e){
            LOGGER.error(e.getMessage());
        }
        return null;

    }

    private static String getPathValue(Path path){
        if (path==null||path.value()==null){
            return "";
        }
        return path.value();

    }
    private static String getProducesValue(Produces produces){
        if (produces == null || produces.value().length == 0){
            return MethodInfo.PRODUCES_TYPE_ALL;
        }

        StringBuilder sb = new StringBuilder();
        for (String str:produces.value()) {
            if (sb.length()==0){
                sb.append(str);
            }else{
                sb.append(";");
                sb.append(str);
            }
        }

        return sb.toString();

    }

    private static Interceptor[] getIntercepter(Class<? extends Interceptor>[] inter, Map<Class<? extends Interceptor>, Interceptor> interceptorMap){
        try {
            Interceptor[] interceptors = new Interceptor[inter.length];
            int i=0;
            for (Class<? extends Interceptor> cls:inter) {
                Interceptor interceptor = interceptorMap.get(cls);
                if (interceptor == null) {
                    interceptor =  cls.getDeclaredConstructor().newInstance();
                    interceptorMap.put(cls, interceptor);
                }
                interceptors[i] = interceptor;
                i++;
            }
            return interceptors;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private static Interceptor[] getBefores(Before before, Map<Class<? extends Interceptor>, Interceptor> interceptorMap){
        if (before != null && before.value().length > 0) {
            return getIntercepter(before.value(), interceptorMap);
        }
        return null;
    }
    private static Interceptor[] getAfters(After after, Map<Class<? extends Interceptor>, Interceptor> interceptorMap){
        if (after != null && after.value().length > 0) {
            return getIntercepter(after.value(), interceptorMap);
        }
        return null;
    }
    public static ClassInfo  get(SummerRouter summerRouter, Class clazz) {
        ClassInfo classInfo = new ClassInfo();
        Path path = (Path) clazz.getAnnotation(Path.class);
        if (path != null && !StringUtil.isNullOrEmpty(path.value())) {
            classInfo.setClassPath(path.value());
        }

        classInfo.setClazzObj(newClass(clazz));
        classInfo.setClazz(clazz);

        Interceptor[] interceptorsClazz =
                getBefores((Before) clazz.getAnnotation(Before.class), summerRouter.getInterceptorMap());
        if (interceptorsClazz!=null){
            classInfo.setBefores(interceptorsClazz);
        }
        interceptorsClazz =
                getAfters((After) clazz.getAnnotation(After.class), summerRouter.getInterceptorMap());
        if (interceptorsClazz!=null){
            classInfo.setAfters(interceptorsClazz);
        }
        ResponseHandler responseHandler = getReponseHandler((Response) clazz.getAnnotation(Response.class), summerRouter.getResponseHandlerMap());
        if (responseHandler != null) {
            classInfo.setResponseHandler(responseHandler);
        }
        for (Method method : clazz.getMethods()) {
            Class mt = method.getDeclaringClass();
            if ( mt ==  Object.class){
                continue;
            }
            MethodInfo methodInfo = new MethodInfo();


            Interceptor[] interceptorsMethod =
                    getBefores(method.getAnnotation(Before.class), summerRouter.getInterceptorMap());
            if (interceptorsMethod!=null){
                methodInfo.setBefores(interceptorsMethod);
            }

            interceptorsMethod =
                    getAfters(method.getAnnotation(After.class), summerRouter.getInterceptorMap());
            if (interceptorsMethod!=null){
                methodInfo.setAfters(interceptorsMethod);
            }

            ResponseHandler reponseHandler = getReponseHandler(method.getAnnotation(Response.class), summerRouter.getResponseHandlerMap());
            if (reponseHandler != null) {
                methodInfo.setResponseHandler(reponseHandler);
            }
            Blocking blocking = method.getAnnotation(Blocking.class);
            if (blocking!=null){
                methodInfo.setBlocking(true);
            }


            Path pathMthod = method.getAnnotation(Path.class);
            Produces produces = method.getAnnotation(Produces.class);

            methodInfo.setMethodPath(getPathValue(pathMthod));
            methodInfo.setProducesType(getProducesValue(produces));

            methodInfo.setHttpMethod(getHttpMethod(method));
            methodInfo.setMethod(method);

            Parameter[] parameters = method.getParameters();
            Class<?>[] parameterTypes = method.getParameterTypes();
            Annotation[][] annotations = method.getParameterAnnotations();


            int i=0;
            for (Annotation[] an:annotations) {

                ArgInfo argInfo = new ArgInfo();

                argInfo.setAnnotation(an);
                argInfo.setClazz(parameterTypes[i]);
                argInfo.setParameter(parameters[i]);

                for (Annotation ant:an) {
                    if (ant instanceof Context) {
                        argInfo.setContext(true);
                    } else if (ant instanceof DefaultValue) {
                        argInfo.setDefaultValue(((DefaultValue) ant).value());
                    } else if (ant instanceof PathParam) {
                        argInfo.setPathParam(true);
                        argInfo.setPathParam(((PathParam) ant).value());
                    } else if (ant instanceof QueryParam) {
                        argInfo.setQueryParam(true);
                        argInfo.setQueryParam(((QueryParam) ant).value());
                    } else if (ant instanceof FormParam) {
                        argInfo.setFormParam(true);
                        argInfo.setFormParam(((FormParam) ant).value());
                    } else if (ant instanceof BeanParam) {
                        argInfo.setBeanParam(true);
                    }
                }

                i++;
                methodInfo.addArgInfo(argInfo);
            }

            classInfo.addMethodInfo(methodInfo);

        }
        summerRouter.classInfos.add(classInfo);
        return classInfo;
    }

    private static ResponseHandler getReponseHandler(Response response, Map<Class<? extends ResponseHandler>, ResponseHandler> responseHandlerMap) {
        if (response == null) {
            return null;
        }
        ResponseHandler responseHandler = responseHandlerMap.get(response.value());
        if (responseHandler != null) {
            return responseHandler;
        }
        try {
            responseHandler = response.value().getDeclaredConstructor().newInstance();
            responseHandlerMap.put(response.value(), responseHandler);
            return responseHandler;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean isRestClass(Class cls) {

        List<Class<Path>> search = Arrays.asList(Path.class);

        for (Class<? extends Annotation> item: search) {
            if (cls.getAnnotation(item) != null) {
                return true;
            }
        }

        return false;
    }

    private static Class getHttpMethod(Method method) {

        List<Class<? extends Annotation>> search = Arrays.asList(
                GET.class,
                POST.class,
                PUT.class,
                DELETE.class,
                OPTIONS.class,
                HEAD.class);

        for (Class<? extends Annotation> item: search) {
            if (method.getAnnotation(item) != null) {
                return item;
            }
        }

        return null;
    }
}
