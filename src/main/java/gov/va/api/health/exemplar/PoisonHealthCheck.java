package gov.va.api.health.exemplar;

import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class PoisonHealthCheck implements HealthIndicator {

  static final AtomicBoolean POISONED = new AtomicBoolean(false);

  @Override
  public Health health() {
    if (POISONED.get()) {
      return Health.down().build();
    }
    return Health.up().build();
  }
}
