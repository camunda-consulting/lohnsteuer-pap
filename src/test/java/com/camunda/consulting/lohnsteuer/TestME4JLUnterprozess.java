package com.camunda.consulting.lohnsteuer;

import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.spring.test.ZeebeSpringTest;

import static io.camunda.zeebe.spring.test.ZeebeTestThreadSupport.*;
import static io.camunda.zeebe.process.test.assertions.BpmnAssert.assertThat;

@SpringBootTest
@ZeebeSpringTest
public class TestME4JLUnterprozess {
  
  @Autowired
  ZeebeClient client;
  
  @Autowired
  ZeebeTestEngine zeebeTestEngine;
  
  @ParameterizedTest
  @CsvSource({
    "1, 0, 2500, 200, 300, 100, 25, 2, 3, 1, 1",
    "2, 0, 2500, 200, 300, 100, 300, 24, 36, 12, 1"
  })
  public void testJahreslohn(int lzz, int af, int re4, int vbez, int lzzfreib, int lzzhinzu, int zre4j, int zvbezj, int jlfreib, int jlhinzu, int f) {
    Map<String, Object> variables = Map.of(
        "LZZ", lzz, 
        "AF", af, 
        "RE4", re4, 
        "VBEZ", vbez, 
        "LZZFREIB", lzzfreib, 
        "LZZHINZU", lzzhinzu);
    ProcessInstanceEvent processInstance = client.newCreateInstanceCommand().bpmnProcessId("MRE4JL-Unterprozess").latestVersion()
        .variables(variables).send().join();
    
    waitForProcessInstanceCompleted(processInstance);
    
    assertThat(processInstance)
        .isCompleted()
        .hasVariableWithValue("ZRE4J", zre4j)
        .hasVariableWithValue("ZVBEZJ", zvbezj)
        .hasVariableWithValue("JLFREIB", jlfreib)
        .hasVariableWithValue("JLHINZU", jlhinzu)
        .hasVariableWithValue("F", f);
  }

}
