package com.cst438;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentGradeRepository;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.EnrollmentRepository;

@SpringBootTest
public class EndToEndTestNewAssignment {
	public static final String CHROME_DRIVER_FILE_LOCATION = "C:\\Users\\caste\\Downloads\\chromedriver_win32\\chromedriver.exe";
	public static final String URL = "http://localhost:3000";
	public static final String TEST_USER_EMAIL = "test@csumb.edu";
	public static final String TEST_INSTRUCTOR_EMAIL = "dwisneski@csumb.edu";
	public static final int SLEEP_DURATION = 1000; // 1 second.
	public static final String TEST_ASSIGNMENT_NAME = "Test Assignment";
	public static final String TEST_COURSE_TITLE = "cst438-software engineering";
	public static final String TEST_STUDENT_NAME = "Test";
	public static final int TEST_COURSE_ID = 123456;
	
	
	@Autowired
	EnrollmentRepository enrollmentRepository;

	@Autowired
	CourseRepository courseRepository;

	@Autowired
	AssignmentGradeRepository assignnmentGradeRepository;

	@Autowired
	AssignmentRepository assignmentRepository;
	
	@Test
	public void addAssignmentTest() throws Exception{
		//create a new course object using the ID of an existing course in database
		Course c = new Course();
		c.setCourse_id(TEST_COURSE_ID);
		c.setInstructor(TEST_INSTRUCTOR_EMAIL);
		c.setSemester("fall");
		c.setYear(2021);
		c.setTitle(TEST_COURSE_TITLE);
		
		//create a new assignment for the new course object that was made and add it to the database
		Assignment a = new Assignment();
		a.setCourse(c);
		// set assignment due date to 24 hours ago
		a.setDueDate(new java.sql.Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000));
		a.setName(TEST_ASSIGNMENT_NAME);
		a.setNeedsGrading(1);
		
		//these two lines of code save the new course and assignment to the database 
		courseRepository.save(c);
		a = assignmentRepository.save(a);
		
		//helps create the webdriver object
		System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
		WebDriver driver = new ChromeDriver();
		//puts an Implicit wait for 10 seconds before throwing exception
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		
		try {
			//this line of code helps access the webpage that the frontend application develops
			driver.get(URL);
			Thread.sleep(SLEEP_DURATION);
			
			//this finds the button on the home page that takes the user to create a new assignment and clicks it
			driver.findElement(By.id("addingAssignment")).click();
			Thread.sleep(SLEEP_DURATION);
			
			//this finds the text field that deals with the assignment name
			//sendKeys is then used to fill in the text field with the TEST_ASSIGNMENT_NAME variable
			WebElement nameOfAssignment = driver.findElement(By.id("AssignmentName"));
			nameOfAssignment.sendKeys(TEST_ASSIGNMENT_NAME);
			
			//this finds the text field that deals with the assignment due date
			//sendKeys is then used to fill in the text field with the due date from the assignment that was created
			WebElement assignmentDueDate = driver.findElement(By.id("dueDateNum"));
			assignmentDueDate.sendKeys(a.getDueDate().toString());
			
			//this finds the text field that deals with which course the assignment goes to
			//sendKeys is then used to fill in the text field with the ID of the course that the assignment should go to
			WebElement assignmentCourseId = driver.findElement(By.id("courseNum"));
			assignmentCourseId.sendKeys("123456");
			
			//this finds the button that adds the new assignment to the database
			driver.findElement(By.id("addNewAssignment")).click();
			Thread.sleep(SLEEP_DURATION);
			
			//after the button was clicked the system is returned to the home page
			//these lines of code go through the data field, "assignmentName", in the data grid on the home page  
			List<WebElement> elements  = driver.findElements(By.xpath("//div[@data-field='assignmentName']/div"));
			boolean found = false;
			int count = 0;
			for (WebElement we : elements) {
				System.out.println(we.getText()); // for debug
				if (we.getText().equals(TEST_ASSIGNMENT_NAME)) {
					//count is being added by one to confirm that the test assignment was added twice to the database
					//once from the save() method on line 73
					//the second time from when the system adds a new assignment, lines 86-107
					count += 1;
					if(count == 2) {
						found=true;
						break;
					}					
				}
			}
			//confirms that both test assignments were found in the data grid
			assertEquals(true, found);
		}catch(Exception ex) {
			throw ex;
		}
		finally {
			//these lines of code delete the two assignments that were added to the database
			//one assignment is from line 73 when the new assignment is being saved
			//the second assignment is from lines 86-107, when the system adds a new assignment using the frontend application
			assignmentRepository.delete(assignmentRepository.findAssignmentById(a.getId()+1));
			assignmentRepository.delete(assignmentRepository.findAssignmentById(a.getId()));
			
			//this ends the webpage being used for the end to end test
			driver.quit();
		}
	}
}
