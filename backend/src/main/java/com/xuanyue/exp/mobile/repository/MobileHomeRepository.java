package com.xuanyue.exp.mobile.repository;

import com.xuanyue.exp.mobile.entity.MobileExpMsg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 移动端首页只读 Repository
 * 瀑布流：exp_msg（标准/教学/学生实验）+ 未关联的 exp_simulator
 */
public interface MobileHomeRepository extends JpaRepository<MobileExpMsg, String>, JpaSpecificationExecutor<MobileExpMsg> {

    String EXP_MSG_ELIGIBLE = " e.status IN ('y', 't') AND ( " +
            "e.exp_type IN ('standard', 'teacher', 'teaching', 'student') " +
            "OR IFNULL(e.simulator_id, '') <> '' OR IFNULL(e.simulator_url, '') <> '' ) ";

    String STANDALONE_SIM = " s.status = 'y' AND NOT EXISTS ( " +
            "SELECT 1 FROM exp_msg em WHERE em.status IN ('y', 't') " +
            "AND em.simulator_id = s.simulator_id ) ";

    /**
     * 首页瀑布流分页（实验 + 独立模拟实验，按创建时间倒序）
     */
    @Query(value = "SELECT item_id, item_source FROM ( " +
            "(SELECT e.exp_id AS item_id, 'exp_msg' AS item_source, e.create_time AS sort_time FROM exp_msg e " +
            "WHERE " + EXP_MSG_ELIGIBLE + ") " +
            "UNION ALL " +
            "(SELECT s.simulator_id AS item_id, 'simulator' AS item_source, s.create_time AS sort_time FROM exp_simulator s " +
            "WHERE " + STANDALONE_SIM + ") " +
            ") feed ORDER BY sort_time DESC LIMIT :size OFFSET :offset",
            nativeQuery = true)
    List<Object[]> findFeedRefsPageAll(@Param("offset") int offset, @Param("size") int size);

    @Query(value = "SELECT COUNT(*) FROM ( " +
            "(SELECT e.exp_id FROM exp_msg e WHERE " + EXP_MSG_ELIGIBLE + ") " +
            "UNION ALL " +
            "(SELECT s.simulator_id FROM exp_simulator s WHERE " + STANDALONE_SIM + ") " +
            ") feed",
            nativeQuery = true)
    long countFeedAll();

    /**
     * 按年级筛选（仅 exp_msg，模拟实验无年级关联）
     */
    @Query(value = "SELECT e.exp_id, 'exp_msg' FROM exp_msg e " +
            "INNER JOIN exp_grade eg ON e.exp_id = eg.exp_id " +
            "WHERE " + EXP_MSG_ELIGIBLE + " AND eg.grade_id IN (:gradeIds) " +
            "GROUP BY e.exp_id, e.create_time ORDER BY e.create_time DESC LIMIT :size OFFSET :offset",
            nativeQuery = true)
    List<Object[]> findFeedRefsPageByGrades(@Param("gradeIds") List<String> gradeIds,
                                            @Param("offset") int offset,
                                            @Param("size") int size);

    @Query(value = "SELECT COUNT(DISTINCT e.exp_id) FROM exp_msg e " +
            "INNER JOIN exp_grade eg ON e.exp_id = eg.exp_id " +
            "WHERE " + EXP_MSG_ELIGIBLE + " AND eg.grade_id IN (:gradeIds)",
            nativeQuery = true)
    long countFeedByGrades(@Param("gradeIds") List<String> gradeIds);

    @Query(value = "SELECT item_id, item_source FROM ( " +
            "(SELECT e.exp_id AS item_id, 'exp_msg' AS item_source, e.create_time AS sort_time FROM exp_msg e " +
            "WHERE " + EXP_MSG_ELIGIBLE + " AND e.exp_name LIKE CONCAT('%', :keyword, '%')) " +
            "UNION ALL " +
            "(SELECT s.simulator_id AS item_id, 'simulator' AS item_source, s.create_time AS sort_time FROM exp_simulator s " +
            "WHERE " + STANDALONE_SIM + " AND (s.simulator_name LIKE CONCAT('%', :keyword, '%') " +
            "OR s.comments LIKE CONCAT('%', :keyword, '%'))) " +
            ") feed ORDER BY sort_time DESC LIMIT :size OFFSET :offset",
            nativeQuery = true)
    List<Object[]> findFeedRefsPageByKeyword(@Param("keyword") String keyword,
                                             @Param("offset") int offset,
                                             @Param("size") int size);

    @Query(value = "SELECT COUNT(*) FROM ( " +
            "(SELECT e.exp_id FROM exp_msg e WHERE " + EXP_MSG_ELIGIBLE +
            " AND e.exp_name LIKE CONCAT('%', :keyword, '%')) " +
            "UNION ALL " +
            "(SELECT s.simulator_id FROM exp_simulator s WHERE " + STANDALONE_SIM +
            " AND (s.simulator_name LIKE CONCAT('%', :keyword, '%') OR s.comments LIKE CONCAT('%', :keyword, '%'))) " +
            ") feed",
            nativeQuery = true)
    long countFeedByKeyword(@Param("keyword") String keyword);

    @Query(value = "SELECT name FROM ( " +
            "(SELECT e.exp_name AS name, e.create_time AS sort_time FROM exp_msg e WHERE " + EXP_MSG_ELIGIBLE + ") " +
            "UNION ALL " +
            "(SELECT s.simulator_name AS name, s.create_time AS sort_time FROM exp_simulator s WHERE " + STANDALONE_SIM + ") " +
            ") feed ORDER BY sort_time DESC LIMIT :limit",
            nativeQuery = true)
    List<String> findRecentFeedNames(@Param("limit") int limit);

    @Query(value = "SELECT COUNT(*) FROM exp_simulator s WHERE " + STANDALONE_SIM,
            nativeQuery = true)
    long countStandaloneSimulators();
}
