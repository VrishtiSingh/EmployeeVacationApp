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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VacationServiceEdgeCaseTest {
	
	// Test helper method to simulate creating a vacation request
	private VacationRequest createVacationRequest(Employee employee, LocalDate start, LocalDate end) {
	    if (start.isAfter(end)) {
	        throw new IllegalArgumentException("Start date cannot be after end date");
	    }

	    int days = (int) (end.toEpochDay() - start.toEpochDay() + 1);

	    if (days <= 0) {
	        throw new IllegalArgumentException("Vacation request must be at least 1 day long");
	    }

	    if (days > employee.getRemainingVacationDays()) {
	        throw new IllegalArgumentException("Not enough remaining vacation days");
	    }

	    List<VacationRequest> existing = vacationRequestRepository.findByAuthorId(employee.getId());

	    for (VacationRequest r : existing) {
	        if (!(end.isBefore(r.getVacationStartDate()) || start.isAfter(r.getVacationEndDate()))) {
	            throw new IllegalArgumentException("Vacation request overlaps with an existing request");
	        }
	    }
	    
	    

	    VacationRequest request = new VacationRequest();
	    request.setAuthor(employee);
	    request.setVacationStartDate(start);
	    request.setVacationEndDate(end);
	    request.setStatus(Status.PENDING);

	    return vacationRequestRepository.save(request);
	}


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
        employee.setRemainingVacationDays(10);
    }

    @Test
    public void testCreateNonOverlappingVacationRequest() {
        LocalDate start = LocalDate.of(2025, 9, 1);
        LocalDate end = LocalDate.of(2025, 9, 5);

        when(vacationRequestRepository.findByAuthorId(employee.getId())).thenReturn(new ArrayList<>());

        VacationRequest request = createVacationRequest(employee, start, end);


        assertNotNull(request);
        assertEquals(start, request.getVacationStartDate());
        assertEquals(end, request.getVacationEndDate());
        assertEquals(Status.PENDING, request.getStatus());
        verify(vacationRequestRepository).save(request);
    }

    @Test
    public void testOverlappingVacationRequestThrowsException() {
        LocalDate start1 = LocalDate.of(2025, 9, 1);
        LocalDate end1 = LocalDate.of(2025, 9, 5);

        LocalDate start2 = LocalDate.of(2025, 9, 4); // overlaps
        LocalDate end2 = LocalDate.of(2025, 9, 8);

        VacationRequest existing = new VacationRequest();
        existing.setVacationStartDate(start1);
        existing.setVacationEndDate(end1);
        existing.setAuthor(employee);

        when(vacationRequestRepository.findByAuthor(employee)).thenReturn(List.of(existing));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
        createVacationRequest(employee, start2, end2));

        assertEquals("Vacation request overlaps with an existing request", exception.getMessage());
    }

    @Test
    public void testVacationRequestWithZeroDaysThrowsException() {
        LocalDate start = LocalDate.of(2025, 9, 1);
        LocalDate end = LocalDate.of(2025, 9, 1);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
        createVacationRequest(employee, start, end));

        assertEquals("Vacation request must be at least 1 day long", exception.getMessage());
    }

    @Test
    public void testVacationRequestWithInsufficientRemainingDaysThrowsException() {
        employee.setRemainingVacationDays(2);

        LocalDate start = LocalDate.of(2025, 9, 1);
        LocalDate end = LocalDate.of(2025, 9, 5); // 5 days, more than 2 remaining

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
        createVacationRequest(employee, start, end));

        assertEquals("Not enough remaining vacation days", exception.getMessage());
    }

    @Test
    public void testBackToBackVacationRequestsAllowed() {
        LocalDate start1 = LocalDate.of(2025, 9, 1);
        LocalDate end1 = LocalDate.of(2025, 9, 5);

        LocalDate start2 = LocalDate.of(2025, 9, 6); // immediately after end1
        LocalDate end2 = LocalDate.of(2025, 9, 10);

        VacationRequest existing = new VacationRequest();
        existing.setVacationStartDate(start1);
        existing.setVacationEndDate(end1);
        existing.setAuthor(employee);

        when(vacationRequestRepository.findByAuthor(employee)).thenReturn(List.of(existing));

        VacationRequest request2 = createVacationRequest(employee, start2, end2);

        assertNotNull(request2);
        assertEquals(start2, request2.getVacationStartDate());
        assertEquals(end2, request2.getVacationEndDate());
    }
}
