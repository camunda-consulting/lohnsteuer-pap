package com.camunda.consulting.lohnsteuer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeDeployment;

@SpringBootApplication
@EnableZeebeClient
@ZeebeDeployment(resources = "classpath*:*.bpmn")
public class LohnsteuerSpringBootApplication {

  public static void main(String[] args) {
    SpringApplication.run(LohnsteuerSpringBootApplication.class, args);
  }

}
