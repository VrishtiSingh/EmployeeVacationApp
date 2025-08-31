package com.example.vacation.controller;

import com.example.vacation.dto.CreateVacationRequestDTO;
import com.example.vacation.entity.VacationRequest;
import com.example.vacation.entity.Employee;
import com.example.vacation.service.VacationService;
import com.example.vacation.repository.EmployeeRepository;
import com.example.vacation.exception.EmployeeNotFoundException;
import com.example.vacation.dto.EmployeeVacationOverviewDTO;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/employee")
@Tag(name = "Employee", description = "Endpoints for managing employee vacation requests")
public class VacationRequestController {

    @Autowired
    private VacationService vacationService;

    @Autowired
    private EmployeeRepository employeeRepository;

    // GET /api/employee/{employeeId}/requests
    @GetMapping("/{employeeId}/requests")
    public List<VacationRequest> getEmployeeRequests(
            @PathVariable Long employeeId,
            @RequestParam(required = false) String status
    ) {
        VacationRequest.Status enumStatus = null;
        if (status != null && !status.isBlank()) {
            enumStatus = VacationRequest.Status.valueOf(status.toUpperCase());
        }
        return vacationService.getRequestsForEmployee(employeeId, enumStatus);
    }

    // POST /api/employee/requests
    @PostMapping("/requests")
    public ResponseEntity<?> createRequest(@Valid @RequestBody CreateVacationRequestDTO dto) {
        try {
            VacationRequest request = vacationService.createRequest(dto);
            return new ResponseEntity<>(request, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // GET /api/employee/{employeeId}/overlaps?start=yyyy-MM-dd&end=yyyy-MM-dd
    @GetMapping("/{employeeId}/overlaps")
    @Operation(
            summary = "Get overlapping vacation requests for an employee",
            description = "Returns true if any existing vacation requests overlap with the given date range"
    )
    public ResponseEntity<Boolean> checkOverlap(
            @PathVariable("employeeId") Long employeeId,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        boolean hasOverlap = vacationService.hasOverlap(employee, start, end);
        return ResponseEntity.ok(hasOverlap);
    }
    
 // GET /api/employee/{employeeId}/overlapping-requests?start=yyyy-MM-dd&end=yyyy-MM-dd
    @GetMapping("/{id}/overlapping-requests")
    @Operation(
        summary = "Get all overlapping vacation requests for a given date range",
        description = "Returns a list of existing vacation requests that overlap with the specified range"
    )
    public ResponseEntity<List<VacationRequest>> getOverlappingRequests(
            @PathVariable Long id,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        List<VacationRequest> overlaps = vacationService.getOverlappingRequestsForEmployee(employee, start, end);
        return ResponseEntity.ok(overlaps);
    }
    
    @GetMapping("/{employeeId}/overview")
    @Operation(
            summary = "Get detailed vacation overview for a single employee",
            description = "Includes employee info, remaining vacation days, and all requests grouped by status"
    )
    public ResponseEntity<EmployeeVacationOverviewDTO> getEmployeeOverview(@PathVariable Long employeeId) {
        EmployeeVacationOverviewDTO overview = vacationService.getEmployeeVacationOverview(employeeId, null);
        return ResponseEntity.ok(overview);
    }

    
    
    

}
