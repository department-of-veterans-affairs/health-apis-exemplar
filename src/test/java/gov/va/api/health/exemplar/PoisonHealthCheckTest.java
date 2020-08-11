package gov.va.api.health.exemplar;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

class PoisonHealthCheckTest {

  @Test
  void healthReturnsDownWhenPoisoned() {
    PoisonHealthCheck.POISONED.set(true);
    assertThat(new PoisonHealthCheck().health().getStatus()).isEqualTo(Status.DOWN);
  }

  @Test
  void healthReturnsUpWhenNotPoisoned() {
    PoisonHealthCheck.POISONED.set(false);
    assertThat(new PoisonHealthCheck().health().getStatus()).isEqualTo(Status.UP);
  }
}
