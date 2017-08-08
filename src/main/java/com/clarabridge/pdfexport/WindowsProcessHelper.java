package com.clarabridge.pdfexport;

import org.apache.commons.io.IOUtils;

import static java.lang.System.getProperty;
import static java.util.Locale.ENGLISH;

/**
 * Created by Pavel_Dzunovich on 8/8/2017.
 */
public class WindowsProcessHelper {

	public static void killProcess(int pid) throws Exception {
		executeWithOutput("taskkill", "/F", "/PID", String.valueOf(pid));
	}

	public static int getPidByListeningPort(int port) throws Exception {
		Netstat netstat = Netstat.getNetstat();

		return netstat.getConnections().stream()
				.filter(connection -> port == connection.getLocalAddress().getPort()
					&& Netstat.State.LISTENING == connection.getState())
				.map(Netstat.Connection::getPid)
				.findFirst()
				.orElse(-1);
	}

	public static String executeWithOutput(String... command) throws Exception {
		throwIfNotWindows();

		ProcessBuilder builder = new ProcessBuilder(command)
				.redirectErrorStream(true);

		Process process = builder.start();
		String output = IOUtils.toString(process.getInputStream(), "UTF-8");
		process.waitFor();

		return output;
	}

	private static void throwIfNotWindows() {
		if (!isWindows()) throw new IllegalStateException("Current OS is not Windows.");
	}

	private static boolean isWindows() {
		String os = getProperty("os.name")
				.toLowerCase(ENGLISH);
		return os.startsWith("windows");
	}

}
