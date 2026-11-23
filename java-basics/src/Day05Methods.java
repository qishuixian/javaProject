public class Day05Methods {
    // 返回校验结果：null、负时长不合法；空数组合法。
    public static boolean isValidMinutes(int[] minutes) {
        if (minutes == null) {
            return false;
        }
        for (int value : minutes) {
            if (value < 0) {
                return false;
            }
        }
        return true;
    }

    // 调用前先使用 isValidMinutes 校验；long 避免普通 int 累加溢出。
    public static long totalMinutes(int[] minutes) {
        long total = 0;
        for (int value : minutes) {
            total += value;
        }
        return total;
    }

    // 空数组的平均值约定为 0.0；非空时转成 double，避免整数除法。
    public static double averageMinutes(int[] minutes) {
        if (minutes.length == 0) {
            return 0.0;
        }
        return (double) totalMinutes(minutes) / minutes.length;
    }

    // void 表示不返回结果；return 可以提前结束方法。
    public static void printSummary(int[] minutes) {
        if (!isValidMinutes(minutes)) {
            System.out.println("时长不合法：数组不能为 null，元素不能为负数");
            return;
        }
        System.out.println("课节数：" + minutes.length);
        System.out.println("总时长：" + totalMinutes(minutes));
        System.out.println("平均时长：" + averageMinutes(minutes));
    }

    public static void changeNumber(int number) {
        number = 99; // 修改形参，不影响调用方的变量。
    }

    public static void changeFirst(int[] minutes) {
        minutes[0] = 99; // 两份引用指向同一个数组，修改元素对调用方可见。
    }

    public static void replaceArray(int[] minutes) {
        minutes = new int[] {999}; // 只让形参指向新数组。
    }

    public static void main(String[] args) {
        printSummary(new int[] {30, 45, 60, 90});
        printSummary(new int[0]);
        printSummary(new int[] {30, -1});
        printSummary(null);

        int number = 30;
        changeNumber(number);
        System.out.println("修改基本类型形参后：" + number);

        int[] minutes = {30, 45};
        changeFirst(minutes);
        System.out.println("修改数组元素后：" + minutes[0]);
        replaceArray(minutes);
        System.out.println("给数组形参重新赋值后：" + minutes[0]);
    }
}
