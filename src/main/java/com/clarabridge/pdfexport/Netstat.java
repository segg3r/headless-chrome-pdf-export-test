package com.clarabridge.pdfexport;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static java.lang.System.lineSeparator;

/**
 * Created by Pavel_Dzunovich on 8/8/2017.
 */
public class Netstat {

	private List<Connection> connections;

	public static Netstat getNetstat() throws Exception {
		return fromConsoleOutput(WindowsProcessHelper.executeWithOutput("netstat", "-aon"));
	}

	private static Netstat fromConsoleOutput(String output) {
		List<Connection> connections = Arrays.stream(output.split(lineSeparator()))
				.map(Connection::fromOutputLine)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());

		return new Netstat(connections);
	}

	private Netstat(List<Connection> connections) {
		this.connections = connections;
	}

	public List<Connection> getConnections() {
		return connections;
	}

	public Netstat setConnections(List<Connection> connections) {
		this.connections = connections;
		return this;
	}

	public static class Connection {

		private Protocol protocol;
		private Url localAddress;
		private Url foreignAddress;
		private State state;
		private int pid;

		public static Optional<Connection> fromOutputLine(String line) {
			try {
				List<String> items = Arrays.asList(line.split("( )+"));

				if (items.isEmpty()) return Optional.empty();
				if (items.get(0).isEmpty()) items = items.subList(1, items.size());

				return Optional.of(new Connection()
						.setProtocol(Protocol.fromString(items.get(0)))
						.setLocalAddress(Url.parse(items.get(1)))
						.setForeignAddress(Url.parse(items.get(2)))
						.setState(State.fromString(items.get(3)))
						.setPid(Integer.parseInt(items.get(4))));
			} catch (Exception e) {
				return Optional.empty();
			}
		}

		public Protocol getProtocol() {
			return protocol;
		}

		public Connection setProtocol(Protocol protocol) {
			this.protocol = protocol;
			return this;
		}

		public Url getLocalAddress() {
			return localAddress;
		}

		public Connection setLocalAddress(Url localAddress) {
			this.localAddress = localAddress;
			return this;
		}

		public Url getForeignAddress() {
			return foreignAddress;
		}

		public Connection setForeignAddress(Url foreignAddress) {
			this.foreignAddress = foreignAddress;
			return this;
		}

		public State getState() {
			return state;
		}

		public Connection setState(State state) {
			this.state = state;
			return this;
		}

		public int getPid() {
			return pid;
		}

		public Connection setPid(int pid) {
			this.pid = pid;
			return this;
		}
	}

	public static class Url {

		private String host;
		private int port;

		public static Url parse(String url) {
			try {
				String[] items = url.split(":");
				return new Url(items[0], parseInt(items[1]));
			} catch (Exception e) {
				throw new IllegalArgumentException("Could not parse " + url + " as url.");
			}
		}

		public Url(String host, int port) {
			this.host = host;
			this.port = port;
		}

		public String getHost() {
			return host;
		}

		public Url setHost(String host) {
			this.host = host;
			return this;
		}

		public int getPort() {
			return port;
		}

		public Url setPort(int port) {
			this.port = port;
			return this;
		}

	}

	public enum Protocol {
		TCP, UDP, UNKNOWN;

		public static Protocol fromString(String string) {
			try {
				return Protocol.valueOf(string);
			} catch (Exception e) {
				return Protocol.UNKNOWN;
			}
		}
	}

	public enum State {
		LISTENING, ESTABLISHED, TIME_WAIT, UNKNOWN;

		public static State fromString(String string) {
			try {
				return State.valueOf(string);
			} catch (Exception e) {
				return State.UNKNOWN;
			}
		}
	}

}
