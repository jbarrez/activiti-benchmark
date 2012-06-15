/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package be.jorambarrez.activiti.benchmark.profiling;


import java.io.IOException;
import java.lang.reflect.Field;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.engine.impl.util.ClassNameUtil;


/**
 * A simple profiling interceptor, written just for the fun of it.
 * 
 * Add the following to your Spring config:
 * 
 * <property name="customPreCommandInterceptorsTxRequired">
 *    <list>
 *      <bean class="org.activiti.explorer.experimental.ProfilingInterceptor" />
 *    </list>
 * </property>
 * 
 * @author Joram Barrez
 */
public class ProfilingInterceptor extends CommandInterceptor {
  
  protected static final Logger LOGGER = Logger.getLogger(ProfilingInterceptor.class.getName());
  
  public ProfilingInterceptor() {
    try {
      LOGGER.setLevel(Level.INFO);
      FileHandler fileHandler = new FileHandler("profiling.log");
      LOGGER.addHandler(fileHandler);
    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public <T> T execute(Command<T> command) {
    long start = System.currentTimeMillis();
    T result = next.execute(command);
    long end = System.currentTimeMillis();
    LOGGER.info(ClassNameUtil.getClassNameWithoutPackage(command) 
            + ":" + (end-start)
            + ":" + readMemberFields(command));
    return result;
  }
  
  protected String readMemberFields(Command command) {
    StringBuilder strb = new StringBuilder();
    Field[] fields = command.getClass().getDeclaredFields();
    for (Field field : fields) {
      field.setAccessible(true);
      try {
        strb.append(field.getName());
        strb.append("=");
        strb.append(field.get(command) + ", ");
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
    
    if (strb.length() > 0) {
      strb.delete(strb.length()-2, strb.length());
    }
    
    return strb.toString();
  }

}
