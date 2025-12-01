package com.org.app.dcas.service;

import com.org.app.dcas.model.Users;
import com.org.app.dcas.model.Company;
import com.org.app.dcas.repository.UsersRepository;
import org.springframework.stereotype.Service;

@Service
public class CompanyScopedService {

    private final UsersRepository usersRepository;

    public CompanyScopedService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public Long getCompanyIdForUser(Long userId) {
        Users user = usersRepository.findById(userId).orElse(null);
        if (user == null || user.getCompany() == null) {
            throw new IllegalArgumentException("Invalid user or company for userId: " + userId);
        }
        return user.getCompany().getCompanyId();
    }

    public Company getCompanyForUser(Long userId) {
        Users user = usersRepository.findById(userId).orElse(null);
        if (user == null || user.getCompany() == null) {
            throw new IllegalArgumentException("Invalid user or company for userId: " + userId);
        }
        return user.getCompany();
    }
}
