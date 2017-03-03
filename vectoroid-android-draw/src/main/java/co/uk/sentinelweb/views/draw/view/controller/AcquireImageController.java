package co.uk.sentinelweb.views.draw.view.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import co.uk.sentinelweb.commonlibs.errorwrap.EWRunnable;
import co.uk.sentinelweb.commonlibs.errorwrap.OnEWDialogClickListener;
import co.uk.sentinelweb.views.draw.DVGlobals;
import co.uk.sentinelweb.views.draw.R;
import co.uk.sentinelweb.views.draw.file.FileRepository;
import co.uk.sentinelweb.views.draw.file.FileRepository.Directory;
import co.uk.sentinelweb.views.draw.util.OnEWAsyncListener;
import co.uk.sentinelweb.views.draw.view.PictureView;

public class AcquireImageController {
	public static final String TMP_IMG_FILE = ".tmpImgFile";
	
	private static final int REQUEST_CODE_GETCONTENT = 100;
	private static final int REQUEST_CODE_CAPTURE = 101;
	public enum PicutreSrc {UNKNOWN,CAPTURE,GALLERY};
	Activity _act;
	FileRepository _fileRepository;
	PictureView _picEditView;
	OnEWAsyncListener<Integer> _picLoadListener;
	Handler _updateHandler;
	public Bitmap _loadedBitmap;
	EWRunnable _loadPicRunnable;
	public PicutreSrc _src=PicutreSrc.UNKNOWN;
	public int _imgRotation = 0;
	public AcquireImageController(Activity _act, FileRepository _fileRepository,PictureView picEditView) {
		super();
		this._act = _act;
		this._fileRepository = _fileRepository;
		this._picEditView = picEditView;
		_updateHandler=new Handler();
		_loadPicRunnable = new EWRunnable(_act) {
			@Override
			public void doEWrun() {
				_picEditView.setPicture(_loadedBitmap);
				//
				if (_picLoadListener!=null) {
					_picLoadListener.onAsync(0);
				}
				_loadedBitmap = null;
			}
		};
	}

	public Dialog getChooseDialog() {
		SharedPreferences sp = _act.getSharedPreferences(DVGlobals.PREF_FILE, 0);
		boolean hasLastFile = sp.contains(DVGlobals.PREF_LAST_PHOTO);
		final String[] bgitems = {
			//_act.getString(R.string.dialog_selimg_choose_existing_asset),  
			_act.getString(R.string.dialog_selimg_choose_picture), 
			_act.getString(R.string.dialog_selimg_take_picture ), 
			_act.getString(R.string.dialog_selimg_use_current)
		};
		final String[] bgitemsNoLast = {
			//_act.getString(R.string.dialog_selimg_choose_existing_asset),  
			_act.getString(R.string.dialog_selimg_choose_picture), 
			_act.getString(R.string.dialog_selimg_take_picture )
		};
		
		return new AlertDialog.Builder(_act)
				.setTitle(R.string.dialog_title_image_source)
				.setIcon(R.drawable.i_img)
				.setItems(hasLastFile?bgitems:bgitemsNoLast,	new OnEWDialogClickListener(_act) {
					public void onEWClick(DialogInterface dialog,	int which) {
						switch (which) {
							//case 0 : 
								//showGallery(true);
							//	break;
							case 0 : 
								Intent i = new Intent(Intent.ACTION_GET_CONTENT);
								i.setType("image/*");
								_act.startActivityForResult(Intent.createChooser(i,_act.getString(R.string.dialog_title_select_picture)), REQUEST_CODE_GETCONTENT);
								break;
							case 1 : 
								File f = getTmpFile();
								if (f.exists()) {
									AcquireImageController.this.getTmpCaptureDialog().show();
								} else {
									startCapture();
								}
								break;
							case 2 : 
								useCurrentPic();
								break;
						}
					}
				}).create();
	}
	
