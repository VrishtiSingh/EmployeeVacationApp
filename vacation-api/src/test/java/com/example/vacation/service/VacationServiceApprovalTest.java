package com.example.vacation.service;

import com.example.vacation.entity.Employee;
import com.example.vacation.entity.VacationRequest;
import com.example.vacation.entity.VacationRequest.Status;
import com.example.vacation.repository.VacationRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VacationServiceApprovalTest {

    @Mock
    private VacationRequestRepository vacationRequestRepository;

    @InjectMocks
    private VacationService vacationService;

    private Employee employee;
    private Employee manager;

    @BeforeEach
    public void setup() {
        employee = new Employee();
        employee.setId(1L);
        employee.setName("Alice");
        employee.setRemainingVacationDays(10);

        manager = new Employee();
        manager.setId(2L);
        manager.setName("Bob");
        manager.setRole("MANAGER");
    }

    private VacationRequest createRequest(LocalDate start, LocalDate end) {
        VacationRequest request = new VacationRequest();
        request.setId(1L);
        request.setAuthor(employee);
        request.setVacationStartDate(start);
        request.setVacationEndDate(end);
        request.setResolved(false);
        request.setStatus(Status.PENDING);
        return request;
    }

    @Test
    public void testApproveVacationRequestSuccessfully() {
        VacationRequest request = createRequest(LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 5));

        when(vacationRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        vacationService.approveVacationRequest(1L, manager);

        assertEquals(Status.APPROVED, request.getStatus());
        assertTrue(request.isResolved());
        assertEquals(manager, request.getResolvedBy());
        assertEquals(5, employee.getRemainingVacationDays()); // 10 - 5
        verify(vacationRequestRepository).save(request);
    }

    @Test
    public void testRejectVacationRequestSuccessfully() {
        VacationRequest request = createRequest(LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 5));

        when(vacationRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        vacationService.rejectVacationRequest(1L, manager);

        assertEquals(Status.REJECTED, request.getStatus());
        assertTrue(request.isResolved());
        assertEquals(manager, request.getResolvedBy());
        assertEquals(10, employee.getRemainingVacationDays()); // no deduction
        verify(vacationRequestRepository).save(request);
    }

    @Test
    public void testApproveAlreadyResolvedThrowsException() {
        VacationRequest request = createRequest(LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 5));
        request.setResolved(true);
        request.setStatus(Status.APPROVED);

        when(vacationRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> vacationService.approveVacationRequest(1L, manager));

        assertEquals("Vacation request has already been resolved", ex.getMessage());
    }

    @Test
    public void testRejectAlreadyResolvedThrowsException() {
        VacationRequest request = createRequest(LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 5));
        request.setResolved(true);
        request.setStatus(Status.REJECTED);

        when(vacationRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> vacationService.rejectVacationRequest(1L, manager));

        assertEquals("Vacation request has already been resolved", ex.getMessage());
    }

    @Test
    public void testApproveNonExistentRequestThrowsException() {
        when(vacationRequestRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> vacationService.approveVacationRequest(99L, manager));

        assertEquals("Vacation request not found", ex.getMessage());
    }

    @Test
    public void testRejectNonExistentRequestThrowsException() {
        when(vacationRequestRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> vacationService.rejectVacationRequest(99L, manager));

        assertEquals("Vacation request not found", ex.getMessage());
    }

    @Test
    public void testApproveRequestWithInsufficientDaysThrowsException() {
        employee.setRemainingVacationDays(2); // less than request days
        VacationRequest request = createRequest(LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 5));

        when(vacationRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> vacationService.approveVacationRequest(1L, manager));

        assertEquals("Cannot approve: not enough remaining vacation days", ex.getMessage());
    }

    // Optional: test approving overlapping requests if you implement blocking
}
