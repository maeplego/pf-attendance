package com.pf.attendance.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class OidcUserinfoClient {
  public record UserInfo(String sub, String orgId) {}

  private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
  private final ObjectMapper json = new ObjectMapper();

  public UserInfo resolve(String internalBase, String bearerToken) {
    if (internalBase == null || internalBase.isBlank() || bearerToken == null || bearerToken.isBlank()) {
      return null;
    }
    try {
      HttpRequest req =
          HttpRequest.newBuilder(URI.create(internalBase.replaceAll("/$", "") + "/userinfo"))
              .timeout(Duration.ofSeconds(5))
              .header("Authorization", "Bearer " + bearerToken)
              .GET()
              .build();
      HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
      if (res.statusCode() != 200) {
        return null;
      }
      JsonNode node = json.readTree(res.body());
      JsonNode sub = node.get("sub");
      if (sub == null || sub.asText().isBlank()) {
        return null;
      }
      String orgId = null;
      JsonNode org = node.get("org_id");
      if (org != null && !org.asText().isBlank()) {
        orgId = org.asText().trim();
      }
      return new UserInfo(sub.asText().trim(), orgId);
    } catch (IOException ex) {
      return null;
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      return null;
    }
  }
}
