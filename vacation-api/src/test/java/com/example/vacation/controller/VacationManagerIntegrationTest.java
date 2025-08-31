package com.example.vacation.controller;

import com.example.vacation.dto.CreateVacationRequestDTO;
import com.example.vacation.dto.UpdateVacationRequestDTO;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class VacationManagerIntegrationTest {

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
        employee.setName("Alice Employee");
        employee.setRemainingVacationDays(10);
        employeeRepository.save(employee);

        manager = new Employee();
        manager.setName("Bob Manager");
        manager.setRole("MANAGER");
        employeeRepository.save(manager);
    }

    @Test
    public void testFullManagerFlow() throws Exception {
        // 1️⃣ Employee creates a vacation request
        CreateVacationRequestDTO createDto = new CreateVacationRequestDTO();
        createDto.setAuthorId(employee.getId());
        createDto.setVacationStartDate(LocalDate.of(2025, 9, 1));
        createDto.setVacationEndDate(LocalDate.of(2025, 9, 5));

        String createJson = objectMapper.writeValueAsString(createDto);

        mockMvc.perform(post("/api/employee/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.author.id").value(employee.getId()))
                .andExpect(jsonPath("$.status").value(Status.PENDING.toString()));

        // 2️⃣ Manager checks for overlapping requests (should be false)
        mockMvc.perform(get("/api/employee/{id}/overlaps", employee.getId())
                        .param("start", "2025-09-03")
                        .param("end", "2025-09-06"))
                .andExpect(status().isOk())
                .andExpect(content().string("true")); // overlaps with created request

        // 3️⃣ Manager approves the request
        VacationRequest savedRequest = vacationRequestRepository.findAll().get(0);

        UpdateVacationRequestDTO updateDto = new UpdateVacationRequestDTO();
        updateDto.setManagerId(manager.getId());
        updateDto.setStatus(Status.APPROVED.toString());

        String updateJson = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(put("/api/employee/requests/{id}/process", savedRequest.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(Status.APPROVED.toString()))
                .andExpect(jsonPath("$.resolved").value(true))
                .andExpect(jsonPath("$.resolvedBy.id").value(manager.getId()));

        // 4️⃣ Employee's remaining vacation days updated
        Employee updatedEmployee = employeeRepository.findById(employee.getId()).get();
        assert(updatedEmployee.getRemainingVacationDays() == 5);

        // 5️⃣ Filter employee requests by status APPROVED
        mockMvc.perform(get("/api/employee/{employeeId}/requests", employee.getId())
                        .param("status", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value(Status.APPROVED.toString()));
    }
}
