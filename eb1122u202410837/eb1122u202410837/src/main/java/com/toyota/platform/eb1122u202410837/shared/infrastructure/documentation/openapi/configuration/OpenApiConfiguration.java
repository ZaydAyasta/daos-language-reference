package com.toyota.platform.eb1122u202410837.shared.infrastructure.documentation.openapi.configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

  @Value("${documentation.application.description}")
  private String applicationDescription;

  @Value("${documentation.application.version}")
  private String applicationVersion;

  @Bean
  public OpenAPI toyotaPlatformOpenApi() {
    return new OpenAPI()
        .info(new Info()
            .title("Toyota Platform API")
            .description(applicationDescription)
            .version(applicationVersion)
            .license(new License()
                .name("Apache 2.0")
                .url("https://springdoc.org")))
        .externalDocs(new ExternalDocumentation()
            .description("Toyota Platform Documentation")
            .url("https://github.com/upc-is-si729/daos-language-reference"));
  }
}
