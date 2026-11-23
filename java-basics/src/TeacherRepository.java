// 接口只规定“能做什么”，不规定数据如何存储。
public interface TeacherRepository {
    // 按 ID 查找；不存在时返回 null。
    Teacher findById(long id);
}
