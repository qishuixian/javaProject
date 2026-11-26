package learning.boot.day38;

import org.springframework.stereotype.Service;

@Service
public class TimedTeacherService {
    @Timed
    public String findName() {
        return "张老师";
    }

    public String selfCall() {
        // 普通 this 调用不经过 Spring 代理，findName 上的切面不会触发。
        return this.findName();
    }
}
