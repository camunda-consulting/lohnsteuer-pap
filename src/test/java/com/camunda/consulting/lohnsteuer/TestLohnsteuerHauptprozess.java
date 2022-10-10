package com.camunda.consulting.lohnsteuer;


import org.camunda.community.process_test_coverage.junit5.platform8.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.spring.test.ZeebeSpringTest;


import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static io.camunda.zeebe.spring.test.ZeebeTestThreadSupport.*;
import static io.camunda.zeebe.process.test.assertions.BpmnAssert.assertThat;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ZeebeSpringTest
@ExtendWith(ProcessEngineCoverageExtension.class)
public class TestLohnsteuerHauptprozess {
  @Autowired
  ZeebeClient client;
  
  @Autowired
  ZeebeTestEngine zeebeTestEngine;
  
  @Test
  public void testLohnsteuerEinfach() {
    Map<String, Object> variables = withVariables(
        "KRV", 0,
        "KVZ", 1.30,
        "PVS", 1,
        "PVZ", 1,
        "LZZ", 1, 
        "AF", 0, 
        "RE4", 2500, 
        "VBEZ", 200, 
        "LZZFREIB", 300, 
        "LZZHINZU", 100,
        "VJAHR", 2006,
        "VBEZM", 10000,
        "VBEZS", 15000,
        "ZMVB", 12,
        "TAB1", new Double[] {0.0, 0.4, 0.384, 0.368},
        "TAB2", new Integer[] {0, 3000, 2880, 2760},
        "TAB3", new Integer[] {0, 900, 864, 828},
        "ALTER1", 1,
        "AJAHR", 2009,
        "TAB4", new Double[] {0.0, 0.4, 0.384, 0.368, 0.352, 0.336},
        "TAB5", new Integer[] {0, 1900, 1824, 1748, 1672, 1596},
        "STKL", 2,
        "PKV", 0,
        "ZKF", 1);
    
    ProcessInstanceEvent processInstance = client.newCreateInstanceCommand().bpmnProcessId("Lohnsteuer-2022").latestVersion()
        .variables(variables).send().join();
    
    waitForProcessInstanceCompleted(processInstance, Duration.ofSeconds(10));
    
    assertThat(processInstance)
        .isCompleted()
        .hasVariableWithValue("ZRE4J", 25)
        .hasVariableWithValue("ZVBEZJ", 2)
        .hasVariableWithValue("JLFREIB", 3)
        .hasVariableWithValue("JLHINZU", 1)
        .hasVariableWithValue("F", 1);
  }

  private Map<String, Object> withVariables(String key, Object value, Object... keyAndValuePair) {
    Map<String, Object> map = new HashMap<>();
    map.put(key, value);
    for (int i = 0; i < keyAndValuePair.length / 2; i++) {
      map.put((String) keyAndValuePair[i*2], keyAndValuePair[i*2+1]);
    }
    return map;
  }
  
  @Test
  public void testWithVariablesPositive() {
    assertThat(withVariables("key1", 1, "key2", "value2")).containsEntry("key1", 1).containsEntry("key2", "value2");
  }
}
