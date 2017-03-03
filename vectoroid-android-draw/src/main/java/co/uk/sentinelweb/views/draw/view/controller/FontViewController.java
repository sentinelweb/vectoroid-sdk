package co.uk.sentinelweb.views.draw.view.controller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import co.uk.sentinelweb.commonlibs.errorwrap.OnEWClickListener;
import co.uk.sentinelweb.views.draw.R;
import co.uk.sentinelweb.views.draw.controller.FontController;
import co.uk.sentinelweb.views.draw.controller.FontController.Font;
import co.uk.sentinelweb.views.draw.file.FileRepository;
import co.uk.sentinelweb.views.draw.model.internal.ModeData.Mode;
import co.uk.sentinelweb.views.draw.view.DrawView;

public class FontViewController {
	DrawView _drawing;
	Dialog _currentDialog=null;
	ViewGroup _currentDialogView=null;
	public FontViewController(DrawView d) {
		super();
		this._drawing = d;
	}
	public Dialog getFontsDialog(Context c, OnClickListener setClick) {
		if (setClick==null) {
			setClick = new OnEWClickListener( c ) {
				
				@Override
				public void onEWClick(View v) {
					FontController fc = FileRepository.getFileRepository(null).getFontController();//c,
					int which =(Integer) v.getTag();
					String fontName = fc._ttFontsOrder.get(which);
					Typeface t = fc.getTTFont(fontName);
					_drawing.setCurrentFont(fontName, t);
					 closeDialog();
				}
			};
		}
		if (_currentDialog==null) {
			FontController fc = FileRepository.getFileRepository(null).getFontController();//c,
			fc.scanFonts(c);
			ScrollView hsv = new ScrollView(c);
			LinearLayout ll = new LinearLayout(c);
			ll.setOrientation(LinearLayout.VERTICAL);
			for (int i=0;i<fc._ttFontsOrder.size();i++) {
				String name = fc._ttFontsOrder.get(i);
				//Font f = fc._ttfontFiles.get(name);
				ImageView iv = new ImageView(c);
				iv.setImageBitmap(BitmapFactory.decodeFile(fc.getFontPreviewFile(name).getAbsolutePath()));
				iv.setTag(i);
				iv.setAdjustViewBounds(true);
				iv.setOnClickListener(setClick);
				LinearLayout.LayoutParams llp=new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
				llp.setMargins(1,1,1,1);
				ll.addView(iv,llp);
			}
			LinearLayout.LayoutParams llp=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
			llp.setMargins(1,1,1,1);
			hsv.addView(ll,llp);
			_currentDialogView = ll;
			_currentDialog=new AlertDialog.Builder(c)
				.setTitle(R.string.dialog_title_select_font)
				.setIcon(R.drawable.i_text)
				.setView(hsv).create();
		} else {
			for (int i=0;i<_currentDialogView.getChildCount();i++) {
				_currentDialogView.getChildAt(i).setOnClickListener(setClick);
			}
		}
		return _currentDialog;
	}
	
	public void closeDialog() {
		if (_currentDialog!=null && _currentDialog.isShowing()) {
			_currentDialog.dismiss();
		}
		//_currentDialogView=null;
		//_currentDialog=null;
		
	}
}
