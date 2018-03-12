package ir.kasra_sh.MikroServer.Server;

import java.net.Socket;

public interface SocketReceiver {
    void onReceive(Socket s);
}
