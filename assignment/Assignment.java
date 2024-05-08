package com.mchis.assignment;

import com.mchis.part.Part;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SuperBuilder
public class Assignment extends Part {
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime uploadedTime;
    private String path;
    private boolean uploadedStatus;
    private boolean gradedStatus;

    @OneToMany(mappedBy = "assignment")
    private List<AssignmentGrade> grades;
}
