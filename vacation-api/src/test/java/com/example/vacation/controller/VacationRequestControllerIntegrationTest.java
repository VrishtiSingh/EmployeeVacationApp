package com.example.vacation.controller;


import com.example.vacation.dto.CreateVacationRequestDTO;
import com.example.vacation.entity.Employee;
import com.example.vacation.entity.VacationRequest;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class VacationRequestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private VacationRequestRepository vacationRequestRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Employee employee;
    private Employee manager;

    @BeforeEach
    public void setup() {
        vacationRequestRepository.deleteAll();
        employeeRepository.deleteAll();

        employee = new Employee();
        employee.setName("Alice");
        employee.setRemainingVacationDays(10);
        employeeRepository.save(employee);

        manager = new Employee();
        manager.setName("Bob");
        manager.setRole("MANAGER");
        employeeRepository.save(manager);
    }

    @Test
    public void testCreateVacationRequestAndCheckOverlap() throws Exception {
        CreateVacationRequestDTO dto = new CreateVacationRequestDTO();
        dto.setAuthorId(employee.getId());
        dto.setVacationStartDate(LocalDate.of(2025, 9, 1));
        dto.setVacationEndDate(LocalDate.of(2025, 9, 5));

        // Create vacation request
        mockMvc.perform(post("/api/employee/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("PENDING"));

        // Check overlap (should be true)
        mockMvc.perform(get("/api/employee/" + employee.getId() + "/overlaps")
                        .param("start", "2025-09-03")
                        .param("end", "2025-09-06"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // Check non-overlapping dates (should be false)
        mockMvc.perform(get("/api/employee/" + employee.getId() + "/overlaps")
                        .param("start", "2025-09-06")
                        .param("end", "2025-09-10"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    public void testGetEmployeeRequestsWithStatusFilter() throws Exception {
        VacationRequest request1 = new VacationRequest();
        request1.setAuthor(employee);
        request1.setVacationStartDate(LocalDate.of(2025, 9, 1));
        request1.setVacationEndDate(LocalDate.of(2025, 9, 5));
        request1.setStatus(VacationRequest.Status.PENDING);
        vacationRequestRepository.save(request1);

        VacationRequest request2 = new VacationRequest();
        request2.setAuthor(employee);
        request2.setVacationStartDate(LocalDate.of(2025, 10, 1));
        request2.setVacationEndDate(LocalDate.of(2025, 10, 5));
        request2.setStatus(VacationRequest.Status.APPROVED);
        vacationRequestRepository.save(request2);

        // Filter by PENDING
        mockMvc.perform(get("/api/employee/" + employee.getId() + "/requests")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        // No filter (should return both)
        mockMvc.perform(get("/api/employee/" + employee.getId() + "/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
