package com.example.vacation.controller;

import com.example.vacation.dto.CreateVacationRequestDTO;
import com.example.vacation.dto.UpdateVacationRequestDTO;
import com.example.vacation.entity.Employee;
import com.example.vacation.entity.VacationRequest;
import com.example.vacation.entity.VacationRequest.Status;
import com.example.vacation.service.VacationService;
import com.example.vacation.dto.EmployeeVacationOverviewDTO;
import com.example.vacation.repository.EmployeeRepository;
import com.example.vacation.repository.VacationRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.boot.test.mock.mockito.MockBean;



import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.beans.factory.annotation.Autowired;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class VacationRequestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private VacationRequestRepository vacationRequestRepository;

	@Autowired
	private ObjectMapper objectMapper;

	// Add this
	@MockBean
	private VacationService vacationService;


    private Employee employee;

    @BeforeEach
    public void setup() {
        vacationRequestRepository.deleteAll();
        employeeRepository.deleteAll();

        employee = new Employee();
        employee.setName("Test Employee");
        employee.setRemainingVacationDays(30);
        employeeRepository.save(employee);
    }
    
    @Test
    public void testGetOverlappingRequestsForEmployee() throws Exception {
        // Create multiple vacation requests for the employee
        VacationRequest request1 = new VacationRequest();
        request1.setAuthor(employee);
        request1.setVacationStartDate(LocalDate.of(2025, 9, 1));
        request1.setVacationEndDate(LocalDate.of(2025, 9, 5));
        request1.setResolved(false);
        request1.setStatus(Status.PENDING);
        vacationRequestRepository.save(request1);

        VacationRequest request2 = new VacationRequest();
        request2.setAuthor(employee);
        request2.setVacationStartDate(LocalDate.of(2025, 9, 10));
        request2.setVacationEndDate(LocalDate.of(2025, 9, 15));
        request2.setResolved(false);
        request2.setStatus(Status.PENDING);
        vacationRequestRepository.save(request2);

        // Test overlapping period that only overlaps with the first request
        mockMvc.perform(get("/api/employee/{id}/overlaps/list", employee.getId())
                        .param("start", "2025-09-03")
                        .param("end", "2025-09-06"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(request1.getId()))
                .andExpect(jsonPath("$.length()").value(1));

        // Test overlapping period that does not overlap any request
        mockMvc.perform(get("/api/employee/{id}/overlaps/list", employee.getId())
                        .param("start", "2025-09-06")
                        .param("end", "2025-09-09"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // Test overlapping period that overlaps both requests
        mockMvc.perform(get("/api/employee/{id}/overlaps/list", employee.getId())
                        .param("start", "2025-09-04")
                        .param("end", "2025-09-12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    
    @Test
    public void testOverlapCheckEndpoint() throws Exception {
        // Create an existing vacation request for the employee
        VacationRequest existing = new VacationRequest();
        existing.setAuthor(employee);
        existing.setVacationStartDate(LocalDate.of(2025, 9, 1));
        existing.setVacationEndDate(LocalDate.of(2025, 9, 5));
        existing.setResolved(false);
        existing.setStatus(Status.PENDING);
        vacationRequestRepository.save(existing);

        // Case 1: Overlapping period (should return true)
        mockMvc.perform(get("/api/employee/{id}/overlaps", employee.getId())
                        .param("start", "2025-09-03")
                        .param("end", "2025-09-06"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // Case 2: Non-overlapping period (should return false)
        mockMvc.perform(get("/api/employee/{id}/overlaps", employee.getId())
                        .param("start", "2025-09-06")
                        .param("end", "2025-09-10"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }


    @Test
    public void testCreateVacationRequest() throws Exception {
        CreateVacationRequestDTO dto = new CreateVacationRequestDTO();
        dto.setAuthorId(employee.getId());
        dto.setVacationStartDate(LocalDate.now().plusDays(1));
        dto.setVacationEndDate(LocalDate.now().plusDays(3));

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.author.id").value(employee.getId()))
                .andExpect(jsonPath("$.status").value(Status.PENDING.toString()));
    }

    @Test
    public void testGetAllRequests() throws Exception {
        // Create a request first
        VacationRequest request = new VacationRequest();
        request.setAuthor(employee);
        request.setVacationStartDate(LocalDate.now().plusDays(1));
        request.setVacationEndDate(LocalDate.now().plusDays(2));
        request.setStatus(Status.PENDING);
        request.setResolved(false);
        vacationRequestRepository.save(request);

        mockMvc.perform(get("/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].author.id").value(employee.getId()));
    }

    @Test
    public void testGetEmployeeRequests() throws Exception {
        VacationRequest request = new VacationRequest();
        request.setAuthor(employee);
        request.setVacationStartDate(LocalDate.now().plusDays(1));
        request.setVacationEndDate(LocalDate.now().plusDays(2));
        request.setStatus(Status.PENDING);
        request.setResolved(false);
        vacationRequestRepository.save(request);

        mockMvc.perform(get("/requests/employee/{employeeId}", employee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].author.id").value(employee.getId()));
    }

    @Test
    public void testProcessVacationRequest() throws Exception {
        // Create a request
        VacationRequest request = new VacationRequest();
        request.setAuthor(employee);
        request.setVacationStartDate(LocalDate.now().plusDays(1));
        request.setVacationEndDate(LocalDate.now().plusDays(2));
        request.setStatus(Status.PENDING);
        request.setResolved(false);
        vacationRequestRepository.save(request);

        UpdateVacationRequestDTO dto = new UpdateVacationRequestDTO();
        dto.setManagerId(employee.getId()); // for simplicity using same employee
        dto.setStatus(Status.APPROVED.toString());

        mockMvc.perform(put("/requests/{id}/process", request.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(Status.APPROVED.toString()))
                .andExpect(jsonPath("$.resolved").value(true));
    }
    
    @Test
    public void testGetEmployeeOverviewWithStatusFilter() throws Exception {
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

        when(vacationService.getEmployeeVacationOverview(Long.valueOf(1L), Status.PENDING))
        .thenReturn(overviewDTO);



        mockMvc.perform(get("/api/employee/1/overview")
                        .param("status", "pending")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vacationRequests[0].status").value("PENDING"));
    }

}
