package com.cst438.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentDTO;
import com.cst438.domain.EnrollmentRepository;

@RestController
public class EnrollmentController {

	@Autowired
	CourseRepository courseRepository;

	@Autowired
	EnrollmentRepository enrollmentRepository;

	/*
	 * endpoint used by registration service to add an enrollment to an existing
	 * course.
	 */
	@PostMapping("/enrollment")
	@Transactional
	public EnrollmentDTO addEnrollment(@RequestBody EnrollmentDTO enrollmentDTO) {
		
		//TODO  complete this method in homework 4
		// this creates a new Enrollment to store the student's enrollment in the enrollmentRepository
		Enrollment e = new Enrollment();
		e.setStudentEmail(enrollmentDTO.studentEmail);
		e.setStudentName(enrollmentDTO.studentName);
		// this line gets the course from the courseRepository with the same course_id
		// if there is no course with the same course_id then the variable c is set to null
		Course c = courseRepository.findById(enrollmentDTO.course_id).orElse(null);
		// the following if statement checks if the course stated in the variable enrollmentDTO is in the database
		// if it is not then an error is thrown and a message is displayed in the console on why the error has occurred
		if(c == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course if not found.");
		}
		e.setCourse(c);
		e = enrollmentRepository.save(e);
		enrollmentDTO.id = e.getId();
		// returns the enrollmentDTO since this method expects an EnrollmentDTO to be returned
		return enrollmentDTO;
		
	}

}
