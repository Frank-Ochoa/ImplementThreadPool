package ImplementThreadPool;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

public class WorkManager implements Runnable
{
	// Have a runnable that is a marker task that gets put on the queue when shut down is called

	private Queue<MyFuture> workQ;
	private Semaphore pullLock;
	private Mutex addLock;
	private AtomicInteger completedCount;

	public WorkManager(Queue<MyFuture> workQ, Semaphore pullLock, Mutex addLock,
			AtomicInteger completedCount)
	{
		this.workQ = workQ;
		this.pullLock = pullLock;
		this.addLock = addLock;
		this.completedCount = completedCount;
	}

	@Override public void run()
	{

		while (true)
		{
			try
			{
				//System.err.println("THREAD");
				pullLock.acquire();
			} catch (Exception e)
			{
				//System.err.println("THREAD CRAPPED OUT");
				e.printStackTrace();
			}

			try
			{
				addLock.lock();
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}

			MyFuture<Object> future = workQ.remove();

			addLock.unlock();

			Object task = future.getTask();

			if (task instanceof ExitTask)
			{
				//System.err.println("EXIT");
				return;
			}
			else if (task instanceof Runnable)
			{
				//System.err.println("RUN");
				try
				{
					((Runnable) task).run();
					future.setValue(null);
				}catch (Exception e)
				{
					e.printStackTrace();
					future.setMyException(new MyExecutionException(e));
				}
			}
			else if (task instanceof Callable)
			{
				try
				{
					// Set the value of the future to w/e object the call method returned
					future.setValue(((Callable) task).call());

				} catch (Exception e)
				{
					// pass on the exception
					e.printStackTrace();
					future.setMyException(new MyExecutionException(e));
				}

			}

			// Increment the number of completed tasks
			completedCount.getAndIncrement();
		}
	}
}
