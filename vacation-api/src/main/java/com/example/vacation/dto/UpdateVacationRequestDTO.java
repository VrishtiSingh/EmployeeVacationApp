package com.example.vacation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for updating (approving/rejecting) a vacation request")
public class UpdateVacationRequestDTO {

    @Schema(description = "ID of the manager processing the request", example = "2", required = true)
    private Long managerId;

    @Schema(description = "New status of the vacation request", example = "APPROVED", allowableValues = {"PENDING","APPROVED","REJECTED"}, required = true)
    private String status;

    // Getters and setters
    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
