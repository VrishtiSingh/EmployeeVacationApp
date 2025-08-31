package com.example.vacation.controller;

import com.example.vacation.dto.EmployeeVacationOverviewDTO;
import com.example.vacation.entity.Employee;
import com.example.vacation.entity.VacationRequest;
import com.example.vacation.entity.VacationRequest.Status;
import com.example.vacation.service.VacationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VacationRequestController.class)
public class VacationRequestControllerOverviewTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VacationService vacationService;

    @Autowired
    private ObjectMapper objectMapper;

    private Employee employee;

    @BeforeEach
    public void setup() {
        employee = new Employee();
        employee.setId(1L);
        employee.setName("Alice");
        employee.setRole("EMPLOYEE");
        employee.setRemainingVacationDays(10);
    }

    @Test
    public void testGetEmployeeOverview() throws Exception {
        VacationRequest request = new VacationRequest();
        request.setId(1L);
        request.setAuthor(employee);
        request.setVacationStartDate(LocalDate.of(2025, 9, 1));
        request.setVacationEndDate(LocalDate.of(2025, 9, 5));
        request.setStatus(Status.PENDING);

        EmployeeVacationOverviewDTO overviewDTO = new EmployeeVacationOverviewDTO(
                employee.getId(),
                employee.getName(),
                employee.getRole(),
                employee.getRemainingVacationDays(),
                List.of(request)
        );

        when(vacationService.getEmployeeVacationOverview(1L, (Status) null)).thenReturn(overviewDTO);


        mockMvc.perform(get("/api/employee/1/overview")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(1))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.remainingVacationDays").value(10))
                .andExpect(jsonPath("$.vacationRequests[0].id").value(1))
                .andExpect(jsonPath("$.vacationRequests[0].status").value("PENDING"));
    }
}
