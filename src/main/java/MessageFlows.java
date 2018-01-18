
import java.util.Map;
import java.util.logging.Logger;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;


public class MessageFlows implements JavaDelegate{



    private final Logger LOGGER = Logger.getAnonymousLogger();

    public void execute (DelegateExecution execution) throws Exception {

        RuntimeService runtimeService = execution.getProcessEngineServices().getRuntimeService();

        String sendMessage="";

        switch (execution.getCurrentActivityId()){
            case "SendTask_0rshnh8":
                sendMessage = "Message1";
                break;
            case "Task_12d1s5n":
                sendMessage = "Message_Engine_BudgetPruefen";
                break;
        }
        Map<String,Object> processVariables = execution.getVariables();

        processVariables.forEach((s,o) -> LOGGER.info("String: " + s +"\tObject: " + o + " \n")
        );


        runtimeService.startProcessInstanceByMessage(sendMessage,processVariables);


        LOGGER.info("\n\n  ... LoggerDelegate invoked by "
                + "processDefinitionId=" + execution.getProcessDefinitionId()
                + ", activityId=" + execution.getCurrentActivityId()
                + ", activityName='" + execution.getCurrentActivityName().replaceAll("\n", " ") + "'"
                + ", processInstanceId=" + execution.getProcessInstanceId()
                + ", businessKey=" + execution.getProcessBusinessKey()
                + ", executionId=" + execution.getId()
                + ", modelName=" + execution.getBpmnModelInstance().getModel().getModelName()
                + ", elementId" + execution.getBpmnModelElementInstance().getId()
                + " \n\n");

    }
}