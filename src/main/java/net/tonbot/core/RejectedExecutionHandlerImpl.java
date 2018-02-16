package net.tonbot.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RejectedExecutionHandlerImpl implements RejectedExecutionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(RejectedExecutionHandlerImpl.class);

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		BlockingQueue<Runnable> queue = executor.getQueue();

		LOG.error(
				"Unable to handle event because thread pool is full.\n"
						+ "Core pool size: {}, Maximum: {}, Current: {}, Active: {}, Queue size: {}/{}",
				executor.getCorePoolSize(), executor.getMaximumPoolSize(), executor.getPoolSize(),
				executor.getActiveCount(), queue.size(), queue.size() + queue.remainingCapacity());
	}

}
