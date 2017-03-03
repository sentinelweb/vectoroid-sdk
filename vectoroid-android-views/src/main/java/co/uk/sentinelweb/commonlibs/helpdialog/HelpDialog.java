/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.uk.sentinelweb.commonlibs.helpdialog;



import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import co.uk.sentinelweb.commonlibs.R;


public class HelpDialog extends Dialog  {
	private static final int MSG_TIME = 5000;
	private static final String MARKET_PREFIX="market://";
	private static final String PLAY_PREFIX="://play.google.com";
	public WebView webView;
	private JSInterface js = new JSInterface();
	public LaunchListener launchListener;
	ImageView popBut;
	ImageView moreBut;
	ImageView stopBut;
	ImageView reloadBut;
	ImageView closeBut;
	ProgressBar progress;
	boolean wasTrayExpanded;
	boolean isTrayExpanded = false;;
	boolean isLoading = false;;
	TextView msg ;
	long lastMsg = 0;
	ConnectivityManager cm;
	StringBuffer useString = new StringBuffer();
	public String defaultUrl;
	String pref;
	Activity act;
	private static String  ver = null;
	public HelpDialog(Activity context, int butId,String url,String pref) {
        super(context);
        cm = (ConnectivityManager)context.getSystemService(Activity.CONNECTIVITY_SERVICE);
        this.defaultUrl=url;
        this.pref=pref;
        this.act=context;
        setLaunchListener(new LaunchListener(context)); 
        if (butId>-1) {
	  	    ImageView  helpBut = (ImageView) context.findViewById(butId);
			helpBut.setOnClickListener(getOpenListener());
        }
    }

