package com.mchis.assignment;

import com.mchis.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssignmentGradeRepository extends JpaRepository<AssignmentGrade, Integer> {
    Optional<AssignmentGrade> findByStudentAndAssignment(User student, Assignment assignment);
}
