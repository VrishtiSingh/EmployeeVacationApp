package com.example.vacation.controller;

import com.example.vacation.dto.UpdateVacationRequestDTO;
import com.example.vacation.entity.VacationRequest;
import com.example.vacation.service.VacationService;
import com.example.vacation.entity.Employee;
import com.example.vacation.repository.EmployeeRepository;
import com.example.vacation.exception.EmployeeNotFoundException;
import com.example.vacation.dto.EmployeeVacationOverviewDTO;
import com.example.vacation.repository.VacationRequestRepository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;


import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.List;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    @Autowired
    private VacationService vacationService;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private VacationRequestRepository vacationRequestRepository;


    // GET /api/manager/requests?status=pending
    @GetMapping("/requests")
    public List<VacationRequest> getAllRequests(@RequestParam(required = false) String status) {
        VacationRequest.Status enumStatus = null;
        if (status != null && !status.isBlank()) {
            enumStatus = VacationRequest.Status.valueOf(status.toUpperCase());
        }
        return vacationService.getAllRequests(enumStatus);
    }

    // PUT /api/manager/requests/{id}/process
    @PutMapping("/requests/{id}/process")
    public ResponseEntity<?> processRequest(
            @PathVariable Long id,
            @RequestBody UpdateVacationRequestDTO dto
    ) {
        try {
            VacationRequest request = vacationService.processRequest(id, dto);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/employee/{employeeId}/overlaps")
    public ResponseEntity<?> getOverlappingRequests(
            @PathVariable Long employeeId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        boolean hasOverlap = vacationService.hasOverlap(employee, startDate, endDate);

        if (hasOverlap) {
            return ResponseEntity.ok("There is an overlapping vacation request.");
        } else {
            return ResponseEntity.ok("No overlapping vacation requests.");
        }
    }
    
    public EmployeeVacationOverviewDTO getEmployeeVacationOverview(Long employeeId, VacationRequest.Status statusFilter) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        List<VacationRequest> requests = vacationRequestRepository.findByAuthorId(employeeId);

        if (statusFilter != null) {
            requests = requests.stream()
                    .filter(r -> r.getStatus() == statusFilter)
                    .toList();
        }

        return new EmployeeVacationOverviewDTO(
                employee.getId(),
                employee.getName(),
                employee.getRole(),
                employee.getRemainingVacationDays(),
                requests
        );
    }

    public ManagerController(VacationService vacationService) {
        this.vacationService = vacationService;
    }

    @GetMapping("/employees/{id}/overview")
    @Operation(summary = "Get detailed vacation overview for an employee")
    public EmployeeVacationOverviewDTO getEmployeeOverview(
            @PathVariable("id") Long employeeId,
            @RequestParam(value = "status", required = false) VacationRequest.Status status
    ) {
        return vacationService.getEmployeeVacationOverview(employeeId, status);
    }
  



}
