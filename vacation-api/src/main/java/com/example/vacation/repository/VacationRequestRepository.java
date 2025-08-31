package com.example.vacation.repository;


import com.example.vacation.entity.VacationRequest;
import com.example.vacation.entity.Employee;
import com.example.vacation.entity.VacationRequest.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VacationRequestRepository extends JpaRepository<VacationRequest, Long> {

    List<VacationRequest> findByAuthorId(Long authorId);
    
    List<VacationRequest> findByAuthor(Employee author);

    List<VacationRequest> findByAuthorIdAndStatus(Long authorId, Status status);

    List<VacationRequest> findByStatus(Status status);
}
