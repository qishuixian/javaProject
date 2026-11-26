package learning.persistence;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class TeacherSearchService {
    private final Day43TeacherMapper mapper;
    public TeacherSearchService(Day43TeacherMapper mapper) { this.mapper = mapper; }

    public List<TeacherRow> search(String name, String subject, Long minId, String sort, String direction) {
        String sortKey = normalize(sort);
        String order = normalize(direction);
        sortKey = sortKey == null ? "id" : sortKey.toLowerCase(Locale.ROOT);
        order = order == null ? "asc" : order.toLowerCase(Locale.ROOT);
        if (!Arrays.asList("id", "name", "subject").contains(sortKey)) {
            throw new IllegalArgumentException("排序字段只允许 id、name、subject");
        }
        if (!Arrays.asList("asc", "desc").contains(order)) {
            throw new IllegalArgumentException("排序方向只允许 asc、desc");
        }
        if (minId != null && minId < 1) {
            throw new IllegalArgumentException("最小编号必须大于等于1");
        }
        return mapper.search(normalize(name), normalize(subject), minId, sortKey, order);
    }

    private String normalize(String value) {
        if (value == null) { return null; }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
