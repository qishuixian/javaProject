import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PageResult<T> {
    private final List<T> records;
    private final long total;
    private final int pageNum;
    private final int pageSize;

    public PageResult(List<T> records, long total, int pageNum, int pageSize) {
        if (records == null || total < 0 || pageNum <= 0 || pageSize <= 0) throw new IllegalArgumentException("分页参数不合法");
        this.records = Collections.unmodifiableList(new ArrayList<>(records));
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }
    public List<T> getRecords() { return records; }
    public long getTotal() { return total; }
    public int getPageNum() { return pageNum; }
    public int getPageSize() { return pageSize; }
    public boolean isEmpty() { return records.isEmpty(); }
}
