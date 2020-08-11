package gov.va.api.health.exemplar;

import java.net.Inet4Address;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(produces = {"application/json"})
@Slf4j
@Validated
public class Controller {

  static final String INSTANCE_ID = instanceId();
  static final AtomicInteger REQUEST_COUNT = new AtomicInteger(0);

  static final List<double[]> WASTED_SPACE = new CopyOnWriteArrayList<>();

  @SneakyThrows
  private static String instanceId() {
    SecureRandom random = new SecureRandom();

    byte[] address = Inet4Address.getLocalHost().getAddress();
    String id =
        String.format(
                "%2H%2H%2H%2H%2H",
                address[0], address[1], address[2], address[3], random.nextInt(1000))
            .replace(' ', '0');
    log.info("Instance {}", id);
    return id;
  }

  @PostMapping({"/busy", "/busy/{seconds}"})
  long busy(@PathVariable(name = "seconds", required = false) @Min(1) @Max(300) Integer seconds) {
    if (seconds == null) {
      seconds = 10;
    }
    log.info("Randomly adding numbers for the next {} seconds", seconds);
    var quitingTime = Instant.now().plusSeconds(seconds);
    var lastReport = Instant.ofEpochMilli(0);
    Random random = new SecureRandom();
    int operations = 0;
    long result = 0;
    while (Instant.now().isBefore(quitingTime)) {
      operations++;
      if (random.nextBoolean()) {
        result += random.nextInt();
      } else {
        result -= random.nextInt();
      }
      if (Duration.between(lastReport, Instant.now()).toSeconds() > 2) {
        log.info("After {} operations, the value is {}", operations, result);
        lastReport = Instant.now();
      }
    }
    log.info("Wow. After {} operations, the result is {}", operations, result);
    return result;
  }

  @PostMapping({"goodbye", "/goodbye/{exit}"})
  void goodbye(@PathVariable(name = "exit", required = false) Integer exit) {
    log.warn("Goodbye {}", exit);
    System.exit(exit == null ? 0 : exit);
  }

  @PostMapping({"/heal"})
  void heal() {
    PoisonHealthCheck.POISONED.set(false);
  }

  @GetMapping({"/hello", "/hello/{status}"})
  @SneakyThrows
  ResponseEntity<Greeting> hello(
      @RequestHeader MultiValueMap<String, String> headers,
      @PathVariable(required = false, name = "status") Integer status) {
    if (status == null) {
      status = PoisonHealthCheck.POISONED.get() ? 418 : 200;
    }
    var greeting =
        Greeting.builder()
            .instance(INSTANCE_ID)
            .requestCount(REQUEST_COUNT.incrementAndGet())
            .poisoned(PoisonHealthCheck.POISONED.get())
            .wastedSpace(WASTED_SPACE.size())
            .time(Instant.now())
            .hostname(Inet4Address.getLocalHost().getHostName())
            .headers(headers == null ? null : new LinkedMultiValueMap<>(headers))
            .status(status)
            .build();
    return ResponseEntity.status(status).body(greeting);
  }

  @PostMapping({"/memory/consume", "/memory/consume/{chunks}"})
  int memoryConsume(@PathVariable(value = "chunks", required = false) Integer chunks) {
    if (chunks == null) {
      chunks = 1;
    }
    for (int i = 0; i < chunks; i++) {
      memoryConsumeOneChunk();
    }
    return WASTED_SPACE.size();
  }

  private void memoryConsumeOneChunk() {
    Random random = new SecureRandom();
    final int doublesPerMB = (1024 * 1024) / (8 + 8);
    WASTED_SPACE.add(random.doubles(doublesPerMB).toArray());
  }

  @PostMapping({"/memory/free"})
  int memoryFree() {
    int items = WASTED_SPACE.size();
    WASTED_SPACE.clear();
    Runtime.getRuntime().gc();
    return items;
  }

  @PostMapping({"/poison"})
  void poison() {
    PoisonHealthCheck.POISONED.set(true);
  }

  @Value
  @Builder
  public static class Greeting {
    Instant time;
    String hostname;
    String instance;
    int requestCount;

    LinkedMultiValueMap<String, String> headers;

    int status;
    boolean poisoned;
    int wastedSpace;
  }
}
