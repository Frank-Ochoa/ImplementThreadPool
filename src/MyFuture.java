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
		// TODO: This implementation of throwing the exception seems a bit hacky, so we should consider it
		//		 but maybe it's what we need to do

		while(!isDone())
		{
			// Wait until the task is done
			//System.out.println("isDone Loop");
		}


			if (task instanceof Runnable)
			{
				return null;
			}
			else if (task instanceof Callable)
			{
				if(futureResult == null)
				{
					throw new MyExecutionException("Bad");
				}
				else
				{
					return futureResult;
				}
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
		System.out.println(this.status);
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
