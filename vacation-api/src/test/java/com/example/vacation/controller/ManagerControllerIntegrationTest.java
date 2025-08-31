package com.example.vacation.controller;

import com.example.vacation.entity.Employee;
import com.example.vacation.entity.VacationRequest;
import com.example.vacation.entity.VacationRequest.Status;
import com.example.vacation.repository.EmployeeRepository;
import com.example.vacation.repository.VacationRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ManagerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private VacationRequestRepository vacationRequestRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Employee employee;

    @BeforeEach
    public void setup() {
        vacationRequestRepository.deleteAll();
        employeeRepository.deleteAll();

        employee = new Employee();
        employee.setName("Alice Manager");
        employee.setRole("EMPLOYEE");
        employee.setRemainingVacationDays(20);
        employeeRepository.save(employee);

        // Add some vacation requests
        VacationRequest pending = new VacationRequest();
        pending.setAuthor(employee);
        pending.setVacationStartDate(LocalDate.of(2025, 9, 1));
        pending.setVacationEndDate(LocalDate.of(2025, 9, 5));
        pending.setStatus(Status.PENDING);
        pending.setResolved(false);

        VacationRequest approved = new VacationRequest();
        approved.setAuthor(employee);
        approved.setVacationStartDate(LocalDate.of(2025, 10, 1));
        approved.setVacationEndDate(LocalDate.of(2025, 10, 5));
        approved.setStatus(Status.APPROVED);
        approved.setResolved(true);

        vacationRequestRepository.saveAll(List.of(pending, approved));
    }

    @Test
    public void testGetEmployeeOverviewWithoutStatusFilter() throws Exception {
        mockMvc.perform(get("/api/manager/employees/{id}/overview", employee.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(employee.getId()))
                .andExpect(jsonPath("$.name").value("Alice Manager"))
                .andExpect(jsonPath("$.remainingVacationDays").value(20))
                .andExpect(jsonPath("$.vacationRequests.length()").value(2));
    }

    @Test
    public void testGetEmployeeOverviewWithStatusFilter() throws Exception {
        mockMvc.perform(get("/api/manager/employees/{id}/overview", employee.getId())
                        .param("status", "PENDING")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(employee.getId()))
                .andExpect(jsonPath("$.vacationRequests.length()").value(1))
                .andExpect(jsonPath("$.vacationRequests[0].status").value("PENDING"));
    }

    @Test
    public void testGetEmployeeOverviewWithNoMatchingStatus() throws Exception {
        mockMvc.perform(get("/api/manager/employees/{id}/overview", employee.getId())
                        .param("status", "REJECTED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vacationRequests.length()").value(0));
    }
}
