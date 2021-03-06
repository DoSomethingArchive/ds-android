package org.dosomething.android.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
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
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.dosomething.android.DSConstants;
import org.dosomething.android.FadeInResizeBitmapDisplayer;
import org.dosomething.android.R;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.tasks.AbstractWebserviceTask;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.SFGData;
import org.dosomething.android.transfer.SFGGalleryItem;
import org.dosomething.android.transfer.WebFormSelectOptions;
import org.dosomething.android.widget.ProgressBarImageLoadingListener;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.InjectView;

/**
 * SFG = Share For Good
 * Main gallery page for Share for Good campaign types. Displays list of the 
 * sharable gallery items.
 */
public class SFGGallery extends AbstractActivity implements OnScrollListener {
	
	private final float LOAD_MORE_PERCENTAGE = 0.8f;
	
	@Inject private LayoutInflater inflater;
	@Inject private ImageLoader imageLoader;
	@Inject private UserContext userContext;
	@Inject @Named("ProximaNova-Bold")Typeface typefaceBold;

	@InjectView(R.id.list) private PullToRefreshListView pullToRefreshView;
	@InjectView(R.id.filters) private LinearLayout filtersView;
	@InjectView(R.id.type_filter) private Spinner typeFilterSpinner;
	@InjectView(R.id.location_filter) private Spinner locFilterSpinner;
	
	private Campaign campaign;
	private ArrayList<SFGGalleryItem> galleryItems;
	private ListView list;
	private SFGListAdapter listAdapter;
	private String lastTypeFilter;
	private String lastLocationFilter;
	private int lastIdQuery;
	private boolean webTaskInProgress;

	@Override
	protected String getPageName() {
		return "SFGGallery";
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sfggallery);
		
		// Instantiating vars
		lastIdQuery = -1;
		galleryItems = new ArrayList<SFGGalleryItem>();
		listAdapter = new SFGListAdapter(galleryItems);
		list = pullToRefreshView.getRefreshableView();
		list.setAdapter(listAdapter);
		
