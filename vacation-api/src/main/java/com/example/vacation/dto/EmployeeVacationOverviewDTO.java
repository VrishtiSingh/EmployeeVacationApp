package com.example.vacation.dto;

import com.example.vacation.entity.VacationRequest;
import java.util.List;

public class EmployeeVacationOverviewDTO {
	
	

    private Long employeeId;
    private String name;
    private String role; // optional, can be null
    private int remainingVacationDays;
    private List<VacationRequest> vacationRequests;

    public EmployeeVacationOverviewDTO(Long employeeId, String name, String role,
                                      int remainingVacationDays, List<VacationRequest> vacationRequests) {
        this.employeeId = employeeId;
        this.name = name;
        this.role = role;
        this.remainingVacationDays = remainingVacationDays;
        this.vacationRequests = vacationRequests;
    }

    // Getters & setters
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getRemainingVacationDays() { return remainingVacationDays; }
    public void setRemainingVacationDays(int remainingVacationDays) { this.remainingVacationDays = remainingVacationDays; }

    public List<VacationRequest> getVacationRequests() { return vacationRequests; }
    public void setVacationRequests(List<VacationRequest> vacationRequests) { this.vacationRequests = vacationRequests; }
}
