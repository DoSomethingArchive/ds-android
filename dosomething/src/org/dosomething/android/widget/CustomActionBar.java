package org.dosomething.android.widget;

import roboguice.RoboGuice;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class CustomActionBar extends com.markupartist.android.widget.ActionBar {
	
	@Inject @Named("DINComp-CondBold")Typeface titleTypeface;
	
	public CustomActionBar(Context context, AttributeSet attrs)  {
		super(context, attrs);
		RoboGuice.getInjector(context).injectMembers(this);
		
		getTitleView().setTypeface(titleTypeface);
		getTitleView().setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24.0f);
	}
	
}
