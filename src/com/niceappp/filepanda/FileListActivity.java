package com.niceappp.filepanda;

import java.io.File;
import java.io.IOException;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * An activity representing a list of Files. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link FileDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link FileListFragment} and the item details (if present) is a
 * {@link FileDetailFragment}.
 * <p>
 * This activity also implements the required {@link FileListFragment.Callbacks}
 * interface to listen for item selections.
 */
public class FileListActivity extends FragmentActivity implements
		FileListFragment.Callbacks {

	private static final String TAG = "FileListActivity";

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_list);

		String root = getIntent().getStringExtra("root");
		String title = getIntent().getStringExtra("title");
		if (title != null)
			setTitle(title);

		FileListFragment fragment = (FileListFragment) getSupportFragmentManager()
				.findFragmentById(R.id.file_list);
		fragment.setActivateOnItemClick(true);

		if (findViewById(R.id.file_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
		}

		if (root != null) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
			fragment.loadFileDir(root);
			Log.d(TAG, "ROOT=" + root);
		}

		displayFreeSpace();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Callback method from {@link FileListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(File f) {
		if (mTwoPane) {
			// XXX TODO
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(FileDetailFragment.ARG_ITEM_ID, f.getName());
			FileDetailFragment fragment = new FileDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.file_detail_container, fragment).commit();

		} else {

			openFile(f);

			// In single-pane mode, simply start the detail activity
			// // for the selected item ID.
			// Intent detailIntent = new Intent(this, FileDetailActivity.class);
			// detailIntent.putExtra(FileDetailFragment.ARG_ITEM_ID, id);
			// startActivity(detailIntent);
		}
	}

	public void openFile(File f) {
		if (f.isDirectory()) {
			Intent fileList = new Intent(this, FileListActivity.class);
			String title = (String) getTitle();
			if ("FilePanda".equalsIgnoreCase(title))
				title = "";
			try {
				fileList.putExtra("root", f.getCanonicalPath());
				fileList.putExtra("title", title + "/" + f.getName());
				startActivity(fileList);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
		    MimeTypeMap myMime = MimeTypeMap.getSingleton();
		    Intent newIntent = new Intent(android.content.Intent.ACTION_VIEW);
		    String mimeType = myMime.getMimeTypeFromExtension( 
		    		FilePandaApplication.fileExt(f.getName().toString()).substring(1));
		    newIntent.setDataAndType(Uri.fromFile(f), mimeType);
		    
		    try {
		        startActivity(newIntent);
		    } catch (android.content.ActivityNotFoundException e) {
		        Toast.makeText(this, "No handler for this type of file.", 
		        		Toast.LENGTH_LONG).show();
		    }
		}
	}
	
	private void displayFreeSpace() {
		ProgressBar statusBar = (ProgressBar) findViewById(R.id.progressBar);
		
		int total = FilePandaApplication.totalMemory();
		int busy = FilePandaApplication.busyMemory();
		
		if (busy/total > 0.5  ) { 
			statusBar.setVisibility(View.GONE);
		} else {		
			statusBar.setMax(total);
			statusBar.setProgress(busy);
		}
	}


}
