package org.birenheide.bf;

import org.junit.Test;

import static org.junit.Assert.fail;

public class InterpreterTestCase {

	protected void runInterpreterInSeparateThread(
			BrainfuckInterpreter interpreter, int timeOut) {
		NotifyingRunnable wrapper = new NotifyingRunnable(interpreter);
		try {
			wrapper.startAndJoin(timeOut);
		} catch (InterruptedException ex) {
			fail(ex.getMessage());
		}
	}

	@Test
	public final void testNormalRun() throws Exception {
		NotifyingRunnable wrapper = new NotifyingRunnable(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {
					throw new RuntimeException(ex);
				}
			}
		});
		wrapper.startAndJoin(110);
	}

	@Test(expected = InterruptedException.class)
	public final void testInterruptedRun() throws Exception {
		NotifyingRunnable wrapper = new NotifyingRunnable(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {

				}
			}
		});
		wrapper.startAndJoin(90);
	}

	private static class NotifyingRunnable implements Runnable {

		private final Runnable wrapped;
		private volatile boolean terminated = false;

		NotifyingRunnable(Runnable wrapped) {
			this.wrapped = wrapped;
		}

		void startAndJoin(int timeOut) throws InterruptedException {
			Thread t = new Thread(this, "Interpreter");
			t.setDaemon(true);
			t.start();
			t.join(timeOut);
			if (!terminated) {
				t.interrupt();
				throw new InterruptedException("Interpreter Run timed out");
			}
		}

		@Override
		public void run() {
			this.terminated = false;
			this.wrapped.run();
			if (!Thread.interrupted()) {
				this.terminated = true;
			}
		}

	}
}
