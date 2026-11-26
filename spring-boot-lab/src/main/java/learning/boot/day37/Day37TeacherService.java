package learning.boot.day37;

import learning.boot.day35.TeacherResponse;
import org.springframework.stereotype.Service;

@Service
public class Day37TeacherService {
    public TeacherResponse find(long id) {
        if (id != 10001L) {
            throw new TeacherNotFoundException();
        }
        return new TeacherResponse(10001L, "张老师", "Java");
    }

    public TeacherResponse simulateFailure() {
        // 仅供学习异常链路：此文本必须只出现在服务端日志。
        throw new IllegalStateException("DAY37_INTERNAL_DEMO_FAILURE");
    }
}
