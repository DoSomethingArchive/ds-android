package org.dosomething.android.activities;

import java.util.ArrayList;
import java.util.List;

import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.tasks.AbstractWebserviceTask;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.SFGData;
import org.dosomething.android.transfer.SFGGalleryItem;
import org.dosomething.android.transfer.WebFormSelectOptions;
import org.dosomething.android.widget.ActionBarSubMenu;
import org.dosomething.android.widget.CustomActionBar;
import org.dosomething.android.widget.CustomActionBar.SubMenuAction;
import org.dosomething.android.widget.ProgressBarImageLoadingListener;
import org.json.JSONArray;
import org.json.JSONObject;

import roboguice.inject.InjectView;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * SFG = Share For Good
 * Main gallery page for Share for Good campaign types. Displays list of the 
 * sharable gallery items.
 */
public class SFGGallery extends AbstractActivity {
	
	@Inject private LayoutInflater inflater;
	@Inject private ImageLoader imageLoader;
	@Inject private UserContext userContext;
	@Inject @Named("DINComp-CondBold")Typeface dinTypeface;
	
	@InjectView(R.id.actionbar) private CustomActionBar actionBar;
	@InjectView(R.id.list) private PullToRefreshListView pullToRefreshView;
	@InjectView(R.id.filters) private LinearLayout filtersView;
	
	private Campaign campaign;
	private ListView list;
	private String lastTypeFilter;
	private String lastLocationFilter;

	@Override
	protected String getPageName() {
		return "SFGGallery";
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sfggallery);
		
