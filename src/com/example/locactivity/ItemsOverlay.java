package com.example.locactivity;

/*Class: ItemsOverlay
 * A base class for an Overlay which consists of a list of OverlayItems. This handles drawing a marker for each point, 
 *and maintaining a focused item. It also matches screen-taps to items, and dispatches Focus-change events to an optional listener.
 */
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class ItemsOverlay extends ItemizedOverlay{
	
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	
	Context mContext;
	
	public ItemsOverlay(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		mContext = context;	
		
	}


	public ItemsOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		// TODO Auto-generated constructor stub
	}

	@Override
	protected OverlayItem createItem(int i) {
		// TODO Auto-generated method stub
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return mOverlays.size();
	}
	
	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}
	
	@Override
	protected boolean onTap(int index) {
	  OverlayItem item = mOverlays.get(index);
	  AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
	  dialog.setTitle(item.getTitle());
	  dialog.setMessage(item.getSnippet());
	  dialog.show();
	  return true;
	}

}
