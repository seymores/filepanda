package com.niceappp.filepanda;

import java.io.File;

import android.app.Application;
import android.os.Environment;
import android.os.StatFs;

public class FilePandaApplication extends Application {

	public static int totalMemory() {
		StatFs statFs = new StatFs(Environment.getRootDirectory()
				.getAbsolutePath());
		int Total = (statFs.getBlockCount() * statFs.getBlockSize()) / 1048576;
		return Total;
	}

	public static int freeMemory() {
		StatFs statFs = new StatFs(Environment.getRootDirectory()
				.getAbsolutePath());
		int Free = (statFs.getAvailableBlocks() * statFs.getBlockSize()) / 1048576;
		return Free;
	}

	public static int busyMemory() {
		StatFs statFs = new StatFs(Environment.getRootDirectory()
				.getAbsolutePath());
		int Total = (statFs.getBlockCount() * statFs.getBlockSize()) / 1048576;
		int Free = (statFs.getAvailableBlocks() * statFs.getBlockSize()) / 1048576;
		int Busy = Total - Free;
		return Busy;
	}
	
	/**
	 * Helper method to extract the file extension from the file full name.
	 * @param url
	 * @return
	 */
	public static String fileExt(String url) {
	    if (url.indexOf("?")>-1) {
	        url = url.substring(0,url.indexOf("?"));
	    }
	    
	    if (url.lastIndexOf(".") == -1) {
	        return url;
	    } else {
	        String ext = url.substring(url.lastIndexOf(".") );
	        if (ext.indexOf("%")>-1) {
	            ext = ext.substring(0,ext.indexOf("%"));
	        }
	        if (ext.indexOf("/")>-1) {
	            ext = ext.substring(0,ext.indexOf("/"));
	        }
	        return ext.toLowerCase();
	    }
	}

	/**
	 * Helper method to delete the given file. 
	 * Will delete all the files within it if it's a directory.
	 * @param f
	 * @return delete status
	 */
	public static boolean deleteFile(File f) {
		boolean rs = false;
		
		if (f.isDirectory()) {
			File[] files = f.listFiles();			
			for (File file : files) {
				rs = file.delete();
			}			
		} 
		rs = f.delete();
				
		return rs;
	}
}
