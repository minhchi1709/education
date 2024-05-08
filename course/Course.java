package com.mchis.course;

import com.mchis.section.Section;
import com.mchis.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

import static jakarta.persistence.CascadeType.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Course {
    @Id
    @GeneratedValue
    private Integer id;
    private String name;
    private String description;
    @ManyToMany(mappedBy = "learningCourses")
    private List<User> students;
    @ManyToMany(mappedBy = "assistingCourses")
    private List<User> assistants;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id")
    private User teacher;
    @OneToMany(mappedBy = "course", cascade = REMOVE)
    private List<Section> sections;
}
