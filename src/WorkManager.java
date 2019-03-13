import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

public class WorkManager implements Runnable
{
	private Queue<MyFuture> workQ;
	private Lock pullLock;
	private Lock addLock;
	private Lock visibilityLock;
	private boolean shutdown;
	private AtomicInteger completedCount;

	public WorkManager(Queue<MyFuture> workQ, Lock pullLock, Lock addLock, Lock visibilityLock, boolean shutdown,
			AtomicInteger completedCount)
	{
		this.workQ = workQ;
		this.pullLock = pullLock;
		this.addLock = addLock;
		this.visibilityLock = visibilityLock;
		this.shutdown = shutdown;
		this.completedCount = completedCount;
	}

	@Override public void run()
	{
		if (!shutdown)
		{
			while (true)
			{
				doWork();
			}
		}
		// shutdown has been flagged as true
		else
		{
			// do all that above until the workQ is empty
			while(!workQ.isEmpty())
			{
				doWork();
			}
		}
	}

	private void doWork()
	{
		pullLock.lock();
		addLock.lock();

		MyFuture<Object> future = workQ.remove();

		if (!workQ.isEmpty())
		{
			pullLock.unlock();
		}

		addLock.unlock();

		Object task = future.getTask();

		if (task instanceof Runnable)
		{
			((Runnable) task).run();
			visibilityLock.lock();
			future.setStatus(true);
			visibilityLock.unlock();
		}
		if (task instanceof Callable)
		{
			try
			{
				Object y = ((Callable) task).call();
				// Need to lock/unlock to act as a memory fence, and propagate changes out to other threads
				visibilityLock.lock();
				future.setValue(y);
				future.setStatus(true);
				visibilityLock.unlock();

			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		// Increment the number of completed tasks
		completedCount.getAndIncrement();
	}
}
