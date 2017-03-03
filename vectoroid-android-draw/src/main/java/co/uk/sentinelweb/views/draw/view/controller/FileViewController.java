package co.uk.sentinelweb.views.draw.view.controller;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PointF;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import co.uk.sentinelweb.commonlibs.errorwrap.OnEWClickListener;
import co.uk.sentinelweb.commonlibs.errorwrap.OnEWDialogClickListener;
import co.uk.sentinelweb.commonlibs.globutton.GlowButton;
import co.uk.sentinelweb.commonlibs.numscroll.NumberScroller;
import co.uk.sentinelweb.views.draw.DVGlobals;
import co.uk.sentinelweb.views.draw.R;
import co.uk.sentinelweb.views.draw.file.FileRepository;
import co.uk.sentinelweb.views.draw.file.SaveFile;
import co.uk.sentinelweb.views.draw.file.SaveFile.Option;
import co.uk.sentinelweb.views.draw.file.export.svg.SVG;
import co.uk.sentinelweb.views.draw.model.Drawing;
import co.uk.sentinelweb.views.draw.util.MediaScan;
import co.uk.sentinelweb.views.draw.util.ValidationUtil;
import co.uk.sentinelweb.views.draw.view.DrawView;

public class FileViewController {
	DrawView _drawView;
	SaveFile _saveFile;
	Dialog _saveDialog;
	Dialog _newDrawingDialog;
	Dialog _newSetDialog;
	//TextView _saveNameText;
	CheckBox _saveJSONChk;
	CheckBox _saveSVGChk;
	CheckBox _saveZIPChk;
	CheckBox _embedChk;
	private String[] _fileNames;
	private OnClickListener _defFileDialogClick;
	NumberScroller _widthScroll;
	NumberScroller _heightScroll;
	public FileViewController(DrawView d) {
		super();
		this._drawView = d;
	}
	
	public Dialog getFileLoadDialog(Context c,OnClickListener customClick,final FileRepository fr ) {
		final ArrayList<SaveFile> files = fr.getFiles(c);//FileRepository.getFileRepository(c)
		_fileNames = new String[files.size()];
		for (int i=0;i<files.size();i++) {_fileNames[i]=files.get(i).getName();}
		_defFileDialogClick = new OnEWDialogClickListener(c) { 
			public void onEWClick(DialogInterface dialog, int which) {
				_saveFile = fr.getFiles(c).get(which);//FileRepository.getFileRepository(c)
				loadSaveFile(_saveFile, null);
				dialog.dismiss();
			}
		};
		Dialog filesDialog=new AlertDialog.Builder(c)
			.setTitle(R.string.dialog_title_select_file)
			.setIcon(R.drawable.i_load) 
			.setItems(_fileNames,customClick!=null?customClick:_defFileDialogClick).create();
		return filesDialog;
	}
	
