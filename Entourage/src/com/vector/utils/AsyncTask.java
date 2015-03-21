/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vector.utils;

import android.annotation.TargetApi;
import android.os.Handler;
import android.os.Message;
import android.os.Process;

import java.util.ArrayDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import android.util.Log;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AsyncTask<Params, Progress, Result> {
	private static final String LOG_TAG = "AsyncTask";

	private static final int CORE_POOL_SIZE = 5;
	private static final int MAXIMUM_POOL_SIZE = 128;
	private static final int KEEP_ALIVE = 1;

	private static final ThreadFactory sThreadFactory = new ThreadFactory() {
		private final AtomicInteger mCount = new AtomicInteger(1);

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
		}
	};

	private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>(
			10);

	// Executor that can be used to execute tasks in parallel.
	public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
			CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS,
			sPoolWorkQueue, sThreadFactory,
			new ThreadPoolExecutor.DiscardOldestPolicy());

	// Executor that executes tasks one at a time in serial order. This
	// serialization is global to a particular process.
	public static final Executor SERIAL_EXECUTOR = ApiUtils.hasHoneycomb() ? new SerialExecutor()
			: Executors.newSingleThreadExecutor(sThreadFactory);

	public static final Executor DUAL_THREAD_EXECUTOR = Executors
			.newFixedThreadPool(2, sThreadFactory);

	private static final int MESSAGE_POST_RESULT = 0x1;
	private static final int MESSAGE_POST_PROGRESS = 0x2;

	private static final InternalHandler sHandler = new InternalHandler();

	private static volatile Executor sDefaultExecutor = SERIAL_EXECUTOR;
	private final WorkerRunnable<Params, Result> mWorker;
	private final FutureTask<Result> mFuture;

	private volatile Status mStatus = Status.PENDING;

	private final AtomicBoolean mCancelled = new AtomicBoolean();
	private final AtomicBoolean mTaskInvoked = new AtomicBoolean();

	@TargetApi(11)
	private static class SerialExecutor implements Executor {
		final ArrayDeque<Runnable> mTasks = new ArrayDeque<Runnable>();
		Runnable mActive;

		@Override
		public synchronized void execute(final Runnable r) {
			mTasks.offer(new Runnable() {
				@Override
				public void run() {
					try {
						r.run();
					} finally {
						scheduleNext();
					}
				}
			});
			if (mActive == null) {
				scheduleNext();
			}
		}

		protected synchronized void scheduleNext() {
			if ((mActive = mTasks.poll()) != null) {
				THREAD_POOL_EXECUTOR.execute(mActive);
			}
		}
	}

	// Indicates the current status of the task. Each status will be set only
	// once during the lifetime of a task.
	public enum Status {
		// Indicates that the task has not been executed yet.
		PENDING,
		// Indicates that the task is running
		RUNNING,
		// Indicates that task has finished.
		FINISHED,
	}

	// Used to force static handler to be created.
	public static void init() {
		sHandler.getLooper();
	}

	public static void setDefaultExecutor(Executor exec) {
		sDefaultExecutor = exec;
	}

	// Creates a new asynchronous task. This constructor must be invoked on the
	// UI thread.
	public AsyncTask() {
		mWorker = new WorkerRunnable<Params, Result>() {
			@Override
			public Result call() throws Exception {
				mTaskInvoked.set(true);

				Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
				// no inspection unchecked
				return postResult(doInBackground(mParams));
			}
		};

		mFuture = new FutureTask<Result>(mWorker) {
			@Override
			protected void done() {
				try {
					postResultIfNotInvoked(get());
				} catch (InterruptedException e) {
					Log.w(LOG_TAG, e);
				} catch (ExecutionException e) {
					throw new RuntimeException(
							"An error occured while executing doInBackground()",
							e.getCause());
				} catch (CancellationException e) {
					postResultIfNotInvoked(null);
				}
			}
		};
	}

	private void postResultIfNotInvoked(Result result) {
		final boolean wasTaskInvoked = mTaskInvoked.get();
		if (!wasTaskInvoked) {
			postResult(result);
		}
	}

	private Result postResult(Result result) {
		@SuppressWarnings("unchecked")
		Message message = sHandler.obtainMessage(MESSAGE_POST_RESULT,
				new AsyncTaskResult<Result>(this, result));
		message.sendToTarget();
		return result;
	}

	// Returns the current status of this task.
	public final Status getStatus() {
		return mStatus;
	}

	// Override this method to perform a computation on a background thread.
	protected abstract Result doInBackground(Params... params);

	// Runs on the UI thread before #doInBackground
	protected void onPreExecute() {
	}

	// Runs on the UI thread after #doInBackground
	protected void onPostExecute(Result result) {
	}

	// Runs on the UI thread after #publishProgress is invoked.
	protected void onProgressUpdate(Progress... values) {
	}

	// Runs on the UI thread after {@link #cancel(boolean)} is invoked and
	// #doInBackground has finished.
	protected void onCancelled(Result result) {
		onCancelled();
	}

	protected void onCancelled() {
	}

	// Returns <tt>true</tt> if this task was cancelled before it completed
	// normally
	public final boolean isCancelled() {
		return mCancelled.get();
	}

	// Attempts to cancel execution of this task.
	public final boolean cancel(boolean mayInterruptIfRunning) {
		mCancelled.set(true);
		return mFuture.cancel(mayInterruptIfRunning);
	}

	// Waits if necessary for the computation to complete, and then retrieves
	// its result.
	public final Result get() throws InterruptedException, ExecutionException {
		return mFuture.get();
	}

	// Waits if necessary for at most the given time for the computation to
	// complete, and then retrieves its result.
	// @param timeout - Time to wait before cancelling the operation.
	// @param unit- The time unit for the timeout.
	public final Result get(long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return mFuture.get(timeout, unit);
	}

	// Executes the task with the specified parameters.
	@SuppressWarnings("incomplete-switch")
	public final AsyncTask<Params, Progress, Result> executeOnExecutor(
			Executor exec, Params... params) {
		if (mStatus != Status.PENDING) {
			switch (mStatus) {
			case RUNNING:
				throw new IllegalStateException("Cannot execute task:"
						+ " the task is already running.");
			case FINISHED:
				throw new IllegalStateException("Cannot execute task:"
						+ " the task has already been executed "
						+ "(a task can be executed only once)");
			}
		}

		mStatus = Status.RUNNING;
		onPreExecute();
		mWorker.mParams = params;
		exec.execute(mFuture);
		return this;
	}

	public static void execute(Runnable runnable) {
		sDefaultExecutor.execute(runnable);
	}

	// This method can be invoked from #doInBackground to publish
	// updates on the UI thread while the background computation is still
	// running.
	protected final void publishProgress(Progress... values) {
		if (!isCancelled()) {
			sHandler.obtainMessage(MESSAGE_POST_PROGRESS,
					new AsyncTaskResult<Progress>(this, values)).sendToTarget();
		}
	}

	private void finish(Result result) {
		if (isCancelled()) {
			onCancelled(result);
		} else {
			onPostExecute(result);
		}
		mStatus = Status.FINISHED;
	}

	private static class InternalHandler extends Handler {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public void handleMessage(Message msg) {
			AsyncTaskResult result = (AsyncTaskResult) msg.obj;
			switch (msg.what) {
			case MESSAGE_POST_RESULT:
				// There is only one result
				result.mTask.finish(result.mData[0]);
				break;
			case MESSAGE_POST_PROGRESS:
				result.mTask.onProgressUpdate(result.mData);
				break;
			}
		}
	}

	private static abstract class WorkerRunnable<Params, Result> implements
			Callable<Result> {
		Params[] mParams;
	}

	@SuppressWarnings({ "rawtypes" })
	private static class AsyncTaskResult<Data> {
		final AsyncTask mTask;
		final Data[] mData;

		AsyncTaskResult(AsyncTask task, Data... data) {
			mTask = task;
			mData = data;
		}
	}
}