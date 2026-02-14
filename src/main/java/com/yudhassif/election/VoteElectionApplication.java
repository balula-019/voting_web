package com.yudhassif.election;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
//@EnableScheduling
public class VoteElectionApplication {

	public static void main(String[] args) {
		SpringApplication.run(VoteElectionApplication.class, args);
	}

}
// in order to use @Scheduled annotation make sure you annotate in the service or component annotation
// then the method should not contain any parameter