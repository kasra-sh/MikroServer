package ir.kasra_sh.MikroWebServer.IO;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import ir.kasra_sh.MikroWebServer.HTTPUtils.HTTPConnection;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class FiberStreamWriter extends Fiber<Void> {
    InputStream is;
    HTTPConnection con;
    public FiberStreamWriter(InputStream inputStream, HTTPConnection con){
        is = inputStream;
        this.con = con;
    }

    @Override
    protected Void run() throws SuspendExecution, InterruptedException {
        try {
            byte[] buff = new byte[4096];
            // FIXME: 11/14/17
            //System.out.println("FSW before loop !");
            while (true) {
                int tr = is.read(buff);
                if (tr<=0) break;
                if (tr<0) throw new Exception("File read error!");
                con.writer.write(buff, 0, tr);
                //Fiber.park(1, TimeUnit.NANOSECONDS);
            }

            // FIXME: 11/14/17
            //System.out.println("FSW after loop !");
            con.writer.finish();
            //System.out.println("FiberStreamWriter finished !");
        } catch (Exception e){
            con.writer.finish();
            System.out.println("FSW error !");
            //e.printStackTrace();
        }
        return null;

    }
}