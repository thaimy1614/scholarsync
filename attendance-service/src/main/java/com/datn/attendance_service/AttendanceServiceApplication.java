package com.datn.attendance_service;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@EnableKafka
public class AttendanceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AttendanceServiceApplication.class, args);
	}

	@Bean
	NewTopic sendAttendanceReminder() {
		return new NewTopic("sendAttendanceReminderToParent", 3, (short) 1);
	}

	@Bean
	NewTopic sendWarningReminder() {
		return new NewTopic("sendWarningReminderToParent", 3, (short) 1);
	}

}
