  /*
   * Copyright (c) 2022 - Bosch Security Systems - GmbH
   * Estrada Nacional 109/IC 1 | Zona Industrial de Ovar, Pardala | 3880-728 S. João | Portugal
   * (www.bosch.com)
   *
   * This software is the proprietary information of BOSCH.
   * Use is subject to license terms.
   */

  package com.bosch.bt.indego.redis;

  import static org.junit.jupiter.api.Assertions.assertEquals;
  import static org.junit.jupiter.api.Assertions.assertFalse;
  import static org.junit.jupiter.api.Assertions.assertTrue;

  import com.bosch.bt.indego.redis.jedis.JedisService;
  import com.bosch.bt.indego.redis.jedis.JedisServiceImpl;
  import java.util.HashMap;
  import java.util.Optional;
  import java.util.SortedMap;
  import java.util.TreeMap;
  import org.jboss.logging.Logger;
  import org.junit.jupiter.api.AfterAll;
  import org.junit.jupiter.api.BeforeAll;
  import org.junit.jupiter.api.Test;
  import org.testcontainers.containers.GenericContainer;
  import org.testcontainers.junit.jupiter.Container;
  import org.testcontainers.junit.jupiter.Testcontainers;
  import redis.clients.jedis.GeoCoordinate;

  @Testcontainers
  class JedisServiceTest {

    private static JedisService<String> client;
    public static final String KEY = "test";
    private Logger logger = Logger.getLogger(this.getClass());

    @Container
    static GenericContainer<?> redis =
        new GenericContainer<>("redis/redis-stack:6.2.6-v9").withExposedPorts(6379)
            .withEnv("REDIS_PASSWORD", KEY).withCommand(
                "sh",
                "-c",
                // Disable ipv6 & Make it listen on all interfaces, not just localhost
                "redis-server --appendonly yes --requirepass ${REDIS_PASSWORD}"
            );

    @BeforeAll
    static void setup() {
      client = new JedisServiceImpl<>(redis.getHost() + ":" + redis.getFirstMappedPort(), KEY, false);
    }

    @AfterAll
    static void stop() {
      redis.close();
    }

    @Test
    public void testAddOfGeoCoordinates_InputWithNonNullValues_ReturnTrue() {
      HashMap<String, String> map = new HashMap<>();
      map.put("test", "test");
      boolean result = client.geoAdd("test", new GeoCoordinate(1.0, 1.0), map);
      assertTrue(result);
    }

    @Test
    public void testSearchOfGeoCoordinates_InputWithNonNullValues_ReturnEmpty() {
      final SortedMap<String, String> search = new TreeMap<>();
      client.geoAdd("test", new GeoCoordinate(1.0, 1.0), search);
      SortedMap<String, String> result = client.geoSearch("test", new GeoCoordinate(1.0, 1.0), 5.0, String.class);
      assertEquals(result.size(), 0);
    }

    @Test
    void testSearchOfGeoCoordinates_InputWithNonNullValues_ReturnOneResult() {
      final SortedMap<String, String> search = new TreeMap<>();
      search.put("test3", "test3");
      boolean assertion = client.geoAdd("test", new GeoCoordinate(1.0, 1.0), search);
      SortedMap<String, String> result = client.geoSearch("test", new GeoCoordinate(1.0, 1.0), 5.0, String.class);
      assertEquals(result.size(), 1);
    }

    @Test
    void testCreationOfCache_InputWithNonNullValues_ReturnOk() {
      String result = client.add("test23", "value123");
      assertEquals(result, "OK");
    }

    @Test
    void testCreationOfCache_InputWithNullValues_ReturnNOk() {
      assertFalse(client.add(null, null, 120));
    }

    @Test
    void testRetrieveAnExistingKey_InputWithNonNullValues_ReturnValidKey() {
      // Given
      String key = "test333";
      String value = "randomValue";
      client.add(key, value);

      // When
      Optional<String> result = client.get("test333");

      // Then
      assertTrue(result.isPresent(), "Expected a non-empty result");
      assertEquals("randomValue", result.get(), "An error occur when trying to get a value from cache");
    }

    @Test
    void testRetrieveAnExistingKey_InputWithNullValues_ReturnEmptyString() {
      // Given
      String key = "test132";
      String value = "randomValue";
      client.add(key, value);

      // When
      Optional<String> result = client.get(null);

      // Then
      assertTrue(result.isEmpty(), "Expected a empty result");
    }

    @Test
    void testDeletionOfKey_InputWithNonNullValues_ReturnValidOutput() {
      // Given
      String key = "test999";
      String value = "randomValue";
      client.add(key, value);

      // When
      Long result = client.delete(key);

      // Then
      assertEquals(result, 1, "Expected to delete 1 key");
      assertFalse(client.get(key).isPresent(), "Key should be deleted");
    }

    @Test
    void testDeletionOfMultipleKey_InputWithNonNullValues_ReturnValidOutput() {
      // Given
      String key1 = "test998";
      String key2 = "test999";
      String value1 = "randomValue1";
      String value2 = "randomValue2";
      client.add(key1, value1);
      client.add(key2, value2);

      // When
      Long result = client.delete(key1, key2);

      // Then
      assertEquals(result, 2, "Expected to delete 2 keys");
      assertFalse(client.get(key1).isPresent(), "Key should be deleted");
      assertFalse(client.get(key2).isPresent(), "Key should be deleted");
    }

    @Test
    void testCheckIfKeyExist_InputWithNonNullValues_ReturnValidOutput() {
      // Given
      String key = "test89";
      String value = "randomValue";
      client.add(key, value);

      // When
      Optional<String> result = client.get("test89");

      // Then
      assertTrue(result.isPresent(), "Expected a non-empty result");
      assertEquals(value, result.get(), "Unexpected deleted value");
    }

    @Test
    void testCheckIfKeyExist_InputInvalidKey_ReturnValidOutput() {
      // Given
      String key = "test00";
      String value = "randomValue";
      client.add(key, value);

      // When
      Optional<String> result = client.get("test11");

      // Then
      assertFalse(result.isPresent(), "Expected a empty result");
    }

    @Test
    void testCheckIfKeyExist_InputNullValue_ReturnValidOutput() {
      // Given
      String key = "test4123";
      String value = "randomValue";
      client.add(key, value);

      // When
      Optional<String> result = client.get(null);

      // Then
      assertTrue(result.isEmpty(), "An error occur when trying to check if a key exist.");
    }

  }
