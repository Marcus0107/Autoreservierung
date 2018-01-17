
import java.util.logging.Logger;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;




public class MessageFlows implements JavaDelegate{



    private final Logger LOGGER = Logger.getLogger(MessageFlows.class.getName());


    public void execute (DelegateExecution execution) throws Exception {

        RuntimeService runtimeService = execution.getProcessEngineServices().getRuntimeService();
        runtimeService.startProcessInstanceByMessage("Message1");


        LOGGER.info("\n\n  ... LoggerDelegate invoked by "
                + "processDefinitionId=" + execution.getProcessDefinitionId()
                + ", activtyId=" + execution.getCurrentActivityId()
                + ", activtyName='" + execution.getCurrentActivityName() + "'"
                + ", processInstanceId=" + execution.getProcessInstanceId()
                + ", businessKey=" + execution.getProcessBusinessKey()
                + ", executionId=" + execution.getId()
                + " \n\n");



//        runtimeService.startProcessInstanceByMessage("Message_Mitarbeiter_AntragAusfuellen");
    }


/*
    public void message_Mitarbeiter_AntragAusfuellen_execute(DelegateExecution execution) throws Exception {
            RuntimeService runtimeService = execution.getProcessEngineServices().getRuntimeService();
            runtimeService.startProcessInstanceByMessage("Message_Mitarbeiter_AntragAusfuellen");
    }
*/





}