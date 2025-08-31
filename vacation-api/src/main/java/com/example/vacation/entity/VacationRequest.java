package com.example.vacation.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class VacationRequest {

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee author;

    @ManyToOne
    private Employee resolvedBy; // manager who approves/rejects

    private LocalDate vacationStartDate;
    private LocalDate vacationEndDate;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING; // default is PENDING

    private LocalDateTime requestCreatedAt = LocalDateTime.now();

    private boolean resolved;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getAuthor() { return author; }
    public void setAuthor(Employee author) { this.author = author; }

    public Employee getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(Employee resolvedBy) { this.resolvedBy = resolvedBy; }

    public LocalDate getVacationStartDate() { return vacationStartDate; }
    public void setVacationStartDate(LocalDate vacationStartDate) { this.vacationStartDate = vacationStartDate; }

    public LocalDate getVacationEndDate() { return vacationEndDate; }
    public void setVacationEndDate(LocalDate vacationEndDate) { this.vacationEndDate = vacationEndDate; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getRequestCreatedAt() { return requestCreatedAt; }
    public void setRequestCreatedAt(LocalDateTime requestCreatedAt) { this.requestCreatedAt = requestCreatedAt; }

    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }
}
