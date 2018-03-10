package ir.kasra_sh;

import ir.kasra_sh.HTTPUtils.HTTPConnection;
import ir.kasra_sh.HTTPUtils.RequestParser;
import ir.kasra_sh.HTTPUtils.ResponseWriter;
import ir.kasra_sh.HTTPUtils.SocketIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class HTTPCommandLine {
    private ServerSocket s;
    private SocketIO socket;
    private RequestParser rp;


    public HTTPCommandLine(int port) throws IOException {
        s = new ServerSocket();
        s.bind(new InetSocketAddress(port));
    }

    public HTTPConnection listen() throws IOException {
        try {
            socket.close();
        } catch (Exception e){ }

        socket = new SocketIO(s.accept());
        rp = new RequestParser(socket);
        rp.parseHeader();
        if (rp.getErrCode() == 0) {
            HTTPConnection c;
            c = rp.getHTTPConnection();
            c.writer = new ResponseWriter(socket);
            return c;
        } else
            return null;

    }
}
