package learning.boot.day40;

import learning.boot.day35.TeacherConverter;
import learning.boot.day35.TeacherEntity;
import learning.boot.day35.TeacherResponse;
import learning.boot.day36.ValidatedTeacherRequest;
import learning.boot.day37.TeacherNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class TeacherCrudService {
    private final MemoryTeacherRepository repository;
    public TeacherCrudService(MemoryTeacherRepository repository) { this.repository = repository; }

    public TeacherResponse create(ValidatedTeacherRequest request) {
        return TeacherConverter.toResponse(repository.create(request.getName(), request.getSubject()));
    }
    public TeacherResponse find(long id) { return response(repository.find(id)); }
    public TeacherResponse update(long id, ValidatedTeacherRequest request) {
        return response(repository.update(id, request.getName(), request.getSubject()));
    }
    private TeacherResponse response(TeacherEntity entity) {
        if (entity == null) { throw new TeacherNotFoundException(); }
        return TeacherConverter.toResponse(entity);
    }
}
