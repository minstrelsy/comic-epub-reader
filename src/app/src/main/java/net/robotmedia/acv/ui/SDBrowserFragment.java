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
import android.content.Context;
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
    private LinearLayout emptyFolderLayout;
    private OnFragmentInteractionListener mListener;

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
        emptyFolderLayout = (LinearLayout) view.findViewById(R.id.empty_folder_layout);
        browserListView = (ListView) view.findViewById(R.id.list);

        String storageState = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(storageState)) {
            currentDirectory = new File(comicsPath);

            browserListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    File file = (File) parent.getItemAtPosition(position);

                    if(file == null) {
                        return;
                    }

                    if (file.isDirectory()) {
                        changeDirectory(file);
                    } else if (file.exists()) {
                        //ToDo: Display actions menu
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
        currentDirectory = directory;

        File[] validFiles = directory.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                String ext = FileUtils.getFileExtension(filename);
                return filename.indexOf(".") != 0 && (supportedExtensions.containsKey(ext.toLowerCase()) || dir.isDirectory());
            }
        });

        if(validFiles.length == 0) {
            browserListView.setVisibility(ListView.GONE);
            emptyFolderLayout.setVisibility(LinearLayout.VISIBLE);
            return;
        }

        browserListView.setVisibility(ListView.VISIBLE);
        emptyFolderLayout.setVisibility(LinearLayout.GONE);

		browserListView.setAdapter(new ListAdapter(validFiles));
	}

	public void fragmentOnBackPressed() {
        if(!browserBack(currentDirectory)){
            setResultAndFinish(null);
        }
    }

	private void setResultAndFinish(ArrayList<File> files) {
        if(files != null) {
            mListener.onFilesSelected(files);
            return;
        }

        mListener.finishThisFragment(this);
	}

    private boolean browserBack(File file){
        if(file != null && file.getParent() != null){
            changeDirectory(file.getParentFile());
            return true;
        }

        return false;
    }

	public class ListAdapter extends BaseAdapter {
		File[] files;

		public ListAdapter(File[] files) {
            this.files = files;
		}

		public int getCount() {
            return files.length;
		}

		public File getItem(int position) {
			if (position < files.length)
				return files[position];

            return null;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			File file = files[position];
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     */
    public interface OnFragmentInteractionListener {
        void onFilesSelected(ArrayList<File> selectedFiles);
        void finishThisFragment(Fragment fragment);
    }
}
