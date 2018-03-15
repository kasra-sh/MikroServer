package ir.kasra_sh.MikroServer.Server;

import co.paralleluniverse.fibers.Suspendable;
//import ir.kasra_sh.HTTPUtils.*;
import ir.kasra_sh.MikroServer.HTTPUtils.*;
import ir.kasra_sh.MikroServer.Server.Proxy.ReverseProxy;
import ir.kasra_sh.MikroServer.Utils.MimeTypes;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;


public class RouterFiber implements Runnable {

    private KSocket socket;
    private RequestParser requestParser;
    private Handler handler;
    private Set<Entry<String,Handler>> routes;
    private Set<Entry<String,AbstractMap.SimpleEntry<Handler,String>>> files;
    private Set<Entry<String, InetSocketAddress>> proxies;
    private HashMap<String,HashMap<String, String>> overrides;
    private HTTPConnection connection;


    public RouterFiber(KSocket s,
                       Set<Entry<String,Handler>> routes,
                       Set<Entry<String,AbstractMap.SimpleEntry<Handler,String>>> files,
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
        try {
            try {
                boolean found = false;
                requestParser = new RequestParser(socket);
                String route = requestParser.getRoute();
                //socket.getSocket().getInetAddress().getHostAddress()
                //System.out.println("Route = "+route);

                if (route == null) {
                    sendError();
                    return;
                }

                for (Entry<String, Handler> e:
                        routes) {
                    String pt=e.getKey();
                    if (e.getKey().endsWith("*")) {
                        pt = e.getKey().replace("*", "[_\\-\\.\\?\\&\\w\\d\\/]*");
                    }

                    if (route.matches(pt)) {
                        //System.out.println("Matched !");
                        handler = e.getValue().getClass().newInstance();
                        handler.setContext(e.getKey().replace("*", ""));
                        //System.out.println("Context : "+handler.getContext());
                        requestParser.parseHeader();
                        connection = requestParser.getHTTPConnection();
                        connection.writer = new ResponseWriter(socket);
                        if (requestParser.getErrCode()!=0) {
                            sendErrorCode(requestParser.getErrCode(),connection);
                            break;
                        }
                        handler.setConnection(connection);
                        if (handler.handle() == 0)
                            found = true;
                        break;
                    }
                }
                if (!found) {
                    for (Entry<String, AbstractMap.SimpleEntry<Handler,String>> e :
                            files) {
                        String pt = e.getKey();
                        if (e.getKey().endsWith("*")) {
                            pt = e.getKey().replace("*", "[_\\-\\.\\?\\&\\w\\d\\/\\%]*");
                            //System.out.println(pt);
                        }
                        if (route.matches(pt)) {
                            handler = e.getValue().getKey().getClass().newInstance();
                            handler.setContext(e.getKey().replace("*", ""));
                            requestParser.parseHeader();
                            connection = requestParser.getHTTPConnection();
                            connection.setFilePath(e.getValue().getValue());
                            connection.writer = new ResponseWriter(socket);
                            handler.setConnection(connection);
                            if (requestParser.getErrCode()!=0) {
                                sendErrorCode(requestParser.getErrCode(),connection);
                                break;
                            }
                            if (handler.handle() == 0)
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
                    System.out.println("Not Found - "+route);
                    //handler = routes.get("/404");
                    requestParser.parseHeader();
                    connection = requestParser.getHTTPConnection();
                    connection.writer = new ResponseWriter(socket);
                    connection.writer.header.setStatus(ResponseCode.TEMPORARY_REDIRECT);
                    connection.writer.header.setProperty("Location","/404");
                    connection.writer.writeHeader();
                    connection.writer.finish();
                    //handler.handle(connection);
                }

            } catch (Exception e){

            }

            //outputStream.flush();

            //socket.close();
           //// ServerStats.incServed();
           //// ServerStats.decActive();
            return;
        } catch (Exception e){
            try {
                socket.close();
            } catch (Exception xe) { }
            ServerStats.decActive();
            return;
        }
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
        ServerStats.incServed();
        ServerStats.decActive();
    }

    private void sendErrorCode(int code, HTTPConnection conn) {
        try {
            conn.writer.writeResponse(code, "");
        } catch (IOException e) {
        }
    }
}
