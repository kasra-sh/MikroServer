package ir.kasra_sh;

import co.paralleluniverse.fibers.SuspendExecution;
import com.google.gson.Gson;
import ir.kasra_sh.Handlers.*;
import ir.kasra_sh.MikroWebServer.HTTPUtils.HTTPConnectionEx;
import ir.kasra_sh.MikroWebServer.HTTPUtils.ResponseCode;
import ir.kasra_sh.MikroWebServer.IO.LightWebServerEx;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;

public class Main {

    private static boolean started = false;
    private static LightWebServerEx lws;

    public static void main(String[] args) throws IOException {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                throwable.printStackTrace();
            }
        });


        Scanner input  = new Scanner(System.in);
        System.setProperty("co.paralleluniverse.fibers.detectRunawayFibers","false");


        HTTPCommandLine cmd = new HTTPCommandLine(9095);
        while (true) {
            HTTPConnectionEx hc = cmd.listen();
            if (hc.getRoute().startsWith("/start")){
                //System.out.println("START");
                if (hc.getOption("port")!=null && !started) {
                    hc.writer.getHeader().setStatus(ResponseCode.OK);
                    hc.writer.writeAll("OK !");
                    System.out.println("ON PORT " + hc.getOption("port"));
                    int port = Integer.valueOf(hc.getOption("port"));
                    if (hc.getOption("root")!=null){
                        String root = hc.getOption("root");
                        start(port,root);
                    } else
                        start(port,System.getProperty("os.home"));

                } else {
                    hc.writer.getHeader().setStatus(ResponseCode.BAD_REQUEST);
                    hc.writer.writeAll("Error !");
                }
            }

            if (hc.getRoute().startsWith("/stop")) {
                if (started)
                    stop();
                hc.writer.writeAll("OK !");
            }

        }

    }


    private static void start(int port, String root){
        if (started) return;
        lws = new LightWebServerEx();
        //LightWebServerEx keyserver = new LightWebServerEx();
        LightWebServerEx errServer = new LightWebServerEx();
        LightWebServerEx lx = new LightWebServerEx();
        //LightWebServer proxyServer = new LightWebServer();
        //lws.useTLS("/home/blkr/www/keystore.jks");
        try {
            //proxyServer.addProxyPath("/resources*",new InetSocketAddress("localhost",8080));
            lx.addFileHandler("/files*",root, new FileServerHandler());
            RandomKeyHandler.root = root+"/";
            lx.addContextHandler("/genkey*",new RandomKeyHandler());
            lx.addProxyPath("/404*", new InetSocketAddress("localhost",8000));
            errServer.addContextHandler("/404", new ErrorHandler());

            lx.start(8080, 100);
            Thread.sleep(200);
            errServer.start(8000,10);
            //proxyServer.startDynamic(8000,40);
            started = true;
        } catch (IOException e) {
            e.printStackTrace();
            started = false;
        } catch (SuspendExecution suspendExecution) {
            started = false;
            suspendExecution.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void stop(){
        //lws.stop();
        started = false;
    }
}
