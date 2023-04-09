package com.cst438.services;


import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Course;
import com.cst438.domain.CourseDTOG;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentDTO;
import com.cst438.domain.EnrollmentRepository;


public class RegistrationServiceMQ extends RegistrationService {

	@Autowired
	EnrollmentRepository enrollmentRepository;

	@Autowired
	CourseRepository courseRepository;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	public RegistrationServiceMQ() {
		System.out.println("MQ registration service ");
	}

	// ----- configuration of message queues

	@Autowired
	Queue registrationQueue;


	// ----- end of configuration of message queue

	// receiver of messages from Registration service
	
	@RabbitListener(queues = "gradebook-queue")
	@Transactional
	public void receive(EnrollmentDTO enrollmentDTO) {
		// prints out a message in the console to confirm the message was received 
		System.out.println("Receive enrollment:" + enrollmentDTO);
		
		//TODO  complete this method in homework 4
		
		// this creates a new Enrollment to store the student's enrollment in the enrollmentRepository
		Enrollment e = new Enrollment();
		e.setStudentName(enrollmentDTO.studentName);
		e.setStudentEmail(enrollmentDTO.studentEmail);
		Course c = courseRepository.findById(enrollmentDTO.course_id).orElse(null);
		// this if statement checks if the course stated in the variable enrollmentDTO is in the database
		// if it is not then an error is thrown and a message is displayed in the console on why the error has occurred
		if(c == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course if not found.");
		}
		e.setCourse(c);
		e = enrollmentRepository.save(e);
		enrollmentDTO.id = e.getId();
	}

	// sender of messages to Registration Service
	@Override
	public void sendFinalGrades(int course_id, CourseDTOG courseDTO) {
		// prints out a message stating that a message is being sent to the registration service
		System.out.println("Sending message to registration service");
		
		//TODO  complete this method in homework 4
		
		// this uses rabbitTemplate to send the courseDTO variable to the RabbitListener in the file GradebookServiceMQ.java in the registration service
		rabbitTemplate.convertAndSend(registrationQueue.getName(), courseDTO);
		// this confirms that the variable was sent correctly
		System.out.println("Message sent to registration service for course "  + course_id);
	}

}
