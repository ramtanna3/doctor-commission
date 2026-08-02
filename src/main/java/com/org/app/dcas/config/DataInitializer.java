package com.org.app.dcas.config;

import com.org.app.dcas.model.Company;
import com.org.app.dcas.model.Users;
import com.org.app.dcas.repository.CompanyRepository;
import com.org.app.dcas.repository.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Ensures every user in the DB has authentication credentials.
 * Runs once on startup. Safe to run multiple times (idempotent).
 *
 * Default credentials assigned to users that have no username:
 *   user_id=1  → username="admin",   password="Admin@123"
 *   others     → username=email,     password="Chang3Me!"
 *
 * Users MUST change their passwords after first login.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsersRepository usersRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsersRepository usersRepository,
                           CompanyRepository companyRepository,
                           PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        List<Users> users = usersRepository.findAll();

        // Fresh database — create a default company and admin user together
        if (users.isEmpty()) {
            log.info("DataInitializer - no users found, creating default company and admin user");

            // Create default company if none exists
            List<Company> companies = companyRepository.findByIsActiveTrue();
            Company company;
            if (companies.isEmpty()) {
                company = new Company();
                company.setName("Default Company");
                company.setIsActive(true);
                company.setCreatedBy("system");
                company.setUpdatedBy("system");
                company = companyRepository.save(company);
                log.info("DataInitializer - created default company with companyId={}", company.getCompanyId());
            } else {
                company = companies.get(0);
                log.info("DataInitializer - using existing companyId={}", company.getCompanyId());
            }

            Users admin = new Users();
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setEmail("admin@dcas.com");
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
            admin.setIsActive(true);
            admin.setCompany(company);
            usersRepository.save(admin);
            log.info("DataInitializer - created admin user for companyId={}", company.getCompanyId());
            log.info("DataInitializer - done (fresh setup complete)");
            return;
        }

        // Existing users — ensure each has auth credentials set
        for (Users user : users) {
            if (user.getUsername() != null) {
                continue; // already has credentials
            }

            String username;
            String rawPassword;

            if (user.getUserId() != null && user.getUserId() == 1L) {
                username = "admin";
                rawPassword = "Admin@123";
            } else {
                username = (user.getEmail() != null && !user.getEmail().isBlank())
                        ? user.getEmail()
                        : "user_" + user.getUserId();
                rawPassword = "Chang3Me!";
            }

            user.setUsername(username);
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
            usersRepository.save(user);
            log.info("DataInitializer - set credentials for userId={}, username='{}'", user.getUserId(), username);
        }

        log.info("DataInitializer - done");
    }
}
