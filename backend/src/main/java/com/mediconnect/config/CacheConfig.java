package com.mediconnect.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration class for setting up caching in the application.
 * Enables Spring's caching abstraction and configures Caffeine as the cache provider.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Creates and configures a Caffeine cache specification.
     * <p>
     * This bean defines the default caching behavior including initial capacity,
     * maximum size, expiration policy, and statistics recording.
     *
     * @return a configured Caffeine builder instance
     */
    @Bean
    public Caffeine<Object, Object> caffeineSpec() {
        return Caffeine.newBuilder()
                .initialCapacity(100)                               // Sets the initial hash table capacity
                .maximumSize(10_000)                                // Limits the cache to 10,000 entries
                .expireAfterWrite(10, TimeUnit.MINUTES)    // Entries expire 10 minutes after being written
                .recordStats();                                     // Enables collection of cache statistics
    }

    /**
     * Configures the Spring CacheManager using Caffeine.
     * <p>
     * This bean registers specific cache names (doctors, specializations, availability)
     * and applies the Caffeine specification defined above to all of them.
     *
     * @param caffeine the Caffeine specification bean to apply
     * @return a configured CaffeineCacheManager instance
     */
    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("doctors", "specializations", "availability");
        cacheManager.setCaffeine(caffeine);                         // Applies the common Caffeine configuration to all caches
        return cacheManager;
    }
}