package ir.kasra_sh.MikroWebServer.Utils;

import java.util.concurrent.TimeUnit;

public class Sleeper {
    public void sleepMS(long ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) { }
    }
    public void sleepNS(long ns){
        try {
            TimeUnit.NANOSECONDS.sleep(ns);
        } catch (InterruptedException e) { }
    }
    public void sleepMicS(long mics){
        try {
            TimeUnit.MICROSECONDS.sleep(mics);
        } catch (InterruptedException e) { }
    }
}
