package com.romanblack.android.widget.imagegallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageGallery extends AbsoluteLayout{
    
    private final int HANDLER_INIT_CENTER_DECODED = 0;
    private final int HANDLER_INIT_RIGHT_DECODED = 1;
    private final int HANDLER_INIT_LEFT_DECODED = 2;
    private final int HANDLER_SLIDING_LEFT = 3;
    private final int HANDLER_SLIDING_RIGHT = 4;
    private final int HANDLER_SLIDE_LEFT = 5;
    private final int HANDLER_SLIDE_RIGHT = 6;
    private final int HANDLER_NO_SLIDING = 7;
    private final int HANDLER_RETURN_FROM_LEFT = 8;
    private final int HANDLER_RETURN_FROM_RIGHT = 9;
    private final int DECODING_END = 10;
    private final int INVALIDATE = 11;
    
    public static final int DIRECTION_SLIDE_LEFT = 0;
    public static final int DIRECTION_SLIDE_RIGHT = 1;
    
    private static final int POSITION_LEFT = -1;
    private static final int POSITION_CENTER = 0;
    private static final int POSITION_RIGHT = 1;
    
    private final int MINIMUM_SCALE = 1;
    private final int MAXIMUM_SCALE = 2;
    
    private boolean slidingLeft = false;
    private boolean initialized = false;
    private boolean touchEnabled = true;
    
    private int fPosition = 0;
    private int sPosition = 0; 
    private int tPosition = 0;
    private int actualX = 0;
    private int actualY = 0;
    private int currentImagePosition = 0;
    private int lastXTouch = 0; // this variables using in onTouchEvent()
    private int lastYTouch = 0;
    
    private int bitmapPosition = 0; // position of bitmap need to be decoded
    private int correctingSpeed = 30; // in pixels per second
    private int frapsInterval = 15;
    
    private float density = 1;
    private float actualScale = 1;
    
    private ExecutorService executorService = null;
    
    private Context ctx = null;
    
    private ImageView firstImgView = null;
    private ImageView secondImageView = null;
    private ImageView thirdImageView = null;
    
    private ScaleGestureDetector scaleGestureDetector;
    
    private Bitmap[] bitmaps = new Bitmap[5];
    
    private ArrayList<String> imagePaths = new ArrayList<String>();
    
    private OnSlideEndListener onSlideEndListener = null;
    private PositionChangedListener positionChangedListener = null;
    
    private Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            
            switch(msg.what){
                case HANDLER_INIT_CENTER_DECODED:{
                    drawCenter();
                }break;
                case HANDLER_INIT_RIGHT_DECODED:{
                    drawRight();
                }break;
                case HANDLER_INIT_LEFT_DECODED:{
                    drawLeft();
                }break;
                case HANDLER_SLIDING_LEFT:{
                    handler.sendEmptyMessage(HANDLER_SLIDE_LEFT);
                }break;
                case HANDLER_SLIDING_RIGHT:{
                    handler.sendEmptyMessage(HANDLER_SLIDE_RIGHT);
                }break;
                case HANDLER_SLIDE_LEFT:{
                    actualX = actualX + Math.round(correctingSpeed * density);
                    
                    requestSizes();
                    
                    invalidate();
                    
                    if(actualX >= ((15 * density) + Math.round(getWidth()))){// * actualScale)){
                        actualX = Math.round(((15 * density) + Math.round(getWidth()) * actualScale));
                        onSlideEnd(DIRECTION_SLIDE_LEFT);
                    }else{
                        handler.sendEmptyMessageDelayed(HANDLER_SLIDE_LEFT, frapsInterval);
                    }
                }break;
                case HANDLER_SLIDE_RIGHT:{
                    actualX = actualX - Math.round(correctingSpeed * density);
                    
                    requestSizes();
                    
                    invalidate();
                    
                    if(actualX <= -((15 * density) + Math.round(getWidth()) * actualScale)){
                        actualX = Math.round(-((15 * density) + Math.round(getWidth()) * actualScale));
                        onSlideEnd(DIRECTION_SLIDE_RIGHT);
                    }else{
                        handler.sendEmptyMessageDelayed(HANDLER_SLIDE_RIGHT, frapsInterval);
                    }
                }break;
                case HANDLER_NO_SLIDING:{
                    int h = getHeight();
                    
                    if(actualX > 0){
                        handler.sendEmptyMessage(HANDLER_RETURN_FROM_LEFT);
                    }else if(actualX < ((1 - actualScale) * getWidth())){
                        handler.sendEmptyMessage(HANDLER_RETURN_FROM_RIGHT);
                    }
                }break;
                case HANDLER_RETURN_FROM_LEFT:{
                    actualX = actualX - Math.round(correctingSpeed * density);
                    
                    if(actualX < 0){
                        actualX = 0;
                    }
                    
                    requestSizes();
                    
                    invalidate();
                    
                    if(actualX == 0){
                        
                    }else{
                        handler.sendEmptyMessageDelayed(HANDLER_RETURN_FROM_LEFT, frapsInterval);
                    }
                }break;
                case HANDLER_RETURN_FROM_RIGHT:{
                    actualX = actualX + Math.round(correctingSpeed * density);
                    
                    if(actualX > (-((actualScale - 1) * getWidth()))){
                        actualX = Math.round(-((actualScale - 1) * getWidth()));
                    }
                    
                    requestSizes();
                    
                    invalidate();
                    
                    if(actualX == Math.round(-((actualScale - 1) * getWidth()))){
                        
                    }else{
                        handler.sendEmptyMessageDelayed(HANDLER_RETURN_FROM_RIGHT, frapsInterval);
                    }
                }break;
                case DECODING_END:{
                    switch(fPosition){
                        case POSITION_LEFT:{
                            firstImgView.setImageBitmap(bitmaps[1]);
                        }break;
                        case POSITION_CENTER:{
                            firstImgView.setImageBitmap(bitmaps[2]);
                        }break;
                        case POSITION_RIGHT:{
                            firstImgView.setImageBitmap(bitmaps[3]);
                        }break;
                    }
                    
                    switch(sPosition){
                        case POSITION_LEFT:{
                            secondImageView.setImageBitmap(bitmaps[1]);
                        }break;
                        case POSITION_CENTER:{
                            secondImageView.setImageBitmap(bitmaps[2]);
                        }break;
                        case POSITION_RIGHT:{
                            secondImageView.setImageBitmap(bitmaps[3]);
                        }break;
                    }
                    
                    switch(tPosition){
                        case POSITION_LEFT:{
                            thirdImageView.setImageBitmap(bitmaps[1]);
                        }break;
                        case POSITION_CENTER:{
                            thirdImageView.setImageBitmap(bitmaps[2]);
                        }break;
                        case POSITION_RIGHT:{
                            thirdImageView.setImageBitmap(bitmaps[3]);
                        }break;
                    }
                }break;
                case INVALIDATE:{
                    requestSizes();
                    invalidate();
                }break;
            }
        }
        
    };
    
    public ImageGallery(Context ctx){
        super(ctx);
        
        this.ctx = ctx;
        
       // init();
    }
    
    public ImageGallery(Context ctx, ArrayList<String> imagePaths){
        super(ctx);
        
        this.ctx = ctx;
        this.imagePaths = imagePaths;
        
       // init();
    }
    
    public ImageGallery(Context ctx, ArrayList<String> imagePaths, int position){
        super(ctx);
        
        this.ctx = ctx;
        this.imagePaths = imagePaths;
        this.currentImagePosition = position;
        
       // init();
    }
    
    public ImageGallery(Context ctx, AttributeSet attrs){
        super(ctx, attrs);
        
        this.ctx = ctx;
        
       // init();
    }
    
    public ImageGallery(Context ctx, AttributeSet attrs, ArrayList<String> imagePaths){
        super(ctx, attrs);
        
        this.ctx = ctx;
        this.imagePaths = imagePaths;
        
      //  init();
    }
    
    public ImageGallery(Context ctx, AttributeSet attrs, ArrayList<String> imagePaths, int position){
        super(ctx, attrs);
        
        this.ctx = ctx;
        this.imagePaths = imagePaths;
        this.currentImagePosition = position;
        
       // init();
    }
    
    public ImageGallery(Context ctx, AttributeSet attrs, int defStyle){
        super(ctx, attrs, defStyle);
        
        this.ctx = ctx;
        
       // init();
    }
    
    public ImageGallery(Context ctx, AttributeSet attrs, int defStyle, ArrayList<String> imagePaths){
        super(ctx, attrs, defStyle);
        
        this.ctx = ctx;
        this.imagePaths = imagePaths;
        
       // init();
    }
    
    public ImageGallery(Context ctx, AttributeSet attrs, int defStyle, ArrayList<String> imagePaths, int position){
        super(ctx, attrs, defStyle);
        
        this.ctx = ctx;
        this.imagePaths = imagePaths;
        this.currentImagePosition = position;
        
      //  init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        //init();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        
        if(!initialized){
            if(!imagePaths.isEmpty()){
                init();
            
                initialized = true;
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        handler.sendEmptyMessageDelayed(INVALIDATE, 30);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!touchEnabled){
            return false;
        }
        
        scaleGestureDetector.onTouchEvent(event);
        if(scaleGestureDetector.isInProgress()){
            return true;
        }
        
        int action = event.getAction();
        
        switch(action){
            case MotionEvent.ACTION_DOWN:{
                handler.removeMessages(HANDLER_RETURN_FROM_LEFT);
                handler.removeMessages(HANDLER_RETURN_FROM_RIGHT);
                handler.removeMessages(HANDLER_SLIDE_LEFT);
                handler.removeMessages(HANDLER_SLIDE_RIGHT);
                
                lastXTouch = Math.round(event.getX());
                lastYTouch = Math.round(event.getY());
            }break;
            case MotionEvent.ACTION_UP:{
                lastXTouch = 0;
                lastYTouch = 0;
                
                if((actualX + (8 * density) + Math.round(getWidth() * actualScale)) <= (getWidth() / 2)){
                    handler.sendEmptyMessage(HANDLER_SLIDING_RIGHT);
                }else if((actualX - (8 * density)) >= (getWidth() / 2)){
                    handler.sendEmptyMessage(HANDLER_SLIDING_LEFT);
                }else{
                    handler.sendEmptyMessage(HANDLER_NO_SLIDING);
                }
            }break;
            case MotionEvent.ACTION_MOVE:{
                int deltaX = Math.round(event.getX()) - lastXTouch;
                int deltaY = Math.round(event.getY()) - lastYTouch;
                
                lastXTouch = Math.round(event.getX());
                lastYTouch = Math.round(event.getY());
                
                actualX = actualX + deltaX;
                actualY = actualY + deltaY;
                
                if(actualY > 0){
                    actualY = 0;
                }
                
                int minActualY = getHeight() - Math.round(getHeight() * actualScale);
                
                if(actualY < minActualY){
                    actualY = minActualY;
                }
                
                if(currentImagePosition == 0){
                    int maxActualX = Math.round(getWidth() * 0.4f);
                    
                    if(actualX > maxActualX){
                        actualX = maxActualX;
                    }
                }
                
                if(currentImagePosition == (imagePaths.size() - 1)){
                    int minActualX = Math.round(getWidth() * (1 - 0.4f - actualScale));
                    
                    if(actualX < minActualX){
                        actualX = minActualX;
                    }
                }
                
                requestSizes();
                
                invalidate();
            }break;
        }
        
        return super.onTouchEvent(event);
    }
    
    private void init(){
        setWillNotDraw(false);
        
        density = ctx.getResources().getDisplayMetrics().density;
        
        firstImgView = new ImageView(ctx); 
        //firstImgView.setBackgroundColor(Color.WHITE);//test
        secondImageView = new ImageView(ctx);
        //secondImageView.setBackgroundColor(Color.WHITE);//test
        thirdImageView = new ImageView(ctx);
        //thirdImageView.setBackgroundColor(Color.WHITE);//test
        
        fPosition = POSITION_LEFT;
        sPosition = POSITION_CENTER;
        tPosition = POSITION_RIGHT;
        
        addView(firstImgView);
        addView(secondImageView);
        addView(thirdImageView);
        
        requestLayout(firstImgView, fPosition);
        requestLayout(secondImageView, sPosition);
        requestLayout(thirdImageView, tPosition);
        
        executorService = Executors.newFixedThreadPool(3);
        
        executorService.execute(new InatializationBitmapsDecodingRunnable());
        
        scaleGestureDetector = new ScaleGestureDetector(ctx, new ScaleDetectorListener());
    }
    
    private void requestSizes(){
        requestLayout(firstImgView, fPosition);
        requestLayout(secondImageView, sPosition);
        requestLayout(thirdImageView, tPosition);
    }
    
    private void requestLayout(ImageView imageView, int position){
        int x = 0;
        int y = 0;
        int height = 0;
        int width = 0;
        
        switch(position){
            case POSITION_LEFT:{
                height = getHeight();
                width = getWidth();
                x = actualX - (int)(15 * density) - width;
                y = 0;
            }break;
            case POSITION_CENTER:{
                x = actualX;
                y = actualY;
                
                height = (int)(getHeight() * actualScale);
                width = (int)(getWidth() * actualScale);
            }break;
            case POSITION_RIGHT:{
                height = getHeight();
                width = getWidth();
                x = actualX + (int)(15 * density) + Math.round(width * actualScale);
                y = 0;
            }break;
        }
        
        AbsoluteLayout.LayoutParams lp = new AbsoluteLayout.LayoutParams(width, height, x, y);
        imageView.setLayoutParams(lp);
    }
    
    private void onSlideEnd(int direction){
        int oldPosition = currentImagePosition;
        
        if(direction == DIRECTION_SLIDE_RIGHT){
            slidingLeft = false;
            
            switch(fPosition){
                case POSITION_LEFT:{
                    fPosition = POSITION_RIGHT;
                }break;
                case POSITION_CENTER:{
                    fPosition = POSITION_LEFT;
                }break;
                case POSITION_RIGHT:{
                    fPosition = POSITION_CENTER;
                }break;
            }
            
            switch(sPosition){
                case POSITION_LEFT:{
                    sPosition = POSITION_RIGHT;
                }break;
                case POSITION_CENTER:{
                    sPosition = POSITION_LEFT;
                }break;
                case POSITION_RIGHT:{
                    sPosition = POSITION_CENTER;
                }break;
            }
            
            switch(tPosition){
                case POSITION_LEFT:{
                    tPosition = POSITION_RIGHT;
                }break;
                case POSITION_CENTER:{
                    tPosition = POSITION_LEFT;
                }break;
                case POSITION_RIGHT:{
                    tPosition = POSITION_CENTER;
                }break;
            }
            
            currentImagePosition++;
            
            actualX = 0;
            actualY = 0;
            actualScale = 1;
            
            requestSizes();
            
            invalidate();
        }else if(direction == DIRECTION_SLIDE_LEFT){
            slidingLeft = true;
            
            switch(fPosition){
                case POSITION_LEFT:{
                    fPosition = POSITION_CENTER;
                }break;
                case POSITION_CENTER:{
                    fPosition = POSITION_RIGHT;
                }break;
                case POSITION_RIGHT:{
                    fPosition = POSITION_LEFT;
                }break;
            }
            
            switch(sPosition){
                case POSITION_LEFT:{
                    sPosition = POSITION_CENTER;
                }break;
                case POSITION_CENTER:{
                    sPosition = POSITION_RIGHT;
                }break;
                case POSITION_RIGHT:{
                    sPosition = POSITION_LEFT;
                }break;
            }
            
            switch(tPosition){
                case POSITION_LEFT:{
                    tPosition = POSITION_CENTER;
                }break;
                case POSITION_CENTER:{
                    tPosition = POSITION_RIGHT;
                }break;
                case POSITION_RIGHT:{
                    tPosition = POSITION_LEFT;
                }break;
            }
            
            currentImagePosition--;
            
            actualX = 0;
            actualY = 0;
            actualScale = 1;
            
            requestSizes();
            
            invalidate();
        }
        
        executorService.execute(new BitmapDecodingRunnable());
        
        if(onSlideEndListener != null){
            onSlideEndListener.onSlideEnd(direction, currentImagePosition);
        }
        
        if(positionChangedListener != null){
            positionChangedListener.onPositionChanged(currentImagePosition, oldPosition);
        }
        
        touchEnabled = true;
    }
    
    private void addBitmapToStart(Bitmap bitmap){
        try{
            bitmaps[4].recycle();
        }catch(NullPointerException nPEx){
        }
        
        for(int i = 0; i < 4; i++){
            bitmaps[4 - i] = bitmaps[4 - i - 1];
        }
        
        bitmaps[0] = bitmap;
    }
    
    private void addBitmapToEnd(Bitmap bitmap){
        try{
            bitmaps[0].recycle();
        }catch(NullPointerException nPEx){
        }
        
        for(int i = 0; i < 4; i++){
            bitmaps[i] = bitmaps[i + 1];
        }
        
        bitmaps[4] = bitmap;
    }
    
    private void drawCenter(){
        if(fPosition == POSITION_CENTER){
            firstImgView.setImageBitmap(bitmaps[2]);
        }else if(sPosition == POSITION_CENTER){
            secondImageView.setImageBitmap(bitmaps[2]);
        }else if(tPosition == POSITION_CENTER){
            thirdImageView.setImageBitmap(bitmaps[2]);
        }
    }
    
    private void drawRight(){
        if(fPosition == POSITION_RIGHT){
            firstImgView.setImageBitmap(bitmaps[3]);
        }else if(sPosition == POSITION_RIGHT){
            secondImageView.setImageBitmap(bitmaps[3]);
        }else if(tPosition == POSITION_RIGHT){
            thirdImageView.setImageBitmap(bitmaps[3]);
        }
    }
    
    private void drawLeft(){
        if(fPosition == POSITION_LEFT){
            firstImgView.setImageBitmap(bitmaps[1]);
        }else if(sPosition == POSITION_LEFT){
            secondImageView.setImageBitmap(bitmaps[1]);
        }else if(tPosition == POSITION_LEFT){
            thirdImageView.setImageBitmap(bitmaps[1]);
        }
    }

    /**
     * @return the imagePaths
     */
    public ArrayList<String> getImagePaths() {
        return imagePaths;
    }

    /**
     * @param imagePaths the imagePaths to set
     */
    public void setImagePaths(ArrayList<String> imagePaths) {
        this.imagePaths = imagePaths;
        
        if(!initialized){
            if(!imagePaths.isEmpty()){
                init();
            
                initialized = true;
            }
        }
    }
    
    /**
     * @param position the currentImagePosition to set
     */
    public void setPosition(int position) {
        int oldPosition = currentImagePosition;
        
        this.currentImagePosition = position;
        
        if(initialized){
            executorService.execute(new InatializationBitmapsDecodingRunnable());
            
            if(positionChangedListener != null){
                positionChangedListener.onPositionChanged(currentImagePosition, oldPosition);
            }
        }
    }
    
    public void slideLeft(){
        if(canSlideLeft()){
            touchEnabled = false;
            
            handler.sendEmptyMessage(HANDLER_SLIDE_LEFT);
        }
    }
    
    public void slideRight(){
        if(canSlideRight()){
            touchEnabled = false;
            
            handler.sendEmptyMessage(HANDLER_SLIDE_RIGHT);
        }
    }
    
    public boolean canSlideLeft(){
        return currentImagePosition > 0;
    }
    
    public boolean canSlideRight(){
        return currentImagePosition < (imagePaths.size() - 1);
    }

    /**
     * @param onSlideEndListener the onSlideEndListener to set
     */
    public void setOnSlideEndListener(OnSlideEndListener onSlideEndListener) {
        this.onSlideEndListener = onSlideEndListener;
    }
    
    public void setPositionChangedListener(PositionChangedListener positionChangedListener){
        this.positionChangedListener = positionChangedListener;
    }
    
    private class BitmapDecodingRunnable implements Runnable{

        public void run() {
            int neededPosition = 0;
            
            if(slidingLeft){
                neededPosition = currentImagePosition - 2;
            }else{
                neededPosition = currentImagePosition + 2;
            }
            
            if((neededPosition > -1) && (neededPosition < imagePaths.size())){
                Bitmap tmpBmp = decodeImageFile(neededPosition);
                
                if(slidingLeft){
                    addBitmapToStart(tmpBmp);
                }else{
                    addBitmapToEnd(tmpBmp);
                }
            }else{
                if(slidingLeft){
                    addBitmapToStart(null);
                }else{
                    addBitmapToEnd(null);
                }
            }
            
            handler.sendEmptyMessage(DECODING_END);
        }
        
    }
    
    private class InatializationBitmapsDecodingRunnable implements Runnable{

        public void run() {
            bitmaps[2] = decodeImageFile(currentImagePosition);
            
            handler.sendEmptyMessage(HANDLER_INIT_CENTER_DECODED);
            
            if(currentImagePosition == imagePaths.size() - 1){
                bitmaps[3] = null;
            }else{
                bitmaps[3] = decodeImageFile(currentImagePosition + 1);
            }
            
            handler.sendEmptyMessage(HANDLER_INIT_RIGHT_DECODED);
            
            if(currentImagePosition == 0){
                bitmaps[1] = null;
            }else{
                bitmaps[1] = decodeImageFile(currentImagePosition - 1);
            }
            
            handler.sendEmptyMessage(HANDLER_INIT_LEFT_DECODED);
            
            if(currentImagePosition > imagePaths.size() - 3){
                bitmaps[4] = null;
            }else{
                bitmaps[4] = decodeImageFile(currentImagePosition + 2);
            }
            
            if(currentImagePosition < 2){
                bitmaps[0] = null;
            }else{
                bitmaps[0] = decodeImageFile(currentImagePosition - 2);
            }
        }
        
    }
    
    private Bitmap decodeImageFile(int position){
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        
        BitmapFactory.decodeFile(imagePaths.get(position), opts);
        
        int width = opts.outWidth, height = opts.outHeight;
        int scale = 1;
        while(true){
            if(width/2 < getWidth() || height/2 < getHeight())
            break;
            width /=2;
            height /=2;
            scale *=2;
        }

        opts = new BitmapFactory.Options();
        opts.inSampleSize = scale;
        
        return BitmapFactory.decodeFile(imagePaths.get(position), opts);
    }
    
    private class ScaleDetectorListener implements ScaleGestureDetector.OnScaleGestureListener{
        
        float scaleFocusX = 0;
        float scaleFocusY = 0;

        public boolean onScale(ScaleGestureDetector arg0) {
            float previousScale = actualScale;
            int previousX = actualX;
            int previousY = actualY;
            
            float scale = arg0.getScaleFactor() * actualScale * 100 / 100;
            
            if(scale < MINIMUM_SCALE){
                actualScale = MINIMUM_SCALE;
                
                actualX = 0;
                actualY = 0;
            }else if(scale > MAXIMUM_SCALE){
                actualScale = MAXIMUM_SCALE;
            }else{
                actualScale = scale;
                
                float a1 = scaleFocusX - previousX;
            
                actualX = (int)Math.round(previousX - ((scaleFocusX - previousX)*(arg0.getScaleFactor() - 1)));
                actualY = (int)Math.round(previousY - ((scaleFocusY - previousY)*(arg0.getScaleFactor() - 1)));
            }

            requestSizes();
            
            invalidate();
            
            return true;
        }

        public boolean onScaleBegin(ScaleGestureDetector arg0) {
            invalidate();
            
            scaleFocusX = arg0.getFocusX();
            scaleFocusY = arg0.getFocusY();
            
            return true;
        }

        public void onScaleEnd(ScaleGestureDetector arg0) {
            scaleFocusX = 0;
            scaleFocusY = 0;
        }
    }
    
    public interface PositionChangedListener{
        
        public void onPositionChanged(int newPosition, int oldPosition);
        
    }
    
    public interface OnSlideEndListener{
        
        public void onSlideEnd(int direction, int currentPosition);
        
    }
    
}
