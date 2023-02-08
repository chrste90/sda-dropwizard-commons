package org.sdase.commons.server.dropwizard.metadata;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * The {@code MetadataContext} stores information about a long-running business processes that is
 * independent of specific APIs or business rules of a single service. A {@code MetaContext} is
 * built from the metadata fields of input APIs like HTTP or Kafka message headers. It is added
 * automatically to output APIs when using platform clients from the {@code JerseyClientBundle} or
 * the {@code KafkaMessageProducer}.
 *
 * <p>Services that claim to support {@code MetadataContext} must take care that it is also kept and
 * reloaded when the business process is interrupted and proceeded later. An interrupted business
 * process in these terms is for example asynchronous processing in a new {@link Thread} or
 * finishing the current step of the process by saving the current state to a database.
 *
 * <p>The metadata fields (e.g. header names) that are put into the {@code MetadataContext} must be
 * configured by {@linkplain System#getenv(String) environment variable} or {@linkplain
 * System#getProperty(String) system property} {@code METADATA_FIELDS} as comma separated list of
 * header names, e.g. {@code business-process-id,tenant-id}. Header names are treated
 * case-insensitive.
 *
 * <p>Technically, the {@code MetadataContext} is implemented as static {@link ThreadLocal}. When
 * code is executed in a new {@link Thread}, the context must be transferred. Helper methods are
 * available for {@linkplain #transferMetadataContext(Runnable) <code>Runnable</code>} and
 * {@linkplain #transferMetadataContext(Callable) <code>Callable</code>}
 */
// TODO not picked up from request headers and Kafka consumers as documented yet.
// TODO Not handled in platform client and KafkaMessageProducer as documented yet.
public interface MetadataContext {

  /**
   * @return the immutable metadata context of the current {@link Thread}, never {@code null}.
   */
  static MetadataContext current() {
    return MetadataContextUtil.current();
  }

  /**
   * @return the current metadata context, never {@code null}. Changes in the returned instance will
   *     not affect the {@link MetadataContext} of the current {@link Thread}.
   */
  static DetachedMetadataContext detachedCurrent() {
    return DetachedMetadataContext.of(MetadataContextUtil.current());
  }

  /**
   * Creates a new {@link MetadataContext} for the current {@link Thread}. Any existing {@link
   * MetadataContext} in the current {@link Thread} will be replaced.
   *
   * @param metadataContext the new {@link MetadataContext}, e.g. {@linkplain
   *     DetachedMetadataContext#toMetadataContext() derived} from a {@link
   *     DetachedMetadataContext}.
   */
  static void createContext(DetachedMetadataContext metadataContext) {
    MetadataContextHolder.set(metadataContext.toMetadataContext());
  }

  /**
   * Transfers the current metadata context to the runnable when executed in a new thread.
   *
   * @param runnable The runnable to wrap with the current metadata context.
   * @return The original runnable wrapped with code to transfer the metadata context when executed
   *     in a new thread.
   */
  static Runnable transferMetadataContext(Runnable runnable) {
    return MetadataContextUtil.transferMetadataContext(runnable);
  }

  /**
   * Transfers the current metadata context to the callable when executed in a new thread.
   *
   * @param callable The runnable to wrap with the current metadata context.
   * @return The original callable wrapped with code to transfer the metadata context when executed
   *     in a new thread.
   */
  static <V> Callable<V> transferMetadataContext(Callable<V> callable) {
    return MetadataContextUtil.transferMetadataContext(callable);
  }

  /**
   * @return all available keys in the metadata context.
   */
  Set<String> keys();

  /**
   * {@link #valuesByKeyFromEnvironment(String)} should be preferred to be independent of different
   * environments.
   *
   * @param key a key in the metadata context
   * @return the values stored in the metadata context by this key
   */
  List<String> valuesByKey(String key);

  /**
   * {@linkplain #valuesByKey(String) reads} the values of the metadata context from a key that is
   * configurable by {@linkplain System#getProperty(String) system properties} or {@linkplain
   * System#getenv(String) environment variables}. This is the preferred approach to get information
   * from the metadata context for a specific need.
   *
   * <p>The {@code environmentOrPropertyName} should refer to the business use of the context values
   * in a specific service. The actual key is dependent on the environment where the service is
   * used. Other services may need the same key in a different context and operators may have their
   * own assumptions about the naming.
   *
   * @param environmentOrPropertyName the name of a {@linkplain System#getProperty(String) system
   *     property} or {@linkplain System#getenv(String) environment variable} that defines the
   *     actual key for a specific need of the service.
   * @return the values stored in the metadata context by this key
   * @throws KeyConfigurationMissingException if the given {@code environmentOrPropertyName} does
   *     not resolve to a metadata context key
   */
  default List<String> valuesByKeyFromEnvironment(String environmentOrPropertyName)
      throws KeyConfigurationMissingException {
    return valuesByKey(MetadataContextUtil.keyFromConfiguration(environmentOrPropertyName));
  }
}