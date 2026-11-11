package com.cjy.bikesystem.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 对已有课程数据库做轻量、幂等的结构升级，不重建或清空业务表。
 */
@Component
public class DatabaseMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        Integer areaColumns = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'bike' AND COLUMN_NAME = 'Area'",
                Integer.class
        );
        if (areaColumns != null && areaColumns == 0) {
            jdbcTemplate.execute("ALTER TABLE Bike ADD COLUMN Area VARCHAR(50) NULL");
        }

        Integer violationTypeColumns = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'securityrecord' " +
                        "AND COLUMN_NAME = 'ViolationType'",
                Integer.class
        );
        if (violationTypeColumns != null && violationTypeColumns == 0) {
            jdbcTemplate.execute("ALTER TABLE SecurityRecord ADD COLUMN ViolationType VARCHAR(20) NULL");
        }

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS DispatchPlan (
                    PlanID VARCHAR(24) PRIMARY KEY,
                    PlanDate DATETIME NOT NULL,
                    AlgorithmVersion VARCHAR(30) NOT NULL,
                    Confidence DECIMAL(5,2) NOT NULL,
                    Summary VARCHAR(500) NOT NULL,
                    ItemsJson LONGTEXT NOT NULL,
                    Status VARCHAR(20) NOT NULL,
                    CreatedAt DATETIME NOT NULL,
                    AppliedAt DATETIME NULL
                )
                """);

        String payMethodConstraint = jdbcTemplate.query(
                "SELECT CHECK_CLAUSE FROM information_schema.CHECK_CONSTRAINTS " +
                        "WHERE CONSTRAINT_SCHEMA = DATABASE() AND CONSTRAINT_NAME = 'chk_bill_paymethod'",
                resultSet -> resultSet.next() ? resultSet.getString(1) : null
        );
        if (payMethodConstraint != null && !payMethodConstraint.contains("余额")) {
            jdbcTemplate.execute("ALTER TABLE BillRecord DROP CHECK chk_bill_paymethod");
            jdbcTemplate.execute("""
                    ALTER TABLE BillRecord
                    ADD CONSTRAINT chk_bill_paymethod
                    CHECK (PayMethod IN ('微信', '支付宝', '银行卡', '余额'))
                    """);
        }

        jdbcTemplate.update("""
                UPDATE Bike
                SET Area = CASE BikeID
                    WHEN '00000001' THEN '教学区'
                    WHEN '00000002' THEN '图书馆'
                    WHEN '00000003' THEN '宿舍区'
                    WHEN '00000004' THEN '教学区'
                    WHEN '00000005' THEN '食堂区'
                    WHEN '00000006' THEN '体育馆'
                    ELSE '其他'
                END
                WHERE Area IS NULL OR TRIM(Area) = ''
                """);

        jdbcTemplate.update("""
                UPDATE SecurityRecord
                SET ViolationType = CASE
                    WHEN ViolationPosition LIKE '%禁停%' OR ViolationPosition LIKE '%通道%'
                        THEN '违规停车'
                    WHEN ViolationPosition LIKE '%损坏%' THEN '车辆损坏'
                    ELSE '其他违规'
                END
                WHERE ViolationType IS NULL OR TRIM(ViolationType) = ''
                """);
    }
}
