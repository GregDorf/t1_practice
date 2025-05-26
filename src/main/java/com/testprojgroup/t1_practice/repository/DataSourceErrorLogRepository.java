package com.testprojgroup.t1_practice.repository;

import com.testprojgroup.t1_practice.model.DataSourceErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataSourceErrorLogRepository extends JpaRepository<DataSourceErrorLog, Long> {

}
