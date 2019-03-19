import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadPool<E, T>
{
	private FIFOMutex pullLock;
	private Lock addLock;
	private boolean shutdown;
	private Thread[] pool;
	private Queue<MyFuture> workQ;
	private AtomicInteger completedCount;
	private int numThreads;

	public ThreadPool(int n)
	{
		this.pullLock = new FIFOMutex();
		// Lock the lock initially so that the threads are waiting right when it starts
		pullLock.lock();

		this.addLock = new ReentrantLock();

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

	public ThreadPool()
	{
		this(Runtime.getRuntime().availableProcessors());
	}

	public MyFuture<E> submit(Runnable runnable)
	{
		return submitWork(runnable);
	}

	public MyFuture<T> submit(Callable<T> callable)
	{
		return submitWork(callable);
	}

	private MyFuture submitWork(Object object)
	{
		System.out.println("submitWork got called");

		// When shut down is called, it locks the addLock and in theory, no other thread should be able unlock it
		// (But then said thread would be Dead Locked I believe, always waiting to be able to acquire that lock)
		// and thus no longer be able to acquire the addLock here and then submit, oh but the add lock is unlocked no matter
		// what, maybe take out the finally, unless I add in the shutdown lock, but that's almost the same thing
		// if I used a try/finally, so maybe go back to using the boolean here

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
			pullLock.unlock();
			System.out.println("Pull lock was unlocked");
		}

	}

	public void shutdown()
	{

		// These tasks will act as markers on the Queue and when all have been taken off, means to shutdown the ThreadPool
		this.shutdown = true;


		// Add the exist tasks to the workQ
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
				pullLock.unlock();
			}
		}
	}

	public int getTaskCount()
	{
		return completedCount.get();
	}

}
