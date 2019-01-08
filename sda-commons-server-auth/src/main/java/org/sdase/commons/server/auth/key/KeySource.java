package org.sdase.commons.server.auth.key;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The source for loading keys.
 */
public interface KeySource {

   static final Logger LOGGER = LoggerFactory.getLogger(KeySource.class);

   /**
    * Loads the keys provided by this source.
    * @return All keys that this source provides.
    * @throws KeyLoadFailedException if loading of keys failed. If a source intentionally does not provide keys, an
    *                                empty list should be returned instead of throwing an exception.
    */
   List<LoadedPublicKey> loadKeysFromSource() throws KeyLoadFailedException; // NOSONAR declaring RuntimeException for documentation

   /**
    * Loads the keys provided by this source again. Implementations may override the default method
    * @return All keys that this source provides or
    *          <ul>
    *             <li>an empty {@link Optional} if the source does not support reloading or</li>
    *             <li>an empty {@link Optional} if reloading failed</li>
    *          </ul>
    *          <p>
    *
    *          </p>
    *          Implementations may not support reloading if the source knows that the keys will never change. Callers
    *          should keep the previously loaded keys, when receiving an empty {@link Optional} here.
    */
   default Optional<List<LoadedPublicKey>> reloadKeysFromSource() {
      try {
         return Optional.of(loadKeysFromSource());
      } catch (KeyLoadFailedException e) {
         LOGGER.error("An error occured while loading the public keys", e);
         return Optional.empty();
      }
   }

}
