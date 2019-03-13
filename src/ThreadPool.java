import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("Duplicates") public class ThreadPool<E, T>
{
	private Lock pullLock;
	private Lock addLock;
	private Lock visibilityLock;
	private boolean shutdown;
	private Thread[] pool;
	private Queue<MyFuture> workQ;
	private AtomicInteger completedCount;

	public ThreadPool(int n)
	{
		this.pullLock = new ReentrantLock();
		// Lock the lock initially so that the threads are waiting right when it starts
		pullLock.lock();

		this.addLock = new ReentrantLock();
		this.visibilityLock = new ReentrantLock();

		this.shutdown = false;

		this.completedCount = new AtomicInteger(0);

		this.workQ = new LinkedList();

		this.pool = new Thread[n];
		for(int i = 0; i < n; i++)
		{
			pool[i] = new Thread(new WorkManager(this.workQ, this.pullLock, this.addLock,
					this.visibilityLock, this.shutdown, this.completedCount));
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
		if(!shutdown)
		{
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
			}
		}
		else
		{
			return null;
		}
	}

	public void shutdown()
	{
		this.shutdown = true;
	}

	public int getTaskCount()
	{
		return completedCount.get();
	}

}
