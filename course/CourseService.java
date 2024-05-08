package com.mchis.course;

import com.mchis.assignment.*;
import com.mchis.exception.OperationNotPermittedException;
import com.mchis.file.FileRepository;
import com.mchis.file.FileRequest;
import com.mchis.file.FileStorageService;
import com.mchis.file.FileUpload;
import com.mchis.part.Part;
import com.mchis.section.Section;
import com.mchis.section.SectionRepository;
import com.mchis.section.SectionRequest;
import com.mchis.text.Text;
import com.mchis.text.TextRepository;
import com.mchis.text.TextRequest;
import com.mchis.user.User;
import com.mchis.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final TextRepository textRepository;
    private final FileStorageService fileStorageService;
    private final FileRepository fileRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentGradeRepository assignmentGradeRepository;

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Course addCourse(
            CourseRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        Course course = Course.builder()
                .name(request.name())
                .description(request.description())
                .students(new ArrayList<>())
                .assistants(new ArrayList<>())
                .sections(new ArrayList<>())
                .teacher(user)
                .build();
        List<Course> courses = user.getTeachingCourses();
        courses.add(course);
        user.setTeachingCourses(courses);
        userRepository.save(user);
        return courseRepository.save(course);
    }

    public Course editTeachingCourse(
            Integer courseId,
            CourseRequest request,
            Authentication authentication
    ) throws OperationNotPermittedException {
        checkTeacher(authentication, courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Course with id %d does not exist", courseId)));
        course.setName(request.name());
        course.setDescription(request.description());
        return courseRepository.save(course);
    }

    public void deleteTeachingCourse(
            Integer courseId,
            Authentication authentication) throws OperationNotPermittedException {
        checkTeacher(authentication, courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Course with id %d does not exist", courseId)));
        courseRepository.delete(course);
    }

    public Course getCourseById(Integer id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course with course id " + id + " not found"));
    }

    public List<Course> getAllMyCourses(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getLearningCourses();
    }

    public List<Course> getAllTeachingCourses(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getTeachingCourses();
    }

    public List<Course> getAllAssistingCourses(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getAssistingCourses();
    }

    public Course registerCourse(
            Integer id,
            Authentication authentication
    ) throws OperationNotPermittedException {
        User user = (User) authentication.getPrincipal();
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Course with id %d does not exist", id)));
        if (Objects.equals(user, course.getTeacher())) {
            throw new OperationNotPermittedException(String.format("User %s is the teacher of course %s and therefore cannot register this course",
                    user.getFullName(), course.getName()));
        }
        List<User> students = course.getStudents();
        List<Course> courses = user.getLearningCourses();
        courses.add(course);
        user.setLearningCourses(courses);
        students.add(user);
        userRepository.save(user);
        return courseRepository.save(course);
    }

    public Course addAssistant(
            String email,
            Integer courseId,
            Authentication authentication
    ) throws OperationNotPermittedException {
        checkTeacher(authentication, courseId);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with email %s not found", email)));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Course with id %d does not exist", courseId)));
        List<User> assistants = course.getAssistants();
        List<Course> courses = user.getAssistingCourses();
        courses.add(course);
        user.setLearningCourses(courses);
        assistants.add(user);
        userRepository.save(user);
        return courseRepository.save(course);
    }

    public void addSection(
            Integer courseId,
            SectionRequest request,
            Authentication authentication
    ) throws OperationNotPermittedException {
        checkTeacher(authentication, courseId);
        checkAssistant(authentication, courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Course with id %d does not exist", courseId)));
        List<Section> sections = course.getSections();
        Section section = Section.builder()
                .name(request.name())
                .course(course)
                .parts(new ArrayList<>())
                .build();
        sectionRepository.save(section);
        sections.add(section);
        course.setSections(sections);
        courseRepository.save(course);
    }

    public void editSection(
            Integer courseId,
            Integer sectionId,
            SectionRequest request,
            Authentication authentication
    ) throws OperationNotPermittedException {
        checkTeacher(authentication, courseId);
        checkAssistant(authentication, courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Course with id %d not found", courseId)));
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Section in course %s with id %d not found", course.getName(), sectionId)));
        section.setName(request.name());
        sectionRepository.save(section);
    }

    public void deleteSection(
            Integer courseId,
            Integer sectionId,
            Authentication authentication
    ) throws OperationNotPermittedException {
        checkTeacher(authentication, courseId);
        checkAssistant(authentication, courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Course with id %d not found", courseId)));
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Section in course %s with id %d not found", course.getName(), sectionId)));
        sectionRepository.delete(section);
    }

    public void addPartText(
            Integer courseId,
            Integer sectionId,
            TextRequest request,
            Authentication authentication
    ) throws OperationNotPermittedException {
        checkTeacher(authentication, courseId);
        checkAssistant(authentication, courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Course with id %d not found", courseId)));
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Section in course %s with id %d not found", course.getName(), sectionId)));

        Text part = Text.builder()
                .title(request.title())
                .text(request.text())
                .section(section)
                .build();
        textRepository.save(part);
        addPartToSection(part, section);
    }

    public void editPartText(
            Integer courseId,
            Integer sectionId,
            Integer partId,
            TextRequest request,
            Authentication authentication
    ) throws OperationNotPermittedException {
        checkTeacher(authentication, courseId);
        checkAssistant(authentication, courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Course with id %d not found", courseId)));
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Section in course %s with id %d not found", course.getName(), sectionId)));
        Text part = textRepository.findById(partId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Part with id %d in section %s in course %s not found", partId, section.getName(), course.getName())));
        part.setTitle(request.title());
        part.setText(request.text());
        textRepository.save(part);
    }

    public void deletePartText(
            Integer courseId,
            Integer sectionId,
            Integer partId,
            Authentication authentication
    ) throws OperationNotPermittedException {
        checkTeacher(authentication, courseId);
        checkAssistant(authentication, courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Course with id %d not found", courseId)));
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Section in course %s with id %d not found", course.getName(), sectionId)));
        Text part = textRepository.findById(partId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Part with id %d in section %s in course %s not found", partId, section.getName(), course.getName())));
        textRepository.delete(part);
    }

    public void addPartFile(
            Integer courseId,
            Integer sectionId,
            FileRequest request,
            MultipartFile file,
            Authentication authentication
    ) throws OperationNotPermittedException {
        checkTeacher(authentication, courseId);
        checkAssistant(authentication, courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Course with id %d not found", courseId)));
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Section in course %s with id %d not found", course.getName(), sectionId)));
        String path = fileStorageService.saveFile(file, courseId);
        FileUpload createdFile = FileUpload.builder()
                .title(request.title())
                .name(request.name())
                .path(path)
                .section(section)
                .uploadTime(LocalDateTime.now())
                .build();
        fileRepository.save(createdFile);
        addPartToSection(createdFile, section);
    }

    public void editPartFile(
            Integer courseId,
            Integer sectionId,
            Integer partId,
            FileRequest request,
            MultipartFile file,
            Authentication authentication
    ) throws OperationNotPermittedException {
        checkTeacher(authentication, courseId);
        checkAssistant(authentication, courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Course with id %d not found", courseId)));
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Section in course %s with id %d not found", course.getName(), sectionId)));
        FileUpload part = fileRepository.findById(partId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Part with id %d in section %s in course %s not found", partId, section.getName(), course.getName())));
        fileStorageService.deleteFile(part.getPath());
        String path = fileStorageService.saveFile(file, courseId);
        part.setTitle(request.title());
        part.setName(request.name());
        part.setPath(path);
        fileRepository.save(part);
    }

    public void deletePartFile(
            Integer courseId,
            Integer sectionId,
            Integer partId,
            Authentication authentication
    ) throws OperationNotPermittedException {
        checkTeacher(authentication, courseId);
        checkAssistant(authentication, courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Course with id %d not found", courseId)));
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Section in course %s with id %d not found", course.getName(), sectionId)));
        FileUpload part = fileRepository.findById(partId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Part with id %d in section %s in course %s not found", partId, section.getName(), course.getName())));
        fileStorageService.deleteFile(part.getPath());
        fileRepository.delete(part);
    }

    public void addPartAssignment(
            Integer courseId,
            Integer sectionId,
            AssignmentRequest request,
            MultipartFile file,
            Authentication authentication
    ) throws OperationNotPermittedException {
        checkTeacher(authentication, courseId);
        checkAssistant(authentication, courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Course with id %d not found", courseId)));
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Section in course %s with id %d not found", course.getName(), sectionId)));
        String path = fileStorageService.saveAssignment(file, courseId);
        Assignment createdFile = Assignment.builder()
                .title(request.title())
                .name(request.name())
                .path(path)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .uploadedTime(LocalDateTime.now())
                .uploadedStatus(false)
                .gradedStatus(false)
                .section(section)
                .grades(new ArrayList<>())
                .build();
        assignmentRepository.save(createdFile);
        addPartToSection(createdFile, section);
    }

    public void editPartAssignment(
            Integer courseId,
            Integer sectionId,
            Integer partId,
            AssignmentRequest request,
            MultipartFile file,
            Authentication authentication
    ) throws OperationNotPermittedException {
        checkTeacher(authentication, courseId);
        checkAssistant(authentication, courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Course with id %d not found", courseId)));
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Section in course %s with id %d not found", course.getName(), sectionId)));
        Assignment part = assignmentRepository.findById(partId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Part with id %d in section %s in course %s not found", partId, section.getName(), course.getName())));
        fileStorageService.deleteFile(part.getPath());
        String path = fileStorageService.saveAssignment(file, courseId);
        part.setTitle(request.title());
        part.setName(request.name());
        part.setPath(path);
        part.setStartTime(request.startTime());
        part.setEndTime(request.endTime());
        part.setUploadedTime(LocalDateTime.now());
        assignmentRepository.save(part);
    }

    public void deletePartAssignment(
            Integer courseId,
            Integer sectionId,
            Integer partId,
            Authentication authentication
    ) throws OperationNotPermittedException {
        checkTeacher(authentication, courseId);
        checkAssistant(authentication, courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Course with id %d not found", courseId)));
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Section in course %s with id %d not found", course.getName(), sectionId)));
        Assignment part = assignmentRepository.findById(partId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Part with id %d in section %s in course %s not found", partId, section.getName(), course.getName())));
        fileStorageService.deleteFile(part.getPath());
        assignmentRepository.delete(part);
    }

    public void gradeAssignment(
            Integer courseId,
            Integer sectionId,
            Integer partId,
            GradingAssignmentRequest request,
            Authentication authentication) throws OperationNotPermittedException {
        checkTeacher(authentication, courseId);
        checkAssistant(authentication, courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Course with id %d not found", courseId)));
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Section in course %s with id %d not found", course.getName(), sectionId)));
        Assignment part = assignmentRepository.findById(partId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Part with id %d in section %s in course %s not found", partId, section.getName(), course.getName())));
        User student = userRepository.findById(request.studentId())
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with id %d not found", request.studentId())));
        List<AssignmentGrade> studentGrades = student.getGrades();
        List<AssignmentGrade> assignmentGrades = part.getGrades();
        AssignmentGrade grade = AssignmentGrade.builder()
                .grade(request.grade())
                .student(student)
                .assignment(part)
                .build();
        assignmentGradeRepository.save(grade);
        studentGrades.add(grade);
        assignmentGrades.add(grade);
        student.setGrades(studentGrades);
        part.setGrades(assignmentGrades);
        userRepository.save(student);
        assignmentRepository.save(part);
    }

    public void editAssignmentGrade(
            Integer courseId,
            Integer sectionId,
            Integer partId,
            GradingAssignmentRequest request,
            Authentication authentication) throws OperationNotPermittedException {
        checkTeacher(authentication, courseId);
        checkAssistant(authentication, courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Course with id %d not found", courseId)));
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Section in course %s with id %d not found", course.getName(), sectionId)));
        Assignment part = assignmentRepository.findById(partId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Part with id %d in section %s in course %s not found", partId, section.getName(), course.getName())));
        User student = userRepository.findById(request.studentId())
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with id %d not found", request.studentId())));
        AssignmentGrade grade = assignmentGradeRepository.findByStudentAndAssignment(student, part)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Assignment for course %s of student %s not found", course.getName(), student.getName())));
        grade.setGrade(request.grade());
        assignmentGradeRepository.save(grade);
    }

    public void deleteAssignmentGrade(
            Integer courseId,
            Integer sectionId,
            Integer partId,
            GradingAssignmentRequest request,
            Authentication authentication) throws OperationNotPermittedException {
        checkTeacher(authentication, courseId);
        checkAssistant(authentication, courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Course with id %d not found", courseId)));
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Section in course %s with id %d not found", course.getName(), sectionId)));
        Assignment part = assignmentRepository.findById(partId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Part with id %d in section %s in course %s not found", partId, section.getName(), course.getName())));
        User student = userRepository.findById(request.studentId())
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with id %d not found", request.studentId())));
        AssignmentGrade grade = assignmentGradeRepository.findByStudentAndAssignment(student, part)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Assignment for course %s of student %s not found", course.getName(), student.getName())));
        assignmentGradeRepository.delete(grade);
    }

    private void checkAssistant(Authentication authentication, Integer courseId) throws OperationNotPermittedException {
        User assistant = (User) authentication.getPrincipal();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course with course id " + courseId + " not found"));
        boolean notAssistant = true;
        for (User a : course.getAssistants()) {
            if (Objects.equals(a.getId(), assistant.getId())) {
                notAssistant = false;
                break;
            }
        }
        if (notAssistant) {
            throw new OperationNotPermittedException(String.format("User %s is not assistant of course %s not found", assistant.getName(), course.getName()));
        }
    }

    private void checkTeacher(Authentication authentication, Integer courseId) throws OperationNotPermittedException {
        User teacher = (User) authentication.getPrincipal();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course with course id " + courseId + " not found"));
        if (!Objects.equals(teacher.getId(), course.getTeacher().getId())) {
            throw new OperationNotPermittedException(String.format("User %s is not the teacher of course %s",
                    teacher.getFullName(), course.getName()));
        }
    }

    private void addPartToSection(Part part, Section section) {
        List<Part> parts = section.getParts();
        parts.add(part);
        section.setParts(parts);
        sectionRepository.save(section);
    }
}
