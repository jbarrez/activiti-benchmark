package be.jorambarrez.activiti.benchmark.profiling;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;


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

/**
 * @author Joram Barrez
 */
public class ProfilingLogParser {
  
  protected static final DateFormat DATE_FORMATTER = new SimpleDateFormat("dd-MM-yyy-hh-mm-ss");

  public void execute() throws Exception {
    
    Map<String, List<Long>> executionTimes = new HashMap<String, List<Long>>();
    
    File input = new File("profiling.log");
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setValidating(false);
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

    // See http://marcels-javanotes.blogspot.com/2005/11/parsing-xml-file-without-having-access.html
    EntityResolver resolver = new EntityResolver() {
        public InputSource resolveEntity (String publicId, String systemId) {
            String empty = "";
            ByteArrayInputStream bais = new ByteArrayInputStream(empty.getBytes());
            System.out.println("resolveEntity:" + publicId + "|" + systemId);
            return new InputSource(bais);
        }
    };

    documentBuilder.setEntityResolver(resolver);
    Document doc = documentBuilder.parse(input);
    
    NodeList messages = doc.getElementsByTagName("message");
    System.out.println("Found " + messages.getLength() + " entries");
    
    for (int i=0; i<messages.getLength(); i++) {
      Node message = messages.item(i);
      String text = message.getTextContent();

      String[] content = text.split(":");
      String command = content[0];
      Long time = Long.valueOf(content[1]);
      
      if (!executionTimes.containsKey(command)) {
        executionTimes.put(command, new ArrayList<Long>());
      }
      executionTimes.get(command).add(time);
    }
    
    System.out.println("All messages parsed. Generating aggregations.");
    BufferedWriter writer = new BufferedWriter(new FileWriter(new File("profile_report_" + DATE_FORMATTER.format(new Date()) + ".txt")));
    
    for (String command : executionTimes.keySet()) {
      long totalExecutionTime = 0;
      for (Long executionTime : executionTimes.get(command)) {
        totalExecutionTime += executionTime;
      }
      long averageTime = totalExecutionTime/executionTimes.get(command).size();
      writer.append(command + ": nr execs=" + executionTimes.get(command).size() + ". Total exec time=" + totalExecutionTime + " ms. Average=" + averageTime);
      writer.newLine();
    }
    
    writer.close();
  }

}
