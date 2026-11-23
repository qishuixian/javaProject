# Day 9：String、equals 与 hashCode

## 1. 今天要解决的问题

两份教师数据的 ID 一样，是否就算相等？两个字符串内容一样，为什么 `==` 可能返回 false？HashSet 又如何去重？

先记住三句话：

- 引用类型的 `==` 判断是否指向同一个对象；基本类型的 `==` 比较值。
- `equals` 是方法，具体比较规则由类实现，不能一概说它总是比较内容。
- 相等对象必须具有相同 hashCode，但哈希值相同的对象不一定相等。

## 2. 文件与运行

- [Day09Equality.java](../java-basics/src/Day09Equality.java)：运行入口。
- [TeacherIdentity.java](../java-basics/src/TeacherIdentity.java)：按 ID 定义相等规则的身份对象。
- [Teacher.java](../java-basics/src/Teacher.java)：继续使用 Day6 的原类，对比默认行为。

在 IDEA 打开 Day09Equality.java，点击 main 左侧绿色三角运行。无需脚本。前几课 Teacher 的相等行为保持原样，今天专门新增身份类来学习。

## 3. 字符串的 == 和 equals

```java
String first = new String("张老师");
String second = new String("张老师");

System.out.println(first == second);      // false
System.out.println(first.equals(second)); // true
```

两次 new 创建两个 String 对象。两个变量指向不同对象，所以 == 为 false；String 已经重写 equals，逐内容比较，所以 equals 为 true。

平时直接写 `String name = "张老师"` 即可，这里故意使用 new 只是让对象身份的差别更清楚。

```java
String a = "Java";
String b = "Java";
System.out.println(a == b); // 这里为 true：相同字面量共享字符串池中的对象。
```

不要因为这个结果就用 == 判断字符串内容。输入、接口返回值或运行时生成的字符串不一定共享对象；内容比较使用 equals。

### null 怎么比较

```java
String name = null;
System.out.println("张老师".equals(name)); // false
// name.equals("张老师"); // 空指针异常：null 不能调用方法。
```

如果两个变量都可能为 null，可以使用 `java.util.Objects.equals(a, b)`：两个都为 null 返回 true，一个为 null 返回 false，否则调用 equals。

## 4. String 不可变与 final 不同

```java
String title = "Java";
title.concat(" 入门");
System.out.println(title); // Java

title = title.concat(" 入门");
System.out.println(title); // Java 入门
```

concat 返回连接后的字符串，不修改原 String 对象。第二次是变量改为引用返回的字符串，并非原字符串内部变了。

String 的内容不可变是 String 类的设计；final 是限制变量不能重新赋值。普通 String 变量也指向不可变字符串，而 final Teacher 仍可以通过 rename 修改姓名，这是两个不同层面。

## 5. 自定义类的 equals 不会自动按字段比较

```java
Teacher a = new Teacher(10001L, "张老师");
Teacher b = new Teacher(10001L, "张老师");
System.out.println(a.equals(b)); // false
```

Teacher 没有重写 equals，继承的是 Object 默认方法，默认比较对象身份。所有普通 Java 类都直接或间接继承 Object，因此即使没有显式写 equals，也可以调用它。

如果业务规定“相同 ID 就是相同教师身份”，需要自己定义这个规则。本课新增 TeacherIdentity，只保留不可变 ID。假设 ID 在当前命名空间内唯一；若不同业务线可能复用 ID，身份规则还需要业务线等字段，不能只比较 ID。

## 6. 逐行读懂 equals

```java
@Override
public boolean equals(Object other) {
    if (this == other) {
        return true;
    }
    if (!(other instanceof TeacherIdentity)) {
        return false;
    }
    TeacherIdentity that = (TeacherIdentity) other;
    return this.id == that.id;
}
```

| 写法 | 解释 |
|---|---|
| @Override | 编译器检查是否正确重写 Object 的方法 |
| boolean | 返回相等或不相等 |
| Object other | 参数可以接收任意对象；签名必须匹配原方法 |
| this | 当前正在调用 equals 的对象 |
| this == other | 本来就是同一个对象，可以直接返回 true |
| other instanceof TeacherIdentity | 检查参数是否属于该类型；null 的检查结果也是 false |
| !(...) | 条件取反：不是该类型就不相等 |
| (TeacherIdentity) other | 已确认类型后，转换引用的静态类型，以便访问该类字段；不创建新对象 |
| this.id == that.id | 两个 long 基本类型按数值比较 |

`that` 只是局部变量名，可以换成 another。一个类的方法可以访问该类其他实例的 private 字段，因此 that.id 合法。

