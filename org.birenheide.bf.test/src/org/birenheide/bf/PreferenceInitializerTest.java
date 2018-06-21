package org.birenheide.bf;

import static org.junit.Assert.fail;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWTException;
import org.junit.Test;

public class PreferenceInitializerTest {

	@Test
	public void testPreferenceInitializer() throws Exception {
		Future<Void> result = Executors.newSingleThreadExecutor().submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				BfPreferenceInitializer initializer = new BfPreferenceInitializer();
				try {
					initializer.initializeDefaultPreferences();
				}
				catch (SWTException ex) {
					fail(ex.getMessage());
				}
				return null;
			}
		});
		try {
			result.get(10, TimeUnit.SECONDS);
		} 
		catch (ExecutionException ex) {
			if (ex.getCause() instanceof Exception) {
				throw (Exception) ex.getCause();
			}
			else if (ex.getCause() instanceof AssertionError) {
				throw (AssertionError) ex.getCause();
			}
			else {
				throw ex;
			}
		}
	}
}
