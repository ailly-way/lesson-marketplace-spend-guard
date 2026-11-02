package lessonmarket;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class InfraiAccountClient {
    private final HttpClient http = HttpClient.newHttpClient();
    private final String baseUrl;
    private final String key;

    public InfraiAccountClient(String baseUrl, String key) { this.baseUrl = baseUrl; this.key = key; }

    public String setBudget(double hardCapUsd, String period) throws IOException, InterruptedException {
        return request("PUT", "/v1/account/budget/set", "{\"hard_cap_usd\":" + hardCapUsd + ",\"period\":\"" + period + "\"}");
    }

    public String usageTimeseries() throws IOException, InterruptedException { return request("GET", "/v1/account/usage/timeseries", null); }

    public String createTemporaryKey() throws IOException, InterruptedException {
        return request("POST", "/v1/account/keys/create", "{\"name\":\"lesson-demo-temporary\",\"idempotency_key\":\"lesson-demo-1\"}");
    }

    private String request(String method, String path, String body) throws IOException, InterruptedException {
        String endpoint = baseUrl.endsWith("/v1") && path.startsWith("/v1")
                ? baseUrl + path.substring(3) : baseUrl + path;
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(endpoint))
                .header("Authorization", "Bearer " + key).header("Content-Type", "application/json");
        b.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        HttpResponse<String> response = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
        for (int attempt = 0; response.statusCode() == 429 && attempt < 3; attempt++) {
            long delay = 200L << attempt;
            String retryAfter = response.headers().firstValue("Retry-After").orElse("");
            try { if (!retryAfter.isBlank()) delay = Long.parseLong(retryAfter) * 1000L; } catch (NumberFormatException ignored) { }
            Thread.sleep(delay);
            response = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
        }
        String envelope = response.body();
        Matcher ok = Pattern.compile("\\\"ok\\\"\\s*:\\s*(true|false)").matcher(envelope);
        if (ok.find() && "false".equals(ok.group(1))) throw new IOException("Infrai request rejected: " + envelope);
        if (response.statusCode() == 429) throw new IOException("Infrai request rate limited: " + envelope);
        if (response.statusCode() >= 500) throw new IOException("Infrai transport failure: " + response.statusCode());
        return envelope;
    }
}
