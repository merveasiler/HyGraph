package org.neo4j.examples.server.unmanaged;

import java.lang.management.ManagementFactory;

public class MeasureSourceConsumption {

	public String measureMemoryConsumption(Runtime runtime) {
		long total, free, used;
		int mb = 1024*1024;
		String response = "";

		total = runtime.totalMemory();
		free = runtime.freeMemory();
		used = total - free;
		response += "Total Memory: " + total / mb + "MB\n";
		response += "Memory Used: " + used / mb + "MB\n";
		response += "Memory Free: " + free / mb + "MB\n";
		response += "Percent Used: " + ((double)used/(double)total)*100 + "%\n";
		return response;
	}

	public int measureCPUConsumption(long cpuStartTime, long elapsedStartTime, int cpuCount) {
		long end = System.nanoTime();
		long totalAvailCPUTime = cpuCount * (end-elapsedStartTime);
		long totalUsedCPUTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime()-cpuStartTime;
		float per = ((float)totalUsedCPUTime*100)/(float)totalAvailCPUTime;
		return (int)per;
	}
}
