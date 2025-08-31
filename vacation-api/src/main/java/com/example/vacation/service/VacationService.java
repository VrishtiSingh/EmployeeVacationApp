package com.example.vacation.service;

import com.example.vacation.dto.CreateVacationRequestDTO;
import com.example.vacation.dto.UpdateVacationRequestDTO;
import com.example.vacation.entity.Employee;
import com.example.vacation.entity.VacationRequest;
import com.example.vacation.dto.EmployeeVacationOverviewDTO;
import com.example.vacation.entity.VacationRequest.Status;
import com.example.vacation.exception.EmployeeNotFoundException;
import com.example.vacation.exception.NotEnoughVacationDaysException;
import com.example.vacation.exception.VacationRequestNotFoundException;
import com.example.vacation.repository.EmployeeRepository;
import com.example.vacation.repository.VacationRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class VacationService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private VacationRequestRepository vacationRequestRepository;

    public VacationRequest createVacationRequest(VacationRequest request) {
        return vacationRequestRepository.save(request);
    }

    public VacationRequest getVacationRequestById(Long id) {
        return vacationRequestRepository.findById(id)
                .orElseThrow(() -> new VacationRequestNotFoundException(id));
    }

    // Check for overlapping vacation requests for an employee
 // Refactored to use the reusable method
    /**
     * Returns true if there is any overlapping vacation request for the given employee/date range.
     */
    public boolean hasOverlap(Employee employee, LocalDate startDate, LocalDate endDate) {
        return !getOverlappingRequestsForEmployee(employee, startDate, endDate).isEmpty();
    }

    /**
     * Overloaded helper for VacationRequest objects.
     */
    public boolean hasOverlap(Employee employee, VacationRequest request) {
        return hasOverlap(employee, request.getVacationStartDate(), request.getVacationEndDate());
    }


    public VacationRequest createRequest(CreateVacationRequestDTO dto) {
        Employee author = employeeRepository.findById(dto.getAuthorId())
                .orElseThrow(() -> new EmployeeNotFoundException(dto.getAuthorId()));

        int requestedDays = (int) ChronoUnit.DAYS.between(dto.getVacationStartDate(), dto.getVacationEndDate()) + 1;
        if (requestedDays > author.getRemainingVacationDays()) {
            throw new NotEnoughVacationDaysException(
                    "Cannot create request: employee has only " + author.getRemainingVacationDays() + " remaining days"
            );
        }

        VacationRequest newRequest = new VacationRequest();
        newRequest.setAuthor(author);
        newRequest.setVacationStartDate(dto.getVacationStartDate());
        newRequest.setVacationEndDate(dto.getVacationEndDate());
        newRequest.setStatus(Status.PENDING);
        newRequest.setResolved(false);
        newRequest.setRequestCreatedAt(LocalDateTime.now());

        if (hasOverlap(author, newRequest)) {
            throw new NotEnoughVacationDaysException("Vacation request overlaps with an existing one.");
        }

        return vacationRequestRepository.save(newRequest);
    }

    // Process a vacation request (approve/reject) using DTO
    public VacationRequest processRequest(Long requestId, UpdateVacationRequestDTO dto) {
        VacationRequest request = getVacationRequestById(requestId);

        Employee manager = employeeRepository.findById(dto.getManagerId())
                .orElseThrow(() -> new EmployeeNotFoundException(dto.getManagerId()));

        request.setResolvedBy(manager);
        request.setStatus(Status.valueOf(dto.getStatus().toUpperCase()));

        if (request.getStatus() == Status.APPROVED) {
            Employee author = request.getAuthor();
            int requestedDays = (int) ChronoUnit.DAYS.between(request.getVacationStartDate(), request.getVacationEndDate()) + 1;

            if (requestedDays > author.getRemainingVacationDays()) {
                throw new NotEnoughVacationDaysException(
                        "Cannot approve: employee has only " + author.getRemainingVacationDays() + " remaining vacation days"
                );
            }

            author.setRemainingVacationDays(author.getRemainingVacationDays() - requestedDays);
            employeeRepository.save(author);
        }

        request.setResolved(true);
        return vacationRequestRepository.save(request);
    }

    // --- NEW METHODS FOR TESTING & CONVENIENCE ---
    public VacationRequest approveVacationRequest(Long requestId, Employee manager) {
        UpdateVacationRequestDTO dto = new UpdateVacationRequestDTO();
        dto.setManagerId(manager.getId());
        dto.setStatus("APPROVED");
        return processRequest(requestId, dto);
    }

    public VacationRequest rejectVacationRequest(Long requestId, Employee manager) {
        UpdateVacationRequestDTO dto = new UpdateVacationRequestDTO();
        dto.setManagerId(manager.getId());
        dto.setStatus("REJECTED");
        return processRequest(requestId, dto);
    }

    // Get requests for an employee, optionally filtered by status
    public List<VacationRequest> getRequestsForEmployee(Long employeeId, Status status) {
        if (status != null) {
            return vacationRequestRepository.findByAuthorIdAndStatus(employeeId, status);
        }
        return vacationRequestRepository.findByAuthorId(employeeId);
    }

    // Get all requests, optionally filtered by status
    public List<VacationRequest> getAllRequests(Status status) {
        if (status != null) {
            return vacationRequestRepository.findByStatus(status);
        }
        return vacationRequestRepository.findAll();
    }
    
    public List<VacationRequest> getOverlappingRequestsForEmployee(Employee employee, LocalDate startDate, LocalDate endDate) {
        List<VacationRequest> requests = vacationRequestRepository.findByAuthorId(employee.getId());
        return requests.stream()
                .filter(r -> r.getStatus() != VacationRequest.Status.REJECTED &&
                        !(startDate.isAfter(r.getVacationEndDate()) || endDate.isBefore(r.getVacationStartDate()))
                )
                .toList();
    }
    
    public EmployeeVacationOverviewDTO getEmployeeVacationOverview(Long employeeId, VacationRequest.Status statusFilter) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        // Get all requests for this employee
        List<VacationRequest> requests = vacationRequestRepository.findByAuthorId(employeeId);

        return new EmployeeVacationOverviewDTO(
                employee.getId(),
                employee.getName(),
                employee.getRole(), // optional
                employee.getRemainingVacationDays(),
                requests
        );
    }


    
    
}