不能把签名改成 `equals(TeacherIdentity other)` 来替代重写：那是另一个重载方法，集合调用 Object 约定的 equals 时不会按你预期使用它。

本课将 TeacherIdentity 声明为 final，禁止子类改变相等规则，降低继承场景下保持规则一致的难度。

## 7. 为什么还要写 hashCode

```java
@Override
public int hashCode() {
    return Long.hashCode(id);
}
```

hashCode 返回一个 int，用于哈希集合定位候选元素，不能当作全球唯一 ID、对象内存地址或加密摘要。

我们的 equals 比较 id，所以 hashCode 也基于同一个 id。Long.hashCode(id) 是 Java 8 提供的静态方法，将 long 数值转换成 int 哈希值。

**重写 equals 时应同时重写 hashCode。** 如果相同 ID 的对象 equals 为 true，却得到不同哈希值，HashSet 等集合可能把它们放在不同位置，无法按约定去重或查找。

反方向并不成立：String 的 `"Aa"` 和 `"BB"` 具有相同 hashCode，内容却不同。这称为哈希冲突，不能用“哈希值相等”替代 equals。

相等规则还应满足：自己等于自己；a 等于 b 则 b 等于 a；a 等于 b 且 b 等于 c 则 a 等于 c；比较所依据的数据不变时，多次结果保持一致；非 null 对象与 null 比较为 false。

## 8. 预览 HashSet 去重

```java
Set<TeacherIdentity> identities = new HashSet<>();
identities.add(new TeacherIdentity(10001L));
identities.add(new TeacherIdentity(10001L));
identities.add(new TeacherIdentity(10002L));
System.out.println(identities.size()); // 2
```

先把这行声明理解为“创建一个存放 TeacherIdentity 的、不保留重复元素的集合”。Set 是接口，HashSet 是实现类；这和 Day7 的“接口类型变量指向实现对象”一致。尖括号限制元素类型，Day11～Day12 再系统学习集合与泛型。

HashSet 大致先借助哈希值定位候选，再用 equals 判断是否已有相等元素；并不是简单比较内存地址，也不是哈希值一样就算重复。add 返回 true 表示加入新元素，false 表示已有相等元素。

参与 equals/hashCode 的字段放入集合后不应变化，否则可能出现元素还在集合中，却无法正确查找、删除的问题。因此本课使用 final id，不把可变姓名放入身份规则。

## 9. 运行预期

```text
字符串 ==：false
字符串 equals：true
已知非空字符串比较 null：false
未接收 concat 返回值：Java
接收 concat 返回值：Java 入门
同一个 Teacher 对象：true
两个 Teacher 的默认 equals：false
身份对象 ==：false
相同 ID 的 equals：true
不同 ID 的 equals：false
相等对象的哈希值相同：true
身份与 null 比较：false
身份与字符串比较：false
首次添加：true
添加相同 ID：false
添加不同 ID：true
去重后身份数：2
Aa 和 BB 哈希值相同：true
Aa 和 BB 内容相等：false
```

## 10. 与目标项目的联系

教学服务里需要区分教师 ID、报告 ID、课节 ID 等业务身份。判断两份数据是否重复，首先要确定业务唯一键，不能只比较展示姓名，也不能默认所有 DTO 已实现按字段相等。

阅读真实代码时，应查看类是否手写 equals/hashCode，或使用了会生成这些方法的工具注解，再判断比较行为。本课不修改 aic-teaching-service 的实体规则。

## 11. 今日练习

1. 创建第三个相同 ID 的 TeacherIdentity，分别验证对称性和传递性。
2. 用 HashSet 存入两份相同 ID 的原 Teacher，观察 size 为 2；再对比身份类的结果。
3. 将 Aa 和 BB 放入 HashSet<String>，验证哈希冲突不会让它们被错误去重，size 仍为 2。
4. 使用 Objects.equals 比较两个 null，以及 null 和一个普通字符串。
5. 尝试把 equals(Object other) 改成 equals(TeacherIdentity other)，观察 @Override 的编译错误，然后恢复。

## 12. 今日验收

- [ ] 能解释引用类型 == 与 equals 的差别。
- [ ] 能说明 String 不可变与 final 变量的区别。
- [ ] 能解释 Teacher 默认 equals 为什么返回 false。
- [ ] 能逐行解释 TeacherIdentity 的 equals。
- [ ] 能说明为什么重写 equals 需要同时重写 hashCode。
- [ ] 能解释相同 ID 去重与哈希冲突的演示结果。

下一课：入门综合练习，整合对象、方法、校验与查询能力。
