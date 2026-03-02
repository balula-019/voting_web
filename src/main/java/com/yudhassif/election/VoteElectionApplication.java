package com.yudhassif.election;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class VoteElectionApplication {

          @Value("${properties.hibernate.jdbc.time_zone:UTC}")
////        @Value("${app.timezone:UTC}")
        private String applicationTimeZone;

	public static void main(String[] args) {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        TimeZone uy = TimeZone.getDefault();
        System.out.print("===============Timezone===: " + uy);
		SpringApplication.run(VoteElectionApplication.class, args);
	}
    @PostConstruct
        public void executeAfterMain() {
            TimeZone.setDefault(TimeZone.getTimeZone(applicationTimeZone));
        }
//    public void init() {
//        // Setting the timezone to Dar es Salaam (EAT - UTC+3)
//        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.systemDefault()));
//
//        System.out.println("==============================================");
//        System.out.println("Server Local Time: " + new Date());
//        System.out.println("Current Timezone: " + TimeZone.getDefault().getID());
//        System.out.println("==============================================");
//    }
}
//```


 // todo to test that election happen automatically then every student can generate voterId?
// in order to use @Scheduled annotation make sure you annotate in the service or component annotation
// then the method should not contain any parameter