package com.xuanyue.exp.mobile.repository;

import com.xuanyue.exp.mobile.entity.MobileExpMsg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 移动端首页只读 Repository
 * 瀑布流：exp_msg + exp_simulator + 已审核 mb_work
 */
public interface MobileHomeRepository extends JpaRepository<MobileExpMsg, String>, JpaSpecificationExecutor<MobileExpMsg> {

    /** 统一 UNION 字符串列排序规则，避免跨表 collation 冲突 (MySQL 1271) */
    String CS = "utf8mb4_unicode_ci";
    String EXP_ID = "CONVERT(e.exp_id USING utf8mb4) COLLATE " + CS;
    String SIM_ID = "CONVERT(s.simulator_id USING utf8mb4) COLLATE " + CS;
    String SRC_EXP = "CONVERT('exp_msg' USING utf8mb4) COLLATE " + CS;
    String SRC_SIM = "CONVERT('simulator' USING utf8mb4) COLLATE " + CS;
    String EXP_NAME = "CONVERT(e.exp_name USING utf8mb4) COLLATE " + CS;
    String SIM_NAME = "CONVERT(s.simulator_name USING utf8mb4) COLLATE " + CS;

    String EXP_MSG_ELIGIBLE = " e.status = 'y' AND ( " +
            "e.exp_type IN ('standard', 'teacher', 'teaching', 'student') " +
            "OR IFNULL(e.simulator_id, '') <> '' OR IFNULL(e.simulator_url, '') <> '' ) ";

    String STANDALONE_SIM = " s.status = 'y' AND NOT EXISTS ( " +
            "SELECT 1 FROM exp_msg em WHERE em.status = 'y' " +
            "AND em.simulator_id = s.simulator_id ) ";

    /** 独立模拟实验：通过关联 exp_msg.exp_grade 匹配年级 */
    String SIM_GRADE_EXISTS = " EXISTS ( SELECT 1 FROM exp_msg e " +
            "INNER JOIN exp_grade eg ON e.exp_id = eg.exp_id " +
            "WHERE IFNULL(e.simulator_id, '') <> '' AND e.simulator_id = s.simulator_id " +
            "AND eg.grade_id IN (:gradeIds) ) ";

    String FEED_UNION_BY_GRADES = "(SELECT " + EXP_ID + " AS item_id, " + SRC_EXP + " AS item_source, e.create_time AS sort_time FROM exp_msg e " +
            "INNER JOIN exp_grade eg ON e.exp_id = eg.exp_id " +
            "WHERE " + EXP_MSG_ELIGIBLE + " AND eg.grade_id IN (:gradeIds) " +
            "GROUP BY e.exp_id, e.create_time) " +
            "UNION ALL " +
            "(SELECT " + SIM_ID + " AS item_id, " + SRC_SIM + " AS item_source, s.create_time AS sort_time FROM exp_simulator s " +
            "WHERE " + STANDALONE_SIM + " AND " + SIM_GRADE_EXISTS + ") ";

    String FEED_UNION_ALL = "(SELECT " + EXP_ID + " AS item_id, " + SRC_EXP + " AS item_source, e.create_time AS sort_time FROM exp_msg e " +
            "WHERE " + EXP_MSG_ELIGIBLE + ") " +
            "UNION ALL " +
            "(SELECT " + SIM_ID + " AS item_id, " + SRC_SIM + " AS item_source, s.create_time AS sort_time FROM exp_simulator s " +
            "WHERE " + STANDALONE_SIM + ") ";

