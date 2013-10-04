package org.dosomething.android.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;

import com.commonsware.cwac.endless.EndlessAdapter;
import com.google.inject.Inject;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.activities.GalleryImageItemDisplay;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.tasks.AbstractWebserviceTask;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.GalleryItem;
import org.dosomething.android.widget.SquareImageView;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gallery of images for a campaign.
 */
public class CampaignGalleryFragment extends AbstractCampaignFragment {

    private static final String CAMPAIGN = DSConstants.EXTRAS_KEY.CAMPAIGN.getValue();
    private static final String GALLERY_IMG_AUTHORS = "gallery-img-authors";
    private static final String GALLERY_NUM_ITEMS = "gallery-num-items";
    private static final String GALLERY_IMG_POS = "gallery-img-pos";
    private static final String GALLERY_IMG_URLS = "gallery-img-urls";

    @Inject LayoutInflater inflater;
    @Inject private ImageLoader imageLoader;
    @Inject private UserContext userContext;

    private GridView gridView;
    private Context context;
    private int imagePixels;
    private Campaign campaign;
    private String feedUrl;
    private GalleryFeedTask mGalleryFeedTask;

    @Override
    public String getFragmentName() {
        return "Gallery";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.campaign_gallery, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        campaign = (Campaign)args.getSerializable(CAMPAIGN);

        context = getActivity();

        gridView = (GridView)view.findViewById(R.id.gridview);
        gridView.setOnItemClickListener(listener);

        imagePixels = getResources().getDimensionPixelSize(R.dimen.gallery_item);

        feedUrl = campaign.getGallery().getFeed();

        mGalleryFeedTask = new GalleryFeedTask(feedUrl);
        mGalleryFeedTask.execute();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Cancel any retrieval task in progress and hide the progress bar
        if (mGalleryFeedTask != null) {
            mGalleryFeedTask.cancel(true);
            Activity activity = getActivity();
            if (activity != null)
                activity.setProgressBarIndeterminateVisibility(Boolean.FALSE);
        }
    }

    private AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            GalleryItem item = (GalleryItem) gridView.getAdapter().getItem(position);
            switch (item.getType()) {
                case IMAGE:
                    Intent intent = new Intent(context, GalleryImageItemDisplay.class);
                    intent.putExtra(CAMPAIGN, campaign);
                    intent.putExtra(GALLERY_IMG_POS, position);

                    DSEndlessGridAdapter gridAdapter = (DSEndlessGridAdapter)gridView.getAdapter();
                    DSGridAdapter mga = (DSGridAdapter)gridAdapter.getWrapped();
                    int numItems = mga.getCount();

                    String[] urls = new String[numItems];
                    String[] authors = new String[numItems];
                    for(int i = 0; i < numItems; i++) {
                        GalleryItem gItem = (GalleryItem) gridView.getAdapter().getItem(i);
                        urls[i] = gItem.getUrl();
                        authors[i] = gItem.getAuthor();
                    }

                    intent.putExtra(GALLERY_IMG_URLS, urls);
                    intent.putExtra(GALLERY_IMG_AUTHORS, authors);
                    intent.putExtra(GALLERY_NUM_ITEMS, numItems);

                    startActivity(intent);
                    break;
                case VIDEO:
                    Pattern pattern = Pattern.compile("<embed src=\"([^\"]+)\"");
                    Matcher matcher = pattern.matcher(item.getUrl());
                    if(matcher.find() && matcher.groupCount()==1) {
                        String url = matcher.group(1);
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    }
                    break;
                default:
                    throw new RuntimeException();
            }
        }
    };

    private class DSEndlessGridAdapter extends EndlessAdapter {

        private List<GalleryItem> items;
        private int page = 1;

        public DSEndlessGridAdapter(ListAdapter wrapped) {
            super(getActivity(), wrapped, R.layout.gallery_loading_item);
        }

        @Override
        protected boolean cacheInBackground() throws Exception {

            items = getPage(feedUrl, page);

            return !items.isEmpty();
        }

        @Override
        protected void appendCachedData() {

            page++;

            DSGridAdapter adapter = (DSGridAdapter) getWrappedAdapter();

            for(GalleryItem item : items) {
                adapter.add(item);
            }
        }

        protected DSGridAdapter getWrapped() {
            return (DSGridAdapter) getWrappedAdapter();
        }

    }

    private class DSGridAdapter extends ArrayAdapter<GalleryItem> {

        private DSGridAdapter(Context context, List<GalleryItem> items) {
            super(context, android.R.layout.simple_list_item_1, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SquareImageView answer = (SquareImageView)inflater.inflate(R.layout.gallery_square_image, parent, false);
            GalleryItem item = getItem(position);

            imageLoader.displayImage(item.getThumb(), answer);
            return answer;
        }
    }

    private static List<GalleryItem> getPage(String feedUrl, int page) throws Exception{

        List<GalleryItem> answer = new ArrayList<GalleryItem>();

        JSONObject obj = AbstractWebserviceTask.doGet(feedUrl + "?page=" + page, null).getBodyAsJSONObject();

        JSONArray imageItems = obj.optJSONArray("image_items");
        if(imageItems!=null) {
            for(int i=0; i<imageItems.length(); i++) {
                JSONObject imageItemWrapper = imageItems.getJSONObject(i);
                JSONObject imageItem = imageItemWrapper.getJSONObject("image_item");
                answer.add(new GalleryItem(GalleryItem.GalleryItemType.IMAGE, imageItem));
            }
        }

        JSONArray videoItems = obj.optJSONArray("video_items");
        if(videoItems!=null) {
            for(int i=0; i<videoItems.length(); i++) {
                JSONObject videoItemWrapper = videoItems.getJSONObject(i);
                JSONObject videoItem = videoItemWrapper.getJSONObject("video_item");
                answer.add(new GalleryItem(GalleryItem.GalleryItemType.VIDEO, videoItem));
            }
        }

        return answer;
    }

    private class GalleryFeedTask extends AbstractWebserviceTask {

        private String feedUrl;
        private List<GalleryItem> loadedItems;

        public GalleryFeedTask(String feedUrl) {
            super(userContext);
            this.feedUrl = feedUrl;
        }

        @Override
        protected void onPreExecute() {
            Activity activity = getActivity();
            if (activity != null)
                activity.setProgressBarIndeterminateVisibility(Boolean.TRUE);
        }

        @Override
        protected void onSuccess() {
            Activity activity = getActivity();
            if (activity != null) {
                DSEndlessGridAdapter adapter = new DSEndlessGridAdapter(new DSGridAdapter(activity, loadedItems));
                gridView.setAdapter(adapter);
            }
        }

        @Override
        protected void onFinish() {
            Activity activity = getActivity();
            if (activity != null)
                activity.setProgressBarIndeterminateVisibility(Boolean.FALSE);
        }

        @Override
        protected void onError(Exception e) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.gallery_feed_failed))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.ok_upper), null)
                    .create()
                    .show();
        }

        @Override
        protected void doWebOperation() throws Exception {
            loadedItems = getPage(feedUrl, 0);
        }
    }
}
