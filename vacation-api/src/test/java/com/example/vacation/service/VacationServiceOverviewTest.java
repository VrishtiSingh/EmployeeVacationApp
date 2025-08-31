package com.example.vacation.service;

import com.example.vacation.dto.EmployeeVacationOverviewDTO;
import com.example.vacation.entity.Employee;
import com.example.vacation.entity.VacationRequest;
import com.example.vacation.repository.EmployeeRepository;
import com.example.vacation.repository.VacationRequestRepository;
import com.example.vacation.exception.EmployeeNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
public class VacationServiceOverviewTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private VacationRequestRepository vacationRequestRepository;

    @InjectMocks
    private VacationService vacationService;

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
    public void testGetEmployeeVacationOverview_withRequests() {
        VacationRequest request = new VacationRequest();
        request.setId(1L);
        request.setAuthor(employee);
        request.setVacationStartDate(LocalDate.of(2025, 9, 1));
        request.setVacationEndDate(LocalDate.of(2025, 9, 5));

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(vacationRequestRepository.findByAuthorId(1L)).thenReturn(List.of(request));

        EmployeeVacationOverviewDTO overview = vacationService.getEmployeeVacationOverview(1L, null);

        assertEquals(employee.getId(), overview.getEmployeeId());
        assertEquals(employee.getName(), overview.getName());
        assertEquals(employee.getRole(), overview.getRole());
        assertEquals(employee.getRemainingVacationDays(), overview.getRemainingVacationDays());
        assertEquals(1, overview.getVacationRequests().size());
        assertEquals(request, overview.getVacationRequests().get(0));
    }

    @Test
    public void testGetEmployeeVacationOverview_noRequests() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(vacationRequestRepository.findByAuthorId(1L)).thenReturn(List.of());

        EmployeeVacationOverviewDTO overview = vacationService.getEmployeeVacationOverview(1L, null);

        assertEquals(0, overview.getVacationRequests().size());
    }

    @Test
    public void testGetEmployeeVacationOverview_employeeNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> {
            vacationService.getEmployeeVacationOverview(99L, null);
        });
    }
}
