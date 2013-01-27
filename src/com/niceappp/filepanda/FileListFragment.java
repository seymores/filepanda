package com.niceappp.filepanda;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;

/**
 * A list fragment representing a list of Files. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link FileDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class FileListFragment extends ListFragment {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	private static final String TAG = "FileListFragment";

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = fileSelectCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	protected FilesAdapter adapter;
	protected String currentFilePath;

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(File file);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks fileSelectCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(File f) {
		}
	};

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public FileListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		adapter = new FilesAdapter(getActivity());
		setListAdapter(adapter);
		adapter.loadFiles(null);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		int index = info.position;
		File f = (File) adapter.getItem(index);
		String header = "Options";
		if (f != null)
			header = f.getName();
		menu.setHeaderTitle(header);

		menu.add("Open");
		menu.add("Rename");
		menu.add("Delete");

		if (f != null && !f.isDirectory())
			menu.add("Share");

		menu.add("Get info");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		int index = info.position;
		File f = (File) adapter.getItem(index);

		Log.d(TAG, "  ==>" + f.getName());

		String menuName = (String) item.getTitle();
		FileListActivity activity = (FileListActivity) getActivity();

		if ("Open".equalsIgnoreCase(menuName)) {
			// Open file
			activity.openFile(f);
		} else if ("Rename".equalsIgnoreCase(menuName)) {
			// Rename
			askToRenameFile(f);
		} else if ("Share".equalsIgnoreCase(menuName)) {
			// Share
			shareFile(f);
		} else if ("Delete".equalsIgnoreCase(menuName)) {
			askConfirmToDelete(f);
		} else if ("Get info".equalsIgnoreCase(menuName)) {
			// Get more file info
			getMoreInfo(f);
		}
		return super.onContextItemSelected(item);
	}

	private void shareFile(File f) {
		MimeTypeMap myMime = MimeTypeMap.getSingleton();
		String mimeType = myMime.getMimeTypeFromExtension(FilePandaApplication
				.fileExt(f.getName().toString()).substring(1));
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.setType(mimeType);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		Uri uri = Uri.fromFile(f);
		intent.putExtra(Intent.EXTRA_STREAM, uri);
		startActivity(Intent.createChooser(intent, "Share with"));
	}

	private void getMoreInfo(File f) {

		StringBuffer sb = new StringBuffer();

		String canExe = f.canExecute() ? "Yes" : "No";
		String canWrite = f.canWrite() ? "Yes" : "No";
		String canRead = f.canRead() ? "Yes" : "No";
		String fileSize = f.length() / 1000 + "kb";

		try {
			sb.append(f.getCanonicalPath().toString());

			sb.append("\nCan execute? " + canExe);
			sb.append("\nCan read? " + canRead);
			sb.append("\nCan write? " + canWrite);
			sb.append("\nFile size: " + fileSize);
			sb.append("\nLast modified: " + new Date(f.lastModified()));

		} catch (IOException e) {
			e.printStackTrace();
		}

		new AlertDialog.Builder(getActivity()).setTitle(f.getName())
				.setMessage(sb.toString()).setPositiveButton("Dismiss", null)
				.show();
	}

	private void askToRenameFile(final File f) {

		final EditText textfield = new EditText(getActivity());
		textfield.setHint("New file name");
		new AlertDialog.Builder(getActivity())
				.setTitle("Rename " + f.getName()).setView(textfield)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String newName = textfield.getText().toString();
						if (newName == null || newName.length() == 0)
							return;
						File dir = f.getParentFile();
						File newfile = new File(dir.getPath(), newName);
						boolean rs = f.renameTo(newfile);
						Log.d(TAG, " * Renamed to " + newfile.getName()
								+ ", success? " + rs);
						FileListFragment.this.loadFileDir(currentFilePath);
					}
				}).setNegativeButton("Cancel", null).show();
	}

	private void askConfirmToDelete(final File f) {
		Log.d(TAG, " * To delete file=" + f);

		new AlertDialog.Builder(getActivity())
				.setTitle("Confirm delete file?")
				.setMessage("Are you sure you want to delete this file?")
				.setPositiveButton("Delete",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								boolean rs = FilePandaApplication.deleteFile(f);
								FileListFragment.this.adapter
										.loadFiles(currentFilePath);
								Log.d(TAG, " Deleted " + f.getName() + "?" + rs);
							}
						}).setNegativeButton("Cancel", null).show();
	}

	public void loadFileDir(String dirpath) {
		currentFilePath = dirpath;
		adapter.loadFiles(currentFilePath);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}

		registerForContextMenu(getListView());

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = fileSelectCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		File f = (File) adapter.getItem(position);
		mCallbacks.onItemSelected(f);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}

}
