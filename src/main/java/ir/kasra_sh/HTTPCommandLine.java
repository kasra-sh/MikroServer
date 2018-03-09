package ir.kasra_sh;

import ir.kasra_sh.MikroWebServer.HTTPUtils.HTTPConnectionEx;
import ir.kasra_sh.MikroWebServer.HTTPUtils.RequestParserEx;
import ir.kasra_sh.MikroWebServer.HTTPUtils.ResponseWriterEx;
import ir.kasra_sh.MikroWebServer.HTTPUtils.SocketIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class HTTPCommandLine {
    private ServerSocket s;
    private SocketIO socket;
    private RequestParserEx rp;


    public HTTPCommandLine(int port) throws IOException {
        s = new ServerSocket();
        s.bind(new InetSocketAddress(port));
    }

    public HTTPConnectionEx listen() throws IOException {
        try {
            socket.close();
        } catch (Exception e){ }

        socket = new SocketIO(s.accept());
        rp = new RequestParserEx(socket);
        rp.parseHeader();
        if (rp.getErrCode() == 0) {
            HTTPConnectionEx c;
            c = rp.getHTTPConnection();
            c.writer = new ResponseWriterEx(socket);
            return c;
        } else
            return null;

    }
}
