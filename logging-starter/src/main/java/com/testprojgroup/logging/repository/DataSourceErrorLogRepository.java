package com.testprojgroup.logging.repository;

import com.testprojgroup.logging.model.DataSourceErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface DataSourceErrorLogRepository extends JpaRepository<DataSourceErrorLog, Long> {

}
