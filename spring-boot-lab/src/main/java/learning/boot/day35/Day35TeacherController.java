package learning.boot.day35;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/day35/teachers")
public class Day35TeacherController {
    private final TeacherLayerService service;

    public Day35TeacherController(TeacherLayerService service) {
        this.service = service;
    }

    @GetMapping("/example")
    public TeacherResponse example() {
        return service.example();
    }

    @PostMapping("/preview")
    public TeacherResponse preview(@RequestBody TeacherCreateRequest request) {
        return service.preview(request);
    }
}
