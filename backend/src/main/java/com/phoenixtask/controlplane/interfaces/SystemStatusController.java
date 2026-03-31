package com.phoenixtask.controlplane.interfaces;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
public class SystemStatusController {

  @GetMapping("/status")
  public Map<String, String> status() {
    return Map.of(
        "application", "PhoenixTask®",
        "status", "UP"
    );
  }
}
