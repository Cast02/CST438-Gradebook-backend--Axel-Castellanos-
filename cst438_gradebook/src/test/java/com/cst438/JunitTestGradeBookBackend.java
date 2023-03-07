package com.cst438;

import java.text.SimpleDateFormat;
//import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import java.sql.Date;



import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.springframework.test.context.ContextConfiguration;




import com.cst438.controllers.GradeBookController;
import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentGrade;
import com.cst438.domain.AssignmentGradeRepository;
import com.cst438.domain.AssignmentListDTO;
import com.cst438.domain.AssignmentListDTO.AssignmentDTO;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.GradebookDTO;
import com.cst438.services.RegistrationService;
//import com.google.common.base.Optional;
import com.fasterxml.jackson.databind.ObjectMapper;

@ContextConfiguration(classes = { GradeBookController.class })
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest
public class JunitTestGradeBookBackend {
	
	static final String URL = "http://localhost:8081";
	
	public static final int TEST_COURSE_ID = 999001;
	public static final String TEST_INSTRUCTOR_EMAIL = "dwisneski@csumb.edu";
	public static final int TEST_YEAR = 2021;
	public static final String TEST_SEMESTER = "Fall";
	public static final String TEST_COURSE_TITLE = "Test Course";
	
	public static final int TEST_ASSIGNMENT_ID = 1;
	public static final String TEST_DUE_DATE = "2021-09-05";
 	public static final String TEST_ASSIGNMENT_NAME = "test";
	public static final int TEST_NEEDS_GRADING = 0;
	
	public static final String TEST_STUDENT_EMAIL = "test@csumb.edu";
	public static final String TEST_STUDENT_NAME = "test";
	
	@MockBean
	AssignmentRepository assignmentRepository;

	@MockBean
	AssignmentGradeRepository assignmentGradeRepository;

	@MockBean
	CourseRepository courseRepository;
	
	@MockBean
	RegistrationService registrationService;
	
	@Autowired
	private MockMvc mvc;
	
