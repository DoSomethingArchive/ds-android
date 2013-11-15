package org.dosomething.android.transfer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.dosomething.android.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Data for the gallery in the campaign Do It page.
 */
public class CampaignGalleryData implements ICampaignSectionData, Serializable {

    private static final long serialVersionUID = 1L;

    public static String TYPE_ID = "GalleryImages";

    // URLs for the thumbnail images
    private String mThumbnailUrl1;
    private String mThumbnailUrl2;
    private String mThumbnailUrl3;

    // URLs for full-size images
    private String mImageUrl1;
    private String mImageUrl2;
    private String mImageUrl3;

    public CampaignGalleryData(JSONObject json) {
        mThumbnailUrl1 = json.optString("thumbnail-url-1");
        mThumbnailUrl2 = json.optString("thumbnail-url-2");
        mThumbnailUrl3 = json.optString("thumbnail-url-3");
        mImageUrl1 = json.optString("image-url-1");
        mImageUrl2 = json.optString("image-url-2");
        mImageUrl3 = json.optString("image-url-3");
    }

    /**
     * Add the contents of this object to a ViewGroup container to be displayed.
     *
     * @param context Current context
     * @param parent ViewGroup of the container parent to add this section to
     */
    public void addToView(Context context, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.campaign_gallery_row, parent, false);

        loadImage((ImageView)v.findViewById(R.id.image1), mThumbnailUrl1);
        loadImage((ImageView)v.findViewById(R.id.image2), mThumbnailUrl2);
        loadImage((ImageView)v.findViewById(R.id.image3), mThumbnailUrl3);

        // Add inflated view to the parent
        parent.addView(v);
    }

    /**
     * Return the contents of this datastore as a JSONObject.
     *
     * @return JSONObject representation of this data
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("thumbnail-url-1", mThumbnailUrl1);
        json.put("thumbnail-url-2", mThumbnailUrl2);
        json.put("thumbnail-url-3", mThumbnailUrl3);
        json.put("image-url-1", mImageUrl1);
        json.put("image-url-2", mImageUrl2);
        json.put("image-url-3", mImageUrl3);

        return json;
    }

    /**
     * Load image into a View.
     *
     * @param imageView ImageView to load image into
     * @param url URL of image
     */
    private void loadImage(ImageView imageView, String url) {
        if (url != null && url.length() > 0) {
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(url, imageView);
        }
    }
}
