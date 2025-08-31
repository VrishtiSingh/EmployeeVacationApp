package com.example.vacation.service;

import com.example.vacation.entity.Employee;
import com.example.vacation.entity.VacationRequest;
import com.example.vacation.repository.VacationRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class VacationServiceTest {

    @Mock
    private VacationRequestRepository vacationRequestRepository;

    @InjectMocks
    private VacationService vacationService; // Your service class

    private Employee author;
    private VacationRequest vacationRequest;

    @BeforeEach
    void setUp() {
        author = new Employee();
        author.setId(1L);
        author.setName("John Doe"); // Make sure your Employee has a setName() method

        vacationRequest = new VacationRequest();
        vacationRequest.setId(1L);
        vacationRequest.setAuthor(author);
        vacationRequest.setVacationStartDate(LocalDate.of(2025, 9, 1));
        vacationRequest.setVacationEndDate(LocalDate.of(2025, 9, 5));
        vacationRequest.setStatus(VacationRequest.Status.PENDING);
    }

    @Test
    void testCreateVacationRequest() {
        when(vacationRequestRepository.save(any(VacationRequest.class))).thenReturn(vacationRequest);

        VacationRequest created = vacationService.createVacationRequest(vacationRequest);

        assertNotNull(created);
        assertEquals(author, created.getAuthor());
        assertEquals(LocalDate.of(2025, 9, 1), created.getVacationStartDate());
        verify(vacationRequestRepository, times(1)).save(vacationRequest);
    }
    

    @Test
    void testGetVacationRequestById() {
        when(vacationRequestRepository.findById(1L)).thenReturn(Optional.of(vacationRequest));

        VacationRequest found = vacationService.getVacationRequestById(1L);

        assertNotNull(found);
        assertEquals(author, found.getAuthor());
        verify(vacationRequestRepository, times(1)).findById(1L);
    }
}
