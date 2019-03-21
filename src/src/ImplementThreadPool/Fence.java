package ImplementThreadPool.ImplementThreadPool.src.src.ImplementThreadPool;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Fence
{
	public static void fence()
	{
		Lock lock = new ReentrantLock();
		lock.lock();
		lock.unlock();
	}
}
