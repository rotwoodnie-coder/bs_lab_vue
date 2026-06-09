package com.xuanyue.exp.data.repository;

import com.xuanyue.exp.data.entity.DataFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataFileRepository extends JpaRepository<DataFile, String> {
}
