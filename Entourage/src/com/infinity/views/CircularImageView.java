package com.infinity.views;

import com.infinity.entourage.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class CircularImageView extends ImageView{
	
	//Custom Attributes 
	//radius {make this an enum for preset styles (PROFILE, THUMBNAIL, 
	boolean hasBorder; 
	boolean hasShadow; 
	int borderWidth; 
	int selectorStrokeWidth; 
	
	
	//Drawing Objects 
	Paint mPaint; 
	Bitmap mImage; 
	Bitmap mBitmap; 
	BitmapShader mBitmapShader; 
	Canvas mCanvas; 
	int mCanvasSize; 
	

	public CircularImageView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		Log.d("C Y C L E TRACKER", "INIT CONSTRUCTOR"); 
		init(context, attrs, defStyleAttr); 
		// TODO Auto-generated constructor stub
	}

	public CircularImageView(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.circularImageViewStyle);
		Log.d("C Y C L E TRACKER", "BASIC CONSTRUCTOR"); 
		// TODO Auto-generated constructor stub
	}

	public CircularImageView(Context context) {
		this(context, null);
		Log.d("C Y C L E TRACKER", "EMPTY CONSTRUCTOR "); 
		// TODO Auto-generated constructor stub
	}
	
	public void init(Context context, AttributeSet attrs, int defStyleAttr){
		Log.d("C Y C L E TRACKER", " I N I T ! ! ! ! ! ! !"); 
		
		mPaint = new Paint(); 
		mPaint.setAntiAlias(true); 
		//refreshBitmapShader(); 
		//Get the custom attributes for the view from its styleable 
		TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircularImageView, defStyleAttr, 0);
		
		hasShadow = attributes.getBoolean(R.styleable.CircularImageView_hasShadow, false); 
		
		//Send the TypedArray back to the cache as we are done with the shared resource
		attributes.recycle(); 
		
	}
	
	
	@Override 
	public void onMeasure(int wMeasureSpec, int hMeasureSpec){
		//super.onMeasure(wMeasureSpec, hMeasureSpec); 
		
		Log.d("C Y C L E TRACKER", "onMeasure"); 
		
		//get width/height by checking for parent imposed constraints, then setting to canvas values 
		int w = getWidthSpec(wMeasureSpec); 
		int h = getHeightSpec(hMeasureSpec); 
		
		//call setMeasured dimension, which stores the width/height values in the view 
		setMeasuredDimension(w,h); 
		
	}
	
	public int getWidthSpec(int widthSpec){
		
		// Check to see if the parent is imposing any constraints on the view
		// if so, set the size value to the respective constraint
		// else, set the size value to the mCanvasSize (bitmap size)
		int mode = MeasureSpec.getMode(widthSpec); 
		int size = MeasureSpec.getSize(widthSpec); 
		
		if (mode == MeasureSpec.EXACTLY){
			Log.d("MeasureSpec", "EXACTLY"); 
			return size; 
			
		}else if (mode == MeasureSpec.AT_MOST){
			Log.d("MeasureSpec", "AT_MOST"); 
			return size; 
		}else {
			Log.d("MeasureSpec", "None specified, using xml"); 
			return mCanvasSize; 
		}
		
		
	}
	
	public int getHeightSpec(int heightSpec){
		
		int mode = MeasureSpec.getMode(heightSpec); 
		int size = MeasureSpec.getSize(heightSpec); 
		
		//Check to see if the parent is imposing any constraints on the view
		//if so, set the size value to the respective constraint
		//else, set the size value to the mCanvasSize (bitmap size) 
		if (mode == MeasureSpec.EXACTLY){
			Log.d("Height MeasureSpec", "EXACTLY"); 
			return size; 
		}else if (mode == MeasureSpec.AT_MOST){
			Log.d("Height MeasureSpec", "AT_MOST"); 
			return size; 
		}else {
			Log.d("Height MeasureSpec", "None specified, using xml"); 
			return mCanvasSize; 
		}
	}
	
	private void refreshBitmapShader(){
		Log.d("C Y C L E TRACKER", "refreshBitmapShader"); 
		mBitmapShader = new BitmapShader(Bitmap.createScaledBitmap(mImage, mCanvasSize - 10 , mCanvasSize - 10, false), Shader.TileMode.CLAMP, Shader.TileMode.CLAMP); 
	}
	
	private Bitmap drawableToBitmap(Drawable drawable){
		Log.d("C Y C L E TRACKER", "drawableToBitmap"); 
		
		//if the image argument is null, do nothing 
		if(drawable == null){
			return null; 
			//Get Bitmap via getBitmap() if its an Instance of BitmapDrawable 
		}else if (drawable instanceof BitmapDrawable){
			return ((BitmapDrawable) drawable).getBitmap(); 
		}else{
		
		//Return 
		Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicHeight(), drawable.getIntrinsicWidth(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return bmp; 
		}
	}
	
	@Override 
	public void onDraw(Canvas canvas){
		Log.d("C Y C L E TRACKER", "onDraw()"); 
		//if the drawable is null, there is nothing to set
		//in future iterations set default icon 
		
		if(mImage == null) return; 
		
		if(mImage.getHeight() == 0 | mImage.getWidth() == 0) return; 
		
		int oldCanvas = mCanvasSize; 
		
		mCanvasSize = canvas.getWidth(); 
		if(mCanvasSize != canvas.getHeight()) mCanvasSize = canvas.getHeight(); 
		
		
		if(mCanvasSize != oldCanvas){
		//if the size of the canvas has changed, refresh bitmap shader 
		refreshBitmapShader(); 
		}
		
		mPaint.setShader(mBitmapShader); 
		
		int center = mCanvasSize / 2; 
		
		if (hasShadow) addShadow(); 
		
		canvas.drawCircle(center, center, center - 10, mPaint); 
	}
	
	public void addShadow(){
		setLayerType(LAYER_TYPE_SOFTWARE, mPaint); 
		mPaint.setColor(Color.RED);
		mPaint.setShadowLayer(5.0f, 0.0f, 4.0f, Color.RED); 
	}
	
	public void invalidate(){
		super.invalidate(); 
		Log.d("C Y C L E TRACKER", "invalidate()"); 
		
		//if the shader isnt null or if the canvasSize > 0 then it has been changed since initialization and needs to be reset 
		mImage = drawableToBitmap(getDrawable()); 
		if(mBitmapShader != null || mCanvasSize > 0) refreshBitmapShader(); 
	}
	
	public void invalidate(Rect dirty){
		super.invalidate(dirty); 
		Log.d("C Y C L E TRACKER", "invalidate(dirty)"); 
		mImage = drawableToBitmap(getDrawable()); 
		if(mBitmapShader != null || mCanvasSize > 0) refreshBitmapShader(); 
	}
	
	public void invalidate(int l,int t, int r,int b){
		super.invalidate(l, t, r, b); 
		
		Log.d("C Y C L E TRACKER", "invalidate(l,t,r,b)"); 
		
		mImage = drawableToBitmap(getDrawable()); 
		if(mBitmapShader != null || mCanvasSize > 0) refreshBitmapShader(); 
	}

}
