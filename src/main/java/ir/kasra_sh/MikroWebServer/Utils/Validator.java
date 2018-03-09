package ir.kasra_sh.MikroWebServer.Utils;

public class Validator {
    public static boolean validateFilePath(String path) {
        if (path.contains("../")) {
            return false;
        }
        return true;
    }
}
