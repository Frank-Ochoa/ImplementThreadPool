package ImplementThreadPool;

import java.util.LinkedList;
import java.util.List;

public class Test
{
	public static void main(String[] args) throws InterruptedException
	{
		for(int j = 0; j < 10_000; j++)
		{
			ThreadPool threadPool = new ThreadPool();

			List<MyFuture<Integer>> futures = new LinkedList<>();

			for (int i = 0; i < 100; i++)
			{
				try
				{
					futures.add(threadPool.submit(new ToyRunnable(1)));
				} catch (InterruptedException e)
				{

					e.printStackTrace();
				}
			}

			int y = 0;

			//Thread.sleep(500000);

			for (MyFuture<Integer> x : futures)
			{
				try
				{
					y += x.get();
				} catch (MyExecutionException e)
				{
					e.printStackTrace();
				}
			}

			threadPool.shutdown();
			futures.add(threadPool.submit(new ToyRunnable(1)));

			System.out.println(threadPool.getTaskCount());
			System.out.println(y);
		}
	}
}
