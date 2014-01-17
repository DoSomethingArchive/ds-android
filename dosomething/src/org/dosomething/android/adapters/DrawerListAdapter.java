package org.dosomething.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.dosomething.android.R;

import java.util.List;

/**
 * Custom adapter for the navigation drawer.
 */
public class DrawerListAdapter extends ArrayAdapter<String> {

    private LayoutInflater mInflater;
    private Context mContext;
    private List<String> mMenuItems;

    public DrawerListAdapter(Context context, List<String> menuItems) {
        super(context, android.R.layout.simple_list_item_1, menuItems);

        mInflater = LayoutInflater.from(context);
        mContext = context;
        mMenuItems = menuItems;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        if (v == null) {
            v = mInflater.inflate(R.layout.drawer_list_row, null);
        }

        String itemTitle = mMenuItems.get(position);
        TextView textView = (TextView)v.findViewById(R.id.drawer_item_text);
        textView.setText(itemTitle);

        ImageView imageView = (ImageView)v.findViewById(R.id.drawer_item_image);
        if (itemTitle.equals(mContext.getString(R.string.drawer_item_campaigns))) {
            imageView.setImageResource(R.drawable.action_bar_home);
        }
        else if (itemTitle.equals(mContext.getString(R.string.drawer_item_reminders))) {
            imageView.setImageResource(R.drawable.action_bar_reminder);
        }
        else if (itemTitle.equals(mContext.getString(R.string.drawer_item_profile))) {
            imageView.setImageResource(R.drawable.action_bar_profile);
        }
        else if (itemTitle.equals(mContext.getString(R.string.drawer_item_settings))) {
            imageView.setImageResource(R.drawable.action_bar_config);
        }
        else if (itemTitle.equals(mContext.getString(R.string.drawer_item_login))) {
            imageView.setImageResource(R.drawable.action_bar_login);
        }
        else if (itemTitle.equals(mContext.getString(R.string.drawer_item_logout))) {
            imageView.setImageResource(R.drawable.action_bar_logout);
        }

        return v;
    }
}
