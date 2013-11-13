package org.dosomething.android.transfer;

import android.content.Context;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * ICampaignSectionData
 * Interface for a campaign info datastore.
 */
public interface ICampaignSectionData {

    /**
     * Add the contents of this object to a ViewGroup container to be displayed.
     *
     * @param context Current context
     * @param parent ViewGroup of the container parent to add this section to
     */
    public void addToView(Context context, ViewGroup parent);

    /**
     * Return the contents of this datastore as a JSONObject.
     *
     * @return JSONObject representation of this data
     */
    public JSONObject toJSON() throws JSONException;
}
