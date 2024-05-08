package com.mchis.course;

import com.mchis.assignment.AssignmentRequest;
import com.mchis.assignment.GradingAssignmentRequest;
import com.mchis.exception.OperationNotPermittedException;
import com.mchis.file.FileRequest;
import com.mchis.section.SectionRequest;
import com.mchis.text.TextRequest;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("courses")
@Tag(name = "Course")
public class CourseController {
    private final CourseService courseService;

    @GetMapping("")
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/{course-id}")
    public ResponseEntity<Course> getCourseById(@PathVariable("course-id") Integer id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @PostMapping("")
    public ResponseEntity<?> addCourse(
            @RequestBody CourseRequest course,
            Authentication authentication) {
        return ResponseEntity.ok(courseService.addCourse(course, authentication));
    }

    @GetMapping("/my/{course-id}")
    public ResponseEntity<Course> getMyCourseById(@PathVariable("course-id") Integer id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @GetMapping("/my")
    public ResponseEntity<List<Course>> getAllMyCourses(Authentication authentication) {
        return ResponseEntity.ok(courseService.getAllMyCourses(authentication));
    }

    @GetMapping("/teach/{course-id}")
    public ResponseEntity<Course> getTeachingCourseById(@PathVariable("course-id") Integer id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @PutMapping("/teach/{course-id}")
    public ResponseEntity<Course> editTeachingCourse(
            @PathVariable("course-id") Integer courseId,
            @RequestBody CourseRequest request,
            Authentication authentication
    ) throws OperationNotPermittedException {
        return ResponseEntity.ok(courseService.editTeachingCourse(courseId, request, authentication));
    }

    @DeleteMapping("/teach/{course-id}")
    public ResponseEntity<?> deleteTeachingCourse(
            @PathVariable("course-id") Integer courseId,
            Authentication authentication
    ) throws OperationNotPermittedException {
        courseService.deleteTeachingCourse(courseId, authentication);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/teach")
    public ResponseEntity<List<Course>> getAllTeachingCourses(Authentication authentication) {
        return ResponseEntity.ok(courseService.getAllTeachingCourses(authentication));
    }

    @PostMapping("/teach/{course-id}/create-section")
    public ResponseEntity<?> addSection(
            @PathVariable("course-id") Integer id,
            @RequestBody SectionRequest request,
            Authentication authentication
    ) throws OperationNotPermittedException {
        courseService.addSection(id, request, authentication);
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/teach/{course-id}/sections/{section-id}")
    public ResponseEntity<?> editSection(
            @PathVariable("course-id") Integer courseId,
            @PathVariable("section-id") Integer sectionId,
            @RequestBody SectionRequest request,
            Authentication authentication
    ) throws OperationNotPermittedException {
        courseService.editSection(courseId, sectionId, request, authentication);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/teach/{course-id}/sections/{section-id}")
    public ResponseEntity<?> deleteSection(
            @PathVariable("course-id") Integer courseId,
            @PathVariable("section-id") Integer sectionId,
            Authentication authentication
    ) throws OperationNotPermittedException {
        courseService.deleteSection(courseId, sectionId, authentication);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/teach/{course-id}/sections/{section-id}/texts")
    public ResponseEntity<?> addPartText(
            @PathVariable("course-id") Integer courseId,
            @PathVariable("section-id") Integer sectionId,
            @RequestBody TextRequest request,
            Authentication authentication
    ) throws OperationNotPermittedException {
        courseService.addPartText(courseId, sectionId, request, authentication);
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/teach/{course-id}/sections/{section-id}/texts/{part-id}")
    public ResponseEntity<?> editPartText(
            @PathVariable("course-id") Integer courseId,
            @PathVariable("section-id") Integer sectionId,
            @PathVariable("part-id") Integer partId,
            @RequestBody TextRequest request,
            Authentication authentication
    ) throws OperationNotPermittedException {
        courseService.editPartText(courseId, sectionId, partId, request, authentication);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/teach/{course-id}/sections/{section-id}/texts/{part-id}")
    public ResponseEntity<?> deletePartText(
            @PathVariable("course-id") Integer courseId,
            @PathVariable("section-id") Integer sectionId,
            @PathVariable("part-id") Integer partId,
            Authentication authentication
    ) throws OperationNotPermittedException {
        courseService.deletePartText(courseId, sectionId, partId, authentication);
        return ResponseEntity.accepted().build();
    }

    @PostMapping(value = "/teach/{course-id}/sections/{section-id}/files", consumes = "multipart/form-data")
    public ResponseEntity<?> addPartFile(
            @PathVariable("course-id") Integer courseId,
            @PathVariable("section-id") Integer sectionId,
            FileRequest request,
            @Parameter()
            @RequestPart("file") MultipartFile file,
            Authentication authentication
    ) throws OperationNotPermittedException {
        courseService.addPartFile(courseId, sectionId, request, file, authentication);
        return ResponseEntity.accepted().build();
    }

    @PutMapping(value = "/teach/{course-id}/sections/{section-id}/files/{part-id}", consumes = "multipart/form-data")
    public ResponseEntity<?> editPartFile(
            @PathVariable("course-id") Integer courseId,
            @PathVariable("section-id") Integer sectionId,
            @PathVariable("part-id") Integer partId,
            @RequestBody FileRequest request,
            @Parameter()
            @RequestPart("file") MultipartFile file,
            Authentication authentication
    ) throws OperationNotPermittedException {
        courseService.editPartFile(courseId, sectionId, partId, request, file, authentication);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping(value = "/teach/{course-id}/sections/{section-id}/files/{part-id}")
    public ResponseEntity<?> deletePartFile(
            @PathVariable("course-id") Integer courseId,
            @PathVariable("section-id") Integer sectionId,
            @PathVariable("part-id") Integer partId,
            Authentication authentication
    ) throws OperationNotPermittedException {
        courseService.deletePartFile(courseId, sectionId, partId, authentication);
        return ResponseEntity.accepted().build();
    }

    @PostMapping(value = "/teach/{course-id}/sections/{section-id}/assignments", consumes = "multipart/form-data")
    public ResponseEntity<?> addPartAssignment(
            @PathVariable("course-id") Integer courseId,
            @PathVariable("section-id") Integer sectionId,
            AssignmentRequest request,
            @Parameter()
            @RequestPart("file") MultipartFile file,
            Authentication authentication
    ) throws OperationNotPermittedException {
        courseService.addPartAssignment(courseId, sectionId, request, file, authentication);
        return ResponseEntity.accepted().build();
    }

    @PutMapping(value = "/teach/{course-id}/sections/{section-id}/assignments/{part-id}", consumes = "multipart/form-data")
    public ResponseEntity<?> editPartAssignment(
            @PathVariable("course-id") Integer courseId,
            @PathVariable("section-id") Integer sectionId,
            @PathVariable("part-id") Integer partId,
            @RequestBody AssignmentRequest request,
            @Parameter()
            @RequestPart("file") MultipartFile file,
            Authentication authentication
    ) throws OperationNotPermittedException {
        courseService.editPartAssignment(courseId, sectionId, partId, request, file, authentication);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping(value = "/teach/{course-id}/sections/{section-id}/assignments/{part-id}")
    public ResponseEntity<?> deletePartAssignment(
            @PathVariable("course-id") Integer courseId,
            @PathVariable("section-id") Integer sectionId,
            @PathVariable("part-id") Integer partId,
            Authentication authentication
    ) throws OperationNotPermittedException {
        courseService.deletePartAssignment(courseId, sectionId, partId, authentication);
        return ResponseEntity.accepted().build();
    }

    @PostMapping(value = "/teach/{course-id}/sections/{section-id}/assignments")
    public ResponseEntity<?> gradeAssignment(
            @PathVariable("course-id") Integer courseId,
            @PathVariable("section-id") Integer sectionId,
            @PathVariable("part-id") Integer partId,
            GradingAssignmentRequest request,
            Authentication authentication
    ) throws OperationNotPermittedException {
        courseService.gradeAssignment(courseId, sectionId, partId, request, authentication);
        return ResponseEntity.accepted().build();
    }

    @PutMapping(value = "/teach/{course-id}/sections/{section-id}/assignments")
    public ResponseEntity<?> editAssignmentGrade(
            @PathVariable("course-id") Integer courseId,
            @PathVariable("section-id") Integer sectionId,
            @PathVariable("part-id") Integer partId,
            GradingAssignmentRequest request,
            Authentication authentication
    ) throws OperationNotPermittedException {
        courseService.editAssignmentGrade(courseId, sectionId, partId, request, authentication);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping(value = "/teach/{course-id}/sections/{section-id}/assignments")
    public ResponseEntity<?> deleteAssignmentGrade(
            @PathVariable("course-id") Integer courseId,
            @PathVariable("section-id") Integer sectionId,
            @PathVariable("part-id") Integer partId,
            GradingAssignmentRequest request,
            Authentication authentication
    ) throws OperationNotPermittedException {
        courseService.deleteAssignmentGrade(courseId, sectionId, partId, request, authentication);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/assist/{course-id}")
    public ResponseEntity<Course> getAssistingCourseById(@PathVariable("course-id") Integer id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @GetMapping("/assist")
    public ResponseEntity<List<Course>> getAllAssistingCourses(Authentication authentication) {
        return ResponseEntity.ok(courseService.getAllAssistingCourses(authentication));
    }

    @PostMapping("/register")
    public ResponseEntity<Course> registerCourse(@RequestParam("course") Integer id, Authentication authentication) throws OperationNotPermittedException {
        return ResponseEntity.ok(courseService.registerCourse(id, authentication));
    }

    @PostMapping("/add-assistant/{email}")
    public ResponseEntity<Course> addAssistant(@PathVariable("email") String email, Authentication authentication, @RequestParam("course") Integer courseId) throws OperationNotPermittedException {
        return ResponseEntity.ok(courseService.addAssistant(email, courseId, authentication));
    }
}
