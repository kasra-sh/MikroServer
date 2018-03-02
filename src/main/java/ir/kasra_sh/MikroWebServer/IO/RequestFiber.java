package ir.kasra_sh.MikroWebServer.IO;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import ir.kasra_sh.MikroWebServer.HTTPUtils.HTTPConnection;
import ir.kasra_sh.MikroWebServer.HTTPUtils.RequestParser;
import ir.kasra_sh.MikroWebServer.HTTPUtils.ResponseCode;
import ir.kasra_sh.MikroWebServer.HTTPUtils.ResponseWriter;
import ir.kasra_sh.MikroWebServer.IO.StaticFS.FileServerFiber;
import ir.kasra_sh.MikroWebServer.Utils.MimeTypes;

import java.io.*;
import java.net.Socket;
import java.util.Map.Entry;
import java.util.Set;


public class RequestFiber extends Fiber<Void> {

    private Socket socket;
    byte[] bytes = new byte[8192];
    private InputStream inputStream;
    private OutputStream outputStream;
    //private PrintWriter pr;
    private RequestParser requestParser = new RequestParser(null);
    private Handler handler;
    //private Hashtable<String,Handler> routes;
    private Set<Entry<String,Handler>> routeSet;
    private Set<Entry<String,String>> files;
    private HTTPConnection connection;


    public RequestFiber(Socket s, Set<Entry<String,Handler>> routes, Set<Entry<String,String>> files) throws IOException {
        //this.routes = routes;
        routeSet = routes;
        this.files = files;
        socket = s;
        //handler = new EchoHandler();
    }

    @Suspendable
    @Override
    public Void run() throws SuspendExecution, InterruptedException {
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            //socket.setSoTimeout(5000);
            //pr = new PrintWriter(outputStream);

            //ServerStats.incActive();
            int len = inputStream.read(bytes);
            if (len<0) {
                throw new IOException("Socket Closed !");
            }
            //int h = requestParser.parseHeaders(bytes,0,len);
            /*if (h<0) {
                socket.close();
                return null;
            }*/

            try {
                boolean found = false;

                String route = requestParser.extractRoute(bytes,0,len);

                if (route == null) {
                    sendError();
                    return null;
                }

                if (requestParser.getErrCode()!=0) {
                    sendError();
                    return null;
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
                            new FileServerFiber(e, requestParser, bytes, len, socket).start();
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

            }

            //outputStream.flush();

            //socket.close();
           //// ServerStats.incServed();
           //// ServerStats.decActive();
            return null;
        } catch (Exception e){
            try {
                socket.close();
            } catch (Exception xe) { }
            ServerStats.decActive();
            return null;
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
