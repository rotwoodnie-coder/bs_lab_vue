package com.xuanyue.exp.exp.repository;

import com.xuanyue.exp.exp.entity.ExpSimulator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpSimulatorRepository extends JpaRepository<ExpSimulator, String> {

    List<ExpSimulator> findBySimulatorNameContainingOrCommentsContaining(String simulatorName, String comments);

    List<ExpSimulator> findByStatus(String status);

    List<ExpSimulator> findBySimulatorNameContainingOrCommentsContainingAndStatus(String simulatorName, String comments, String status);

    /** 通过 exp_msg + exp_grade 关联年级 */
    @Query(value = "SELECT DISTINCT s.simulator_id FROM exp_simulator s " +
            "INNER JOIN exp_msg e ON e.simulator_id = s.simulator_id " +
            "INNER JOIN exp_grade eg ON e.exp_id = eg.exp_id " +
            "WHERE s.status = :status AND eg.grade_id IN (:gradeIds)",
            nativeQuery = true)
    List<String> findIdsByStatusAndGradeIds(@Param("status") String status,
                                            @Param("gradeIds") List<String> gradeIds);
}
