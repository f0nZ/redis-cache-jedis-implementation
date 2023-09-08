/*
 * Copyright (c) 2023 - Bosch Security Systems - GmbH
 * Estrada Nacional 109/IC 1 | Zona Industrial de Ovar, Pardala | 3880-728 S. João | Portugal
 * (www.bosch.com)
 *
 * This software is the proprietary information of BOSCH.
 * Use is subject to license terms.
 */

package com.bosch.bt.indego.redis;

import java.util.Map;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;

public class RedisContainer {

  @Container
  static GenericContainer<?> redis =
      new GenericContainer<>("redis/redis-stack:6.2.6-v9").withExposedPorts(6379)
          .withEnv("REDIS_PASSWORD", "test").withCommand(
              "sh",
              "-c",
              // Disable ipv6 & Make it listen on all interfaces, not just localhost
              "redis-server --appendonly yes --requirepass ${REDIS_PASSWORD}"
          );

  public Map<String, String> start() {
    redis.start();

    return Map.of("quarkus.redis.hosts", redis.getHost() + ":" + redis.getFirstMappedPort(),
        "quarkus.redis.password", redis.getEnvMap().get("REDIS_PASSWORD"),
        "quarkus.redis.ssl", "false");
  }

  public void stop() {
    redis.close();
  }
}
