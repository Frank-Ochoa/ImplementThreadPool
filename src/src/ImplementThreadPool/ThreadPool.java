package ImplementThreadPool;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPool<E, T>
{
	private Semaphore pullLock;
	private Mutex addLock;
	private boolean shutdown;
	private Thread[] pool;
	private Queue<MyFuture> workQ;
	private AtomicInteger completedCount;
	private int numThreads;

	public ThreadPool(int n) throws InterruptedException
	{
		// Lock the lock initially so that the threads are waiting right when it starts
		this.pullLock = new Semaphore(0);

		this.addLock = new Mutex();

		this.shutdown = false;

		this.completedCount = new AtomicInteger(0);

		this.numThreads = n;

		this.workQ = new LinkedList();

		this.pool = new Thread[n];
		for (int i = 0; i < n; i++)
		{

			pool[i] = new Thread(
					new WorkManager(this.workQ, this.pullLock, this.addLock, this.completedCount));
		}
		for (int i = 0; i < n; i++)
		{
			pool[i].start();
		}

	}

	public ThreadPool() throws InterruptedException
	{
		this(Runtime.getRuntime().availableProcessors());
	}

	public MyFuture<E> submit(Runnable runnable) throws InterruptedException
	{
		return submitWork(runnable);
	}

	public MyFuture<T> submit(Callable<T> callable) throws InterruptedException
	{
		return submitWork(callable);
	}

	private MyFuture submitWork(Object object) throws InterruptedException
	{

		//System.err.println("SUBMIT WORK");
		if (shutdown)
		{
			System.err.println("Thread Pool was shutdown. No further tasks allowed.");
			return null;
		}

		try
		{
			addLock.lock();
			MyFuture task = new MyFuture<>(object);
			workQ.add(task);
			return task;
		} finally
		{
			addLock.unlock();
			pullLock.release();

		}

	}

	public void shutdown() throws InterruptedException
	{

		// These tasks will act as markers on the Queue and when all have been taken off, means to shutdown the ThreadPool
		// Remember to fence this out
		this.shutdown = true;
		Fence.fence();


		// Add the exit tasks to the workQ
		for (int i = 0; i < numThreads; i++)
		{
			try
			{
				addLock.lock();
				MyFuture<ExitTask> exitTask = new MyFuture<>(new ExitTask());
				workQ.add(exitTask);
			} finally
			{
				addLock.unlock();
				pullLock.release();
			}
		}
	}

	public int getTaskCount()
	{
		return completedCount.get();
	}

}
