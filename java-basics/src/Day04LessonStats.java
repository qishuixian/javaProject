public class Day04LessonStats {
    public static void main(String[] args) {
        int[] lessonMinutes = {30, 45, 60, 90};
        boolean[] completed = {true, false, true, true};

        int totalMinutes = 0;
        int completedCount = 0;
        for (int i = 0; i < lessonMinutes.length; i++) {
            totalMinutes += lessonMinutes[i];
            if (completed[i]) {
                completedCount++;
            }
        }

        int total = 0;
        for (int minutes : lessonMinutes) {
            total += minutes;
        }

        double averageMinutes = lessonMinutes.length == 0
                ? 0.0
                : (double) totalMinutes / lessonMinutes.length;
        System.out.println("课节总数：" + lessonMinutes.length);
        System.out.println("已完成数：" + completedCount);
        System.out.println("总时长：" + totalMinutes + " 分钟");
        System.out.println("平均时长：" + averageMinutes + " 分钟");
        System.out.println(total);
    }
}
