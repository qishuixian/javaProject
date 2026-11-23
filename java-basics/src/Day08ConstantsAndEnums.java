public class Day08ConstantsAndEnums {
    // static：属于类；final：赋值后不能重新赋值。
    public static final int MIN_SCORE = 0;
    public static final int MAX_SCORE = 100;
    public static final int PASS_SCORE = 60;

    // 返回类型从 Day3 的 String 变为 ReviewStatus。
    public static ReviewStatus reviewStatus(int score) {
        if (score < MIN_SCORE || score > MAX_SCORE) {
            return ReviewStatus.INVALID;
        }
        if (score >= PASS_SCORE) {
            return ReviewStatus.PASSED;
        }
        return ReviewStatus.PENDING;
    }

    public static void main(String[] args) {
        System.out.println("通过分数：" + Day08ConstantsAndEnums.PASS_SCORE);
        int[] scores = {-1, 0, 59, 60, 100, 101};
        for (int score : scores) {
            ReviewStatus status = reviewStatus(score);
            System.out.println("分数 " + score + "：" + status);
        }

        ReviewStatus status = reviewStatus(80);
        // 枚举可以使用 == 比较；字符串内容比较则应使用 equals。
        if (status == ReviewStatus.PASSED) {
            System.out.println("审核已通过");
        }

        // final 限制变量不能换指向，不会冻结对象内容。
        final Teacher teacher = new Teacher(10001L, "张老师");
        teacher.rename("李老师");
        System.out.println("final 引用的对象改名后：" + teacher.getName());
        // teacher = new Teacher(10002L, "王老师"); // 取消注释会编译失败。

        final int[] minutes = {30, 45};
        minutes[0] = 60;
        System.out.println("final 数组的首个元素：" + minutes[0]);
        // minutes = new int[] {90}; // 取消注释会编译失败。
        // PASS_SCORE = 80; // 常量不能重新赋值。
    }
}
