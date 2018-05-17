package ir.kasra_sh.MikroServer;
//
//import com.google.gson.Gson;
//import com.google.gson.JsonObject;
import ir.kasra_sh.MikroServer.HTTPUtils.HTTPMethod;
import ir.kasra_sh.MikroServer.Server.Annotations.Methods;
import ir.kasra_sh.MikroServer.Server.Annotations.Route;
import ir.kasra_sh.MikroServer.Server.HTTPHandler;

import java.io.IOException;

@Methods(HTTPMethod.POST)
@Route("/api/getselfposts")
public class MultiHandler extends HTTPHandler {
    @Override
    public int handle() {
        try {
//            Gson gson = new Gson();
//            JsonObject jso = gson.fromJson(conn.getBody(), JsonObject.class);
//            String user_id = String.valueOf(jso.get("user_id").getAsLong());
//            System.out.println("user_id: "+user_id);
//            System.out.println(jso);
            res.writeResponse(200,"OK!");
            conn.kSocket().flush();
            conn.kSocket().close();
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return 0;
    }
}
