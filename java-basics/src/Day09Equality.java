import java.util.HashSet;
import java.util.Set;

public class Day09Equality {
    public static void main(String[] args) {
        // 故意 new 两个字符串，清楚地演示“对象不同，内容相同”。
        String firstName = new String("张老师");
        String secondName = new String("张老师");
        System.out.println("字符串 ==：" + (firstName == secondName));
        System.out.println("字符串 equals：" + firstName.equals(secondName));

        String optionalName = null;
        System.out.println("已知非空字符串比较 null：" + "张老师".equals(optionalName));
        String title = "Java";
        title.concat(" 入门"); // 返回新字符串，原字符串不变。
        System.out.println("未接收 concat 返回值：" + title);
        title = title.concat(" 入门");
        System.out.println("接收 concat 返回值：" + title);

        Teacher firstTeacher = new Teacher(10001L, "张老师");
        Teacher secondTeacher = new Teacher(10001L, "张老师");
        Teacher sameReference = firstTeacher;
        System.out.println("同一个 Teacher 对象：" + (firstTeacher == sameReference));
        // Teacher 没有重写 equals，默认仍比较对象身份。
        System.out.println("两个 Teacher 的默认 equals：" + firstTeacher.equals(secondTeacher));

        TeacherIdentity firstId = new TeacherIdentity(10001L);
        TeacherIdentity sameId = new TeacherIdentity(10001L);
        TeacherIdentity otherId = new TeacherIdentity(10002L);
        System.out.println("身份对象 ==：" + (firstId == sameId));
        System.out.println("相同 ID 的 equals：" + firstId.equals(sameId));
        System.out.println("不同 ID 的 equals：" + firstId.equals(otherId));
        System.out.println("相等对象的哈希值相同：" + (firstId.hashCode() == sameId.hashCode()));
        System.out.println("身份与 null 比较：" + firstId.equals(null));
        System.out.println("身份与字符串比较：" + firstId.equals("10001"));

        // 仅预览集合去重，Day11 再系统学习 Set 和泛型。
        Set<TeacherIdentity> identities = new HashSet<>();
        System.out.println("首次添加：" + identities.add(firstId));
        System.out.println("添加相同 ID：" + identities.add(sameId));
        System.out.println("添加不同 ID：" + identities.add(otherId));
        System.out.println("去重后身份数：" + identities.size());

        // Java String 中这两个不同字符串恰好具有相同 hashCode。
        System.out.println("Aa 和 BB 哈希值相同：" + ("Aa".hashCode() == "BB".hashCode()));
        System.out.println("Aa 和 BB 内容相等：" + "Aa".equals("BB"));
    }
}
