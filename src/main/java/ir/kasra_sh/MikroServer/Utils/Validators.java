package ir.kasra_sh.MikroServer.Utils;

public class Validators {
    public static boolean validateFilePath(String path) {
        if (path.contains("../")) {
            return false;
        }
        return true;
    }
}