	public Dialog getFileSaveDialog(Context c) {
		if (_saveDialog==null) {
			FrameLayout fls = new FrameLayout(c);
			FrameLayout.inflate(c, R.layout.save_dialog, fls);
			EditText _saveNameText = (EditText)fls.findViewById(R.id.savedialog_edit);
			_saveNameText.setVisibility(View.GONE);
			//_saveNameText.setText(_saveFile.getName());
			_saveJSONChk = (CheckBox)fls.findViewById(R.id.savedialog_json_chk);
			_saveJSONChk.setChecked(true);
			_saveSVGChk = (CheckBox)fls.findViewById(R.id.savedialog_svg_chk);
			_saveSVGChk.setChecked(false);
			_saveZIPChk = (CheckBox)fls.findViewById(R.id.savedialog_zip_chk);
			_saveZIPChk.setChecked(false);
			_embedChk = (CheckBox)fls.findViewById(R.id.savedialog_embed_chk);
			_embedChk.setChecked(false);
			GlowButton b = (GlowButton)fls.findViewById(R.id.savedialog_sets_but);
			b.setVisibility(View.GONE);
			/*
			b.setOnClickListener(new OnEWClickListener(c) {
				@Override
				public void onEWClick(View v) {
					getFileLoadDialog( v.getContext(), new OnEWDialogClickListener(v.getContext()) {
						@Override
						public void onEWClick(DialogInterface arg0, int arg1) {
							//_saveNameText.setText(_fileNames[arg1]);
						}
					}).show();
			}});
			*/
			return new AlertDialog.Builder(c)
					.setView(fls)
					.setTitle(R.string.dialog_title_enter_name)
					.setIcon(R.drawable.i_save)
					.setPositiveButton(R.string.dialog_but_save_publish,new OnEWDialogClickListener(c) {
								public void onEWClick(DialogInterface dialog,int which) {
									//if ( !ValidationUtil.isBlank(_saveNameText.getText().toString()) ) {
										try {
											//_saveFile.setDataDir( _saveNameText.getText().toString() );
											ArrayList<Option> opts = new ArrayList<SaveFile.Option>();
											if (_embedChk.isChecked()) {opts.add(Option.EMBED_ASSET);}
											if (_saveJSONChk.isChecked()) {
												_saveFile.saveJSON( _drawView._drawing, opts );
											}
											if (_saveSVGChk.isChecked()) {
												//_saveFile.saveSVG( _drawView._drawing,opts );
												SVG.saveSVG(_saveFile, _drawView._drawing, opts);
											}
											try {
												_saveFile.saveBitmaps( _drawView._drawing, true, false );
												MediaScan.scanFile( _drawView.getContext(), _saveFile.getBitmapOutputFile(_drawView._drawing.getId()) );
											} catch(OutOfMemoryError oem) {
												Toast.makeText(_drawView.getContext(), R.string.err_save_bitmap, 500).show();
											}
											dialog.dismiss();
										} catch (Exception e) {
											Toast.makeText(_drawView.getContext(), "Cannot save file - try another name", 500).show();
											Log.d(DVGlobals.LOG_TAG,  "Cannot save file ...",e);
										}
									//}
								}
							}).setNeutralButton(R.string.dialog_but_save_only,new OnEWDialogClickListener(c) {
								public void onEWClick(DialogInterface dialog,	int which) {
									//if ( !ValidationUtil.isBlank(_saveNameText.getText().toString()) ) {
										try {
											//_saveFile.setDataDir( _saveNameText.getText().toString() );
											
											ArrayList<Option> opts = new ArrayList<SaveFile.Option>();
											if (_embedChk.isChecked()) {opts.add(Option.EMBED_ASSET);}
											if (_saveJSONChk.isChecked()) {
												_saveFile.saveJSON( _drawView._drawing,opts );
											}
											if (_saveSVGChk.isChecked()) {
												//_saveFile.saveSVG( _drawView._drawing,opts );
												SVG.saveSVG(_saveFile, _drawView._drawing, opts);
											}
											try {
												_saveFile.saveBitmaps( _drawView._drawing, true, true );
											} catch(OutOfMemoryError oem) {
												Toast.makeText(_drawView.getContext(), R.string.err_save_bitmap, 500).show();
											}
											dialog.dismiss();
										} catch (Exception e) {
											Toast.makeText(_drawView.getContext(), "Cannot save file - try another name", 500).show();
											Log.d(DVGlobals.LOG_TAG,  "Cannot save file ...",e);
										}
									//}
								}
							}).setNegativeButton(R.string.dialog_but_cancel,new OnEWDialogClickListener(c) {
								public void onEWClick(DialogInterface dialog,	int which) {dialog.dismiss();}
							}).create();
		}
		//_saveNameText.setText(_saveFile.getName());
		return _saveDialog;
	}
	
