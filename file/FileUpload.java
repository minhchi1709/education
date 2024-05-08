package com.mchis.file;

import com.mchis.part.Part;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "file")
@SuperBuilder
public class FileUpload extends Part{
    private LocalDateTime uploadTime;
    private String name;
    private String path;
}
