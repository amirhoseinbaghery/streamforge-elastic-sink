package ir.datapie.connect.elasticsearch;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

public final class ErrorClassifier {
    public enum Category { RETRYABLE, PERMANENT }

    public Category classify(Integer status, Throwable error) {
        if (status != null && (status == 429 || status == 502 || status == 503 || status == 504)) return Category.RETRYABLE;
        if (error instanceof SocketTimeoutException || error instanceof ConnectException || error instanceof IOException) return Category.RETRYABLE;
        return Category.PERMANENT;
    }
}
