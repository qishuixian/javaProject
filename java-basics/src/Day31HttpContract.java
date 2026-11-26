import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class Day31HttpContract {
    private static String queryParameter(URI uri, String name) {
        String query = uri.getRawQuery();
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2 && name.equals(parts[0])) return parts[1];
        }
        return null;
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/teachers", exchange -> {
            if (!"GET".equals(exchange.getRequestMethod())) {
                respond(exchange, 405, "{\"code\":\"METHOD_NOT_ALLOWED\"}");
                return;
            }
            String id = queryParameter(exchange.getRequestURI(), "id");
            if (id == null || id.isEmpty()) {
                respond(exchange, 400, "{\"code\":\"INVALID_ID\",\"message\":\"id 必填\"}");
                return;
            }
            if ("10001".equals(id)) {
                respond(exchange, 200, "{\"data\":{\"id\":10001,\"name\":\"张老师\"}}");
            } else {
                respond(exchange, 404, "{\"code\":\"TEACHER_NOT_FOUND\"}");
            }
        });
        server.start();
        System.out.println("HTTP 服务已启动：" + server.getAddress().getPort());
        System.out.println("GET /teachers?id=10001 → 200");
        System.out.println("GET /teachers → 400");
        server.stop(0);
        System.out.println("HTTP 服务已停止");
    }
}
