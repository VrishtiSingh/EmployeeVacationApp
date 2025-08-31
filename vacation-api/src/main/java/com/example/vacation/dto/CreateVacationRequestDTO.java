package com.example.vacation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "DTO for creating a new vacation request")
public class CreateVacationRequestDTO {

    @Schema(description = "ID of the employee requesting vacation", example = "1", required = true)
    private Long authorId;

    @Schema(description = "Vacation start date (yyyy-MM-dd)", example = "2025-09-01", required = true)
    private LocalDate vacationStartDate;

    @Schema(description = "Vacation end date (yyyy-MM-dd)", example = "2025-09-05", required = true)
    private LocalDate vacationEndDate;

    // Getters and setters
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public LocalDate getVacationStartDate() { return vacationStartDate; }
    public void setVacationStartDate(LocalDate vacationStartDate) { this.vacationStartDate = vacationStartDate; }

    public LocalDate getVacationEndDate() { return vacationEndDate; }
    public void setVacationEndDate(LocalDate vacationEndDate) { this.vacationEndDate = vacationEndDate; }
}