	public android.view.View.OnClickListener getOpenListener() {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showHelp();  
			}
		  };
	}
	
	public void showHelp() {
		showHelp(HelpDialog.this.defaultUrl);
	}
	
	public void showHelp(String url) {
		show();
		setUri(Uri.parse(url));
		if (HelpGlobals.PREFS_NAME!=null && pref!=null) {
			SharedPreferences s = act.getSharedPreferences(HelpGlobals.PREFS_NAME, 0);
	    	Editor edit = s.edit();
	    	edit.putBoolean(pref, false);
	    	edit.commit();
		}
	}
	
	public boolean checkShow(Activity act,String prefsName) {
		SharedPreferences s = act.getSharedPreferences(prefsName, 0);//Globals.PREFS_NAME
		return s.getBoolean(pref, true);
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	requestWindowFeature(Window.FEATURE_LEFT_ICON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help); 
        setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.help_t1_i_help_off_40);
        setTitle("Help");
        popBut = (ImageView) findViewById(R.id.help_toolbar_pop_but);
        popBut.setOnClickListener(popListener);
        moreBut = (ImageView) findViewById(R.id.help_toolbar_more_but);
        moreBut.setOnClickListener(moreClickListener);
        stopBut = (ImageView) findViewById(R.id.help_toolbar_stop_but);
        stopBut.setOnClickListener(stopListener);
        reloadBut = (ImageView) findViewById(R.id.help_toolbar_reload_but);
        reloadBut.setOnClickListener(reloadListener);
        closeBut = (ImageView) findViewById(R.id.help_toolbar_close_but);
        closeBut.setOnClickListener(closeListener);
        progress = (ProgressBar) findViewById(R.id.help_toolbar_progress);
        msg = (TextView)findViewById(R.id.help_toolbar_msg);
       
        initWebkit();
		setOnKeyListener(onKeyListener);
		updateTray();
    }
    
	@Override
	protected void onStart() {
		super.onStart();
		initWebkit();
		//showMessage ("Use the ellipsis to show/hide.");
		updateTray();
	}

	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	private void initWebkit() {
		 FrameLayout fl = (FrameLayout) findViewById(R.id.help_webview);
		 if (webView!=null) {
			 fl.removeView(webView);
			 webView.destroy();
		 }
		 webView = new WebView(getContext()); 
		 webView.setWebViewClient(webViewClient);
		 webView.setWebChromeClient(webChromeClient);
		 webView.clearCache(true);
		 webView.getSettings().setJavaScriptEnabled(true); 
		 webView.getSettings().setBuiltInZoomControls(true);
		 webView.addJavascriptInterface(js, "pageObject");
		 webView.setFocusableInTouchMode(true);
		 fl.addView(webView);
	}
	
	private Runnable postUpdateTray = new Runnable() {public void run() { updateTray();}};
	private void updateTray() {
		progress.setVisibility(isTrayExpanded && isLoading?View.VISIBLE:View.GONE);
		stopBut.setVisibility(isTrayExpanded && isLoading?View.VISIBLE:View.GONE);
		reloadBut.setVisibility(isTrayExpanded && !isLoading?View.VISIBLE:View.GONE);
		popBut.setVisibility(isTrayExpanded && !isLoading?View.VISIBLE:View.GONE);
		//closeBut.setVisibility(isTrayExpanded && !isLoading?View.VISIBLE:View.GONE);
		moreBut.setSelected(isTrayExpanded);
		if (System.currentTimeMillis()-lastMsg>MSG_TIME) {
			msg.setText("");
		}
		msg.setVisibility("".equals(msg.getText())?View.GONE:View.VISIBLE);
	}
	
	private void startLoading () {
		wasTrayExpanded=isTrayExpanded;
		isTrayExpanded = true;
		isLoading = true;
		progress.setProgress(0);
		updateTray();
	}
	
	private void stopLoading () {
		if (!wasTrayExpanded) {
			isTrayExpanded = false;
		}
		isLoading = false;
		progress.setProgress(0);
		updateTray();
	}
	
	private void updateLoading (int progressInt) {
	    progress.setProgress(progressInt );
	}
	
	private void showMessage (String text) {
		lastMsg=System.currentTimeMillis();
		msg.setText(text);
		updateTray();
		Handler h = new Handler();
		h.postDelayed(postUpdateTray, MSG_TIME+50);
	}
	
	class JSInterface {
		
	}
	
    public void setUri(Uri uri) {
    	if (isNetworkAvailable(getContext())) {
    		webView.loadUrl(uri.toString());
    	} else {
    		useString.delete(0, useString.length());
    		useString.append("<html><body style=\"");
    		useString.append("width:100%;height:100%;text-align:center;margin:10px;margin-top:100px;");
    		useString.append("\"><b>");
    		useString.append(getContext().getString(R.string.help_err_no_inet));
    		useString.append("</b><br/>(");
    		useString.append(getContext().getString(R.string.help_err_no_inet2));
    		useString.append(")<br/>");
    		useString.append(getContext().getString(R.string.help_quick_help).replaceAll("::", "<br/>"));
    		useString.append("</body><html>");
    		webView.loadDataWithBaseURL(getContext().getString(R.string.help_website), useString.toString(), "text/html", "utf-8", null);
    		showMessage (getContext().getString(R.string.help_err_no_inet));
    	}
		//startLoading ();
    }
    
    public static class LaunchListener {
    	protected Activity act;
    	
    	public LaunchListener(Activity act) {
			super();
			this.act = act;
		}

		public boolean launch(String url){
			try {
				if (url.contains(MARKET_PREFIX) ||url.contains(PLAY_PREFIX) || url.indexOf("mailto")==0) {
					Uri parse = Uri.parse(url);
					if (parse.getScheme().equals("mailto")) {
						String addr = parse.getHost();
						if (addr.indexOf("-at-")>-1) {
							StringBuffer bd = new StringBuffer(addr);
							doReplace(bd,"-at-","@");
							addr=bd.toString();
						} else {
							addr = parse.getUserInfo()+"@"+parse.getHost();
						}
						Intent myIntent = new Intent(Intent.ACTION_SEND,Uri.parse(parse.getScheme()+"://"+addr));
						myIntent.putExtra(Intent.EXTRA_SUBJECT, parse.getQueryParameter("subject"));
						myIntent.putExtra(Intent.EXTRA_EMAIL ,new String[]{addr});
						String body = parse.getQueryParameter("body");
						if (body!=null) {
							StringBuffer bd = new StringBuffer(body);
							doReplace(bd,"\\n","\n");
							body=bd.toString();
						} else {body="";}
						myIntent.putExtra(Intent.EXTRA_TEXT, body+"\n\n"+getPhoneDetails(act));
						myIntent.setType("message/rfc822"); 
						act.startActivity(myIntent);
					} else {
						Intent myIntent = new Intent(Intent.ACTION_VIEW,parse);
						act.startActivity(myIntent);
					}
					return true;
				}
				return false;
			} catch (Exception e) {
				Toast.makeText(act,"Unable to parse url ...", 1000).show();
				return false;
			} 
    	}
    }
    
    public static void doReplace(StringBuffer s,String src,String tgt) {
		if (tgt==null) {tgt="";}
		if (tgt.indexOf(src)>-1) {return;}
		int pos = s.indexOf(src);
		while (pos>-1) {
			s.replace(pos, pos + src.length(), tgt);
			pos = s.indexOf(src);
		}
	}
    
	public LaunchListener getLaunchListener() {
		return launchListener;
	}

	public void setLaunchListener(LaunchListener launchListener) {
		this.launchListener = launchListener;
	}
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			//Log.w(Globals.TAG, "Couldn't get connectivity manager");
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}
	

	///////////////////////////// webview listeners /////////////////////////////////////////////////
	WebChromeClient webChromeClient = new WebChromeClient() {
		   public void onProgressChanged(WebView view, int progress) {
		     // Activities and WebViews measure progress with different scales.
		     // The progress meter will automatically disappear when we reach 100%

		     updateLoading(progress);
		   }
		 };
	
		 WebViewClient webViewClient = new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					try {
						if (launchListener!=null) {
							return launchListener.launch(url);
						} 
					} catch (Exception e) {
						
					}
					return false;
				}
				@Override
				public void onPageFinished(WebView view, String url) {
					stopLoading ();
				}

				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon) {
					startLoading ();
				}

				@Override
				public void onReceivedError(WebView view, int errorCode,
						String description, String failingUrl) {
					stopLoading ();
					showMessage(description+":"+errorCode);
				}

				@Override
				public void onTooManyRedirects(WebView view, Message cancelMsg,	Message continueMsg) {
					super.onTooManyRedirects(view, cancelMsg, continueMsg);
				}
			};
			
	///////////////////////////// listeners /////////////////////////////////////////////////
	android.view.View.OnClickListener moreClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			isTrayExpanded=!isTrayExpanded;
			updateTray();
		}
		
	};
	android.view.View.OnClickListener reloadListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (!isLoading) {
				webView.reload();
				isTrayExpanded = false;
				updateTray();
			}
		}
		
	};
	android.view.View.OnClickListener stopListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			webView.stopLoading();
			isTrayExpanded = false;
			updateTray();
		}
		
	};
	android.view.View.OnClickListener popListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (!isLoading && webView.getUrl()!=null) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(webView.getUrl()));
				act.startActivity(i);
				isTrayExpanded = false;
				updateTray();
			}
			
		}
		
	};
	android.view.View.OnClickListener closeListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			dismiss();
		}
		
	};
	long backDblClickPrevertion = -1;
	 OnKeyListener onKeyListener = new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,KeyEvent event) {
				if (System.currentTimeMillis()-backDblClickPrevertion<100) {
					return true;
				}
				if (keyCode==KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
					backDblClickPrevertion =System.currentTimeMillis();
					/*
					int hpos = webView.getUrl().indexOf("#");
					String base = webView.getUrl();
					if ( hpos > -1 ) {
						base.indexOf( base.substring(0,hpos) );
					}
					WebBackForwardList wbfl= webView.copyBackForwardList();
					int ctr=0;
					while (wbfl.getItemAtIndex(ctr++).getUrl().indexOf(base)!=-1) {
						webView.goBack();
					}*/
					webView.goBack();
					return true;
				}
				return false;
			}
			
     };
     public  static String getPhoneDetails(Context c) {
 		return "\n\nPhone " +
 				"brand/model:"+Build.BRAND+":"+	Build.MODEL+"\n " +
 				"Version:"+getVersion(c)+" inc:"+Build.VERSION.INCREMENTAL+
 				" sdk:"+Build.VERSION.SDK+" rel:"+Build.VERSION.RELEASE;
 	}

 	public static String getVersion(Context c) {
 		if (ver==null) {
 			try {
 				ver = c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionName;
 			} catch (NameNotFoundException e) {
 				ver = "Unknown";  
 			}
 		}
 		return ver;
 	}
}
