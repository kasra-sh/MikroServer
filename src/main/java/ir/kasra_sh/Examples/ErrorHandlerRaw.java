package ir.kasra_sh.Examples;

import ir.kasra_sh.HTTPUtils.ResponseCode;
import ir.kasra_sh.MikroWebServer.IO.Handler;
import ir.kasra_sh.MikroWebServer.Utils.MimeTypes;

import java.io.IOException;

public class ErrorHandlerRaw extends Handler {
    @Override
    public int handle() {
        //conn.writer.getHeader().setStatus(ResponseCode.NOT_FOUND);
        //String rt = conn.getRoute();
        //conn.writer.getHeader().setStatus(ResponseCode.OK);
        conn.writer.getHeader().setContentType(MimeTypes.Text.HTML);
/*
        StringBuilder sb = new StringBuilder(2048);
        sb.append(rt).append("<br>");
        for (Map.Entry e:
                conn.getHeaders().entrySet()) {
            sb.append(e.getKey().toString()).append(":").append(e.getValue()).append("<br>");
        }*/
        try {
            conn.writer.writeResponse(ResponseCode.NOT_FOUND, html);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        //conn.writer.finish();
        return 0;
    }


    static String html = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "  <meta charset=\"utf-8\">\n" +
            "  <title>(404) The page you were looking for doesn't exist.</title>\n" +
            "  <link rel=\"stylesheet\" type=\"text/css\" href=\"//cloud.typography.com/746852/739588/css/fonts.css\" />\n" +
            "  <style type=\"text/css\">\n" +
            "    html,\n" +
            "    body {\n" +
            "      margin: 0;\n" +
            "      padding: 0;\n" +
            "      height: 100%;\n" +
            "    }\n" +
            "\n" +
            "    body {\n" +
            "      font-family: \"Whitney SSm A\", \"Whitney SSm B\", \"Helvetica Neue\", Helvetica, Arial, Sans-Serif;\n" +
            "      background-color: #2D72D9;\n" +
            "      color: #fff;\n" +
            "      -moz-font-smoothing: antialiased;\n" +
            "      -webkit-font-smoothing: antialiased;\n" +
            "    }\n" +
            "\n" +
            "    .error-container {\n" +
            "      text-align: center;\n" +
            "      height: 100%;\n" +
            "    }\n" +
            "\n" +
            "    @media (max-width: 480px) {\n" +
            "      .error-container {\n" +
            "        position: relative;\n" +
            "        top: 50%;\n" +
            "        height: initial;\n" +
            "        -webkit-transform: translateY(-50%);\n" +
            "        -ms-transform: translateY(-50%);\n" +
            "        transform: translateY(-50%);\n" +
            "      }\n" +
            "    }\n" +
            "\n" +
            "    .error-container h1 {\n" +
            "      margin: 0;\n" +
            "      font-size: 130px;\n" +
            "      font-weight: 300;\n" +
            "    }\n" +
            "\n" +
            "    @media (min-width: 480px) {\n" +
            "      .error-container h1 {\n" +
            "        position: relative;\n" +
            "        top: 50%;\n" +
            "        -webkit-transform: translateY(-50%);\n" +
            "        -ms-transform: translateY(-50%);\n" +
            "        transform: translateY(-50%);\n" +
            "      }\n" +
            "    }\n" +
            "\n" +
            "    @media (min-width: 768px) {\n" +
            "      .error-container h1 {\n" +
            "        font-size: 220px;\n" +
            "      }\n" +
            "    }\n" +
            "\n" +
            "    .return {\n" +
            "      color: rgba(255, 255, 255, 0.6);\n" +
            "      font-weight: 400;\n" +
            "      letter-spacing: -0.04em;\n" +
            "      margin: 0;\n" +
            "    }\n" +
            "\n" +
            "    @media (min-width: 480px) {\n" +
            "      .return {\n" +
            "        position: absolute;\n" +
            "        width: 100%;\n" +
            "        bottom: 30px;\n" +
            "      }\n" +
            "    }\n" +
            "\n" +
            "    .return a {\n" +
            "      padding-bottom: 1px;\n" +
            "      color: #fff;\n" +
            "      text-decoration: none;\n" +
            "      border-bottom: 1px solid rgba(255, 255, 255, 0.6);\n" +
            "      -webkit-transition: border-color 0.1s ease-in;\n" +
            "      transition: border-color 0.1s ease-in;\n" +
            "    }\n" +
            "\n" +
            "    .return a:hover {\n" +
            "      border-bottom-color: #fff;\n" +
            "    }\n" +
            "  </style>\n" +
            "</head>\n" +
            "\n" +
            "<body>\n" +
            "\n" +
            "<div class=\"error-container\">\n" +
            "  <h1>404</h1>\n" +
            //"  <p class=\"return\">Take me back to <a href=\"/\">designernews.co</a></p>\n" +
            "</div>\n" +
            "\n" +
            "</body>\n" +
            "</html>\n";
}
