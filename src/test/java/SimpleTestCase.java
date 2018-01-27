import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Daniel Meyer
 * @author Martin Schimak
 */
public class SimpleTestCase {
    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Test
    @Deployment(resources = {"Opperatives_BPMN_ProcessEngine.bpmn"})
    public void ruleUsageExample() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("EMPLOYEE_ID",  "danzer");
        variables.put("businessKey",  "123456");
        variables.put("EMAIL", "danzer.marcus@gmail.com");
        variables.put("CUSTOMER_NAME", "SAP");
        variables.put("CONTRACT_START_DATE","2013-01-23T13:42:42");
        variables.put("CONTRACT_END_DATE", "2013-02-23T13:42:42");
        variables.put("CAR_TYPE","Mittelklasse");
        // Given we create a new process instance
        ProcessInstance processInstance = runtimeService()
               // .startProcessInstanceByMessage("message1", "123",variables);

                .startProcessInstanceByKey("Process_MitarbeiterID","123456",
                        withVariables(
                                "EMPLOYEE_ID","danzer",
                                "EMAIL", "danzer.marcus@gmail.com",
                                "CONTRACT_START_DATE","2013-01-23T13:42:42",
                                "CONTRACT_END_DATE", "2013-02-23T13:42:42",
                                "CAR_TYPE","Mittelklasse"
                                ));

        // Then it should be active
        assertThat(processInstance).isActive();
        // And it should be the only instance
        assertThat(processInstanceQuery().count()).isEqualTo(1);
        // And there should exist just a single task within that process instance

        assertThat(task(processInstance)).isNotNull();
        // When we complete that task
        complete(task(processInstance));
        // Then the process instance should be ended
        //assertThat(processInstance).isEnded();
    }
}
