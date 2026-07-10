package org.eclipse.tradista.core.marketdata.fxmacrodata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class FXMacroDataClient {

	private final String apiKey;
	private final String baseUrl;

	public FXMacroDataClient(String apiKey) {
		this(apiKey, "https://api.fxmacrodata.com/v1");
	}

	public FXMacroDataClient(String apiKey, String baseUrl) {
		this.apiKey = apiKey == null ? "" : apiKey;
		this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
	}

	public String dataCatalogue(String currency) throws IOException { return get("/data_catalogue/" + norm(currency)); }
	public String announcements(String currency, String indicator) throws IOException { return get("/announcements/" + norm(currency) + "/" + indicator); }
	public String calendar(String currency) throws IOException { return get("/calendar/" + norm(currency)); }
	public String predictions(String currency, String indicator) throws IOException { return get("/predictions/" + norm(currency) + "/" + indicator); }
	public String forex(String base, String quote) throws IOException { return get("/forex/" + norm(base) + "/" + norm(quote)); }
	public String cot(String currency) throws IOException { return get("/cot/" + norm(currency)); }
	public String commoditiesLatest() throws IOException { return get("/commodities/latest"); }
	public String commodity(String indicator) throws IOException { return get("/commodities/" + indicator); }
	public String curves(String currency) throws IOException { return get("/curves/" + norm(currency)); }
	public String curveProxies(String currency) throws IOException { return get("/curve_proxies/" + norm(currency)); }
	public String forwardCurves(String currency) throws IOException { return get("/forward_curves/" + norm(currency)); }
	public String marketSessions() throws IOException { return get("/market_sessions"); }
	public String riskSentiment() throws IOException { return get("/risk_sentiment"); }
	public String news(String currency) throws IOException { return get("/news/" + norm(currency)); }
	public String pressReleases(String currency) throws IOException { return get("/press-releases/" + norm(currency)); }
	public String centralBankers(String currency) throws IOException { return get("/central_bankers/" + norm(currency)); }

	private String get(String path) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) URI.create(buildUrl(path)).toURL().openConnection();
		connection.setRequestMethod("GET");
		connection.setConnectTimeout(10000);
		connection.setReadTimeout(20000);
		int status = connection.getResponseCode();
		if (status < 200 || status >= 300) throw new IOException("FXMacroData returned HTTP " + status);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
			StringBuilder body = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) body.append(line);
			return body.toString();
		}
	}

	String buildUrl(String path) throws IOException {
		if (apiKey.isEmpty()) return baseUrl + path;
		return baseUrl + path + "?api_key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8.name());
	}

	private static String norm(String value) {
		return value.trim().toLowerCase(Locale.ROOT);
	}
}
