package org.dosomething.android.transfer;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.dosomething.android.DSConstants;
import org.dosomething.android.FadeInResizeBitmapDisplayer;
import org.dosomething.android.MyModule;
import org.dosomething.android.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Store campaign data and offer method to display it to the screen.
 * Layout of the data is Header, Text body, then Image.
 */
public class CampaignTextImageData implements ICampaignSectionData, Serializable {

    private static final long serialVersionUID = 1L;

    public static String TYPE_ID = "TextImage";

    private String mBody;

    private String mHeader;

    private String mImageUrl;

    public CampaignTextImageData(JSONObject json) {
        mBody = json.optString("body");
        mHeader = json.optString("header");
        mImageUrl = json.optString("image");
    }

    /**
     * Add the contents of this object to a ViewGroup container to be displayed.
     *
     * @param context Current context
     * @param parent ViewGroup of the container parent to add this section to
     */
    public void addToView(Context context, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.hbi_row, parent, false);

        // Set the Header text, if any
        TextView tvHeader = (TextView)v.findViewById(R.id.header);
        if (mHeader != null && mHeader.length() > 0) {
            tvHeader.setText(mHeader);

            Typeface typeface = Typeface.create("ProximaNova-Bold", Typeface.BOLD);
            tvHeader.setTypeface(typeface);
        }
        else {
            tvHeader.setVisibility(View.GONE);
        }

        // Set the body text, if any
        TextView tvBody = (TextView)v.findViewById(R.id.body);
        if (mBody != null && mBody.length() > 0) {
            tvBody.setText(mBody);
        }
        else {
            tvBody.setVisibility(View.GONE);
        }

        // Set the image, if any
        ImageView ivImage = (ImageView)v.findViewById(R.id.image);
        if (mImageUrl != null && mImageUrl.length() > 0) {
            DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
                    .displayer(new FadeInResizeBitmapDisplayer(DSConstants.IMAGE_LOADER_FADE_IN_TIME))
                    .build();
            MyModule.ImageLoaderProvider imageLoaderProvider = new MyModule.ImageLoaderProvider(context);
            ImageLoader imageLoader = imageLoaderProvider.get();
            imageLoader.displayImage(mImageUrl, ivImage, imageOptions);
        }
        else {
            ivImage.setVisibility(View.GONE);
        }

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

        json.put("body", mBody);
        json.put("header", mHeader);
        json.put("image", mImageUrl);

        return json;
    }
}