		list = pullToRefreshView.getRefreshableView();
		pullToRefreshView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				fetchItems(false);
			}
		});
		
		// Set Action Bar title
		campaign = (Campaign) getIntent().getExtras().get(DSConstants.EXTRAS_KEY.CAMPAIGN.getValue());
		Boolean objShowSubs = (Boolean)getIntent().getExtras().get(DSConstants.EXTRAS_KEY.SHOW_SUBMISSIONS.getValue());
		if (objShowSubs != null && objShowSubs.booleanValue()) {
			actionBar.setTitle(getString(R.string.campaign_sfg_my_submissions));
		}
		else {
			if (campaign != null) {
				actionBar.setTitle(campaign.getName());
			}
		}
		
		// Setup Custom Action Bar's sub
		SubMenuAction subMenuAction = actionBar.addSubMenuAction(this);
		ActionBarSubMenu subMenuView = subMenuAction.getSubMenuView();
		subMenuView.addMenuItem(this, getString(R.string.campaign_gallery), SFGGallery.getIntent(this, campaign));
		subMenuView.addMenuItem(this, getString(R.string.campaign_sfg_my_pets), SFGGallery.getIntent(this, campaign, true));
		subMenuView.addMenuItem(this, getString(R.string.campaign_sfg_submit_pet), SFGSubmit.getIntent(this, campaign));
		
		// Setup spinners with filter options pulled from campaign data
		Spinner typeFilterSpinner = (Spinner)findViewById(R.id.type_filter);
		SFGData sfgData = campaign.getSFGData();
		ArrayList<WebFormSelectOptions> typeOptions = sfgData.getTypeOptions();
		List<String> types = new ArrayList<String>();
		for (int i = 0; i < typeOptions.size(); i++) {
			WebFormSelectOptions options = typeOptions.get(i);
			types.add(options.getLabel());
		}
		typeFilterSpinner.setAdapter(new FilterAdapter(SFGGallery.this, types));
		
		Spinner locFilterSpinner = (Spinner)findViewById(R.id.location_filter);
		ArrayList<WebFormSelectOptions> locOptions = sfgData.getLocationOptions();
		List<String> locations = new ArrayList<String>();
		for (int i = 0; i < locOptions.size(); i++) {
			WebFormSelectOptions options = locOptions.get(i);
			locations.add(options.getLabel());
		}
		locFilterSpinner.setAdapter(new FilterAdapter(SFGGallery.this, locations));
		
		// Style filter button's typeface and setup click listener 
		Button filterSubmit = (Button)findViewById(R.id.filter_execute);
		filterSubmit.setTypeface(dinTypeface, Typeface.BOLD);
		filterSubmit.setOnClickListener(new OnFilterClickListener());
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// Display toast message if provided one
		String toastMsg = (String)getIntent().getExtras().get(DSConstants.EXTRAS_KEY.TOAST_MSG.getValue());
		if (toastMsg != null && toastMsg.length() > 0) {
			Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
		}
		
		boolean showSubs = false;
		Boolean objShowSubs = (Boolean)getIntent().getExtras().get(DSConstants.EXTRAS_KEY.SHOW_SUBMISSIONS.getValue());
		if (objShowSubs != null && objShowSubs.booleanValue()) {
			filtersView.setVisibility(View.GONE);
			showSubs = objShowSubs.booleanValue();
		}
		
		lastTypeFilter = "";
		lastLocationFilter = "";
		fetchItems(showSubs);
	}
	
	private void fetchItems(boolean showMySubmissions) {
		// Clear any items that might currently be in the list
		list.setAdapter(null);
		
		if (campaign != null) {
			if (showMySubmissions) {
				new SFGGalleryWebserviceTask(campaign.getSFGData().getGalleryUrl(), campaign.getSFGData().getMySubmissionsEndpoint()).execute();
			}
			else {
				new SFGGalleryWebserviceTask(campaign.getSFGData().getGalleryUrl(), lastTypeFilter, lastLocationFilter).execute();
			}
		}
	}
	
	/**
	 * Click listener to execute gallery filter
	 */
	private class OnFilterClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			Spinner typeFilterSpinner = (Spinner)findViewById(R.id.type_filter);
			int typeIndex = typeFilterSpinner.getSelectedItemPosition();
			WebFormSelectOptions typeOpt = campaign.getSFGData().getTypeOptions().get(typeIndex);
			lastTypeFilter = typeOpt.getValue();
			
			Spinner locFilterSpinner = (Spinner)findViewById(R.id.location_filter);
			int locIndex = locFilterSpinner.getSelectedItemPosition();
			WebFormSelectOptions locOpt = campaign.getSFGData().getLocationOptions().get(locIndex);
			lastLocationFilter = locOpt.getValue();
			
			fetchItems(false);
		}
	}
	
	public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign) {
		Intent answer = new Intent(context, SFGGallery.class);
		answer.putExtra(DSConstants.EXTRAS_KEY.CAMPAIGN.getValue(), campaign);
		answer.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return answer;
	}
	
	public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign, String toastMsg) {
		Intent answer = new Intent(context, SFGGallery.class);
		answer.putExtra(DSConstants.EXTRAS_KEY.CAMPAIGN.getValue(), campaign);
		answer.putExtra(DSConstants.EXTRAS_KEY.TOAST_MSG.getValue(), toastMsg);
		answer.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return answer;
	}
	
	public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign, boolean showSubmissions) {
		Intent answer = new Intent(context, SFGGallery.class);
		answer.putExtra(DSConstants.EXTRAS_KEY.CAMPAIGN.getValue(), campaign);
		answer.putExtra(DSConstants.EXTRAS_KEY.SHOW_SUBMISSIONS.getValue(), Boolean.valueOf(showSubmissions));
		answer.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return answer;
	}
	
	/**
	 * Custom adapter for filter options. Allows us to customize the style of the
	 * list of options
	 */
	private class FilterAdapter extends ArrayAdapter<String> {
		public FilterAdapter(Context context, List<String> items) {
		    super(context, android.R.layout.simple_dropdown_item_1line, items);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);
			setDinTypeface(v);
		    
		    return v;
		}
	
		@Override
	    public View getDropDownView(int position, View convertView, ViewGroup parent) {
			View v = super.getDropDownView(position, convertView, parent);
		    setDinTypeface(v);
		    
			return v;
		}
		
		private void setDinTypeface(View v) {
			TextView textView = (TextView)v.findViewById(android.R.id.text1);
		    textView.setTypeface(dinTypeface, Typeface.BOLD);
		    textView.setTextSize(16);
		}
	}
	
	/**
	 * Task to retrieve list data
	 */
	private class SFGGalleryWebserviceTask extends AbstractWebserviceTask {
		private String url;
		private List<SFGGalleryItem> galleryItems;
		
		public SFGGalleryWebserviceTask(String _url) {
			super(userContext);
			
			this.url = _url + campaign.getSFGData().getDefaultEndpoint() + ".json?key=" + DSConstants.PICS_API_KEY;
			this.galleryItems = new ArrayList<SFGGalleryItem>();
		}
		
		public SFGGalleryWebserviceTask(String _url, String endpointOverride) {
			super(userContext);
			
			this.url = _url + endpointOverride + ".json?key=" + DSConstants.PICS_API_KEY + "&userid=" + userContext.getUserUid();
			this.galleryItems = new ArrayList<SFGGalleryItem>();
		}
		
		public SFGGalleryWebserviceTask(String _url, String typeOpt, String locOpt) {
			super(userContext);
			
			this.galleryItems = new ArrayList<SFGGalleryItem>();
			
			String options = "";
			if (typeOpt != null && typeOpt.length() > 0) {
				options = typeOpt;
			}
			
			if (locOpt != null && locOpt.length() > 0) {
				if (options.length() == 0)
					options = locOpt;
				else
					options += "-" + locOpt;
			}
			
			if (options.length() == 0) {
				options = "posts";
			}
			
			this.url = _url + options + ".json?key=" + DSConstants.PICS_API_KEY;
		}
		
		@Override
		protected void onPreExecute() {
			actionBar.setProgressBarVisibility(ProgressBar.VISIBLE);
		}

		@Override
		protected void onSuccess() {
			if (galleryItems != null && galleryItems.size() > 0) {
				SFGListAdapter adapter = new SFGListAdapter(galleryItems);
				list.setAdapter(adapter);
				list.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> av, View v, int position, long id) {
						SFGGalleryItem galleryItem = (SFGGalleryItem)list.getAdapter().getItem(position);
						startActivity(SFGItem.getIntent(getApplicationContext(), galleryItem, campaign));
					}
				});
			}
			else {
				// No results were found
				new AlertDialog.Builder(SFGGallery.this)
					.setMessage(getString(R.string.campaign_sfg_no_results))
					.setCancelable(false)
					.setPositiveButton(getString(R.string.ok_upper), null)
					.create()
					.show();
			}
		}

		@Override
		protected void onFinish() {
			actionBar.setProgressBarVisibility(ProgressBar.GONE);
			pullToRefreshView.onRefreshComplete();
		}

		@Override
		protected void onError(Exception e) {
			new AlertDialog.Builder(SFGGallery.this)
				.setMessage(getString(R.string.campaign_sfg_update_error))
				.setCancelable(false)
				.setPositiveButton(getString(R.string.ok_upper), null)
				.create()
				.show();
		}

		@Override
		protected void doWebOperation() throws Exception {
			WebserviceResponse response = this.doGet(this.url);
			if (response.getStatusCode() < 400) {
				JSONArray jsonItems = response.getBodyAsJSONArray();
				for (int i = 0; i < jsonItems.length(); i++) {
					JSONObject item = jsonItems.getJSONObject(i);
					galleryItems.add(new SFGGalleryItem(item));
				}
			}
		}
	}

	/**
	 * ListAdapter to handle SFG gallery functionality
	 */
	private class SFGListAdapter extends ArrayAdapter<SFGGalleryItem> {
		public SFGListAdapter(List<SFGGalleryItem> items) {
			super(SFGGallery.this, android.R.layout.simple_expandable_list_item_1, items);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				v = inflater.inflate(R.layout.sfggallery_row, null);
			}
			
			SFGGalleryItem item = getItem(position);
			
			ProgressBar progressBar = (ProgressBar)v.findViewById(R.id.progressBar);
			TextView name = (TextView)v.findViewById(R.id.name);
			ImageView image = (ImageView)v.findViewById(R.id.image);
			TextView shareCount = (TextView)v.findViewById(R.id.share_count);
			TextView shelter = (TextView)v.findViewById(R.id.shelter);
			TextView state = (TextView)v.findViewById(R.id.state);
			
			if (name != null) {
				name.setTypeface(dinTypeface, Typeface.BOLD); 
				name.setText(item.getName());
			}
			
			if (image != null && progressBar != null) {
				String imageUrl = campaign.getSFGData().getGalleryUrl() + item.getImageURL();
				imageLoader.displayImage(imageUrl, image, new ProgressBarImageLoadingListener(progressBar));
			}
			
			if (shareCount != null) {
				shareCount.setTypeface(dinTypeface, Typeface.BOLD);
				shareCount.setText("Share Count: "+item.getShareCount());
			}
			
			if (shelter != null) {
				shelter.setTypeface(dinTypeface, Typeface.BOLD);
				shelter.setText(item.getShelter());
			}
			
			if (state != null) {
				state.setTypeface(dinTypeface, Typeface.BOLD);
				state.setText(item.getState());
			}
			
			return v;
		}
	}
	
	
}
