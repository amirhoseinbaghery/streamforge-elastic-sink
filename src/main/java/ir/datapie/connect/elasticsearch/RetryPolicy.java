package ir.datapie.connect.elasticsearch;

public final class RetryPolicy {
    private final int maxAttempts;
    private final long initialBackoff;
    private final long maxBackoff;
    private final long totalTimeout;

    public RetryPolicy(DataPieElasticsearchSinkConfig config) {
        this.maxAttempts = config.integer(DataPieElasticsearchSinkConfig.RETRY_MAX_ATTEMPTS);
        this.initialBackoff = config.longValue(DataPieElasticsearchSinkConfig.RETRY_INITIAL_BACKOFF_MS);
        this.maxBackoff = config.longValue(DataPieElasticsearchSinkConfig.RETRY_MAX_BACKOFF_MS);
        this.totalTimeout = config.longValue(DataPieElasticsearchSinkConfig.RETRY_TOTAL_TIMEOUT_MS);
    }

    public boolean canRetry(int attempt, long startedAt) {
        return attempt < maxAttempts && System.currentTimeMillis() - startedAt < totalTimeout;
    }

    public void backoff(int attempt) throws InterruptedException {
        long multiplier = 1L << Math.min(attempt, 20);
        long delay = Math.min(maxBackoff, initialBackoff * multiplier);
        Thread.sleep(delay);
    }
}
