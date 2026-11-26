package learning.boot.day40;

import java.net.URI;
import javax.validation.Valid;
import learning.boot.day35.TeacherResponse;
import learning.boot.day36.ValidatedTeacherRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/day40/teachers")
public class Day40TeacherController {
    private final TeacherCrudService service;
    public Day40TeacherController(TeacherCrudService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<TeacherResponse> create(@Valid @RequestBody ValidatedTeacherRequest request) {
        TeacherResponse teacher = service.create(request);
        return ResponseEntity.created(URI.create("/api/day40/teachers/" + teacher.getId())).body(teacher);
    }
    @GetMapping("/{id}")
    public TeacherResponse find(@PathVariable("id") long id) { return service.find(id); }

    @PutMapping("/{id}")
    public TeacherResponse update(@PathVariable("id") long id,
            @Valid @RequestBody ValidatedTeacherRequest request) {
        return service.update(id, request);
    }
}
