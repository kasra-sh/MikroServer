package ir.kasra_sh.MikroWebServer.IO;

import java.net.Socket;

public interface SocketReceiver {
    void onReceive(Socket s);
}
