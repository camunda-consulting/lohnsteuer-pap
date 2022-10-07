package com.camunda.consulting.lohnsteuer;


import org.camunda.community.process_test_coverage.junit5.platform8.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.spring.test.ZeebeSpringTest;

import static io.camunda.zeebe.spring.test.ZeebeTestThreadSupport.*;
import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import static io.camunda.zeebe.process.test.assertions.BpmnAssert.assertThat;

@SpringBootTest
@ZeebeSpringTest
@ExtendWith(ProcessEngineCoverageExtension.class)
public class TestLohnsteuerHauptprozess {
  @Autowired
  ZeebeClient client;
  
  @Autowired
  ZeebeTestEngine zeebeTestEngine;
  
  @ParameterizedTest
  @CsvSource({
    "0, 1.30, 1, 1, 1, 0, 2500, 200, 300, 100, 25, 2, 3, 1, 1" /*,
    "2, 0, 2500, 200, 300, 100, 300, 24, 36, 12, 1"*/
  })
  public void testLohnsteuer(int krv, double kvz, int pvs, int pvz, int lzz, int af, int re4, int vbez, int lzzfreib, int lzzhinzu, int zre4j, int zvbezj, int jlfreib, int jlhinzu, int f) {
    Map<String, Object> variables = withVariables(
        "KRV", krv,
        "KVZ", kvz,
        "PVS", pvs,
        "PVZ", pvz,
        "LZZ", lzz, 
        "AF", af, 
        "RE4", re4, 
        "VBEZ", vbez, 
        "LZZFREIB", lzzfreib, 
        "LZZHINZU", lzzhinzu,
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
        "TAB5", new Integer[] {0, 1900, 1824, 1748, 1672, 1596});
    
    ProcessInstanceEvent processInstance = client.newCreateInstanceCommand().bpmnProcessId("Lohnsteuer-2022").latestVersion()
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
