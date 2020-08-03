package gov.va.api.health.exemplar;

import java.net.Inet4Address;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(produces = {"application/json"})
@Slf4j
public class Controller {

  private static final int INSTANCE_ID = new Object().hashCode();
  private static final AtomicInteger REQUEST_COUNT = new AtomicInteger(0);

  @PostMapping({"goodbye", "/goodbye/{exit}"})
  void goodbye(@PathVariable(name = "exit", required = false) Integer exit) {
    log.warn("Goodbye {}", exit);
    System.exit(exit == null ? 0 : exit);
  }

  @GetMapping({"/hello", "/hello/{greeting}"})
  @SneakyThrows
  Greeting hello(
      @RequestHeader MultiValueMap<String, String> headers,
      @PathVariable(required = false, name = "greeting") String greeting) {
    return Greeting.builder()
        .instance(INSTANCE_ID)
        .requestCount(REQUEST_COUNT.incrementAndGet())
        .time(Instant.now())
        .hostname(Inet4Address.getLocalHost().getHostName())
        .headers(headers)
        .greeting(greeting == null ? "hello" : greeting)
        .build();
  }

  @Value
  @Builder
  public static class Greeting {
    Instant time;
    String hostname;
    int instance;
    int requestCount;
    MultiValueMap<String, String> headers;
    String greeting;
  }
}
