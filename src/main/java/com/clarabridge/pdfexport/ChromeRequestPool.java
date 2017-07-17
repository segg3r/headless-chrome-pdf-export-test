package com.clarabridge.pdfexport;

import io.webfolder.cdp.event.network.RequestWillBeSent;
import io.webfolder.cdp.event.network.ResponseReceived;
import io.webfolder.cdp.session.Session;
import io.webfolder.cdp.type.network.Request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.webfolder.cdp.event.Events.NetworkRequestWillBeSent;
import static io.webfolder.cdp.event.Events.NetworkResponseReceived;
import static java.util.stream.Collectors.toList;

/**
 * Created by Pavel_Dzunovich on 7/17/2017.
 */
public class ChromeRequestPool {

	private Map<String, Request> requests = new HashMap<>();

	public ChromeRequestPool(Session session) {
		session.getCommand().getNetwork().enable();

		session.addEventListener((e, d) -> {
			if (NetworkRequestWillBeSent.equals(e)) {
				RequestWillBeSent rr = (RequestWillBeSent) d;
				requests.put(rr.getRequestId(), rr.getRequest());
			} else if (NetworkResponseReceived.equals(e)) {
				ResponseReceived rr = (ResponseReceived) d;
				requests.remove(rr.getRequestId());
			}
		});
	}

	public List<String> getRequestEndpoints() {
		return requests.values().stream()
				.map(Request::getUrl)
				.collect(toList());
	}

	public boolean isEmpty() {
		return requests.isEmpty();
	}

}
