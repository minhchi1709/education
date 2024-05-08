package com.mchis.assignment;

import com.mchis.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class AssignmentGrade {
    @Id
    @GeneratedValue
    private Integer id;
    private Float grade;
    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;
    @ManyToOne
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;
}
