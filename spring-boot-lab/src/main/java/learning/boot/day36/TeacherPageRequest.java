package learning.boot.day36;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class TeacherPageRequest {
    @NotNull(message = "页码不能为null")
    @Min(value = 1, message = "页码必须大于等于1")
    private Integer page = 1;

    @NotNull(message = "每页数量不能为null")
    @Min(value = 1, message = "每页数量必须大于等于1")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer pageSize = 20;

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
}
