package com.duoc.ticketflow.repository;

import com.duoc.ticketflow.model.TicketHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long>, JpaSpecificationExecutor<TicketHistory> {
    List<TicketHistory> findByTicketCodeOrderByTimestampDesc(String ticketCode);
    List<TicketHistory> findByEventCodeOrderByTimestampDesc(String eventCode);
    List<TicketHistory> findByUserIdOrderByTimestampDesc(String userId);
}
