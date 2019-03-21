package ImplementThreadPool;

import java.util.concurrent.Semaphore;

public class MyFuture<V>
{
	private V futureResult;
	private Object task;
	private MyExecutionException myException;
	private Semaphore setLock;
	private boolean status;

	public MyFuture(Object task) throws InterruptedException
	{
		this.task = task;
		this.setLock = new Semaphore(0);
		this.status = false;
	}

	public V get() throws MyExecutionException, InterruptedException
	{

		setLock.acquire();

		if (myException != null)
		{
			throw myException;
		}
		else
		{
			// Doing this, because technically, I think that the unlock will go before the return
			V result = futureResult;
			return result;
		}

	}

	public void setValue(V result)
	{
		this.futureResult = result;
		this.status = true;
		setLock.release();
	}

	public void setMyException(MyExecutionException myException)
	{
		this.myException = myException;
		this.status = true;
		setLock.release();
	}

	public Object getTask()
	{
		return task;
	}

	public boolean isDone()
	{
		return status;
	}
}
