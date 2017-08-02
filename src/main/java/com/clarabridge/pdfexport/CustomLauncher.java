package com.clarabridge.pdfexport;

import io.webfolder.cdp.Launcher;
import io.webfolder.cdp.session.SessionFactory;

import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Created by Pavel_Dzunovich on 8/2/2017.
 */
public class CustomLauncher extends Launcher {

	private List<String> customChromePaths = emptyList();

	public CustomLauncher(int port) {
		super(new SessionFactory(port));
	}

	@Override
	protected List<String> getChromeWinPaths() {
		List<String> result = new LinkedList<>(super.getChromeWinPaths());
		result.addAll(0, customChromePaths);

		return result;
	}

	public CustomLauncher setCustomChromePaths(List<String> customChromePaths) {
		this.customChromePaths = customChromePaths;
		return this;
	}

}
