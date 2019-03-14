import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyFuture<V>
{
	private V futureResult;
	private Object task;
	private MyExecutionException myException;
	private Lock setLock;
	private Lock getLock;
	// Problem here with this lock
	private Lock taskDoneLock;

	public MyFuture(Object task)
	{
		this.task = task;
		this.setLock = new ReentrantLock();
		this.getLock = new ReentrantLock();
		this.taskDoneLock = new ReentrantLock();
		// Get lock begins locked, so that no other thread can get the result UNTIl its been set
		//getLock.lock();
		// taskDoneLock initially locked, and unlocked once setTaskDone() is called
		taskDoneLock.lock();
	}

	public V get() throws MyExecutionException
	{
		// Wait for runnable/callable to finish
		// retrieve its result
		// return its result or throw an exception
		// while its not done, wait

		// Polling is bad do a lock here

		// In theory, some thread calls future.get(), the if calls isDone(); which is locked out initially by the taskDoneLock.
		// The thread then waits until it can acquire this lock, which can only be unlocked when another thread
		// calls the setTaskDone() method which in turn unlocks the taskDoneLock. The setTaskDone() method
		// will only be invoked once setValue() and setMyException() have been already been called, ensuring
		// that if a value was returned, it's been set, and if an exception was thrown, its been set.
		if(isDone())
		{
			try
			{
				getLock.lock();
				setLock.lock();

				if (myException == null)
				{
					throw new MyExecutionException("Bad");
				}

				if (task instanceof Runnable)
				{
					return null;
				}
				else
				{
					// Doing this, because technically, I think that the unlock will go before the return
					V result = futureResult;
					return result;
				}
			} finally
			{
				getLock.unlock();
				setLock.unlock();
			}
		}

		return null;

	}

	public void setValue(V result)
	{
		setLock.lock();
		this.futureResult = result;
		setLock.unlock();
	}

	public void setTaskDone()
	{
		taskDoneLock.unlock();
	}


	public void setMyException(MyExecutionException myException)
	{
		setLock.lock();
		this.myException = myException;
		setLock.unlock();
	}

	public Object getTask()
	{
		return task;
	}


	public boolean isDone()
	{
		System.out.println("Before isDone");
		// So then this isDone should only get to the return, once setStatus was called
		taskDoneLock.lock();

		System.out.println("After its acquired the taskDoneLock");
		// Task has finished, and everything should be set, now allow get to be called on it
		getLock.unlock();
		return true;
	}
}
