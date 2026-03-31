package com.phoenixtask.workspace.application;

import com.phoenixtask.shared.error.ValidationException;
import com.phoenixtask.workspace.integrations.GitProvider;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GitProviderValidationService {

  private final HttpClient httpClient;
  private final Duration timeout;

  public GitProviderValidationService(
      @Value("${phoenixtask.integrations.git.validation-timeout-ms:3000}") long timeoutMs
  ) {
    this.httpClient = HttpClient.newHttpClient();
    this.timeout = Duration.ofMillis(timeoutMs);
  }

  public void validateToken(GitProvider provider, String token) {
    if (provider == GitProvider.GITHUB) {
      validateGithub(token);
      return;
    }
    if (provider == GitProvider.GITLAB) {
      validateGitlab(token);
      return;
    }
    throw new ValidationException("provider: unsupported for validation");
  }

  private void validateGithub(String token) {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.github.com/user"))
        .timeout(timeout)
        .header("Authorization", "token " + token)
        .header("Accept", "application/vnd.github+json")
        .GET()
        .build();
    send(request, "GitHub");
  }

  private void validateGitlab(String token) {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("https://gitlab.com/api/v4/user"))
        .timeout(timeout)
        .header("PRIVATE-TOKEN", token)
        .GET()
        .build();
    send(request, "GitLab");
  }

  private void send(HttpRequest request, String provider) {
    try {
      HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
      int status = response.statusCode();
      if (status < 200 || status >= 300) {
        throw new ValidationException(provider + " token validation failed (status " + status + ")");
      }
    } catch (ValidationException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new ValidationException(provider + " token validation failed (network error)");
    }
  }
}
