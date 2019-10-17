package com.huanhong.decathlonstb.util;

import android.os.Handler;
import android.os.Message;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class MyCountDownTimer {

	private long mMillisInFuture;

	private long mCountdownInterval;

	private long currentTime;

	private boolean isStart;

	private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);

	private Future<String> future;

	public MyCountDownTimer(long millisInFuture, long countDownInterval) {
		mMillisInFuture = millisInFuture;
		mCountdownInterval = countDownInterval;
		initHandler();
	}

	public MyCountDownTimer() {
		this(0, 0);
	}

	public final void stop() {
		isStart = false;
		currentTime = 0;
		if (future != null && !future.isDone()) {
			future.cancel(true);
		}
	}

	/**
	 * Start the countdown.
	 */
	public synchronized final MyCountDownTimer reStart(long millisInFuture,
			long countDownInterval) {
		stop();
		mMillisInFuture = millisInFuture;
		mCountdownInterval = countDownInterval;
		return start();
	}

	/**
	 * Start the countdown.
	 */
	public synchronized final MyCountDownTimer start() {
		if (isStart) {
			return this;
		}
		isStart = true;
		if (mMillisInFuture <= 0) {
			isStart = false;
			onFinish();
			return this;
		}
		currentTime = mMillisInFuture;
		// fixedThreadPool.execute(new Runnable() {
		//
		// @Override
		// public void run() {
		// while (currentTime > 0) {
		// try {
		// Thread.sleep(mCountdownInterval);
		// currentTime -= mCountdownInterval;
		// mHandler.sendEmptyMessage(MSG);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// }
		// }
		// });
		Callable<String> callable = new Callable<String>() {

			@Override
			public String call() throws Exception {
				while (currentTime > 0) {
					try {
						Thread.sleep(mCountdownInterval);
						if (isStart) {
							currentTime -= mCountdownInterval;
							mHandler.sendEmptyMessage(MSG);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				return null;
			}
		};
		future = fixedThreadPool.submit(callable);
		return this;
	}

	/**
	 * Start the countdown.
	 */
	public synchronized final MyCountDownTimer resetStopTime() {
		if (mMillisInFuture <= 0) {
			return this;
		}
		currentTime = mMillisInFuture;
		return this;
	}

	/**
	 * Callback fired on regular interval.
	 * <p>
	 * The amount of time until finished.
	 */
	public void onTick(long currentTime) {

	}

	/**
	 * Callback fired when the time is up.
	 */
	public abstract void onFinish();

	private static final int MSG = 1;

	// handles counting down
	private Handler mHandler = null;

	private void initHandler() {
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				synchronized (MyCountDownTimer.this) {
					if (currentTime <= 0) {
						isStart = false;
						onFinish();
					} else {
						onTick(currentTime);
						isStart = true;
					}
				}
			}
		};
	}
}
