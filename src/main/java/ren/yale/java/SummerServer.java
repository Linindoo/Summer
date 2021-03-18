package ren.yale.java;

import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
/**
 * Yale
 *
 * create at:  2018-02-01 16:40
 **/
public class SummerServer  {
    private Vertx vertx;
    private Router router;
    private SummerRouter summerRouter;
    private SummerService summerService;
    private int port = 0;
    private String host = "0.0.0.0";
    private ServiceDiscovery discovery;


    private SummerServer(String host,int port){
        this(host,port,null);
    }

    private SummerServer(String host, int port, VertxOptions options) {
        this(host, port, options, null);
    }

    private SummerServer(String host, int port, VertxOptions options, Vertx vertx) {
        if (vertx != null) {
            this.vertx = vertx;
        } else if (options != null) {
            this.vertx = Vertx.vertx(options);
        } else {
            this.vertx = Vertx.vertx();
        }
        this.discovery = ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions().setBackendConfiguration(vertx.getOrCreateContext().config()));
        this.router = Router.router(this.vertx);
        this.summerRouter = new SummerRouter(router, discovery, vertx);
        this.summerService = new SummerService(discovery, vertx);
        this.port = port;
        this.host = host;
        init();
    }

    public static SummerServer create(Vertx vertx) {
        return new SummerServer("0.0.0.0",0,null,vertx);
    }

    private void init(){
//        vertx.eventBus().registerDefaultCodec(EventMessage.class, new EventMessageCodec());
    }
    public Vertx getVertx(){
        return vertx;
    }

    public Router getRouter(){
        return router;
    }

    public SummerRouter getSummerRouter(){
        return summerRouter;
    }



    public static SummerServer create(int port){
        return new SummerServer("0.0.0.0",port);
    }
    public static SummerServer create(){
        return new SummerServer("0.0.0.0",0);
    }
    public static SummerServer create(String host,int port){
        return new SummerServer(host,port);
    }
    public static SummerServer create(String host,int port,VertxOptions options){
        return new SummerServer(host,port,options);
    }

    public Promise<Object> start() {
        Promise<Object> promise = Promise.promise();
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(port, host, httpServerAsyncResult -> {
                    if (httpServerAsyncResult.succeeded()) {
                        HttpServer httpServer = httpServerAsyncResult.result();
                        SummerServer.this.setPort(httpServer.actualPort());
                        promise.complete();
                        System.out.println("listen at: http://" + host + ":" + httpServer.actualPort());
                    } else {
                        promise.fail(httpServerAsyncResult.cause());
                        System.out.println(httpServerAsyncResult.cause().getCause());
                    }
                });
        return promise;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }


    public void setVertx(Vertx vertx) {
        this.vertx = vertx;
    }

    public void setRouter(Router router) {
        this.router = router;
    }

    public void setSummerRouter(SummerRouter summerRouter) {
        this.summerRouter = summerRouter;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public SummerService getSummerService() {
        return summerService;
    }

    public void setSummerService(SummerService summerService) {
        this.summerService = summerService;
    }

    public class WebServer extends AbstractVerticle {
        @Override
        public void start(Promise<Void> promise) throws Exception {
            String host = config().getString("host", "0.0.0.0");
            Integer port = config().getInteger("port", 0);
            vertx.createHttpServer()
                    .requestHandler(router)
                    .listen(port, host, httpServerAsyncResult -> {
                        if (httpServerAsyncResult.succeeded()) {
                            HttpServer httpServer = httpServerAsyncResult.result();
                            SummerServer.this.setPort(httpServer.actualPort());
                            promise.complete();
                            System.out.println("listen at: http://" + host + ":" + httpServer.actualPort());
                        } else {
                            promise.fail(httpServerAsyncResult.cause());
                            System.out.println(httpServerAsyncResult.cause().getCause());
                        }
                    });
        }
    }
}
