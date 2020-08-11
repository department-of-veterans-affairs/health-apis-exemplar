package gov.va.api.health.exemplar;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.exemplar.Controller.Greeting;
import java.net.Inet4Address;
import java.time.Duration;
import java.time.Instant;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;

class ControllerTest {
  @Test
  void busyConsumesThread() {
    var before = Instant.now();
    controller().busy(1);
    var after = Instant.now();
    long timeSpent = Duration.between(before, after).toSeconds();
    assertThat(timeSpent).isBetween(1L, 2L);
  }

  private Controller controller() {
    return new Controller();
  }

  @Test
  void healSetPoisonedToFalse() {
    PoisonHealthCheck.POISONED.set(true);
    controller().heal();
    assertThat(PoisonHealthCheck.POISONED.get()).isFalse();
  }

  @Test
  void helloDefaultResponseWhenNotPoisonedIs200() {
    PoisonHealthCheck.POISONED.set(false);
    var hello = controller().hello(null, null);
    assertThat(hello.getStatusCodeValue()).isEqualTo(200);
  }

  @Test
  void helloDefaultResponseWhenPoisonedIs419() {
    PoisonHealthCheck.POISONED.set(true);
    var hello = controller().hello(null, null);
    assertThat(hello.getStatusCodeValue()).isEqualTo(418);
  }

  @Test
  @SneakyThrows
  void helloReponseContainsInstanceInformation() {
    var start = Instant.now();
    var hello = controller().hello(null, null);
    assertThat(hello.getBody().instance()).isEqualTo(Controller.INSTANCE_ID);
    assertThat(hello.getBody().hostname()).isEqualTo(Inet4Address.getLocalHost().getHostName());
    var timeSpent = Duration.between(start, hello.getBody().time()).toSeconds();
    assertThat(timeSpent).isBetween(0L, 1L);
  }

  @Test
  void helloResponseIsHonored() {
    PoisonHealthCheck.POISONED.set(true);
    var hello = controller().hello(null, 123);
    assertThat(hello.getStatusCodeValue()).isEqualTo(123);
  }

  @Test
  void helloResponseTracksRequestCount() {
    Controller.REQUEST_COUNT.set(0);
    controller().hello(null, null);
    controller().hello(null, null);
    var hello = controller().hello(null, null);
    assertThat(hello.getBody().requestCount()).isEqualTo(3);
  }

  @Test
  void helloResponseTracksWastedSpace() {
    Controller.WASTED_SPACE.clear();
    Controller.WASTED_SPACE.add(new double[1]);
    Controller.WASTED_SPACE.add(new double[1]);
    Controller.WASTED_SPACE.add(new double[1]);
    var hello = controller().hello(null, null);
    assertThat(hello.getBody().wastedSpace()).isEqualTo(3);
  }

  @Test
  void memoryConsumeAndFree() {
    Controller.WASTED_SPACE.clear();
    controller().memoryConsume(2);
    controller().memoryConsume(null);
    assertThat(Controller.WASTED_SPACE.size()).isEqualTo(3);
    controller().memoryFree();
    assertThat(Controller.WASTED_SPACE).isEmpty();
  }

  @Test
  void poisonSetPoisonedToTrue() {
    PoisonHealthCheck.POISONED.set(false);
    controller().poison();
    assertThat(PoisonHealthCheck.POISONED.get()).isTrue();
  }

  @Test
  @SneakyThrows
  void roundTripGreeting() {
    var headers = new LinkedMultiValueMap();
    headers.add("h1", "one");
    var before =
        Greeting.builder()
            .time(Instant.now())
            .hostname("localhost")
            .instance("1")
            .requestCount(2)
            .headers(headers)
            .status(3)
            .poisoned(true)
            .wastedSpace(4)
            .build();
    ObjectMapper mapper = JacksonConfig.createMapper();
    var json = mapper.writeValueAsString(before);
    var after = mapper.readValue(json, Greeting.class);
    assertThat(after).isEqualTo(before);
  }
}
