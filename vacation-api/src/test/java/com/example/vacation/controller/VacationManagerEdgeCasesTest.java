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
public class VacationManagerEdgeCasesTest {

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
    public void testRejectVacationRequest() throws Exception {
        // 1️⃣ Create a vacation request
        CreateVacationRequestDTO createDto = new CreateVacationRequestDTO();
        createDto.setAuthorId(employee.getId());
        createDto.setVacationStartDate(LocalDate.of(2025, 9, 1));
        createDto.setVacationEndDate(LocalDate.of(2025, 9, 5));

        String createJson = objectMapper.writeValueAsString(createDto);

        mockMvc.perform(post("/api/employee/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated());

        VacationRequest request = vacationRequestRepository.findAll().get(0);

        // 2️⃣ Reject the request
        UpdateVacationRequestDTO rejectDto = new UpdateVacationRequestDTO();
        rejectDto.setManagerId(manager.getId());
        rejectDto.setStatus(Status.REJECTED.toString());

        String rejectJson = objectMapper.writeValueAsString(rejectDto);

        mockMvc.perform(put("/api/employee/requests/{id}/process", request.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rejectJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(Status.REJECTED.toString()))
                .andExpect(jsonPath("$.resolved").value(true))
                .andExpect(jsonPath("$.resolvedBy.id").value(manager.getId()));

        // 3️⃣ Ensure remaining vacation days unchanged after rejection
        Employee updatedEmployee = employeeRepository.findById(employee.getId()).get();
        assert(updatedEmployee.getRemainingVacationDays() == 10);

        // 4️⃣ Create an overlapping request (should be detected)
        CreateVacationRequestDTO overlappingDto = new CreateVacationRequestDTO();
        overlappingDto.setAuthorId(employee.getId());
        overlappingDto.setVacationStartDate(LocalDate.of(2025, 9, 4));
        overlappingDto.setVacationEndDate(LocalDate.of(2025, 9, 7));

        String overlapJson = objectMapper.writeValueAsString(overlappingDto);

        mockMvc.perform(post("/api/employee/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(overlapJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("overlaps")));

        // 5️⃣ Verify GET /overlaps detects the overlapping range
        mockMvc.perform(get("/api/employee/{id}/overlaps", employee.getId())
                        .param("start", "2025-09-04")
                        .param("end", "2025-09-07"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
