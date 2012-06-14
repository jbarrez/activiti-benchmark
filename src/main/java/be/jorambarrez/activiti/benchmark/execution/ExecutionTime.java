package be.jorambarrez.activiti.benchmark.execution;
/**
 * Object that keeps hold of total execution-time.
 * 
 * @author Frederik Heremans
 */
public class ExecutionTime {

    private long executionTime;
    
    public ExecutionTime() {
        executionTime = 0;
    }
    
    public long getExecutionTime() {
        return this.executionTime;
    }
    
    public void addTime(long timeToAdd)
    {
        synchronized (this)
        {
            executionTime += timeToAdd;
        }
    }
}
