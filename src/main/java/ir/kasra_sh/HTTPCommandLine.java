package ir.kasra_sh;

import ir.kasra_sh.MikroWebServer.HTTPUtils.HTTPConnection;
import ir.kasra_sh.MikroWebServer.HTTPUtils.ResponseWriter;
import ir.kasra_sh.MikroWebServer.HTTPUtils.RequestParser;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class HTTPCommandLine {
    private ServerSocket s;
    private Socket socket;
    private RequestParser rp = new RequestParser(null);


    public HTTPCommandLine(int port) throws IOException {
        s = new ServerSocket();
        s.bind(new InetSocketAddress(port));
    }

    public HTTPConnection listen() throws IOException {
        try {
            socket.close();
        } catch (Exception e){ }

        socket = s.accept();
        byte[] b = new byte[2048];
        int l = socket.getInputStream().read(b);
        int h = rp.parseHeaders(b,0,l);
        if (h>0) {
            HTTPConnection c;
            c = rp.getHTTPConnection();
            c.writer = new ResponseWriter(socket.getOutputStream());
            return c;
        } else
            return null;

    }
}
