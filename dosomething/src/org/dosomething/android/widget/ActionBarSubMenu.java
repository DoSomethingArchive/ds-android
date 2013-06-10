package org.dosomething.android.widget;

import org.dosomething.android.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ActionBarSubMenu extends LinearLayout {
	
	@Inject @Named("DINComp-CondBold")Typeface dinTypeface;

	@Inject
	public ActionBarSubMenu(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.setOrientation(LinearLayout.VERTICAL);
	}

	public void addMenuItem(Context ctx, String label, Intent intent) {
		// Add TextView to this layout
		TextView menuItem = (TextView)LayoutInflater.from(ctx).inflate(R.layout.actionbar_submenu_item, null);
		menuItem.setText(label);
		menuItem.setTypeface(dinTypeface, Typeface.BOLD);
		menuItem.setOnClickListener(new MenuItemClickListener(ctx, intent));
		
		addView(menuItem);
	}
	
	public void toggleMenu() {
		SubMenuAnimation anim;
		if (getVisibility() == View.VISIBLE) {
			anim = new SubMenuAnimation(this, false);
		}
		else {
			anim = new SubMenuAnimation(this, true);
		}
		
		this.clearAnimation();
		this.setAnimation(anim);
		anim.setAnimationListener(anim);
		anim.start();
	}

	public class MenuItemClickListener implements OnClickListener {
		private Context mContext;
		private Intent mIntent;
		
		public MenuItemClickListener(Context ctx, Intent intent) {
			super();
			mContext = ctx;
			mIntent = intent;
		}
		
		public void onClick(View v) {
			Log.v("ACTION", "clicked: "+((TextView)v).getText());
			if (mIntent != null) {
				toggleMenu();
				mIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				mContext.startActivity(mIntent);
			}
		}
	}
	
	private class SubMenuAnimation extends Animation implements AnimationListener {
		private ActionBarSubMenu subMenuView;
		private int fullHeight;
		private final long duration = 200; // in milliseconds
		private boolean isOpenAnim;
		
		public SubMenuAnimation(ActionBarSubMenu v, boolean openAnim) {
			subMenuView = v;
			isOpenAnim = openAnim;
			
			if (isOpenAnim) {
				int baseHeight = getResources().getDimensionPixelSize(R.dimen.actionbar_submenu_item_height);
				int padding = getResources().getDimensionPixelSize(R.dimen.actionbar_submenu_item_padding);
				fullHeight = v.getChildCount() * (baseHeight + padding);
				
				// Ensure height when opening starts at 0
				subMenuView.setVisibility(View.VISIBLE);
				subMenuView.getLayoutParams().height = 0;
				subMenuView.requestLayout();
			}
			else {
				fullHeight = subMenuView.getLayoutParams().height;
			}
			
			this.setDuration(duration);
			this.setInterpolator(new AccelerateInterpolator());
		}
		
		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			int newHeight;
			if (isOpenAnim) {
				newHeight = (int)(fullHeight * interpolatedTime);
			}
			else {
				newHeight = (int)(fullHeight * (1 - interpolatedTime));
			}
			
			subMenuView.getLayoutParams().height = newHeight;
			subMenuView.requestLayout();
		}
		
		@Override
		public boolean willChangeBounds() {
			return true;
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			// At the end of the close animation, change visibility to GONE
			if (!isOpenAnim) {
				subMenuView.setVisibility(View.GONE);
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationStart(Animation animation) {
		}
	}

}
