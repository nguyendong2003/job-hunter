package vn.nguyendong.jobhunter.controller;

import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.nguyendong.jobhunter.domain.Company;
import vn.nguyendong.jobhunter.service.CompanyService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
public class CompanyController {
    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    /*
     * ResponseEntity<?> is a generic type. It represents the entire HTTP response.
     * ? means it can hold any type of response body.
     * 
     * Thay ? bằng Company cũng được
     * 
     */
    @PostMapping("/companies")
    public ResponseEntity<?> createCompany(@Valid @RequestBody Company company) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.companyService.handleCreateCompany(company));
    }

    @GetMapping("/companies")
    public ResponseEntity<List<Company>> getCompanies() {
        List<Company> companies = this.companyService.handleGetCompanies();
        return ResponseEntity.ok(companies);
    }

    @PutMapping("/companies")
    public ResponseEntity<Company> updateCompany(@Valid @RequestBody Company company) {
        Company updatedCompany = this.companyService.handleUpdateCompany(company);
        return ResponseEntity.ok(updatedCompany);
    }

    @DeleteMapping("/companies/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable("id") long id) {
        this.companyService.handleDeleteCompany(id);
        return ResponseEntity.ok(null);
    }

}