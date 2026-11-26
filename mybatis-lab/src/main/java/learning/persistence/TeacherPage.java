package learning.persistence;

import java.util.List;

public class TeacherPage {
    private final int page;
    private final int pageSize;
    private final long total;
    private final List<TeacherRow> records;

    public TeacherPage(int page, int pageSize, long total, List<TeacherRow> records) {
        this.page = page; this.pageSize = pageSize; this.total = total; this.records = records;
    }
    public int getPage() { return page; }
    public int getPageSize() { return pageSize; }
    public long getTotal() { return total; }
    public List<TeacherRow> getRecords() { return records; }
    public int getCurrentCount() { return records.size(); }
    public long getTotalPages() { return total / pageSize + (total % pageSize == 0 ? 0 : 1); }
}
