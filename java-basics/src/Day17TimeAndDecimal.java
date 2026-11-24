import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Day17TimeAndDecimal {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static void main(String[] args) {
        LocalDate startDate = LocalDate.of(2026, 11, 24);
        LocalDate endDate = startDate.plusDays(6);
        System.out.println("课程开始日期：" + startDate);
        System.out.println("课程结束日期：" + endDate);
        System.out.println("日期间隔天数：" + ChronoUnit.DAYS.between(startDate, endDate));
        LocalDateTime start = LocalDateTime.of(startDate, LocalTime.of(19, 30));
        LocalDateTime end = start.plusMinutes(45);
        System.out.println("课程时间：" + start.format(FORMATTER) + " ~ " + end.format(FORMATTER));
        System.out.println("课程时长：" + Duration.between(start, end).toMinutes() + " 分钟");
        BigDecimal rate = new BigDecimal("7").divide(new BigDecimal("9"), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
        System.out.println("完成率：" + rate.setScale(2, RoundingMode.HALF_UP) + "%");
        System.out.println("double 的 0.1 + 0.2：" + (0.1 + 0.2));
        System.out.println("BigDecimal 的 0.1 + 0.2：" + new BigDecimal("0.1").add(new BigDecimal("0.2")));
        System.out.println("解析后日期加一天：" + LocalDate.parse("2026-12-01").plusDays(1));
    }
}
