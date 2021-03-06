package gov.va.api.health.exemplar;

import gov.va.api.health.autoconfig.rest.PathRewriteFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PathRewriteConfig {
  @Bean
  FilterRegistrationBean<PathRewriteFilter> patientRegistrationFilter() {
    var registration = new FilterRegistrationBean<PathRewriteFilter>();
    PathRewriteFilter filter =
        PathRewriteFilter.builder()
            .removeLeadingPath("/exemplar/")
            .removeLeadingPath("/ex/")
            .build();
    registration.setFilter(filter);
    registration.addUrlPatterns(filter.removeLeadingPathsAsUrlPatterns());
    return registration;
  }
}