	@Test
	public void addAssignment() throws Exception{
		MockHttpServletResponse response;
		
		//Creates a new course
		Course course = new Course();
				
		course.setCourse_id(TEST_COURSE_ID);
		course.setSemester(TEST_SEMESTER);
		course.setYear(TEST_YEAR);
		course.setInstructor(TEST_INSTRUCTOR_EMAIL);
		course.setTitle(TEST_COURSE_TITLE);
		
		
		//Creates a new assignment
		Assignment assignment = new Assignment();
		assignment.setCourse(course);
		assignment.setDueDate(Date.valueOf(TEST_DUE_DATE));	
		assignment.setName(TEST_ASSIGNMENT_NAME);
		assignment.setNeedsGrading(1);
		assignment.setId(TEST_ASSIGNMENT_ID);
		

		//Helps return data instead of null values
		given(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).willReturn(Optional.of(assignment));
		given(courseRepository.findById(TEST_COURSE_ID)).willReturn(Optional.of(course));
		given(courseRepository.save(any())).willReturn(course);
		given(assignmentRepository.save(any())).willReturn(assignment);
		
		
		//This is used ass the content portion when calling the REST API, "/addAssignment"
		AssignmentListDTO.AssignmentDTO assignmentDTO = new AssignmentListDTO.AssignmentDTO();
		assignmentDTO.assignmentName = assignment.getName();
		assignmentDTO.dueDate = assignment.getDueDate().toString();
		assignmentDTO.courseId = assignment.getCourse().getCourse_id();
		assignmentDTO.assignmentId = assignment.getId();
		
				

		response = mvc.perform(MockMvcRequestBuilders.post("/addAssignment")
				.content(asJsonString(assignmentDTO))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
		
		//Makes sure that the status came back as OK
		assertEquals(200, response.getStatus());
		
		//Used to confirm that the name and due date of the assignment that was passed in and returned have the same information
		AssignmentListDTO.AssignmentDTO result = fromJsonString(response.getContentAsString(), AssignmentListDTO.AssignmentDTO.class);
		assertEquals(TEST_ASSIGNMENT_NAME, result.assignmentName);
		assertEquals(TEST_DUE_DATE, result.dueDate);
		
	}
	
	@Test
	public void changeNameOnAssignment() throws Exception{
		MockHttpServletResponse response;
		
		//Creates new course
		Course course = new Course();
		
		course.setCourse_id(TEST_COURSE_ID);
		course.setSemester(TEST_SEMESTER);
		course.setYear(TEST_YEAR);
		course.setInstructor(TEST_INSTRUCTOR_EMAIL);
		course.setEnrollments(new java.util.ArrayList<Enrollment>());
		course.setAssignments(new java.util.ArrayList<Assignment>());
		
		//Creates new assignment
		Assignment assignment = new Assignment();
		
		assignment.setCourse(course);
		course.getAssignments().add(assignment);
		assignment.setDueDate(new java.sql.Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000));
		assignment.setId(1);
		assignment.setName("Assignment 1");
		assignment.setNeedsGrading(1);
		
		//Creates new enrollment
		Enrollment enrollment = new Enrollment();
		enrollment.setCourse(course);
		course.getEnrollments().add(enrollment);
		enrollment.setId(TEST_COURSE_ID);
		enrollment.setStudentEmail(TEST_STUDENT_EMAIL);
		enrollment.setStudentName(TEST_STUDENT_NAME);
		
		//Creates new assignmentgrade
		AssignmentGrade ag = new AssignmentGrade();
		ag.setAssignment(assignment);
		ag.setId(1);
		ag.setScore("80");
		ag.setStudentEnrollment(enrollment);
		
		//Returns data so that the return value is not null
		given(assignmentRepository.findById(1)).willReturn(Optional.of(assignment));
		given(assignmentRepository.save(any())).willReturn(assignment);
		given(assignmentGradeRepository.findByAssignmentIdAndStudentEmail(1, TEST_STUDENT_EMAIL)).willReturn(ag);
		given(assignmentGradeRepository.findById(1)).willReturn(Optional.of(ag));
		

		response = mvc.perform(MockMvcRequestBuilders.get("/gradebook/1").accept(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
		
		
		assertEquals(200, response.getStatus());
		verify(assignmentRepository, times(0)).save(any());
		
		//Retrieves the data from calling the REST API, "/gradebook/1"
		GradebookDTO result = fromJsonString(response.getContentAsString(), GradebookDTO.class); 
		
		//Checks to make sure data from the call matches the data that was set up
		assertEquals("Assignment 1", result.assignmentName);
		assertEquals(1, result.grades.size());
		assertEquals(TEST_STUDENT_NAME, result.grades.get(0).name);
		assertEquals("80", result.grades.get(0).grade);
		
		//Changes the assign name to new desired name
		result.assignmentName = "New Assignment Name";
		
		//Uses .put to make changes on assignment
		response = mvc.perform(MockMvcRequestBuilders.put("/gradebook/1")
				.accept(MediaType.APPLICATION_JSON)
				.content(asJsonString(result))
				.contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
		
		assertEquals(200, response.getStatus());
		
		//Confirms that the change has been made to the assignment name
		assertEquals("New Assignment Name", result.assignmentName);
		assertEquals(1, result.grades.size());
		assertEquals(TEST_STUDENT_NAME, result.grades.get(0).name);
		assertEquals("80", result.grades.get(0).grade);
		
	}
	
	@Test
	public void deleteAssignment() throws Exception{
		
		MockHttpServletResponse response;
		MockHttpServletResponse responseForSecondAssignment;
		
		//Creates new course
		Course course = new Course();
		course.setCourse_id(TEST_COURSE_ID);
		course.setCourse_id(TEST_COURSE_ID);
		course.setSemester(TEST_SEMESTER);
		course.setYear(TEST_YEAR);
		
		//Creates an assignment with the variable NeedsGrading = 1
		Assignment assignment = new Assignment();
		assignment.setCourse(course);
		assignment.setDueDate(new java.sql.Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000));
		assignment.setId(1);
		assignment.setName("Assignment 1");
		assignment.setNeedsGrading(1);
		
		//Returns data so that null values are not returned
		given(assignmentRepository.findAssignmentById(1)).willReturn(assignment);
		given(assignmentRepository.save(any())).willReturn(assignment);

		//The delete REST API deletes the assignment with the Id of 1
		response = mvc.perform(
				MockMvcRequestBuilders
			    .delete("/gradebook/1"))
				.andReturn().getResponse();
		
		//Confirms that delete was called and it returned OK
		assertEquals(200, response.getStatus());
		verify(assignmentRepository).delete(any(Assignment.class));
		
		//Creates a new assignment with the variable NeedsGrading = 0
		// Since it is 0 delete should not be performed. 
		Assignment assignmentGraded = new Assignment();
		assignmentGraded.setCourse(course);
		assignmentGraded.setDueDate(new java.sql.Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000));
		assignmentGraded.setId(2);
		assignmentGraded.setName("Assignment 1");
		assignmentGraded.setNeedsGrading(0);
		
		//Returns data so that a null value is not returned
		given(assignmentRepository.findAssignmentById(2)).willReturn(assignmentGraded);
		given(assignmentRepository.save(any())).willReturn(assignmentGraded);
		
		//The delete REST API deletes the assignment with the Id of 2
		responseForSecondAssignment = mvc.perform(
				MockMvcRequestBuilders
			    .delete("/gradebook/2"))
				.andReturn().getResponse();
		
		//Since the assignment is already graded it cannot be deleted
		//Therefore the response that should be returned is 400 
		//Even though it returns an error, we must confirm that delete was called
		assertEquals(400, responseForSecondAssignment.getStatus());
		verify(assignmentRepository).delete(any(Assignment.class));
	}
		

	
	private static String asJsonString(final Object obj) {
		try {

			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static <T> T fromJsonString(String str, Class<T> valueType) {
		try {
			return new ObjectMapper().readValue(str, valueType);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
