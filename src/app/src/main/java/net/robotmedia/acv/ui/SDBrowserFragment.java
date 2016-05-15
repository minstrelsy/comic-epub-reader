/*******************************************************************************
 * Copyright 2009 Robot Media SL
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package net.robotmedia.acv.ui;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

import net.androidcomics.acv.R;
import net.robotmedia.acv.Constants;
import net.robotmedia.acv.logic.PreferencesController;
import net.robotmedia.acv.utils.FileUtils;
import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.os.Environment;
import android.view.*;
import android.widget.*;

public class SDBrowserFragment extends Fragment {
	private static HashMap<String, Integer> supportedExtensions = null;
	private ListView browserListView;
	private LayoutInflater mInflater;
	private PreferencesController preferencesController;
    private String comicsPath;
    private File currentDirectory;

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param comicsPath Path to comic.
	 * @return A new instance of fragment RecentReadsFragment.
	 */
	public static SDBrowserFragment newInstance(String comicsPath) {
		SDBrowserFragment fragment = new SDBrowserFragment();
		Bundle args = new Bundle();
		args.putString(Constants.COMICS_PATH_KEY, comicsPath);
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        comicsPath = getArguments().getString(Constants.COMICS_PATH_KEY, Environment.getExternalStorageDirectory().getAbsolutePath());
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sd_browser, container, false);

        supportedExtensions = Constants.getSupportedExtensions();
        preferencesController = new PreferencesController(getActivity());
        mInflater = inflater;

        String storageState = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(storageState)) {
            currentDirectory = new File(comicsPath);

            browserListView = (ListView) view.findViewById(R.id.list);
            browserListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    File file = (File) parent.getItemAtPosition(position);
                    if ((file != null) && (file.isDirectory())) {
                        String[] images = file.list(new FilenameFilter() {
                            public boolean accept(File dir, String filename) {
                                String ext = FileUtils.getFileExtension(filename);
                                return FileUtils.isImage(ext);
                            }
                        });

                        if (images.length > 0) {
                            setResultAndFinish(file);
                            return true;
                        }
                    }
                    return false;
                }
            });

            browserListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    File file = (File) parent.getItemAtPosition(position);

                    if(file == null) {
                        return;
                    }

                    if (file.isDirectory()) {
                        changeDirectory(file);
                    } else if (file.exists()) {
                        setResultAndFinish(file);
                    }
                }
            });

            (view.findViewById(R.id.browser_back)).setOnClickListener( new TextView.OnClickListener() {
                public void onClick(View view){
                    browserBack(currentDirectory);
                }
            });

            changeDirectory(currentDirectory);
        } else {
            //ToDo: Dialog warning no SD.
        }

        return view;
    }

	private void changeDirectory(File directory) {
        getActivity().setTitle(Objects.equals(directory.getName(), "0") ? getResources().getString(R.string.file) : directory.getName());
		preferencesController.savePreference(Constants.COMICS_PATH_KEY, directory.getAbsolutePath());
		browserListView.setAdapter(new ListAdapter(directory));
	}

	public void fragmentOnKeyDown(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if(!browserBack(currentDirectory)){
                setResultAndFinish(null);
            }
        }
    }

	private void setResultAndFinish(File file) {
		Intent result = new Intent();
		result.putExtra(Constants.COMIC_PATH_KEY, file.getAbsolutePath());
//        parent.setResult(parent.RESULT_OK, result);
//        parent.finish();
	}

    private boolean browserBack(File file){
        if(file != null && file.getParent() != null){
            changeDirectory(file.getParentFile());
        }

        return false;
    }

	public class ListAdapter extends BaseAdapter {
		ArrayList<File> contents = new ArrayList<>();
		private boolean isEmpty;

		public ListAdapter(File current) {
            currentDirectory = current;
			filterContents();
		}

		private void filterContents() {
			String[] allContents = currentDirectory.list();
			contents = new ArrayList<>();
            isEmpty = true;

			if (allContents != null) {
				String path = currentDirectory.getPath();

				for (int i = 0; i < allContents.length; i++) {
					String contentName = allContents[i];

					if (contentName.indexOf(".") != 0) { // Exclude hidden files
						String extension = FileUtils.getFileExtension(contentName);
                        File contentFile = new File(path, contentName);

                        if (supportedExtensions.containsKey(extension.toLowerCase()) || contentFile.isDirectory())
                            contents.add(contentFile);
					}
				}
			}

            isEmpty = (contents.size() == 0);
		}

		public int getCount() {
			if (isEmpty)
				return contents.size() + 1;

            return contents.size();
		}

		public File getItem(int position) {
			if (position < contents.size())
				return contents.get(position);

            return null;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (isEmpty) {
				LayoutInflater inflater = getActivity().getLayoutInflater();
				return inflater.inflate(R.layout.sd_item_empty, null);
			}

			ViewHolder holder;
			File file = contents.get(position);
			String name = file.getName();
			String extension = FileUtils.getFileExtension(name);

			if (convertView == null || !(convertView.getTag() instanceof ViewHolder)) {
				convertView = mInflater.inflate(R.layout.sd_item, parent, false);
				holder = new ViewHolder();

				holder.icon = (ImageView) convertView.findViewById(R.id.sd_item_icon);
				holder.name = (TextView) convertView.findViewById(R.id.sd_item_name);
				holder.size = (TextView) convertView.findViewById(R.id.sd_item_size);

				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}

			if (supportedExtensions.containsKey(extension))
				holder.icon.setImageResource(supportedExtensions.get(extension));

			holder.name.setText(name);
			holder.size.setVisibility(View.GONE);

			if (!file.isDirectory()) {
				holder.size.setVisibility(View.VISIBLE);
				long size = file.length() / 1024;
				holder.size.setText(String.valueOf(size) + " KB");
			}

			return convertView;
		}

	}

	static class ViewHolder {
		ImageView icon;
		TextView name;
		TextView size;
	}
}