	public Dialog getTmpCaptureDialog() {
		return new AlertDialog.Builder(_act)
		.setTitle(R.string.dialog_title_image_capture)
		.setIcon(R.drawable.i_img)
		.setMessage(R.string.dialog_msg_use_previously_captured_image)
		.setPositiveButton(R.string.dialog_but_load,	new OnEWDialogClickListener(_act) {
				public void onEWClick(DialogInterface dialog,int which) {
					loadTmpImage(false);
				}
			})
			.setNegativeButton(R.string.dialog_but_capture,new OnEWDialogClickListener(_act) { 
				public void onEWClick(DialogInterface dialog,	int which) {	
					startCapture();
				}
			}).create();
	}
	
	public void startCapture() {
		File tmpImgFile = getTmpFile();
		if (tmpImgFile.exists()) {
			tmpImgFile.renameTo(new File(_fileRepository.getDirectory(Directory.RAW),"capture_"+System.currentTimeMillis()+".png"));
			//tmpImgFile.delete();
		}
		Intent ic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		//ic.putExtra(MediaStore.EXTRA_OUTPUT, DrawCustomContentProvider.WRITE_IMG_URI);
		
		Uri outputUri = Uri.fromFile( tmpImgFile );
		ic.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
		_act.startActivityForResult(Intent.createChooser(ic, _act.getString(R.string.dialog_title_select_camera)), REQUEST_CODE_CAPTURE);
	}
	public boolean hasPic() {
		return _loadedBitmap!=null;
	}
	public void useCurrentPic() {
		SharedPreferences sp = _act.getSharedPreferences(DVGlobals.PREF_FILE, 0);
		String lastUrl = sp.getString(DVGlobals.PREF_LAST_PHOTO, null);
		if (lastUrl!=null) {
			if (TMP_IMG_FILE.equals(lastUrl)) {
				loadTmpImage(true);
				_src=PicutreSrc.CAPTURE;
			} else {
				loadImageUri(Uri.parse(lastUrl));
				_src=PicutreSrc.GALLERY;
			}
		} else {
			Toast.makeText(_act, R.string.error_no_image, 500).show();
		}
	}
	
