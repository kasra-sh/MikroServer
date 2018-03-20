package ir.kasra_sh.MikroServer.Server;

import co.paralleluniverse.fibers.Suspendable;
//import ir.kasra_sh.HTTPUtils.*;
import ir.kasra_sh.MikroServer.HTTPUtils.*;
import ir.kasra_sh.MikroServer.Server.Annotations.Methods;
import ir.kasra_sh.MikroServer.Server.Proxy.ReverseProxy;
import ir.kasra_sh.MikroServer.Utils.MimeTypes;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.AbstractMap.*;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;


public class RouterFiber implements Runnable {

    private KSocket socket;
    private RequestParser requestParser;
    private Handler handler;
    private Set<Entry<String, Class<? extends Handler>>> routes;
    private Set<Entry<String, SimpleEntry<Class<? extends Handler>,String>>> files;
    private Set<Entry<String, InetSocketAddress>> proxies;
    private HashMap<String,HashMap<String, String>> overrides;
    private HTTPConnection connection;


    public RouterFiber(KSocket s,
                       Set<Entry<String, Class<? extends Handler>>> routes,
                       Set<Entry<String, SimpleEntry<Class<? extends Handler>,String>>> files,
                       Set<Entry<String, InetSocketAddress>> proxies,
                       HashMap<String, HashMap<String, String>> overrides) throws IOException {
        this.proxies = proxies;
        this.overrides = overrides;
        this.routes = routes;
        this.files = files;
        socket = s;
        //handler = new EchoHandler();
    }

    @Suspendable
    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        try {
            boolean found = false;
            requestParser = new RequestParser(socket);
            String route;
            try {
                route = requestParser.getRoute();
            } catch (Exception e){
                //e.getStackTrace();
                throw e;
            }

            //socket.getSocket().getInetAddress().getHostAddress()
            //System.out.println("Route = "+route);

            if (route == null) {
                sendErrorCode(ResponseCode.BAD_REQUEST, connection);
                found = true;
            }

            if (!found)
            for (Entry<String, Class<? extends Handler>> e:
                    routes) {
                String pt=e.getKey();
                if (e.getKey().endsWith("*")) {
                    pt = e.getKey().replace("*", "[_\\-\\.\\?\\&\\w\\d\\/]*");
                }

                if (route.matches(pt)) {
                    //System.out.println("Matched "+pt+" with "+ route);
                    handler = e.getValue().newInstance();
                    HTTPMethod[] al = e.getValue().getAnnotation(Methods.class).value();
                    handler.setContext(e.getKey().replace("*", ""));
                    //System.out.println("Context : "+handler.getContext());
                    requestParser.parseHeader();
                    connection = requestParser.getHTTPConnection();
                    connection.writer = new ResponseWriter(socket);
                    if (requestParser.getErrCode()!=0) {
                        sendErrorCode(requestParser.getErrCode(),connection);
                        found = true;
                        break;
                    }
                    boolean mt = false; // allowed method
                    if (al.length>0 && al.length<4){
                        for (HTTPMethod hm:
                                al) {
                            if (connection.getMethod() == hm) {
                                mt = true;
                                break;
                            }
                        }
                    }
                    if (!mt) {
                        sendErrorCode(ResponseCode.METHOD_NOT_ALLOWED,connection);
                        found = true;
                        break;
                    }
                    handler.setConnection(connection);
                    handler.handle();
                    found = true;
                    break;
                }
            }
            if (!found) {
                for (Entry<String, SimpleEntry<Class<? extends Handler>,String>> e :
                        files) {
                    String pt = e.getKey();
                    if (e.getKey().endsWith("*")) {
                        pt = e.getKey().replace("*", "[_\\-\\.\\?\\&\\w\\d\\/\\%]*");
                        //System.out.println(pt);
                    }
                    if (route.matches(pt)) {
                        handler = e.getValue().getKey().newInstance();
                        handler.setContext(e.getKey().replace("*", ""));
                        requestParser.parseHeader();
                        connection = requestParser.getHTTPConnection();
                        connection.setFilePath(e.getValue().getValue());
                        connection.writer = new ResponseWriter(socket);
                        handler.setConnection(connection);
                        if (requestParser.getErrCode()!=0) {
                            sendErrorCode(requestParser.getErrCode(),connection);
                            found = true;
                            break;
                        }
                        handler.handle();
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                for (Entry<String, InetSocketAddress> e :
                        proxies) {
                    String pt = e.getKey();
                    if (e.getKey().endsWith("*")) {
                        pt = e.getKey().replace("*", "[_\\-\\.\\?\\&\\w\\d\\/\\%]*");
                        //System.out.println("Pattern: "+pt);
                        //System.out.println("Route: "+route);
                    }
                    if (route.matches(pt)) {
                        //System.out.println("Matched");
                        requestParser.parseHeader();
                        connection = requestParser.getHTTPConnection();
                        connection.setContext(e.getKey().replace("*",""));
                        //connection.setFilePath(e.getValue().getValue());
                        connection.writer = new ResponseWriter(socket);
                        //handler.setConnection(connection);
                        if (requestParser.getErrCode()!=0) {
                            sendErrorCode(requestParser.getErrCode(),connection);
                            break;
                        }
                        ReverseProxy rp = new ReverseProxy(e.getValue(),connection);
                        rp.setOverrides(overrides.get(e.getKey()));
                        rp.run();
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                //System.out.println("Not Found - "+route);
                //handler = routes.get("/404");
                //requestParser.parseHeader();
                //connection = requestParser.getHTTPConnection();
                ResponseWriter writer = new ResponseWriter(socket);
                try {
                    writer.writeResponse(404, "Not Found !");
                    writer.finish();
                } catch (Exception e){
                    if (Logger.DEBUG) {
                        e.printStackTrace();
                    }
                }

                //handler.handle(connection);
            }
            if (requestParser.getErrCode() == 0)
                Logger.logReqFinish(socket,startTime,200,route);
            else
                Logger.logReqFinish(socket,startTime,200,route);
        } catch (Exception e){
            if (Logger.DEBUG) e.printStackTrace();
            Logger.logReqFinish(socket, startTime, 500, "Ex("+e.getMessage()+")");
        }

        socket.close();

        return;

    }

    @Suspendable
    private void sendError(){
        //handler = routes.get("/404");
        connection.writer = new ResponseWriter(socket);
        connection.writer.header.setStatus(ResponseCode.BAD_REQUEST);
        connection.writer.header.setContentType(MimeTypes.Text.TXT);
        connection.writer.writeAll("Bad Request !");
        connection.writer.finish();
        socket.close();
    }

    private void sendErrorCode(int code, HTTPConnection conn) {
        try {
            conn.writer.writeResponse(code, "");
            conn.writer.finish();
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }
}
