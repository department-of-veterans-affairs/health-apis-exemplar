package gov.va.api.health.exemplar;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PathRewriteConfigTest {

  @Test
  void beanHandlesExemplar() {
    var filter = new PathRewriteConfig().patientRegistrationFilter().getFilter();
    assertThat(filter.removeLeadingPathsAsUrlPatterns())
        .containsExactlyInAnyOrder("/exemplar/*", "/ex/*");
  }
}
