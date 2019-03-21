package ImplementThreadPool.ImplementThreadPool.src.src.ImplementThreadPool;

import java.util.concurrent.Callable;

public class ToyCallable implements Callable<Integer>
{
	private int i;

	public ToyCallable(int i)
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
