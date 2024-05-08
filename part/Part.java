package com.mchis.part;

import com.mchis.section.Section;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SuperBuilder
public class Part {
    @Id
    @GeneratedValue
    private Integer id;
    private String title;

    @ManyToOne
    @JoinColumn(name = "section_id")
    private Section section;
}