	public void loadTmpImage(boolean forceload) {
    	File tmpImgFile = getTmpFile();
    	
    	int sampleSize = 1;
    	boolean loaded = false;
    	while (!loaded) {
			try {
				if (tmpImgFile.exists()) {
					Options o = new Options();
					o.inSampleSize=sampleSize;
					//picEditView.setCropDimensions(currentDrawing.width, currentDrawing.height);
					if (_picEditView._picture==null || forceload) {
						_picEditView.cleanReference();
				    	System.gc();
						_picEditView.reset();
						_loadedBitmap=BitmapFactory.decodeFile(tmpImgFile.getAbsolutePath(),o);
						_src=PicutreSrc.CAPTURE;
						try {
							ExifInterface exif = new ExifInterface(tmpImgFile.getAbsolutePath());
							_imgRotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,0); 
						} catch (IOException e) {
							e.printStackTrace();
						}
						triggerPicLoad();
						SharedPreferences sp = _act.getSharedPreferences(DVGlobals.PREF_FILE, 0);
						sp.edit().putString(DVGlobals.PREF_LAST_PHOTO, TMP_IMG_FILE).commit();
					}
					//bodyFlipper.setDisplayedChild(BODY_FLIP_PICEDIT);
					loaded = true;
					
				} else {
					break;
				}
			} catch (OutOfMemoryError e) {
				sampleSize+=1;
				if (sampleSize>4) {	break;}
				System.gc();
				try {	Thread.sleep(1000);	} catch (InterruptedException e1) {e1.printStackTrace();}
				Log.d(DVGlobals.LOG_TAG, "loadTmpImage(): OutOfMemoryError recieved: trying new subsampling:"+sampleSize);
			}
    	}
    	/*
    	if (!loaded) {
    		Toast.makeText(_act, R.string.toast_the_image_could_not_be_loaded_due_to_memory_limitations_, 500).show();
    	} else if (sampleSize>1) {
    		Toast.makeText(_act, R.string.toast_the_image_could_not_be_loaded_at_full_resoultion_due_to_memory_limitations_, 500).show();
    	}
    	*/
	}

	public void triggerPicLoad() {
		_updateHandler.post(_loadPicRunnable);
	}
	
	private void loadImageUri(Uri selectedImageUri) {
    	int sampleSize = 1;
    	boolean loaded = false;
    	
    	while (!loaded) {
    		try {
    			_picEditView.cleanReference();
    	    	System.gc();
				ContentResolver cr = _act.getContentResolver();
				InputStream openInputStream = cr.openInputStream(selectedImageUri);
				//picEditView.setCropDimensions(currentDrawing.width, currentDrawing.height);
				_loadedBitmap=BitmapFactory.decodeStream(openInputStream);
				_src=PicutreSrc.GALLERY;
				//if ("file".equals(selectedImageUri.getScheme())) {
					//try {
						//ExifInterface exif = new ExifInterface(selectedImageUri.getPath());
						//_imgRotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,0);
				//	} catch (IOException e) {
				//		e.printStackTrace();
				//	}
				//}
				_imgRotation = getOrientation(_act, selectedImageUri);
				triggerPicLoad();
				SharedPreferences sp = _act.getSharedPreferences(DVGlobals.PREF_FILE, 0);
				sp.edit().putString(DVGlobals.PREF_LAST_PHOTO, selectedImageUri.toString()).commit();
				//bodyFlipper.setDisplayedChild(BODY_FLIP_PICEDIT);
				loaded = true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();break;
			} catch (OutOfMemoryError e) {
				sampleSize+=1;
				if (sampleSize>4) {	break;}
				System.gc();
				try {	Thread.sleep(1000);	} catch (InterruptedException e1) {e1.printStackTrace();}
				Log.d(DVGlobals.LOG_TAG, "loadTmpImage(): OutOfMemoryError recieved: trying new subsampling:"+sampleSize);
			}
    	}
    	/*
    	if (!loaded) {
    		Toast.makeText(_act, R.string.toast_the_image_could_not_be_loaded_due_to_memory_limitations_, 500).show();
    	} else if (sampleSize>1) {
    		Toast.makeText(_act, R.string.toast_the_image_could_not_be_loaded_at_full_resoultion_due_to_memory_limitations_, 500).show();
    	}
    	*/
	}
	
	private int getOrientation(Context context, Uri photoUri) {
	    Cursor cursor = context.getContentResolver().query(photoUri,
	            new String[] { MediaStore.Images.ImageColumns.ORIENTATION },
	            null, null, null);

	    try {
	        if (cursor.moveToFirst()) {
	            return cursor.getInt(0);
	        } else {
	            return -1;
	        }
	    } finally {
	        cursor.close();
	    }
	}
	public boolean isPicObtained(int requestCode, int resultCode) {
		return ((requestCode==REQUEST_CODE_CAPTURE || requestCode==REQUEST_CODE_GETCONTENT) && resultCode==Activity.RESULT_OK);
	}
	
	public boolean activityResultHandler(int requestCode, int resultCode, Intent data) {
		if (requestCode==REQUEST_CODE_CAPTURE) {
			if (resultCode==Activity.RESULT_OK) {
				File f = getTmpFile();
				if (f.exists()) {
					Log.d(DVGlobals.LOG_TAG, "Image captured.");
					loadTmpImage(true);
				} else {
					Log.d(DVGlobals.LOG_TAG, "Image NOT captured.");
				}
			}
			return true;// handled
		} else if (requestCode==REQUEST_CODE_GETCONTENT) {
			if (resultCode==Activity.RESULT_OK) {
				Uri selectedImageUri = data.getData();
				
				loadImageUri(selectedImageUri);
				Log.d(DVGlobals.LOG_TAG, "selected image:"+selectedImageUri.toString());  
			}
			return true;// handled
		}
		return false;
	}

	/**
	 * @return the _picLoadListener
	 */
	public OnEWAsyncListener<Integer> getPicLoadListener() {
		return _picLoadListener;
	}

	/**
	 * @param _picLoadListener the _picLoadListener to set
	 */
	public void setPicLoadListener(OnEWAsyncListener<Integer> picLoadListener) {
		this._picLoadListener = picLoadListener;
	}
	
	private File getTmpFile() {
		return new File(_fileRepository.getDirectory(Directory.RAW),TMP_IMG_FILE);
	}
}
