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
package net.nkbits.epubcomic.ui.settings.tablet;

import net.androidcomics.acv.R;
import net.nkbits.epubcomic.ui.settings.CollectionSettingsHelper;
import android.os.Bundle;

public class StorageSettingsFragment extends ExtendedPreferenceFragment {
	
	private CollectionSettingsHelper helper;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.storage_settings);
		
		this.helper = new CollectionSettingsHelper(this.getActivity());
		this.helper.prepareClearHistory(this.findPreference(CollectionSettingsHelper.PREFERENCE_CLEAR_HISTORY));
	}
	
}
