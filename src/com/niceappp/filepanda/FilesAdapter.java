package com.niceappp.filepanda;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import org.ocpsoft.prettytime.PrettyTime;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


@SuppressLint("DefaultLocale")
public class FilesAdapter extends BaseAdapter {

	private static final String TAG = "FilesAdapter";

	private File[] fileList;
	private PrettyTime p = new PrettyTime();

	private Context ctx;

	public FilesAdapter(Context ctx) {
		super();
		this.ctx = ctx;
	}

	public void loadFiles(String filepath) {

		String startPath = filepath;
		if (startPath == null)
			startPath = Environment.getExternalStorageDirectory().getAbsolutePath();

		File root = new File(startPath);
		File[] files = root.listFiles();

		fileList = files;
		sortFilesByName();		
	}

	@Override
	public int getCount() {
		if (fileList == null)
			return 0;
		return fileList.length;
	}

	@Override
	public Object getItem(int position) {
		return fileList[position];
	}

	@Override
	public long getItemId(int position) {
		File f = fileList[position];
		return f.hashCode();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;

		if (convertView == null) {
			LayoutInflater li = (LayoutInflater) ctx
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = li.inflate(R.layout.file_row, null);
			holder = new ViewHolder();
			holder.title = (TextView) v.findViewById(R.id.title);
			holder.description = (TextView) v.findViewById(R.id.description);
			holder.icon = (ImageView) v.findViewById(R.id.icon);
			convertView = v;
			convertView.setTag(holder);
		}

		holder = (ViewHolder) convertView.getTag();
		File file = fileList[position];
		holder.title.setText(file.getName());
		holder.description.setText(p.format(new Date(file.lastModified())));

		if (file.isDirectory()) {
			holder.icon.setImageResource(R.drawable.folder);
			holder.title.setTypeface(null, Typeface.BOLD);
		} else {
			setMimeType(holder.icon, file);
			holder.title.setTypeface(null, Typeface.NORMAL);
		}

		String[] files = file.list();

		if (files != null && files.length == 0) {
			holder.title.setTextColor(Color.GRAY);
		} else {
			holder.title.setTextColor(Color.BLACK);
		}

		return convertView;
	}

	private String setMimeType(ImageView v, File f) {

		MimeTypeMap myMime = MimeTypeMap.getSingleton();
		String ext = FilePandaApplication.fileExt(f.getName()).substring(1);
		String extMime = myMime.getMimeTypeFromExtension(ext);
		String mimeType = null;
		if (extMime != null) {
			mimeType = extMime;
		} else {
			mimeType = ext;
		}

		Log.d(TAG, " * mimetype=" + mimeType);

		if (mimeType == null) {
			v.setImageResource(R.drawable.unknown);
		} else if (mimeType.contains("image")) {
			v.setImageResource(R.drawable.picture);
		} else if (mimeType.contains("audio") || mimeType.contains("ogg")) {
			v.setImageResource(R.drawable.audio);
		} else if (mimeType.contains("video")) {
			v.setImageResource(R.drawable.video);
		} else if (mimeType.contains("pdf") || mimeType.contains("doc")) {
			v.setImageResource(R.drawable.doc);
		} else if (mimeType.contains("epub") || mimeType.contains("mobi")) {
			v.setImageResource(R.drawable.book);
		} else if (mimeType.contains("application")) {
			v.setImageResource(R.drawable.object);
		} else if (mimeType.contains("text")) {
			v.setImageResource(R.drawable.file);
		} else {
			v.setImageResource(R.drawable.unknown);
		}

		return mimeType;
	}

	public void sortFilesByDate() {
		File[] files = fileList;
		Arrays.sort(files, new Comparator<File>() {
			public int compare(File f1, File f2) {
				return Long.valueOf(f2.lastModified()).compareTo(
						f1.lastModified());
			}
		});
		notifyDataSetChanged();
	}

	public void sortFilesBySize(File[] files) {

		Arrays.sort(files, new Comparator<File>() {
			public int compare(File f1, File f2) {
				return Long.valueOf(f1.length()).compareTo(f2.length());
			}
		});
		notifyDataSetChanged();
	}

	public void sortFilesByName() {
		File[] files = fileList;
		Arrays.sort(files, new Comparator<File>() {
			@SuppressLint("DefaultLocale")
			public int compare(File f1, File f2) {				
				return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
			}
		});
		notifyDataSetChanged();
	}

	class ViewHolder {
		TextView title;
		TextView description;
		ImageView icon;
	}
}
