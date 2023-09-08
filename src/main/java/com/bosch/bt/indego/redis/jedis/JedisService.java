package com.bosch.bt.indego.redis.jedis;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.UUID;
import redis.clients.jedis.GeoCoordinate;

/**
 * The JedisService interface defines the contract for interacting with a Redis service. It provides various methods for
 * adding, deleting, and searching data in Redis.
 */
public interface JedisService<T> {

  /**
   * Adds a key-value pair to Redis.
   *
   * @param key   the key to add
   * @param value the value associated with the key
   */
  String add(final String key, final String value);

  /**
   * Adds a key-value pair to Redis with a specified expiration time.
   *
   * @param key                 the key to add
   * @param value               the value associated with the key
   * @param expirationInSeconds the expiration time in seconds for the key-value pair
   */
  boolean add(final String key, final String value, final int expirationInSeconds);

  /**
   * Retrieves the value associated with the given key from Redis.
   *
   * @param key the key to retrieve the value for
   * @return the value associated with the key, or null if the key does not exist
   */
  Optional<String> get(final String key);

  /**
   * Deletes one or more keys from Redis.
   *
   * @param keys the keys to delete
   * @return the number of keys deleted
   */
  Long delete(final String... keys);

  // ----------------- JSON -----------------
  boolean jadd(String key, T value);

  boolean jadd(String key, byte[] value);

  boolean jadd(String key, byte[] value, long expirationInSeconds);

  boolean jadd(UUID key, T value);

  boolean jadd(UUID key, byte[] value);

  T jget(final UUID key, final Class<T> clazz);


  /**
   * Adds a geo coordinate and a value to a sorted set in Redis.
   *
   * @param key           the key of the sorted set
   * @param geoCoordinate the geo coordinate to add
   * @param value         the value associated with the geo coordinate
   * @return true if the geo coordinate was added successfully, false otherwise
   */
  boolean geoAdd(String key, GeoCoordinate geoCoordinate, Map<String, T> value);

  /**
   * Adds a geo coordinate and a value to a sorted set in Redis.
   *
   * @param key           the key of the sorted set
   * @param geoCoordinate the geo coordinate to add
   * @param value         the value associated with the geo coordinate
   * @param ttl           the value for expiration time in seconds
   * @return true if the geo coordinate was added successfully, false otherwise
   */
  boolean geoAdd(final String key, final GeoCoordinate geoCoordinate, final T value, final Integer ttl);

  /**
   * Searches for values within a specified radius of a given geo coordinate in Redis.
   *
   * @param key           the key of the sorted set
   * @param geoCoordinate the center geo coordinate for the search
   * @param radius        the search radius
   * @param valueType     the class type of the values
   * @return a sorted map of key-value pairs found within the specified radius
   */
  SortedMap<String, T> geoSearch(String key, GeoCoordinate geoCoordinate, double radius, Class<T> valueType);

  /**
   * Searches for values within a specified radius of a given geo coordinate in Redis.
   *
   * @param key           the key of the sorted set
   * @param geoCoordinate the center geo coordinate for the search
   * @param radius        the search radius
   * @param valueType     the class type of the values
   * @param limit         the number of results to return
   * @return a sorted map of key-value pairs found within the specified radius
   */
  List<T> geoSearch(final String key, final GeoCoordinate geoCoordinate, final Double radius, final Class<T> valueType, Integer limit);

  /**
   * Searches for values within a specified radius of a given geo coordinate in Redis.
   *
   * @param key   the key of the sorted set
   * @param value the value associated with the geo coordinate
   * @return String with OK in case of success
   */
  String add(byte[] key, byte[] value);

  T get(byte[] key, Class<T> clazz) throws IOException;

}

