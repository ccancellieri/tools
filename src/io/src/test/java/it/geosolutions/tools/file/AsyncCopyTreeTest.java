package it.geosolutions.tools.file;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class AsyncCopyTreeTest {
	File srcMount;
	File destMount;
	File testFile;
	CompletionService<File> cs;
	ExecutorService ex;
	
	@Before
	public void setUp() throws Exception {
		srcMount = new File("src/test/resources/test-data/");
		destMount = new File("src/test/resources/test-data2/");
		testFile = new File(
				srcMount,
				"collector/569_RS1_20100707050441.046.SCN8.NEAR.1.00000_PRO/RS1_20100707050441.046.SCN8.NEAR.1.00000_geo8.xml");
		
		ex = Executors.newFixedThreadPool(2);

		if (ex == null || ex.isTerminated()) {
			throw new IllegalArgumentException(
					"Unable to run asynchronously using a terminated or null ThreadPoolExecutor");
		}

		cs= new ExecutorCompletionService<File>(ex);
	}

	@After
	public void tearDown() throws Exception {
		if (ex!=null)
			ex.shutdown();
	}

	@Test
	@Ignore
	public void asyncCopyTreeTest() {
		System.out.println("START: asyncCopyTreeTest");
		AsyncCopyTree act = new AsyncCopyTree(FileFilterUtils.or(
				FileFilterUtils.directoryFileFilter(),
				FileFilterUtils.fileFileFilter()), cs, srcMount, destMount);
		Collection<Future<File>> list = act.copy();
		int workSize = list.size();
		try {
			while (workSize-- > 0) {
				Future<File> future = cs.take();
				try {
					System.out.println("copied file: " + future.get());
				} catch (ExecutionException e) {
					e.printStackTrace();
					Assert.fail();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			Assert.fail();
		}
		System.out.println("STOP: asyncCopyTreeTest");
	}
	
	@Test
	@Ignore
	public void asyncCopyTreeStopTest() {
		System.out.println("START: asyncCopyTreeStopTest");
		
		AsyncCopyTree act = new AsyncCopyTree(FileFilterUtils.or(
				FileFilterUtils.directoryFileFilter(),
				FileFilterUtils.fileFileFilter()), cs, srcMount, destMount);
		Collection<Future<File>> list = act.copy();
		int workSize = list.size();
		try {
			while (workSize-- > 0) {
				if (workSize==2){
					act.setCancelled(true);
				}
				Future<File> future = cs.take();
				try {
					if (workSize<=2)
						System.out.println("[STOPPED] copied file: " + future.get());
					else
						System.out.println("copied file: " + future.get());
				} catch (ExecutionException e) {
					e.printStackTrace();
					Assert.fail();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			
		}
		System.out.println("STOP: asyncCopyTreeStopTest");
	}
	
	@Test
	public void asyncStopCopyTest() {
		System.out.println("START: asyncStopCopyTest");

		final AsyncCopyTree act = new AsyncCopyTree(FileFilterUtils.or(
				FileFilterUtils.directoryFileFilter(),
				FileFilterUtils.fileFileFilter()), cs, srcMount, destMount);
		
		final Thread copier=new Thread(new Runnable() {
			public void run() {
				act.setCancelled(true);
				Assert.assertNull("Returned list should be null",act.copy());
			}
		});
		
		copier.start();
		
		try {
			copier.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			Assert.fail();
		}
		
		System.out.println("STOP: asyncStopCopyTest");
	}

	@Test
	@Ignore
	public void rebaseTest() {
		System.out.println("START: rebaseTest");
		try {
			// rebase a file
			System.out.println("Before: " + testFile);
			File rebasedFile = Path.rebaseFile(srcMount, destMount,
					testFile);
			System.out.println("After: " + rebasedFile);

			// TEST: File is not in the mount point dir
			try {
				System.out.println("Before: " + rebasedFile);
				testFile = Path.rebaseFile(srcMount, destMount,
						rebasedFile);
				Assert.fail();
			} catch (IllegalArgumentException e) {
				System.out
						.println("Rise exception: " + e.getLocalizedMessage());
			}

			try {
				// TEST: File is equals to the mount point dir
				testFile = new File(srcMount, "./");
				System.out.println("Before: " + testFile);
				rebasedFile = Path.rebaseFile(srcMount, destMount,
						testFile);
				System.out.println("After: " + rebasedFile);
			} catch (IllegalArgumentException e) {
				Assert.fail();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			org.junit.Assert.fail();
		}
		System.out.println("STOP: rebaseTest");
	}
/*
	@Test
	@Ignore
	public void testFileFilter() {
		System.out.println("START: testFileFilter");
		XStream xstream = new XStream(new DomDriver());
		FileFilter filter = FileFilterUtils.or(
				FileFilterUtils.directoryFileFilter(),
				FileFilterUtils.fileFileFilter());
		FileFilter filter2 = FileFilterUtils.and(FileFilterUtils
				.asFileFilter(filter), FileFilterUtils
				.notFileFilter(FileFilterUtils.nameFileFilter("*.lck",
						IOCase.INSENSITIVE)));
		System.out.println("FILTER: " + xstream.toXML(filter2));
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(new File(
					"src/test/resources/FileFilter.xml"));
			Object obj = xstream.fromXML(fis);
			System.out.println("FILTER OBJ: " + obj.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (XStreamException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		} finally {
			IOUtils.closeQuietly(fis);
		}
		System.out.println("STOP: testFileFilter");
	}
	*/
}
