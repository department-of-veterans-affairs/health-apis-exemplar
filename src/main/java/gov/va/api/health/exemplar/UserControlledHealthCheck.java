package gov.va.api.health.exemplar;

import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class UserControlledHealthCheck implements HealthIndicator {

  static final AtomicBoolean HEALTHY = new AtomicBoolean(true);

  @Override
  public Health health() {
    if (HEALTHY.get()) {
      return Health.up().build();
    }
    return Health.down().build();
  }
}
