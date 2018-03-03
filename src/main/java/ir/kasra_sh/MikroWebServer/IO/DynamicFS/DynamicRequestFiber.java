package ir.kasra_sh.MikroWebServer.IO.DynamicFS;

import co.paralleluniverse.fibers.FiberForkJoinScheduler;
import co.paralleluniverse.fibers.Suspendable;
import ir.kasra_sh.MikroWebServer.HTTPUtils.HTTPConnection;
import ir.kasra_sh.MikroWebServer.HTTPUtils.RequestParser;
import ir.kasra_sh.MikroWebServer.HTTPUtils.ResponseCode;
import ir.kasra_sh.MikroWebServer.HTTPUtils.ResponseWriter;
import ir.kasra_sh.MikroWebServer.IO.Handler;
import ir.kasra_sh.MikroWebServer.IO.Proxy.APIReverseProxy;
import ir.kasra_sh.MikroWebServer.IO.ServerStats;
import ir.kasra_sh.MikroWebServer.Utils.MimeTypes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map.Entry;
import java.util.Set;


public class DynamicRequestFiber implements Runnable {

    private Socket socket;
    byte[] bytes = new byte[8192];
    private InputStream inputStream;
    private OutputStream outputStream;
    //private PrintWriter pr;
    private RequestParser requestParser;
    private Handler handler;
    //private Hashtable<String,Handler> routes;
    private Set<Entry<String,Handler>> routeSet;
    private Set<Entry<String,String>> files;
    private Set<Entry<String,InetSocketAddress>> proxies;
    private HTTPConnection connection;
    //private FiberExecutorScheduler fes;
    private FiberForkJoinScheduler fes;
    private InetAddress redisAddr;


    public DynamicRequestFiber(Socket s,
                               Set<Entry<String,Handler>> routes,
                               Set<Entry<String,String>> files,
                               Set<Entry<String,InetSocketAddress>> proxies,
                               //FiberExecutorScheduler fes
                               FiberForkJoinScheduler fes,
                               InetAddress redisAddress
    ) throws IOException {
        //this.routes = routes;
        routeSet = routes;
        this.files = files;
        socket = s;
        //handler = new EchoHandler();
        this.fes = fes;
        redisAddr = redisAddress;
        this.proxies = proxies;
    }

    @Suspendable
    @Override
    public void run() {
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            requestParser = new RequestParser(inputStream);

            int len = inputStream.read(bytes);

            if (len<0) {
                throw new IOException("Socket Closed !");
            }

            try {
                boolean found = false;

                String route = requestParser.extractRoute(bytes,0,len);

                if (route == null) {
                    sendError();
                    return;
                }

                if (requestParser.getErrCode()!=0) {
                    sendError();
                    return;
                }

                for (Entry<String, InetSocketAddress> e:
                        proxies){
                    String pt=e.getKey();
                    if (e.getKey().endsWith("*")) {
                        pt = e.getKey().replace("*", "[_\\-\\.\\?\\&\\w\\d\\/\\=]*");
                    }


                    if (route.matches(pt)) {
                        //System.out.println("Proxy route Matched !");
                        fes.getExecutor().execute(new APIReverseProxy(e.getValue(),bytes,len,socket));
                        //System.out.println("Proxy route Finished !");
                        found = true;
                        break;
                    }
                }

                for (Entry<String, Handler> e:
                        routeSet){
                    String pt=e.getKey();
                    if (e.getKey().endsWith("*")) {
                        pt = e.getKey().replace("*", "[_\\-\\.\\?\\&\\w\\d\\/]*");
                    }

                    if (route.matches(pt)) {
                        //System.out.println("Matched !");
                        handler = e.getValue().getClass().newInstance();
                        handler.setContext(e.getKey().replace("*", ""));
                        //System.out.println("Context : "+handler.getContext());
                        requestParser.parseHeaders(bytes,0,len);
                        connection = requestParser.getHTTPConnection();
                        connection.setSocket(socket);
                        //int conlen = Integer.valueOf(connection.getHeaders().getProperty("Content-Length"));
                        //requestParser.getBody();
                        //System.out.println("got body");
                        connection.writer = new ResponseWriter(outputStream);
                        if (handler.handle(connection) == 0)
                            found = true;
                        break;
                    }
                }
                if (!found)
                    for (Entry<String, String> e:
                            files){
                        String pt=e.getKey();
                        if (e.getKey().endsWith("*")) {
                            pt = e.getKey().replace("*", "[_\\-\\.\\?\\&\\w\\d\\/]*");
                        }
                        if (route.matches(pt)) {
                            //fes.execute(new FileServerFiber2(e, requestParser, bytes, len, socket));
                            fes.getExecutor().execute(new DynamicFileServerFiber(e, requestParser, bytes, len, socket));
                            //.start();
                            found = true;
                            break;
                        }
                    }
                if (!found) {
                    System.out.println("Not Found");
                    //handler = routes.get("/404");
                    requestParser.parseHeaders(bytes,0,len);
                    connection = requestParser.getHTTPConnection();
                    connection.writer = new ResponseWriter(outputStream);
                    connection.writer.getHeader().setStatus(ResponseCode.TEMPORARY_REDIRECT);
                    connection.writer.getHeader().setProperty("Location","/404");
                    connection.writer.writeHeader();
                    connection.writer.finish();
                    //handler.handle(connection);
                }

            } catch (Exception e){
                e.printStackTrace();
            }

            //outputStream.flush();

            //socket.close();
            // ServerStats.incServed();
            // ServerStats.decActive();
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
        connection.writer = new ResponseWriter(outputStream);
        connection.writer.getHeader().setStatus(ResponseCode.BAD_REQUEST);
        connection.writer.getHeader().setContentType(MimeTypes.Text.TXT);
        connection.writer.writeAll("Bad Request !");
        connection.writer.finish();
        try {
            socket.close();
        } catch (IOException e) {
            //e.printStackTrace();
        }
        ServerStats.incServed();
        ServerStats.decActive();
    }
}
