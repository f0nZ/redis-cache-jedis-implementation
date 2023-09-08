package com.bosch.bt.indego.redis.jedis;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.Config;

/**
 * CDI bean class that produces instances of JedisService with dynamically configured class types.
 */
@ApplicationScoped
public class JedisProducer {

  @Produces
  public <T> JedisService<T> initialize(final Config config) {
    final String host = config.getValue("quarkus.redis.hosts", String.class);
    final String key = config.getValue("quarkus.redis.password", String.class);
    final Boolean ssl = Boolean.parseBoolean(config.getValue("quarkus.redis.ssl", String.class));
    return new JedisServiceImpl<>(host, key, ssl);
  }

}
