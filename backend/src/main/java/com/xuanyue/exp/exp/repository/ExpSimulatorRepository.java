package com.xuanyue.exp.exp.repository;

import com.xuanyue.exp.exp.entity.ExpSimulator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpSimulatorRepository extends JpaRepository<ExpSimulator, String> {

    List<ExpSimulator> findBySimulatorNameContainingOrCommentsContaining(String simulatorName, String comments);

    List<ExpSimulator> findByStatus(String status);

    List<ExpSimulator> findBySimulatorNameContainingOrCommentsContainingAndStatus(String simulatorName, String comments, String status);
}
