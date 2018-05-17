package ir.kasra_sh.MikroServer.Server;

import ir.kasra_sh.MikroServer.Utils.KSocket;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    public static boolean DEBUG = true;
    public static void logReqFinish(KSocket sock, long start, int code, String route, String ... extra){
        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MMM/yyyy hh:mm:ssa"))
                + "> "
                + code
                + " - "
                + sock.getSocket().getInetAddress().toString().substring(1)
                + " - "
                + route
                + " - "
                + (System.currentTimeMillis()-start) + "ms");
        for (String s:
             extra) {
            System.out.println("EXTRA>>>>>>>>>>>>>>>\n\n"+s+"\n\n<<<<<<<<<<<<<<<<<EXTRA");
        }
    }
}
