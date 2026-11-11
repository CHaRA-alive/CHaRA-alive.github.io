package com.cjy.bikesystem.repository;

import com.cjy.bikesystem.entity.Bike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface BikeRepository extends JpaRepository<Bike, String> {

    // 根据车辆状态查询
    List<Bike> findByBikeStatus(String bikeStatus);

    // 根据健康状态查询
    List<Bike> findByHealthStatus(String healthStatus);

    // 根据车辆编号查询
    Bike findByBikeId(String bikeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Bike b where b.bikeId = :bikeId")
    Optional<Bike> findByBikeIdForUpdate(@Param("bikeId") String bikeId);

    List<Bike> findByAreaAndBikeStatusOrderByBikeIdAsc(String area, String bikeStatus);
}
