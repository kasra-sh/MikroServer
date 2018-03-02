package ir.kasra_sh.MikroWebServer.Utils;

public class MemoryCleaner {
    private static int lc=0,mc=0,hc=0;
    private static int heavyGCCount=0;
    private static int lightCap=5000;
    private static int heavyCap=10000;
    private static Sleeper sleeper =new Sleeper();


    public static void autoGC(){
        lc++;
        hc++;
        if (hc>=heavyCap){
            heavyGC();
        }
        if (lc>=lightCap){
            lightGC();
        }
    }
    public static void lightGC(){
        lc++;
        if (lc < lightCap){
            return;
        }
        lc=0;

        System.gc();
        System.out.println("LightClean");
        //sleeper.sleepMS(1);
    }


    private static void heavyGC(){
        hc++;
        if (hc < heavyCap){
            return;
        }
        hc=0;
        lc=0;

        System.gc();

        //sleeper.sleepMS(3);

        System.runFinalization();

        //sleeper.sleepMS(3);

        System.gc();

        System.out.println("HeavyClean");
    }
}
