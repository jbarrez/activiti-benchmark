package be.jorambarrez.activiti.benchmark.execution;

import java.util.List;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

/**
 * {@link Runnable} that starts a process and completes all tasks in it,
 * until the process is finished.
 *
 * @author Frederik Heremans
 * @author Joram Barrez, mostly removing Fred's stuff.
 */
public class ExecuteProcessRunnable implements Runnable {

    private String processInstanceKey;
    private ProcessEngine processEngine;
    private long duration;
    
    public ExecuteProcessRunnable(String processInstanceKey, ProcessEngine processEngine) {
        this.processInstanceKey = processInstanceKey;
        this.processEngine = processEngine;
    }

    public void run() {
        long processStartTime = System.currentTimeMillis();
        ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByKey(processInstanceKey);
        
        duration = System.currentTimeMillis() - processStartTime; 
        
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
    }
    
    public long getDuration() {
    	return duration;
    }
}
