package com.org.app.dcas.repository;

import com.org.app.dcas.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<Users, Long> {
}
