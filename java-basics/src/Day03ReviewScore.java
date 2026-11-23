public class Day03ReviewScore {
    public static String reviewStatus(int score) {
        if (score < 0 || score > 100) {
            return "INVALID";
        }
        if (score >= 60) {
            return "PASSED";
        }
        return "PENDING";
    }

    public static void main(String[] args) {
        int[] scores = {-1, 0, 59, 60, 100, 101};
        for (int score : scores) {
            System.out.println("分数 " + score + "：" + reviewStatus(score));
        }
    }
}