		// Refresh listener for the pull-to-refresh gesture
		pullToRefreshView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				fetchItems(0);
			}
		});
		pullToRefreshView.setOnScrollListener(this);
		
		// Set Action Bar title
		campaign = (Campaign) getIntent().getExtras().get(DSConstants.EXTRAS_KEY.CAMPAIGN.getValue());
		Boolean objShowSubs = (Boolean)getIntent().getExtras().get(DSConstants.EXTRAS_KEY.SHOW_SUBMISSIONS.getValue());

        /**
         * TODO: implement new ActionBar next time a Share for Good campaign comes around.
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
		subMenuView.addMenuItem(this, getString(R.string.campaign_sfg_submit_pet), SFGSubmit.getIntent(this, campaign, userContext));
		 */
		
		// Setup spinners with filter options pulled from campaign data
		typeFilterSpinner = (Spinner)findViewById(R.id.type_filter);
		SFGData sfgData = campaign.getSFGData();
		ArrayList<WebFormSelectOptions> typeOptions = sfgData.getTypeOptions();
		List<String> types = new ArrayList<String>();
		for (int i = 0; i < typeOptions.size(); i++) {
			WebFormSelectOptions options = typeOptions.get(i);
			types.add(options.getLabel());
		}
		typeFilterSpinner.setAdapter(new FilterAdapter(SFGGallery.this, types));
		typeFilterSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// The "featured" filter can't have a location applied to it. Server API does not support it.
				WebFormSelectOptions typeOpt = campaign.getSFGData().getTypeOptions().get(position);
				String typeFilter = typeOpt.getValue();
				if (typeFilter.equals("featured")) {
					// Set location option to default
					locFilterSpinner.setSelection(0);
					// Disable location spinner
					locFilterSpinner.setEnabled(false);
				}
				else {
					locFilterSpinner.setEnabled(true);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
			
		});
		
		locFilterSpinner = (Spinner)findViewById(R.id.location_filter);
		ArrayList<WebFormSelectOptions> locOptions = sfgData.getLocationOptions();
		List<String> locations = new ArrayList<String>();
		for (int i = 0; i < locOptions.size(); i++) {
			WebFormSelectOptions options = locOptions.get(i);
			locations.add(options.getLabel());
		}
		locFilterSpinner.setAdapter(new FilterAdapter(SFGGallery.this, locations));
		
		// Style filter button's typeface and setup click listener 
		Button filterSubmit = (Button)findViewById(R.id.filter_execute);
		filterSubmit.setTypeface(typefaceBold, Typeface.BOLD);
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
		
		Boolean objShowSubs = (Boolean)getIntent().getExtras().get(DSConstants.EXTRAS_KEY.SHOW_SUBMISSIONS.getValue());
		if (objShowSubs != null && objShowSubs.booleanValue()) {
			filtersView.setVisibility(View.GONE);
		}
		
		lastTypeFilter = "";
		lastLocationFilter = "";
		
		// Conducts again the last query done. TODO: This is likely bad programming, because 
		// when going back to the main gallery from an individual post, we're sorta relying
		// on the query to be the same so that a new query is NOT executed.
		if (lastIdQuery >= 0) {
			fetchItems(lastIdQuery);
		}
		else {
			fetchItems(0);
		}
	}
	
	/**
	 * Triggered on scroll events. Determines when to load more items for infinite
	 * load scrolling.
	 */
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// Scroll past a certain %, then load more items
		boolean loadMore = ((float)(firstVisibleItem + visibleItemCount) / totalItemCount) > LOAD_MORE_PERCENTAGE;
		if (loadMore && galleryItems.size() > 0) {
			SFGGalleryItem item = galleryItems.get(galleryItems.size() - 1);
			int lastId = item.getId();
			fetchItems(lastId);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {}
	
	private void fetchItems(int startId) {
		if (campaign != null && !webTaskInProgress && lastIdQuery != startId) {
			lastIdQuery = startId;
			
			boolean showSubs = false;
			Boolean objShowSubs = (Boolean)getIntent().getExtras().get(DSConstants.EXTRAS_KEY.SHOW_SUBMISSIONS.getValue());
			if (objShowSubs != null && objShowSubs.booleanValue()) {
				showSubs = objShowSubs.booleanValue();
			}
			
			if (showSubs) {
				new SFGGalleryWebserviceTask(campaign.getSFGData().getGalleryUrl(), campaign.getSFGData().getMySubmissionsEndpoint(), startId).execute();
			}
			else {
				new SFGGalleryWebserviceTask(campaign.getSFGData().getGalleryUrl(), lastTypeFilter, lastLocationFilter, startId).execute();
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
			
			// When executing a filtered search, clear out old results
			galleryItems.clear();
			lastIdQuery = -1;
			fetchItems(0);
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
			setTypeface(v);
		    
		    return v;
		}
	
		@Override
	    public View getDropDownView(int position, View convertView, ViewGroup parent) {
			View v = super.getDropDownView(position, convertView, parent);
		    setTypeface(v);
		    
			return v;
		}
		
		private void setTypeface(View v) {
			TextView textView = (TextView)v.findViewById(android.R.id.text1);
		    textView.setTypeface(typefaceBold, Typeface.BOLD);
		    textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.f);
		}
	}
	
	/**
	 * Task to retrieve list data
	 */
	private class SFGGalleryWebserviceTask extends AbstractWebserviceTask {
		private String url;
		private boolean isNewSearch;
		
		public SFGGalleryWebserviceTask(String _url, String _endpointOverride, int _startId) {
			super(userContext);

            String apiKey = SFGGallery.this.getString(R.string.createandshare_api_key);
			this.url = _url + _endpointOverride + ".json?key=" + apiKey + "&userid=" + userContext.getUserUid();
			
			if (_startId > 0) {
				this.url += "&last=" + _startId;
				isNewSearch = false;
			}
			else {
				galleryItems.clear();
				isNewSearch = true;
			}

			webTaskInProgress = true;
		}
		
		public SFGGalleryWebserviceTask(String _url, String _typeOpt, String _locOpt, int _startId) {
			super(userContext);
			
			String options = "";
			if (_typeOpt != null && _typeOpt.length() > 0) {
				options = _typeOpt;
			}
			
			if (_locOpt != null && _locOpt.length() > 0) {
				if (options.length() == 0)
					options = _locOpt;
				else
					options += "-" + _locOpt;
			}
			
			if (options.length() == 0) {
				options = campaign.getSFGData().getDefaultEndpoint();
			}

            String apiKey = SFGGallery.this.getString(R.string.createandshare_api_key);
			this.url = _url + options + ".json?key=" + apiKey;
			
			if (_startId > 0) {
				this.url += "&last="+_startId;
				isNewSearch = false;
			}
			else {
				galleryItems.clear();
				isNewSearch = true;
			}
			
			webTaskInProgress = true;
		}
		
		@Override
		protected void onPreExecute() {}

		@Override
		protected void onSuccess() {
			if (galleryItems != null && galleryItems.size() > 0) {
				listAdapter.notifyDataSetChanged();
				
				// On new searches, return the view to the top of the list
				if (isNewSearch) {
					list.setSelection(0);
				}
				
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
			webTaskInProgress = false;
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
				name.setTypeface(typefaceBold, Typeface.BOLD);
				name.setText(item.getName());
			}
			
			if (image != null && progressBar != null) {
				String imageUrl = campaign.getSFGData().getGalleryUrl() + item.getImageURL();
				DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
					.displayer(new FadeInResizeBitmapDisplayer(DSConstants.IMAGE_LOADER_FADE_IN_TIME))
					.build();
				imageLoader.displayImage(imageUrl, image, imageOptions, new ProgressBarImageLoadingListener(progressBar));
				image.setAdjustViewBounds(true);
			}
			
			if (shareCount != null) {
				shareCount.setTypeface(typefaceBold, Typeface.BOLD);
				shareCount.setText("Share Count: "+item.getShareCount());
			}
			
			if (shelter != null) {
				shelter.setTypeface(typefaceBold, Typeface.BOLD);
				shelter.setText(item.getShelter());
			}
			
			if (state != null) {
				state.setTypeface(typefaceBold, Typeface.BOLD);
				state.setText(item.getState());
			}
			
			return v;
		}
	}
}
