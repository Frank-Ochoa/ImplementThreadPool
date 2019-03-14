public class Test
{
	public static void main(String[] args)
	{
		ThreadPool t = new ThreadPool();
		//MyFuture<Integer> x = t.submit(new ToyRunnable(1));
		/*int y = 0;

		try
		{
			y = x.get();
		} catch (MyExecutionException e)
		{
			e.printStackTrace();
		}*/

		t.shutdown();

		//System.out.println(t.getTaskCount());
		//System.out.println(y);

	}
}
