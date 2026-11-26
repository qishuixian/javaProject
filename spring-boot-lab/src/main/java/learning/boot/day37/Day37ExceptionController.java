package learning.boot.day37;

import javax.validation.Valid;
import learning.boot.day35.TeacherResponse;
import learning.boot.day36.ValidatedTeacherRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/day37/teachers")
public class Day37ExceptionController {
    private final Day37TeacherService service;

    public Day37ExceptionController(Day37TeacherService service) { this.service = service; }

    @GetMapping("/{id}")
    public TeacherResponse find(@PathVariable("id") long id) { return service.find(id); }

    @PostMapping("/preview")
    public TeacherResponse preview(@Valid @RequestBody ValidatedTeacherRequest request) {
        return new TeacherResponse(null, request.getName(), request.getSubject());
    }

    @GetMapping("/simulate-error")
    public TeacherResponse simulateError() { return service.simulateFailure(); }
}
