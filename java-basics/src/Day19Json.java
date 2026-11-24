import java.util.ArrayList;
import java.util.List;

public class Day19Json {
    public static String toJson(Teacher teacher) {
        if (teacher == null) {
            throw new IllegalArgumentException("教师不能为 null");
        }
        return "{\"id\":" + teacher.getId() + ",\"name\":\""
                + escape(teacher.getName()) + "\"}";
    }

    public static Teacher fromJson(String json) {
        if (json == null) throw new IllegalArgumentException("JSON 不能为 null");
        String text = json.trim();
        if (!text.startsWith("{") || !text.endsWith("}")) throw new IllegalArgumentException("JSON 必须是对象");
        String body = text.substring(1, text.length() - 1);
        List<String> fields = splitFields(body);
        Long id = null;
        String name = null;
        for (String field : fields) {
            int colon = field.indexOf(':');
            if (colon <= 0) throw new IllegalArgumentException("字段缺少冒号");
            String key = field.substring(0, colon).trim();
            String value = field.substring(colon + 1).trim();
            if ("\"id\"".equals(key)) {
                id = Long.parseLong(value);
            } else if ("\"name\"".equals(key)) {
                name = unquote(value);
            } else {
                throw new IllegalArgumentException("未知字段：" + key);
            }
        }
        if (id == null || name == null) throw new IllegalArgumentException("缺少 id 或 name");
        return new Teacher(id, name);
    }

    private static List<String> splitFields(String body) {
        List<String> result = new ArrayList<>();
        boolean quoted = false;
        int start = 0;
        for (int i = 0; i < body.length(); i++) {
            char c = body.charAt(i);
            if (c == '"' && (i == 0 || body.charAt(i - 1) != '\\')) quoted = !quoted;
            if (c == ',' && !quoted) {
                result.add(body.substring(start, i));
                start = i + 1;
            }
        }
        if (quoted) throw new IllegalArgumentException("字符串引号不完整");
        if (start < body.length()) result.add(body.substring(start));
        return result;
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }

    private static String unquote(String value) {
        if (value.length() < 2 || value.charAt(0) != '"' || value.charAt(value.length() - 1) != '"') {
            throw new IllegalArgumentException("name 必须是字符串");
        }
        String content = value.substring(1, value.length() - 1);
        return content.replace("\\\"", "\"").replace("\\n", "\n")
                .replace("\\r", "\r").replace("\\\\", "\\");
    }

    public static void main(String[] args) {
        Teacher source = new Teacher(10001L, "张\"老师");
        String json = toJson(source);
        Teacher restored = fromJson(json);
        System.out.println("序列化：" + json);
        System.out.println("解析后：" + restored.getId() + " / " + restored.getName());
        System.out.println("内容一致：" + (source.getId() == restored.getId()
                && source.getName().equals(restored.getName())));
        String[] invalid = {"{}", "{\"id\":\"abc\",\"name\":\"王老师\"}",
                "{\"id\":10002,\"name\":\"王老师\",\"extra\":true}"};
        for (String value : invalid) {
            try {
                fromJson(value);
            } catch (RuntimeException exception) {
                System.out.println("拒绝 JSON：" + exception.getMessage());
            }
        }
    }
}
