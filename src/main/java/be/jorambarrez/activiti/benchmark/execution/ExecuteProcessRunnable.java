package be.jorambarrez.activiti.benchmark.execution;

import java.util.List;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

/**
 * {@link Runnable} that starts a process and completes all tasks in it,
 * untill the process is finished.
 *
 * @author Frederik Heremans
 */
public class ExecuteProcessRunnable implements Runnable {

    private String processInstanceKey;
    private ProcessEngine processEngine;
    private ExecutionTime executionTime;
    
    public ExecuteProcessRunnable(String processInstanceKey, ProcessEngine processEngine, ExecutionTime executionTime) {
        this.processInstanceKey = processInstanceKey;
        this.processEngine = processEngine;
        this.executionTime = executionTime;
    }

    public void run() {
        long processStartTime = System.currentTimeMillis();
        ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByKey(processInstanceKey);
        
        long duration = System.currentTimeMillis() - processStartTime; 
        
        if(!processInstance.isEnded())
        {
           List<Task> currentTasks = processEngine.getTaskService()
                       .createTaskQuery().processInstanceId(processInstance.getId())
                       .list();
           while(currentTasks != null && currentTasks.size() > 0)  {
               long start = System.currentTimeMillis();
               
               // Complete all open tasks
               for(Task task : currentTasks)
               {
                   processEngine.getTaskService().complete(task.getId());
               }
               // Only measure the actual time it took to complete the tasks, not including querying for them
               duration += (System.currentTimeMillis() - start);
               
               // Check for possible new tasks
               currentTasks = processEngine.getTaskService()
                    .createTaskQuery().processInstanceId(processInstance.getId())
                    .list();
           }
        }
        
        // Record the result
        executionTime.addTime(duration);        
    }
}
