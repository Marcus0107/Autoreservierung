
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.task.Task;

import javax.xml.ws.Service;


public class MessageFlow implements JavaDelegate{



    private final Logger LOGGER = Logger.getAnonymousLogger();

    public void execute (DelegateExecution execution) throws Exception {

        Map<String,Object> processVariablesToInsert = new TreeMap<String,Object>();
        String sendMessage;
        Mail mail = new Mail();

        switch (execution.getCurrentActivityId()){
            case "UserTask_Mitarbeiter_AntragAusfuellen":
                sendMessage = "Message1";
                sendMessageWithAllVariables(execution, sendMessage, E_EventType.START_EVENT);
                break;
            case "Task_Engine_BudgetPruefen":
                sendMessage = "Message_Engine_BudgetPruefen";
//                ToDo: check if form is valid
                processVariablesToInsert.put("FORM_ISVALID", true);
                setProcessVariables(execution,processVariablesToInsert);
                sendMessageWithAllVariables(execution, sendMessage, E_EventType.START_EVENT);
                break;
            case "ServiceTask_Manager_AntrasstatusAendern":
                sendMessage = "Message_Manager_AntragsstatusAendern";
                processVariablesToInsert.put("REQUEST_STATE", "PROCESSING");
                setProcessVariables(execution,processVariablesToInsert);
                sendMessageWithAllVariables(execution, sendMessage, E_EventType.INTERMEDIATE_THROW_EVENT);
                break;
            case "UserTask_Manager_BudgetPruefen":
                completeTaskWithAllVariables(execution,"Task_Engine_BudgetPruefen", "Process_EngineID");
                break;
            case "SendTask_Engine_Abgelehnt":
                sendMessage = "Message_Engine_AblehnungSenden";
                processVariablesToInsert.put("REQUEST_STATE", "DECLINED");
                setProcessVariables(execution,processVariablesToInsert);
                sendMessageWithAllVariables(execution, sendMessage, E_EventType.INTERMEDIATE_THROW_EVENT);
                break;
            case "UserTask_Engine_AutoBereitstellen":
                sendMessage = "Message_Engine_AutoBereitstellen";
                sendMessageWithAllVariables(execution, sendMessage, E_EventType.INTERMEDIATE_THROW_EVENT);
                break;
            case "EndEvent_Msg_Fuhrpark":
                completeTaskWithAllVariables(execution,"UserTask_Engine_AutoBereitstellen", "Process_EngineID");
                break;
            case "SendTask_Engine_Bestaetigt":
                sendMessage = "Message_Engine_BestaetigungSenden";
                processVariablesToInsert.put("REQUEST_STATE", "APPROVED");
                sendMessageWithAllVariables(execution, sendMessage, E_EventType.INTERMEDIATE_THROW_EVENT);
                break;
            case "IntermediateThrowEvent_DauerGeschaeftsreise":
                processVariablesToInsert.put("TRIP_TOREVIEW", true);
                setProcessVariables(execution,processVariablesToInsert);
                break;
            case "SendTask_Mitarbeiter_ReisekostenabrAnfordern":
                sendMessage = "Message_Mitarbeiter_Reisekostenabrechnung";
                sendMessageWithAllVariables(execution, sendMessage, E_EventType.INTERMEDIATE_THROW_EVENT);
                break;
            case "ServiceTask_Reisekostenabrechnung":
                String email =execution.getVariable("EMAIL").toString();
                String start = execution.getVariable("CONTRACT_START_DATE").toString();
                String end = execution.getVariable("CONTRACT_END_DATE").toString();
                String customer = execution.getVariable("CUSTOMER_NAME").toString();
                String employeeId = execution.getVariable("EMPLOYEE_ID").toString();
                mail.sendMailTo(email,customer,start,end,employeeId);
                break;
            case "SendTask_SendTask_Engine_ReisekostenabrSenden":
                sendMessage = "Message_Engine_Reisekostenabrechnung";
                sendMessageWithAllVariables(execution, sendMessage, E_EventType.INTERMEDIATE_THROW_EVENT);
                break;
        }

        Map<String,Object> processVariables = execution.getVariables();
        processVariables.forEach((s,o) -> LOGGER.info("String: " + s +"\tObject: " + o + " \n")
        );





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

    /*Message methods*/
    private void sendMessageWithAllVariables(DelegateExecution execution, String sendMessage, E_EventType eventType){
        RuntimeService runtimeService = execution.getProcessEngineServices().getRuntimeService();

        switch (eventType){
            case START_EVENT:
                runtimeService.startProcessInstanceByMessage(sendMessage, execution.getProcessBusinessKey(),getProcessVariables(execution));
                break;
            case INTERMEDIATE_THROW_EVENT:
                runtimeService.correlateMessage(sendMessage, execution.getProcessBusinessKey(), getProcessVariables(execution));
                break;
        }
    }

    private void sendMessageWithSpecificVariables(DelegateExecution execution, String sendMessage, Map<String,Object> specificProcessVariablesToInsert, E_EventType eventType){
        RuntimeService runtimeService = execution.getProcessEngineServices().getRuntimeService();

        switch (eventType){
            case START_EVENT:
                runtimeService.startProcessInstanceByMessage(sendMessage,execution.getProcessBusinessKey(), specificProcessVariablesToInsert);
                break;
            case INTERMEDIATE_THROW_EVENT:
                runtimeService.correlateMessage(sendMessage, execution.getProcessBusinessKey(), specificProcessVariablesToInsert);
                break;
        }
    }

    private void sendMessageOnly(DelegateExecution execution, String sendMessage, E_EventType eventType){
        RuntimeService runtimeService = execution.getProcessEngineServices().getRuntimeService();

        runtimeService.startProcessInstanceByMessage(sendMessage,execution.getProcessBusinessKey());
    }


    /*Task methods*/
    private void completeTaskWithAllVariables(DelegateExecution execution, String taskID, String poolID){
        RuntimeService runtimeService = execution.getProcessEngineServices().getRuntimeService();
        TaskService taskService = execution.getProcessEngineServices().getTaskService();


/*        Task task = taskService.createTaskQuery().taskDefinitionKey(taskID).singleResult();
        LOGGER.info("Task ID:" + task +"\t Task Name: " + task.getName());*/


        Task task = taskService.createTaskQuery()
                .active()
                .caseInstanceBusinessKeyLike(execution.getProcessBusinessKey())
                .taskDefinitionKey(taskID)
                .singleResult();



        try{
            LOGGER.info("\n------------------------- " + "activeTasks" + " -------------------------");
            LOGGER.info("\nTask Query:" + "BusinessKey:" + execution.getProcessBusinessKey() + "\tTaskDefinitionKey:" + taskID);
            LOGGER.info("Task ID: " + task.getId() + "\tTask Name:" +task.getName() +"\tProcess Instance ID:" +task.getProcessInstanceId());



        }catch (NullPointerException ne){
            LOGGER.info("no task for given criteria found: NullpointerException");
        }

        try{
            LOGGER.info("\n------------------------- " + "activeTaskList" + " -------------------------");
            taskService.createTaskQuery()
                    .active()
                    .taskDefinitionKey(taskID)
                    .list()
                    .forEach((t) -> LOGGER.info("Task ID: " + "\tTask Name:" +t.getName() +"\tProcess Instance ID:" +t.getProcessInstanceId() + "\tProcess Definition ID:" + t.getProcessDefinitionId() + "\n"));
        }catch (NullPointerException ne) {
            LOGGER.info("no task for given criteria found: EmptyList");
        }


        for(Task t : taskService.createTaskQuery().active().taskDefinitionKey(taskID).list()){
//          ToDo: .caseInstanceBusinessKeyLike(execution.getProcessBusinessKey()) doesn't work
            taskService.complete(t.getId(), getProcessVariables(execution));
            break;
        }


    }

    private void completeTaskWithSpecificVariables(DelegateExecution execution, String taskID, Map<String,Object> specificProcessVariables){
        RuntimeService runtimeService = execution.getProcessEngineServices().getRuntimeService();
        TaskService taskService = execution.getProcessEngineServices().getTaskService();

        taskService.complete(taskID, specificProcessVariables);
    }

    private void completeTaskOnly(DelegateExecution execution, String taskID){
        TaskService taskService = execution.getProcessEngineServices().getTaskService();

        taskService.complete(taskID);
    }


    /*ProcessVariables methods*/
    private Map<String,Object> getProcessVariables(DelegateExecution execution){
        //      GET processVariables and LOG them
        Map<String,Object> processVariables = execution.getVariables();
        LOGGER.info("\n------------------------- " + execution.getCurrentActivityName() + " -------------------------");
        processVariables.forEach((s,o) -> LOGGER.info("String: " + s +"\tObject: " + o + " \n"));

        return processVariables;
    }

    private void setProcessVariables(DelegateExecution execution, Map<String,Object> processVariablesToInsert){
        //      Create runtime variables
        processVariablesToInsert.forEach((s,o) -> execution.setVariable(s, o));
    }





}