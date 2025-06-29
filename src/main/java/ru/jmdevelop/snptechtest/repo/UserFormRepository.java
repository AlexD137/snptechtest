package ru.jmdevelop.snptechtest.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.jmdevelop.snptechtest.entity.FormByUser;

public interface UserFormRepository extends JpaRepository<FormByUser, Long> {
}