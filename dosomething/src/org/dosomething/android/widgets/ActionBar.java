package org.dosomething.android.widgets;

import org.dosomething.android.R;
import org.dosomething.android.activities.Profile;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ActionBar extends RelativeLayout {

	private TextView title;
	
	public ActionBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View v = inflater.inflate(R.layout.action_bar, null);

		title = (TextView) v.findViewById(R.id.title);
		
		addView(v);
		
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ActionBar);
        CharSequence title = a.getString(R.styleable.ActionBar_title);
        
        if (title != null) {
            setTitle(title);
        }
        
        Boolean showHomeButton = a.getBoolean(R.styleable.ActionBar_showHomeButton, false);
        if(showHomeButton){
        	ImageView homeLogo =  (ImageView) v.findViewById(R.id.home_logo);
    		homeLogo.setOnClickListener(homeClicked);
        	homeLogo.setVisibility(ImageView.VISIBLE);
        }
        
        a.recycle();
	}
	
	public void setTitle(CharSequence title){
		this.title.setText(title);
	}
	
	public void setTitle(int resId){
		this.title.setText(resId);
	}
	
	private final OnClickListener homeClicked = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Context context = getContext();
			context.startActivity(new Intent(context, Profile.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
		}
	};

}
