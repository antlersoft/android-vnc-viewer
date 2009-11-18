/**
 * Copyright (C) 2009 Michael A. MacDonald
 */
package android.androidVNC;

import android.graphics.Point;
import android.os.Handler;
import android.os.SystemClock;

/**
 * Handles panning the screen continuously over a period of time
 * @author Michael A. MacDonald
 */
class Panner implements Runnable {
	
	private VncCanvasActivity activity;
	private Handler handler;
	private Point velocity;
	private long lastSent;
	private VelocityUpdater updater;
	
	/**
	 * Specify how the panning velocity changes over time
	 * @author Michael A. MacDonald
	 */
	interface VelocityUpdater {
		/**
		 * Called approximately every 20 ms to update the velocity of panning
		 * @param p X and Y components to update
		 * @param interval Milliseconds since last update
		 * @return False if the panning should stop immediately; true otherwise
		 */
		boolean updateVelocity(Point p, long interval);
	}

	static class DefaultUpdater implements VelocityUpdater {
		
		static DefaultUpdater instance = new DefaultUpdater();

		/**
		 * Don't change velocity
		 */
		@Override
		public boolean updateVelocity(Point p, long interval) {
			return true;
		}
		
	}
	
	Panner(VncCanvasActivity act, Handler hand) {
		activity = act;
		velocity = new Point();
		handler = hand;
	}
	
	void stop()
	{
		handler.removeCallbacks(this);
	}
	
	void start(int xv, int yv, VelocityUpdater update)
	{
		if (update == null)
			update = DefaultUpdater.instance;
		updater = update;
		velocity.x = xv;
		velocity.y = yv;
		
		handler.postDelayed(this, 20);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		long interval = SystemClock.uptimeMillis() - lastSent;
		lastSent += interval;
		double scale = (double)interval / 20.0;
		if ( activity.pan((int)((double)velocity.x * scale), (int)((double)velocity.y * scale)))
		{
			if (updater.updateVelocity(velocity, interval))
			{
				handler.postDelayed(this, 20);
			}
			else
				stop();
		}
		else
			stop();
	}

}
