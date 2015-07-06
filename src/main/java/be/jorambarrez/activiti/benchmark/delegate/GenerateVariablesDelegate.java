package be.jorambarrez.activiti.benchmark.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.el.Expression;

public class GenerateVariablesDelegate implements JavaDelegate {

    private Expression numberOfVariablesString;

    @Override
    public void execute(DelegateExecution delegateExecution) {
        int numberOfVariables = Integer.valueOf(numberOfVariablesString.getValue(delegateExecution).toString());
        for (int i=0; i<numberOfVariables; i++) {
            if (i%2 == 0) {
                delegateExecution.setVariable("var" + i, i); // integer
            } else {
                delegateExecution.setVariable("var" + i, i + ""); // string
            }
        }
    }

}
