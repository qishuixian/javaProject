package learning.persistence;

import org.springframework.stereotype.Service;

@Service
public class TeacherPageService {
    private final Day44TeacherMapper mapper;
    public TeacherPageService(Day44TeacherMapper mapper) { this.mapper = mapper; }

    public TeacherPage query(String subject, int page, int pageSize) {
        if (page < 1) { throw new IllegalArgumentException("页码必须大于等于1"); }
        if (pageSize < 1 || pageSize > 100) { throw new IllegalArgumentException("每页数量必须为1到100"); }
        String filter = subject == null ? null : subject.trim();
        if (filter != null && filter.isEmpty()) { filter = null; }
        // 先转 long 再乘，避免 int 溢出。页码从1开始，偏移量从0开始。
        long offset = ((long) page - 1) * pageSize;
        long total = mapper.count(filter);
        return new TeacherPage(page, pageSize, total, mapper.page(filter, pageSize, offset));
    }
}
