package com.camunda.consulting.lohnsteuer.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;

@Component
public class NoopWorker implements JobHandler {
  
  private static final Logger LOG = LoggerFactory.getLogger(NoopWorker.class);

  @ZeebeWorker(type = "noop")
  public void handle(JobClient client, ActivatedJob job) throws Exception {
    LOG.info("Doing nothing for {}", job.getElementId());
    client.newCompleteCommand(job).send().join();
  }

}