	public Dialog getNewDrawingDialog(Context c) {
		if (_newDrawingDialog==null) {
			FrameLayout fls = new FrameLayout(c);
			FrameLayout.inflate(c, R.layout.newdrawing_dialog, fls);
			_widthScroll = (NumberScroller)fls.findViewById(R.id.nd_width);
			_heightScroll = (NumberScroller)fls.findViewById(R.id.nd_height);
			SharedPreferences sharedPreferences = c.getSharedPreferences(DVGlobals.PREF_FILE, 0);
			_widthScroll.setValue(sharedPreferences.getInt(DVGlobals.PREF_NEW_WIDTH, DVGlobals.PREF_NEW_WIDTH_DEF));
			_heightScroll.setValue(sharedPreferences.getInt(DVGlobals.PREF_NEW_HEIGHT, DVGlobals.PREF_NEW_HEIGHT_DEF));
			
			_newDrawingDialog=new AlertDialog.Builder(c)
				.setView(fls)
				.setTitle(R.string.dialog_title_new_drawing)
				.setIcon(R.drawable.i_new)
				.setPositiveButton(R.string.dialog_but_ok,new OnEWDialogClickListener(c) {
							public void onEWClick(DialogInterface dialog,int which) {
								Drawing dr = new Drawing();
								dr.size=new PointF(_widthScroll.getValue(),_heightScroll.getValue());
								c.getSharedPreferences(DVGlobals.PREF_FILE, 0).edit()
									.putInt(DVGlobals.PREF_NEW_WIDTH, _widthScroll.getValue())
									.putInt(DVGlobals.PREF_NEW_HEIGHT, _heightScroll.getValue())
									.commit();
								dr.background.setColor(Color.WHITE);
								dr.setId("New");
								_saveFile.addDrawingToSet(dr);
								_drawView.setDrawing( dr,true);
							}
						}).setNegativeButton(R.string.dialog_but_cancel,new OnEWDialogClickListener(c) {
							public void onEWClick(DialogInterface dialog,	int which) {dialog.dismiss();}
						}).create();
		}
		_widthScroll.setValue((int)_drawView._drawing.size.x);
		_heightScroll.setValue((int)_drawView._drawing.size.y);
		return _newDrawingDialog;
	}
	
	public Dialog getNewSetDialog(Context c,final FileRepository fr) {
		if (_newSetDialog==null) {
			final EditText t = new EditText(c);
			_newSetDialog=new AlertDialog.Builder(c)
				.setTitle(R.string.dialog_title_new_set)
				.setIcon(R.drawable.i_new)
				.setView(t)
				.setPositiveButton(R.string.dialog_but_ok,new OnEWDialogClickListener(c) {
							public void onEWClick(DialogInterface dialog,int which) {
								String name = t.getText().toString();
								try {
									SaveFile s = new SaveFile(name,fr);//FileRepository.getFileRepository(_drawView.getContext())  //, _drawView.getContext()
									setSaveFile(s);
									_saveFile.saveSet();
									getNewDrawingDialog(_drawView.getContext()).show();
								} catch (Exception e) {
									Toast.makeText(_drawView.getContext(), "Cannot make set", 500);
								}
								
							}
						}).setNegativeButton(R.string.dialog_but_cancel,new OnEWDialogClickListener(c) {
							public void onEWClick(DialogInterface dialog,	int which) {dialog.dismiss();}
						}).create();
		}
		return _newSetDialog;
	}
	/*
	public void setSaveName(String name,Context c) {
		if (!_saveFile.getName().equals(name)) {
			_saveFile=new SaveFile(name,FileRepository.getFileRepository(),c);
		}
	}
	*/
	/**
	 * @return the _saveFile
	 */
	public SaveFile getSaveFile() {
		return _saveFile;
	}
	/**
	 * @param _saveFile the _saveFile to set
	 */
	public void setSaveFile(SaveFile _saveFile) {
		this._saveFile = _saveFile;
	}
	
