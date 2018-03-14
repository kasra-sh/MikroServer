package ir.kasra_sh.MikroServer;

import com.google.gson.Gson;
import ir.kasra_sh.MikroServer.Server.Handler;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MultiHandler extends Handler {
    @Override
    public int handle() {
        //System.out.println(conn.getMultiPart().keySet());
        conn.getMultiPart();
        try {
            Gson gs = new Gson();
            //gs.toJson();
            Files.write(Paths.get("/home/blkr/xxx.png"),conn.getFormData("photo"));
            res.writeResponse(200,"OK!");
            conn.kSocket().flush();
            conn.kSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}