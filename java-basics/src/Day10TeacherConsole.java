import java.util.Scanner;

public class Day10TeacherConsole {
    private static final int CAPACITY = 3;

    public static void main(String[] args) {
        TeacherDirectory directory = new TeacherDirectory(CAPACITY);
        Scanner scanner = new Scanner(System.in);
        System.out.println("教师管理：1 新增，2 查询，3 改名，0 退出（容量 3）");
        while (true) {
            String command = readLine(scanner, "请输入操作：");
            if (command == null || "0".equals(command)) {
                break;
            }
            if (!"1".equals(command) && !"2".equals(command) && !"3".equals(command)) {
                System.out.println("未知操作，请输入 0、1、2 或 3");
                continue;
            }

            String idText = readLine(scanner, "请输入教师 ID：");
            if (idText == null) {
                break;
            }
            try {
                long id = Long.parseLong(idText);
                if (id <= 0) {
                    throw new IllegalArgumentException("教师 ID 必须大于 0");
                }
                if ("2".equals(command)) {
                    printTeacher(directory, id);
                    continue;
                }
                String name = readLine(scanner, "请输入姓名：");
                if (name == null) {
                    break;
                }
                if ("1".equals(command)) {
                    directory.add(id, name);
                    System.out.println("新增成功，当前人数：" + directory.size());
                } else {
                    directory.rename(id, name);
                    System.out.println("改名成功");
                }
            } catch (NumberFormatException exception) {
                System.out.println("输入错误：ID 必须是 long 范围内的整数");
            } catch (IllegalArgumentException exception) {
                System.out.println("操作失败：" + exception.getMessage());
            }
        }
        scanner.close();
        System.out.println("已退出");
    }

    private static String readLine(Scanner scanner, String prompt) {
        System.out.println(prompt);
        if (!scanner.hasNextLine()) {
            return null;
        }
        return scanner.nextLine().trim();
    }

    // 查询只需要 Day7 的接口能力。
    private static void printTeacher(TeacherRepository repository, long id) {
        Teacher teacher = repository.findById(id);
        if (teacher == null) {
            System.out.println("未找到教师：" + id);
        } else {
            System.out.println("教师：" + teacher.getId() + " / " + teacher.getName());
        }
    }
}
