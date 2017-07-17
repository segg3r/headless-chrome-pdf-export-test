package com.clarabridge.pdfexport;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import io.webfolder.cdp.Launcher;
import io.webfolder.cdp.session.Session;
import io.webfolder.cdp.session.SessionFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import static java.awt.Desktop.getDesktop;
import static java.awt.Desktop.isDesktopSupported;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.write;
import static java.util.Arrays.asList;

public class RunnerPDF {

	private static final List<String> HEADLESS_CHROME_ARGUMENTS = asList("--headless", "--disable-gpu");

	private static final int ANGULAR_JAVASCRIPT_START_TIMEOUT = 5000;
	private static final int RESOLVE_ALL_REQUESTS_TIMEOUT = 10000;
	private static final int REPORT_RENDER_TIMEOUT = 5000;

	private static final boolean NO_LANDSCAPE = false;
	private static final boolean NO_HEADER_FOOTER = false;
	private static final boolean PRINT_BACKGROUND = true;
	private static final double NO_SCALE = 1.;
	private static final double NO_MARGIN = 0.;
	private static final String ALL_PAGES = null;

	private static final double CHROME_DPI = 96.;
	private static final double MAGIC_SCALE_ASSUMPTION = .75;
	private static final double NOT_REMOVABLE_MARGINS = 2.;

	private static final int HORIZONTAL_MARGIN_SIZE = 14;
	private static final int VERTICAL_MARGIN_SIZE = 56;
	private static final float NO_MARGIN_FLOAT = 0.f;
	private static final int FIRST_PAGE = 1;

	public static void main(String[] args) throws IOException, DocumentException {
		if (args.length != 3) throw new RuntimeException("Wrong arguments array. Expected: "
				+ "[ url : String, width : int, height : int ]");

		String url = args[0];
		int width = parseInt(args[1]);
		int height = parseInt(args[2]);

		Path preRenderPath = createTempFile("pre-render-pdf", ".pdf");
		Path postRenderPath = createTempFile("result-pdf", ".pdf");

		generatePDF(preRenderPath, url, width, height);
		postRenderPDF(preRenderPath, postRenderPath);

		if (isDesktopSupported()) {
			getDesktop().open(preRenderPath.toFile());
			getDesktop().open(postRenderPath.toFile());
		}
	}

	private static void generatePDF(Path path, String url, int pixelWidth, int pixelHeight) throws IOException {
		executeInHeadlessChrome((session) -> {
			try {
				ChromeRequestPool requestPool = new ChromeRequestPool(session);

				session.navigate(url);
				session.waitDocumentReady();

				session.wait(ANGULAR_JAVASCRIPT_START_TIMEOUT);
				session.waitUntil((s) -> requestPool.isEmpty(), RESOLVE_ALL_REQUESTS_TIMEOUT);
				session.wait(REPORT_RENDER_TIMEOUT);

				byte[] preRender = session
						.getCommand()
						.getPage()
						.printToPDF(NO_LANDSCAPE, NO_HEADER_FOOTER, PRINT_BACKGROUND, NO_SCALE,
								getPreRenderWidth(pixelWidth),
								getPreRenderHeight(pixelHeight),
								NO_MARGIN, NO_MARGIN, NO_MARGIN, NO_MARGIN,
								ALL_PAGES);

				write(path, preRender);
			} catch (Exception e) {
				throw new RuntimeException("Could not execute pdf export in chrome session",e);
			}
		});
	}

	private static void executeInHeadlessChrome(Consumer<Session> consumer) {
		Launcher launcher = new Launcher();
		try (SessionFactory factory = launcher.launch(HEADLESS_CHROME_ARGUMENTS)) {
			String context = factory.createBrowserContext();
			try (Session session = factory.create(context)) {
				consumer.accept(session);
			}
		}
	}

	private static double getPreRenderWidth(int pixelWidth) {
		return pixelWidth / CHROME_DPI * MAGIC_SCALE_ASSUMPTION;
	}

	private static double getPreRenderHeight(int pixelHeight) {
		return pixelHeight / CHROME_DPI * MAGIC_SCALE_ASSUMPTION + NOT_REMOVABLE_MARGINS;
	}

	private static void postRenderPDF(Path preRenderPath, Path postRenderPath)
			throws IOException, DocumentException {
		PdfReader preRenderPdfReader = new PdfReader(preRenderPath.toFile().getAbsolutePath());
		Rectangle preRenderPageSize = preRenderPdfReader.getPageSize(FIRST_PAGE);

		Rectangle postRenderPageSize = new Rectangle(
				preRenderPageSize.getWidth() - HORIZONTAL_MARGIN_SIZE * 2,
				preRenderPageSize.getHeight() - VERTICAL_MARGIN_SIZE * 2);

		Document postRenderDocument = new Document(postRenderPageSize,
				NO_MARGIN_FLOAT, NO_MARGIN_FLOAT, NO_MARGIN_FLOAT, NO_MARGIN_FLOAT);

		PdfWriter postRenderPdfWriter = PdfWriter.getInstance(
				postRenderDocument, new FileOutputStream(postRenderPath.toFile().getAbsoluteFile()));
		postRenderDocument.open();

		PdfContentByte content = postRenderPdfWriter.getDirectContent();
		PdfImportedPage page = postRenderPdfWriter.getImportedPage(preRenderPdfReader, FIRST_PAGE);
		content.addTemplate(page, 1., 0., 0., 1., HORIZONTAL_MARGIN_SIZE, VERTICAL_MARGIN_SIZE);

		postRenderDocument.close();
		preRenderPdfReader.close();
	}

}