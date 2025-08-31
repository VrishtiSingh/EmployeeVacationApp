package com.example.vacation.entity;

import jakarta.persistence.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Entity
@Schema(description = "Represents an employee in the system")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique ID of the employee", example = "1")
    private Long id;

    @Schema(description = "Full name of the employee", example = "Alice Johnson", required = true)
    private String name;

    @Schema(description = "Email address of the employee", example = "alice.johnson@example.com", required = true)
    private String email;

    @Schema(description = "Remaining vacation days for the employee in the current year", example = "30")
    private int remainingVacationDays = 30;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    @Schema(description = "List of vacation requests submitted by the employee")
    private List<VacationRequest> requests;

    // Optional: role field for authorization
    @Schema(description = "Role of the employee", example = "EMPLOYEE", allowableValues = {"EMPLOYEE","MANAGER"})
    private String role = "EMPLOYEE";

    // Constructors
    public Employee() {}

    public Employee(String name) {
        this.name = name;
    }


    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; } 
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public int getRemainingVacationDays() { return remainingVacationDays; }
    public void setRemainingVacationDays(int days) { this.remainingVacationDays = days; }
    public List<VacationRequest> getRequests() { return requests; }
    public void setRequests(List<VacationRequest> requests) { this.requests = requests; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
