package com.databasepreservation.common.server.jobs;

import com.databasepreservation.common.server.jobs.spring.JobController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@SpringBootApplication
public class JobTest {
    public static void main(String[] args) {
      SpringApplication.run(JobTest.class, args);
      JobController jobController = new JobController();
      jobController.run();
    }
}
