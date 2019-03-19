import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

public class WorkManager implements Runnable
{
	// Have a runnable that is a marker task that gets put on the queue when shut down is called

	private Queue<MyFuture> workQ;
	private FIFOMutex pullLock;
	private Lock addLock;
	private AtomicInteger completedCount;

	public WorkManager(Queue<MyFuture> workQ, FIFOMutex pullLock, Lock addLock,
			AtomicInteger completedCount)
	{
		this.workQ = workQ;
		this.pullLock = pullLock;
		this.addLock = addLock;
		this.completedCount = completedCount;
	}

	@Override public void run()
	{
		System.out.println("Got into work manager");

		while (true)
		{
			pullLock.lock();
			addLock.lock();
			System.out.println("A thread aquired the locks");

			MyFuture<Object> future = workQ.remove();

			if (!workQ.isEmpty())
			{
				pullLock.unlock();
			}

			addLock.unlock();

			Object task = future.getTask();

			if (task instanceof ExitTask)
			{
				System.out.println("task was instance of ExistTask");
				return;
			}
			if (task instanceof Runnable)
			{
				((Runnable) task).run();
				future.setValue(null);
			}
			if (task instanceof Callable)
			{
				try
				{
					// Set the value of the future to w/e object the call method returned
					future.setValue(((Callable) task).call());

				} catch (Exception e)
				{
					// pass on the exception
					future.setMyException();
				}

			}

			// Increment the number of completed tasks
			completedCount.getAndIncrement();
		}
	}
}