	public void loadSaveFile(SaveFile _saveFile, String id) {
		setSaveFile( _saveFile );
		if (_saveFile.getSet().hasDrawings()) {
			int idx = _saveFile.getSet().getDrawingIDs().indexOf(id);
			if (idx==-1) {idx=0;}
			Drawing dr = _saveFile.loadJSON(_saveFile.getSet().getDrawingIDs().get(idx));
			if (dr!=null) {
				_drawView.setDrawing( dr,true);
				return;
			}
		}
		Drawing d = makeNewDrawing(_saveFile,null,-1,-1);
		_drawView.setDrawing( d ,true);
	}

	private Drawing makeNewDrawing(SaveFile _saveFile, String newID, int width, int height) {
		// TODO generate from template if it exists
		Drawing d = null;
		if (_saveFile.getSet().getDefaultTemplate()!=null && _saveFile.getSet().getDefaultTemplate().getTemplate()!=null) {
			d=(Drawing)_saveFile.getSet().getDefaultTemplate().getTemplate().duplicate(true);
		}
		if ( d==null) {
				d = new Drawing();
				SharedPreferences sharedPreferences = _drawView.getContext().getSharedPreferences(DVGlobals.PREF_FILE, 0);
				if (width==-1) {
					width = sharedPreferences.getInt(DVGlobals.PREF_NEW_WIDTH, DVGlobals.PREF_NEW_WIDTH_DEF);	
				}
				if (height==-1) {
					height = sharedPreferences.getInt(DVGlobals.PREF_NEW_HEIGHT, DVGlobals.PREF_NEW_HEIGHT_DEF);
				}
				d.background.setColor( Color.WHITE );
		}
		//d.size.set( 4096, 2048 );
        d.setId("New");
        _saveFile.addDrawingToSet( d );
        //_saveFile.getSet().getDrawingIDs().add(d.getId());
		return d; 
	}

	public void loadNext() {// save the drawing first
		int idx = _saveFile.getSet().getDrawingIDs().indexOf(_drawView._drawing.getId());
		if (idx==-1) {
			throw new RuntimeException("shouldnt be here");
		} else if (idx<_saveFile.getSet().getDrawingIDs().size()-1) {
			String nextID = _saveFile.getSet().getDrawingIDs().get(idx+1);;
			if (nextID!=null) {
				saveDrawing(_drawView._drawing);
				loadDrawing(nextID);
			}
		} else {
			Drawing d = makeNewDrawing(_saveFile, null,-1,-1);
			//_saveFile.getSet().getDrawingIDs().add(d.getId());
			//_saveFile.saveSet();
			_saveFile.addDrawingToSet( d) ;
			_drawView.setDrawing( d ,true);
		}
	}
	
	public void loadPrev() {// save the drawing first
		int idx = _saveFile.getSet().getDrawingIDs().indexOf(_drawView._drawing.getId());
		if (idx==-1) {
			throw new RuntimeException("shouldnt be here");
		} else if (idx>0) {
			String nextID = _saveFile.getSet().getDrawingIDs().get(idx-1);
			if (nextID!=null) {
				saveDrawing(_drawView._drawing);
				loadDrawing(nextID);
			}
		} else {
			Toast.makeText(_drawView.getContext(), "You are at the first drawing", 500);
		}
	}
	
	public void loadDrawing(String id) {
		Drawing dr = _saveFile.loadJSON(id);
		if (dr!=null) {
			_drawView.setDrawing( dr,true);
			return;
		} else {
			//Drawing d = makeNewDrawing( _saveFile, null );
			//_drawView.setDrawing( d );
			Toast.makeText(_drawView.getContext(), "Couldnt load drawing:"+id, 500);
		}
	}
	
	public void saveDrawing(Drawing d) {
		if (d!=null) {
			_saveFile.saveJSON(d, null);//options
			_saveFile.renderPreview(d, _drawView._renderer, _saveFile.getPreviewFile(d.getId()));
			_drawView._renderer.setVpd(_drawView._viewPort.data);
		}
	}
}
