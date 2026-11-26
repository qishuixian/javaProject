package learning.boot.day36;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.validation.Valid;
import learning.boot.day35.TeacherCreateRequest;
import learning.boot.day35.TeacherLayerService;
import learning.boot.day35.TeacherResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/day36/teachers")
public class Day36ValidationController {
    private final TeacherLayerService service;

    public Day36ValidationController(TeacherLayerService service) {
        this.service = service;
    }

    @PostMapping("/preview")
    public TeacherResponse preview(@Valid @RequestBody ValidatedTeacherRequest request) {
        // 校验通过才进入方法体；复用 Day35 的转换与预览流程。
        TeacherCreateRequest input = new TeacherCreateRequest();
        input.setName(request.getName());
        input.setSubject(request.getSubject());
        return service.preview(input);
    }

    @PostMapping("/query-preview")
    public Map<String, Object> query(@Valid @RequestBody TeacherPageRequest request) {
        // 只回显通过校验的分页参数，不执行数据库查询。
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("page", request.getPage());
        result.put("pageSize", request.getPageSize());
        return result;
    }
}
