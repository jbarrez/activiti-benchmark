package be.jorambarrez.activiti.benchmark.profiling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		Map<String, List<Long>> executionTimes = new HashMap<String, List<Long>>(); // Maps commands to the list of timings for that command

		File input = new File("profiling.log");
		BufferedReader fileReader = new BufferedReader(new FileReader(new File("profiling.log")));
		
		String line = fileReader.readLine();
		while (line != null) {
			String[] content = line.split(":");
			String command = content[0];
			Long time = Long.valueOf(content[1]);
			
			if (!executionTimes.containsKey(command)) {
				executionTimes.put(command, new ArrayList<Long>());
			}
			executionTimes.get(command).add(time);
			
			line = fileReader.readLine();
		}
		fileReader.close();

		System.out.println("All profiling logs read. Generating aggregations.");

		
		BufferedWriter writer = new BufferedWriter(new FileWriter(
				new File("profile_report_" + DATE_FORMATTER.format(new Date()) + ".txt")));

		for (String command : executionTimes.keySet()) {
			long totalExecutionTime = 0;
			for (Long executionTime : executionTimes.get(command)) {
				totalExecutionTime += executionTime;
			}
			long averageTime = totalExecutionTime / executionTimes.get(command).size();
			writer.append(command + ": nr execs="
					+ executionTimes.get(command).size() + ". Total exec time="
					+ totalExecutionTime + " ms. Average=" + averageTime);
			writer.newLine();
		}

		writer.close();
	}

}
