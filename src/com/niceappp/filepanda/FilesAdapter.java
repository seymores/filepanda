package com.niceappp.filepanda;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ocpsoft.prettytime.PrettyTime;

import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FilesAdapter extends BaseAdapter {

	private List<File> fileList = new ArrayList<File>();
	private PrettyTime p = new PrettyTime();
	
	private Context ctx;
	
	public FilesAdapter(Context ctx) {
		super();
		this.ctx = ctx;		
	}
	
	public void loadFiles(String filepath) {

		String startPath = filepath;
		if (startPath == null)
			startPath = Environment.getExternalStorageDirectory()
					.getAbsolutePath();

		File root = new File(startPath);
		File[] files = root.listFiles();
		fileList.clear();

		for (File file : files) {
			fileList.add(file);
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (fileList == null)
			return 0;
		return fileList.size();
	}

	@Override
	public Object getItem(int position) {
		return fileList.get(position);
	}

	@Override
	public long getItemId(int position) {
		File f = fileList.get(position);
		return f.hashCode();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder;
		
		if (convertView == null) {		
			LayoutInflater li = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = li.inflate(R.layout.file_row, null);
			holder = new ViewHolder();
			holder.title = (TextView)v.findViewById(R.id.title);
			holder.description = (TextView)v.findViewById(R.id.description);
			holder.icon = (ImageView)v.findViewById(R.id.icon);
			convertView = v;
			convertView.setTag(holder);
		}
		holder = (ViewHolder) convertView.getTag();
		File file = fileList.get(position);
		holder.title.setText(file.getName());
		holder.description.setText(p.format(new Date( file.lastModified())));
		
		if (file.isDirectory()) {
			holder.icon.setImageResource(R.drawable.folder);
		} else {
			holder.icon.setImageResource(R.drawable.unknown);
		}
		
		return convertView;
	}
	
	class ViewHolder {
		TextView title;
		TextView description;
		ImageView icon;
	}
}
