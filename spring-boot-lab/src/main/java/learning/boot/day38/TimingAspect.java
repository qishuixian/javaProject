package learning.boot.day38;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TimingAspect {
    private static final Logger LOG = LoggerFactory.getLogger(TimingAspect.class);

    @Around("@annotation(learning.boot.day38.Timed)")
    public Object measure(ProceedingJoinPoint point) throws Throwable {
        long start = System.nanoTime();
        boolean success = false;
        try {
            Object result = point.proceed();
            success = true;
            return result;
        } finally {
            // 不吞异常、不修改返回值；成功与失败都记录耗时。
            double elapsedMs = (System.nanoTime() - start) / 1_000_000.0;
            LOG.info("DAY38_TIMING method={} success={} elapsedMs={}",
                    point.getSignature().toShortString(), success, elapsedMs);
        }
    }
}
