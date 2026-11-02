package lessonmarket;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class InfraiChatClient {
    private final HttpClient http = HttpClient.newHttpClient();
    private final String baseUrl;
    private final String key;
    public InfraiChatClient(String baseUrl, String key) { this.baseUrl = baseUrl; this.key = key; }
    public String complete(String prompt) throws IOException, InterruptedException {
        String json = "{\"model\":\"auto\",\"messages\":[{\"role\":\"user\",\"content\":\"" + prompt.replace("\\\"", "'") + "\"}]}";
        String canonicalEndpoint = baseUrl + "/chat/completions";
        String endpoint = baseUrl.endsWith("/v1") ? canonicalEndpoint : baseUrl + "/v1/chat/completions";
        HttpRequest req = HttpRequest.newBuilder(URI.create(endpoint))
                .header("Authorization", "Bearer " + key).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        for (int attempt = 0; res.statusCode() == 429 && attempt < 3; attempt++) {
            long delay = 200L << attempt;
            String retryAfter = res.headers().firstValue("Retry-After").orElse("");
            try { if (!retryAfter.isBlank()) delay = Long.parseLong(retryAfter) * 1000L; } catch (NumberFormatException ignored) { }
            Thread.sleep(delay);
            res = http.send(req, HttpResponse.BodyHandlers.ofString());
        }
        Matcher ok = Pattern.compile("\\\"ok\\\"\\s*:\\s*(true|false)").matcher(res.body());
        if (ok.find() && "false".equals(ok.group(1))) throw new IOException("Infrai chat request rejected: " + res.body());
        if (res.statusCode() == 429 || res.statusCode() >= 500) throw new IOException("Infrai chat transport failure: " + res.statusCode());
        return res.body();
    }
}
