import java.util.concurrent.Callable;

public class MyFuture<V>
{
	private V futureResult;
	private Object task;
	private boolean status;

	public MyFuture(Object task)
	{
		this.task = task;
		this.status = false;
	}

	public V get() throws MyExecutionException
	{
		// Wait for runnable/callable to finish
		// retrieve its result
		// return its result or throw an exception
		// while its not done, wait
		// Possible bug and questions here

		while(!isDone())
		{
			// Wait until the task is done
		}

		try
		{
			if (task instanceof Runnable)
			{
				return null;
			}
			else if (task instanceof Callable)
			{
				return futureResult;
			}
		}
		catch (Exception e)
		{
			throw new MyExecutionException("Task threw an exception");
		}

		return null;
	}

	public void setValue(V result)
	{
		this.futureResult = result;
	}


	public void setStatus(boolean status)
	{
		this.status = status;
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
