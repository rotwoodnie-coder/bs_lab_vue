package com.xuanyue.exp.edu.repository;

import com.xuanyue.exp.edu.entity.SubjectGroupResearcher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectGroupResearcherRepository extends JpaRepository<SubjectGroupResearcher, String> {
    List<SubjectGroupResearcher> findByGroupId(String groupId);
    List<SubjectGroupResearcher> findByResearcherUserId(String researcherUserId);
    void deleteByGroupId(String groupId);
}
