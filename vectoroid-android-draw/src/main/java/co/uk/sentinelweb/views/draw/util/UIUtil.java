package co.uk.sentinelweb.views.draw.util;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class UIUtil {
	
	public static void hideKeyboard(Activity act,EditText t){
		t.clearFocus();
		InputMethodManager imm = (InputMethodManager)act.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(t.getWindowToken(), 0);
	}
	
	public static void showKeyboard(Activity act,EditText t){
		t.requestFocus();
		InputMethodManager imm = (InputMethodManager)act.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(t, 0);
	}
	
	
}
