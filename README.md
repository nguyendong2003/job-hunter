# Job hunter project
- Java Spring Restful API

# Technology
- Java
- Spring Boot
- Spring Data JPA
- Spring Security
- Spring Validation
- Spring Mail
- Thymeleaf
- MySQL
- Swagger/OpenAPI

# Swagger
- http://localhost:8080/swagger-ui/index.html

# Technique
- RESTful API: Implement CRUD functionalities to manage resources such as jobs, users, subcribers, companies, roles, permissions, resumes,...
- Handle JWT (JSON Web Token) access token, refresh token logic for authentication and authorization
- Role-Based Access Control: Differentiate access levels for guest, registered, and admin users.
- Form Validation: Ensure data integrity and user input validation using Spring Validation.
- Email Notifications: Send email alerts and notifications using Spring Mail.
- Database Integration: Use Spring Data JPA for database operations with MySQL.
- API Documentation: Provide API documentation using Swagger/OpenAPI.

# Project description
- This project is a job search platform, developed in Java Spring Boot, designed to connect job seekers and employers effectively. The platform includes a registration/login system and enables both unregistered and registered users to explore job opportunities.

# Role and Key Features:
1. Guest Users:
- View company and job listings.
- Search jobs by skill or location criteria.

2. Registered Users (Normal User Role by Default):
- Submit resumes for job applications.
- Track application history.
- Subscribe to job alerts based on specific skills.

3. Admin Users:
- Have full permissions, created via backend code, allowing comprehensive management capabilities.


==> Permission Management: Permissions are defined as API endpoints, giving granular access to different functionalities based on user roles. Admins have unrestricted access, while normal users have limited access based on their role.

==> This platform aims to provide a structured, user-friendly job search experience with robust backend support for user management and permissions.
