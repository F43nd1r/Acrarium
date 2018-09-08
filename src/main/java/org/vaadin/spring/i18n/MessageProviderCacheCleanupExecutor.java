package org.vaadin.spring.i18n;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import javax.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * This class will start up a thread that will invoke {@link CompositeMessageSource#clearMessageProviderCaches()} on
 * a regular interval if enabled (by default it is disabled). This feature is intended to be used during development,
 * together with a tool such as JRebel, to prevent time consuming application restarts while tweaking the translations.
 * <p>
 * To enable, specify the interval in seconds in the environment property
 * <code>{@value #ENV_PROP_MESSAGE_PROVIDER_CACHE_CLEANUP_INTERVAL_SECONDS}</code>
 * and disable the message format cache of the {@link CompositeMessageSource}.
 *
 * @author Petter Holmström (petter@vaadin.com)
 * @see CompositeMessageSource#setMessageFormatCacheEnabled(boolean)
 */
public class MessageProviderCacheCleanupExecutor {

    /**
     * An environment property specifying the interval of the cache cleanups. A value of 0 (the default) will
     * disable the cleanups completely.
     */
    public static final String ENV_PROP_MESSAGE_PROVIDER_CACHE_CLEANUP_INTERVAL_SECONDS = "vaadin4spring.i18n.message-provider-cache.cleanup-interval-seconds";
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProviderCacheCleanupExecutor.class);
    private ScheduledExecutorService executorService;
    private int cleanupInterval;
    private ScheduledFuture<?> cleanupJob;

    /**
     * Creates a new {@code MessageProviderCacheCleanupExecutor} and starts up the cleanup thread if cache cleanup
     * has been enabled.
     */
    public MessageProviderCacheCleanupExecutor(final Environment environment,
        final CompositeMessageSource compositeMessageSource) {
        cleanupInterval = environment.getProperty(ENV_PROP_MESSAGE_PROVIDER_CACHE_CLEANUP_INTERVAL_SECONDS,
            Integer.class, 0);
        if (cleanupInterval > 0) {
            if (compositeMessageSource.isMessageFormatCacheEnabled()) {
                LOGGER.warn(
                    "The message format cache is enabled so message provider cache cleanup will not have any effect, disabling");
            } else {
                LOGGER.info("Cleaning up the message provider caches every {} second(s)", cleanupInterval);
                executorService = Executors.newSingleThreadScheduledExecutor();
                cleanupJob = executorService.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        LOGGER.debug("Cleaning up message provider caches");
                        compositeMessageSource.clearMessageProviderCaches();
                    }
                }, cleanupInterval, cleanupInterval, TimeUnit.SECONDS);
            }
        } else {
            LOGGER.info("Message provider cache cleanup is disabled");
        }
    }

    /**
     * Shuts down the cleanup thread.
     */
    @PreDestroy
    public void destroy() {
        if (executorService != null) {
            LOGGER.info("Shutting down message provider cache cleanup thread");
            cleanupJob.cancel(true);
            executorService.shutdown();
        }
    }
}