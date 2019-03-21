package ImplementThreadPool;

import java.util.concurrent.Callable;

public class ToyRunnable implements Callable<Integer>
{
	private int i;

	public ToyRunnable(int i)
	{
		this.i = i;
	}

	/*@Override public void run()
	{
		System.out.println(i);
	}
*/
	@Override public Integer call() throws Exception
	{
		return i;
	}
}
