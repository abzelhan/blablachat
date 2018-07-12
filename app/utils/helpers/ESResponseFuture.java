package utils.helpers;

import org.elasticsearch.action.ActionListener;

import java.util.concurrent.*;
import java.util.function.Function;

/**
 * Created by abzalsahitov@gmail.com  on 3/25/18.
 */
public class ESResponseFuture<T, R> implements Future<T>, ActionListener<T> {

    private volatile boolean cancelled = false;
    private final CountDownLatch countDownLatch;
    private volatile T result = null;
    private volatile Function<T, R> function = null;
    private volatile Exception e = null;
    public static final String SERVER_ERROR = "500";

    public ESResponseFuture() {
        countDownLatch = new CountDownLatch(1);
    }

    public ESResponseFuture(Function<T, R> function) {
        countDownLatch = new CountDownLatch(1);
        this.function = function;
    }


    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        if (isDone()) {
            return false;
        } else {
            countDownLatch.countDown();
            cancelled = true;
            return !isDone();
        }
    }

    public Exception getException() {
        return e;
    }

    public Function<T, R> getFunction() {
        return function;
    }

    public ESResponseFuture<T, R> setFunction(Function<T, R> function) {
        this.function = function;
        return this;
    }

    //nullable
    public R getFetched() throws Exception {
        countDownLatch.await();

        if (result != null && function != null) {
            return function.apply(result);
        } else {
            throw this.e;
        }
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        countDownLatch.await();
        return result;
    }

    @Override
    public T get(final long timeout, final TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        countDownLatch.await(timeout, unit);
        return result;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public boolean isDone() {
        return countDownLatch.getCount() == 0;
    }

    @Override
    public void onResponse(T result) {
        this.result = result;
        countDownLatch.countDown();
    }

    @Override
    public void onFailure(Exception e) {
        result = null;
        this.e = e;
        countDownLatch.countDown();
    }
}
