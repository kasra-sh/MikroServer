package ir.kasra_sh.Handlers;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import ir.kasra_sh.MikroWebServer.HTTPUtils.HTTPConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

class Proxifier extends Fiber<Void> {
    private Socket s;
    private InetSocketAddress ad;
    private HTTPConnection hc;
    Proxifier(HTTPConnection c, InetSocketAddress addr){
        ad = addr;
        hc = c;
    }
    @Override
    protected Void run() throws SuspendExecution, InterruptedException {
        try {
            byte[] buff = new byte[8192];
            s = new Socket(ad.getAddress(),ad.getPort());
            InputStream is = s.getInputStream();
            OutputStream os = s.getOutputStream();
            StringBuilder hdr = new StringBuilder(2048);
            hdr.append(hc.getMethod().toString()).append(" ").append(hc.getRoute()).append(" HTTP/1.1\r\n");
            //System.out.print(hdr);
            //os.write(hdr.getBytes());
            //os.flush();
            for (Map.Entry<Object,Object> e:
                    hc.getHeaders().entrySet()) {
                hdr.append(e.getKey().toString()).append(": ").append(e.getValue().toString()).append("\r\n");
            }
            hdr.append("\r\n");
            os.write(hdr.toString().getBytes());
            //System.out.println("os.write(hdr)");
            //System.out.println(hdr);
            os.flush();
            //os.close();
            s.shutdownOutput();
            //System.out.println("os.flush()");
            while (true) {
                int l = is.read(buff);
                if (l<=0) break;
                //System.out.println("read "+l);
                hc.writer.write(buff, 0, l);
                if (s.isInputShutdown()) break;

            }
            hc.writer.finish();
            s.close();
            //Fiber.sleep(0,1);
            //System.out.println("writer.finish()");
        } catch (IOException e) {
            //e.printStackTrace();
            //System.out.println("\n\nBroke !!!\n\n");
            try {
                s.close();
            }catch (Exception ee){

            }
            try {
                hc.writer.finish();
            }catch (Exception eee){

            }
            //e.printStackTrace();
        }
        return null;
    }
}