    /**
     * 首页混排候选池（实验 + 模拟 + 已审核作品，按时间倒序取前 N 条）
     */
    @Query(value = "SELECT item_id, item_source FROM ( " + FEED_UNION_ALL +
            ") feed ORDER BY sort_time DESC LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findFeedCandidateRefsAll(@Param("limit") int limit);

    /**
     * 首页瀑布流分页（实验 + 独立模拟实验 + 作品，按创建时间倒序）
     */
    @Query(value = "SELECT item_id, item_source FROM ( " + FEED_UNION_ALL +
            ") feed ORDER BY sort_time DESC LIMIT :size OFFSET :offset",
            nativeQuery = true)
    List<Object[]> findFeedRefsPageAll(@Param("offset") int offset, @Param("size") int size);

    @Query(value = "SELECT COUNT(*) FROM ( " +
            "(SELECT " + EXP_ID + " AS item_id FROM exp_msg e WHERE " + EXP_MSG_ELIGIBLE + ") " +
            "UNION ALL " +
            "(SELECT " + SIM_ID + " AS item_id FROM exp_simulator s WHERE " + STANDALONE_SIM + ") " +
            ") feed",
            nativeQuery = true)
    long countFeedAll();

    /**
     * 按年级筛选（实验 exp_grade + 独立模拟 exp 关联年级 + 作品 school_grade_id）
     */
    @Query(value = "SELECT item_id, item_source FROM ( " + FEED_UNION_BY_GRADES +
            ") feed ORDER BY sort_time DESC LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findFeedCandidateRefsByGrades(@Param("gradeIds") List<String> gradeIds,
                                                  @Param("limit") int limit);

    @Query(value = "SELECT " + EXP_ID + ", " + SRC_EXP + " FROM exp_msg e " +
            "INNER JOIN exp_grade eg ON e.exp_id = eg.exp_id " +
            "WHERE " + EXP_MSG_ELIGIBLE + " AND eg.grade_id IN (:gradeIds) " +
            "GROUP BY e.exp_id, e.create_time ORDER BY e.create_time DESC LIMIT :size OFFSET :offset",
            nativeQuery = true)
    List<Object[]> findFeedRefsPageByGrades(@Param("gradeIds") List<String> gradeIds,
                                            @Param("offset") int offset,
                                            @Param("size") int size);

    @Query(value = "SELECT COUNT(*) FROM ( " +
            "(SELECT " + EXP_ID + " AS item_id FROM exp_msg e INNER JOIN exp_grade eg ON e.exp_id = eg.exp_id " +
            "WHERE " + EXP_MSG_ELIGIBLE + " AND eg.grade_id IN (:gradeIds)) " +
            "UNION ALL " +
            "(SELECT " + SIM_ID + " AS item_id FROM exp_simulator s WHERE " + STANDALONE_SIM + " AND " + SIM_GRADE_EXISTS + ") " +
            ") feed",
            nativeQuery = true)
    long countFeedByGrades(@Param("gradeIds") List<String> gradeIds);

    @Query(value = "SELECT item_id, item_source FROM ( " +
            "(SELECT " + EXP_ID + " AS item_id, " + SRC_EXP + " AS item_source, e.create_time AS sort_time FROM exp_msg e " +
            "WHERE " + EXP_MSG_ELIGIBLE + " AND e.exp_name LIKE CONCAT('%', :keyword, '%')) " +
            "UNION ALL " +
            "(SELECT " + SIM_ID + " AS item_id, " + SRC_SIM + " AS item_source, s.create_time AS sort_time FROM exp_simulator s " +
            "WHERE " + STANDALONE_SIM + " AND (s.simulator_name LIKE CONCAT('%', :keyword, '%') " +
            "OR s.comments LIKE CONCAT('%', :keyword, '%'))) " +
            ") feed ORDER BY sort_time DESC LIMIT :size OFFSET :offset",
            nativeQuery = true)
    List<Object[]> findFeedRefsPageByKeyword(@Param("keyword") String keyword,
                                             @Param("offset") int offset,
                                             @Param("size") int size);

    @Query(value = "SELECT COUNT(*) FROM ( " +
            "(SELECT " + EXP_ID + " AS item_id FROM exp_msg e WHERE " + EXP_MSG_ELIGIBLE +
            " AND e.exp_name LIKE CONCAT('%', :keyword, '%')) " +
            "UNION ALL " +
            "(SELECT " + SIM_ID + " AS item_id FROM exp_simulator s WHERE " + STANDALONE_SIM +
            " AND (s.simulator_name LIKE CONCAT('%', :keyword, '%') OR s.comments LIKE CONCAT('%', :keyword, '%'))) " +
            ") feed",
            nativeQuery = true)
    long countFeedByKeyword(@Param("keyword") String keyword);

    @Query(value = "SELECT name FROM ( " +
            "(SELECT " + EXP_NAME + " AS name, e.create_time AS sort_time FROM exp_msg e WHERE " + EXP_MSG_ELIGIBLE + ") " +
            "UNION ALL " +
            "(SELECT " + SIM_NAME + " AS name, s.create_time AS sort_time FROM exp_simulator s WHERE " + STANDALONE_SIM + ") " +
            ") feed ORDER BY sort_time DESC LIMIT :limit",
            nativeQuery = true)
    List<String> findRecentFeedNames(@Param("limit") int limit);

    @Query(value = "SELECT COUNT(*) FROM exp_simulator s WHERE " + STANDALONE_SIM,
            nativeQuery = true)
    long countStandaloneSimulators();

    /** 模拟实验关联的年级（任意 exp_msg + exp_grade，不要求 exp 已发布） */
    @Query(value = "SELECT e.simulator_id, eg.grade_id FROM exp_msg e " +
            "INNER JOIN exp_grade eg ON e.exp_id = eg.exp_id " +
            "WHERE IFNULL(e.simulator_id, '') <> '' AND e.simulator_id IN (:simIds)",
            nativeQuery = true)
    List<Object[]> findGradeIdsBySimulatorIds(@Param("simIds") List<String> simIds);
}
