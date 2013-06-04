package org.dosomething.android.widget;

import org.dosomething.android.R;

import roboguice.RoboGuice;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class CustomActionBar extends com.markupartist.android.widget.ActionBar {
	
	@Inject @Named("DINComp-CondBold") Typeface titleTypeface;
	
	public CustomActionBar(Context context, AttributeSet attrs)  {
		super(context, attrs);
		RoboGuice.getInjector(context).injectMembers(this);
		
		getTitleView().setTypeface(titleTypeface);
		getTitleView().setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24.0f);
	}
	
	public SubMenuAction addSubMenuAction(Context ctx) {
		SubMenuAction subMenuAction = new SubMenuAction(ctx);
		this.addAction(subMenuAction);
		return subMenuAction;
	}
	
	public class SubMenuAction implements Action {
		private ActionBarSubMenu subMenuView;
		
		public SubMenuAction(Context ctx) {
			super();
			
			Activity a = (Activity)ctx;
			subMenuView = (ActionBarSubMenu)a.findViewById(R.id.actionbar_submenu);
		}
		
		@Override
		public int getDrawable() {
			return R.drawable.action_bar_menu;
		}
		
		@Override
		public void performAction(View v) {
			subMenuView.toggleMenu();
		}
		
		public ActionBarSubMenu getSubMenuView() {
			return subMenuView;
		}
	}
}
