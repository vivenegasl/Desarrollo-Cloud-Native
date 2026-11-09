package com.duoc.ticketflow.repository;

import com.duoc.ticketflow.model.Ticket;
import com.duoc.ticketflow.model.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    Optional<Ticket> findByTicketCode(String ticketCode);

    List<Ticket> findByEventCode(String eventCode);

    List<Ticket> findByUserId(String userId);

    List<Ticket> findByEventCodeAndUserId(String eventCode, String userId);

    long countByEventCode(String eventCode);

    long countByEventCodeAndStatus(String eventCode, TicketStatus status);

    @Query("SELECT COALESCE(SUM(t.price), 0) FROM Ticket t WHERE t.eventCode = :eventCode AND t.status <> 'CANCELLED'")
    BigDecimal sumRevenueByEventCode(@Param("eventCode") String eventCode);

    @Query("SELECT COALESCE(SUM(t.fileSizeBytes), 0) FROM Ticket t WHERE t.eventCode = :eventCode AND t.s3Uploaded = true")
    Long sumS3BytesByEventCode(@Param("eventCode") String eventCode);
}
