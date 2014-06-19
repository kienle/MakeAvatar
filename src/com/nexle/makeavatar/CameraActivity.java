package com.nexle.makeavatar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class CameraActivity extends Activity implements OnClickListener, Callback, PictureCallback {
	
	public static final String REACTION_BITMAP = "reaction_bitmap";
	public static final String PHOTO_PATH_EXTRA = "photo_path";
	public static final String PHOTO_ID_EXTRA = "photo_id";
	public static final String CAMERA_DEGREES = "camera_degree";
	public static final String FROM_GALLERY = "from_gallery";
	
    private ImageButton mIbCamera;
    private SurfaceView mSurfaceView;
    
    private ProgressDialog mProgressDialog;

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private boolean isPreviewing = false;

    private int mCurrentCameraId = CameraInfo.CAMERA_FACING_BACK;
    private int mCameraDegrees = 0;
    private SaveImageTask mTask;
    private CopyImageTask mTaskCopy;
    
    private Boolean[] mImageList = new Boolean[4];

    private LinearLayout mLlReset;
    private ImageButton mIbChangeCam;
    
    private ImageView mIvAvatar1;
    private ImageView mIvAvatar2;
    private ImageView mIvAvatar3;
    private ImageView mIvAvatar4;
    
    private ImageButton mIbDelete;
    private ImageButton mIbCheck;
    
    private static final String SMALL_AVATAR_NAME = "avatar_";
    private ArrayList<Bitmap> mImageBitmaps = new ArrayList<Bitmap>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
//    	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview);
        
        mSurfaceView = (SurfaceView) findViewById(R.id.surface);
        
        mIbCamera = (ImageButton) findViewById(R.id.ibCapture);
        mIbCamera.setOnClickListener(this);
        
        if (savedInstanceState != null) {
            mCurrentCameraId = savedInstanceState.getInt("cameraId", CameraInfo.CAMERA_FACING_BACK);
        }
        
        mLlReset = (LinearLayout) findViewById(R.id.llReset);
        mIbChangeCam = (ImageButton) findViewById(R.id.ibChangeCam);
        mLlReset.setOnClickListener(this);
        mIbChangeCam.setOnClickListener(this);
        
        mIvAvatar1 = (ImageView) findViewById(R.id.ivSmallAva1);
        mIvAvatar2 = (ImageView) findViewById(R.id.ivSmallAva2);
        mIvAvatar3 = (ImageView) findViewById(R.id.ivSmallAva3);
        mIvAvatar4 = (ImageView) findViewById(R.id.ivSmallAva4);
        
        mIbDelete = (ImageButton) findViewById(R.id.ibDelete);
        mIbCheck = (ImageButton) findViewById(R.id.ibCheck);
        
        mIbDelete.setOnClickListener(this);
        mIbCheck.setOnClickListener(this);
        
        mProgressDialog = DialogUtils.createProgressDialog(this, "Loading ...");
        
        for (int i = 0; i < 4; i++) {
        	mImageList[i] = false;
        }
    }
    

    @Override
    protected void onResume() {
        super.onResume();
        
        initCameraPanel();
        
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        
        mHolder = mSurfaceView.getHolder();
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mHolder.addCallback(this);

    }

    /**
     *  int for set key and get key from result activity . 
     */
    private static final int SELECT_IMAGE = 0;

    public void onGoToLibrary(View v) {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(i, SELECT_IMAGE);
        
//        Intent i = new Intent(Action.ACTION_MULTIPLE_PICK);
//		startActivityForResult(i, SELECT_MULTIPLE_IMAGE);
    }
    
    private void showLoadingDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.show();
        }
    }

    private void hideLoadingDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_IMAGE) {
                if (data != null) {
                    String picturePath = null;
                    if (ContentResolver.SCHEME_CONTENT.equals(data.getData().getScheme())) {
                        String[] filePathColumn = { MediaStore.Images.Media.DATA };

                        Cursor cursor = getContentResolver().query(data.getData(),
                                filePathColumn, null, null, null);
                        cursor.moveToFirst();

                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        picturePath = cursor.getString(columnIndex);
                        cursor.close();
                    } else if (ContentResolver.SCHEME_FILE.equals(data.getData().getScheme())) {
                        picturePath = data.getDataString();
                    }
                    
                    if (picturePath != null) {
                        showLoadingDialog();
                        mIbCamera.setEnabled(false);
                        if (mTaskCopy != null) {
                            mTaskCopy.cancel(true);
                        }
                        String photoId = StringUtils.getNewPhotoID();
                        String path = getExternalCacheDir().getAbsolutePath();
                        mTaskCopy = new CopyImageTask(this, path, photoId);
                        mTaskCopy.execute(picturePath);
                    }

                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideLoadingDialog();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHolder.removeCallback(this);
        stopAndReleaseCamera();
    }

    private void initCameraPanel() {
        int numOfCamera = 1;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
            numOfCamera = Camera.getNumberOfCameras();
        }

        if (numOfCamera <= 1) {
        	mIbChangeCam.setVisibility(View.GONE);
        } else {
        	mIbChangeCam.setVisibility(View.VISIBLE);
        }
    }

    private void openCamera(int cameraId) {
        stopAndReleaseCamera();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
            mCamera = Camera.open(cameraId);
        } else {
            mCamera = Camera.open();
        }
        mCurrentCameraId = cameraId;
        setUpCamera();
    }

    private void stopAndReleaseCamera() {
        isPreviewing = false;
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private void startCameraPreview() {
        if (mCamera != null) {
        	Log.d("KienLT", "startCameraPreview");
            mCamera.startPreview();
            isPreviewing = true;
        } else {
            isPreviewing = false;
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //Handle config change myself
        super.onConfigurationChanged(newConfig);
        setUpCamera();
    }

    private void setUpCameraDegrees() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
        case Surface.ROTATION_0:
            mCameraDegrees = 90;
            break;
        case Surface.ROTATION_90:
            mCameraDegrees = 0;
            break;
        case Surface.ROTATION_180:
            mCameraDegrees = 270;
            break;
        case Surface.ROTATION_270:
            mCameraDegrees = 180;
            break;
        }
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setDisplayOrientation(mCameraDegrees);
        }
    }

    private void setUpCamera() {
        setUpCameraDegrees();
        Camera.Parameters camParameters = mCamera.getParameters();
        camParameters.setPictureFormat(ImageFormat.JPEG);
        camParameters.setJpegQuality(50);
        List<String> supportedFocusModes = camParameters.getSupportedFocusModes();
        if (supportedFocusModes != null) {
            if (supportedFocusModes.contains(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                camParameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else if (supportedFocusModes.contains(Parameters.FOCUS_MODE_AUTO)) {
                camParameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
            }
        }
        
        List<Size> supportedSizes = camParameters.getSupportedPictureSizes();
        int chosenWidth = 0;
        int chosenHeight = 0;
        for (Size size : supportedSizes) {
            if (size.width > chosenWidth) {
                chosenWidth = size.width;
                chosenHeight = size.height;
            }
        }
        
        Log.d("KienLT", "SELECTED PICTURE SIZE " + chosenWidth + " " + chosenHeight);
        camParameters.setPictureSize(chosenWidth, chosenHeight);

        camParameters.setRotation(90); //set rotation to save the picture
        mCamera.setDisplayOrientation(mCameraDegrees); //set the rotation for preview camera

        mCamera.setParameters(camParameters);
    }

    @Override
    public void onClick(View v) {
    	if (v == mIbCamera) {
	        if (mCamera != null && isPreviewing) {
	            mCamera.takePicture(null, null, null, this);
	        }
    	} else if (v == mIbChangeCam) {
    		onChangeCam();
    	} else if (v == mLlReset) {
    		deleteAllAvatar();
    	} else if (v == mIbDelete) {
    		finish();
    	} else if (v == mIbCheck) {
    		// ToDo
    	}
    }

    private void onChangeCam() {
        if (mCurrentCameraId == CameraInfo.CAMERA_FACING_BACK) {
            openCamera(CameraInfo.CAMERA_FACING_FRONT);
        } else {
            openCamera(CameraInfo.CAMERA_FACING_BACK);
        }
        if (mHolder != null) {
            try {
                mCamera.setPreviewDisplay(mHolder);
                startCameraPreview();
            } catch (IOException e) {
                mCamera.release();
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    	Log.d("KienLT", "surfaceCreated");
        try {
            if (mCamera == null) {
            	Log.d("KienLT", "surfaceCreated openCamera");
                openCamera(mCurrentCameraId);
            }
            mHolder = holder;
            mCamera.setPreviewDisplay(holder);

        } catch (IOException e) {
            e.printStackTrace();
            mCamera.release();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    	Log.d("KienLT", "surfaceChanged");
        startCameraPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("KienLT", "surfaceDestroyed");
        stopAndReleaseCamera();
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        if (data != null) {
            camera.stopPreview();
            mIbCamera.setEnabled(false);
            if (mTask != null) {
                mTask.cancel(true);
            }
            
            String photoId = "";
            for (int i = 0; i < 4; i++) {
            	if (!mImageList[i]) {
            		photoId = "avatar_" + i;
            		break;
            	}
            }
//            String photoId = StringUtils.getNewPhotoID();
            String path = null;
            long availabelInternalMem = getAvailableInternalMemorySize();
            if (availabelInternalMem < 1024 * 1024 * 2) {
                availabelInternalMem = getAvailableExternalMemorySize();
                if (availabelInternalMem == -1 || availabelInternalMem < 1024 * 1024 * 2) {
                    path = null;
                } else {
                    File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    if (dir != null) {
                        path = dir.getAbsolutePath();
                    }
                }
            } else {
            	File dir = getDir("photos", MODE_PRIVATE);;
            	
                if (dir != null) {
                    path = dir.getAbsolutePath();
                    Log.d("KienLT", "[CameraActivity] path = " + path);
                }
            }
            
            if (path != null) {
                showLoadingDialog();
                if (mCurrentCameraId == CameraInfo.CAMERA_FACING_FRONT
                        && (mCameraDegrees == 90 || mCameraDegrees == 270)) {
                    mCameraDegrees = mCameraDegrees + 180;
                }
                mTask = new SaveImageTask(this, path, photoId, mCameraDegrees);
                mTask.execute(data);
            } else {
                DialogUtils.buildAlertDialog(this, R.string.dialog_title, R.string.storage_low_error, false).show();
            }
        } else {
            startCameraPreview();
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("cameraId", mCurrentCameraId);
        super.onSaveInstanceState(outState);

    }

    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    public static boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    public static long getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize;
        } else {
            return -1;
        }
    }

    private class CopyImageTask extends AsyncTask<String, Void, String> {
        private WeakReference<CameraActivity> mContext;
        private String mFilename;
        private String mPath;
        private String mPhotoId;

        public CopyImageTask(CameraActivity context, String path, String photoId) {
            mContext = new WeakReference<CameraActivity>(context);
            mFilename = photoId + ".jpg";
            mPath = path;
            mPhotoId = photoId;
        }
        
        @Override
        protected String doInBackground(String... params) {
            String result = null;
            if (!isCancelled()) {
                String existingPath = params[0];
                File existingFile = new File(existingPath);
                if (existingFile.exists()) {
                    File file = new File(mPath, mFilename);
                    FileChannel fos;
                    FileChannel fin;
                    try {
                        fin = (new FileInputStream(existingFile)).getChannel();
                        fos = (new FileOutputStream(file)).getChannel();
                        fos.transferFrom(fin, 0, fin.size());
                        fin.close();
                        fos.close();
                        result = mPath + "/" + mFilename;
                    } catch (IOException e) {
                        e.printStackTrace();
                        result = null;
                    }
                } else {
                    Log.d("KienLT", "SOURCE FILE NOT EXIST");
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
        	mIbCamera.setEnabled(true);
        	Log.d("KienLT", "select photo success! result = " + result);
        	hideLoadingDialog();
        }
    }

    private class SaveImageTask extends AsyncTask<byte[], Void, String> {
        private WeakReference<CameraActivity> mContext;
        private String mFilename;
        private String mPath;
        private int mCameraDegree;
//        private String mPhotoId;

        public SaveImageTask(CameraActivity context, String path, String photoId, int cameraDegree) {
            mContext = new WeakReference<CameraActivity>(context);
            mFilename = photoId + ".jpg";
            mPath = path;
            mCameraDegree = cameraDegree;
//            mPhotoId = photoId;
        }
        
        @Override
        protected void onPreExecute() {
        	super.onPreExecute();
        	setOrientation(mCameraDegree);
        }
        
        @Override
        protected String doInBackground(byte[]... params) {
            String result = null;
            if (!isCancelled()) {
                byte[] data = params[0];
                if (data != null) {
                    File file = new File(mPath, mFilename);
                    FileOutputStream fos;
                    try {
                        fos = new FileOutputStream(file);
                        fos.write(data);
                        result = file.getAbsolutePath();
                        int degree = mCameraDegree % 360;
                        Log.d("KienLT", "[CameraActivity] SaveImageTask path = " + result + ", degree = " + degree);
                        int tagOrientation = ExifInterface.ORIENTATION_NORMAL;
                        switch (degree) {
                        case 90:
                            tagOrientation = ExifInterface.ORIENTATION_ROTATE_90;
                            break;
                        case 180:
                            tagOrientation = ExifInterface.ORIENTATION_ROTATE_180;
                            break;
                        case 270:
                            tagOrientation = ExifInterface.ORIENTATION_ROTATE_270;
                            break;
                        }
                        
                        fos.flush();
                        fos.close();

                        ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
                        exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, tagOrientation + "");
                        exifInterface.saveAttributes();

                    } catch (IOException e) {
                        e.printStackTrace();
                        result = null;
                    }
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
        	
//        	try {
//		        ExifInterface exif = new ExifInterface(result);
//		        int orientation = exif.getAttributeInt(
//		                ExifInterface.TAG_ORIENTATION,
//		                ExifInterface.ORIENTATION_NORMAL);
//
//		        int angle = 0;
//
//		        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
//		            angle = 90;
//		        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
//		            angle = 180;
//		        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
//		            angle = 270;
//		        }
//
//		        Matrix mat = new Matrix();
//		        mat.postRotate(angle);
//		        BitmapFactory.Options options = new BitmapFactory.Options();
//		        options.inSampleSize = 2;
//
//		        Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(imgFile),
//		                null, options);
//		        Bitmap bitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
//		                bmp.getHeight(), mat, true);
//		        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//		        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
//		        mIvAvatar1.setImageBitmap(bitmap);
//
//		    } catch (IOException e) {
//		        Log.w("TAG", "-- Error in setting image");
//		    } catch (OutOfMemoryError oom) {
//		        Log.w("TAG", "-- OOM Error in setting image");
//		    }
             
        	mIbCamera.setEnabled(true);
        	for (int i = 0; i < 4; i++) {
            	if (!mImageList[i]) {
            		mImageList[i] = true;
            		break;
            	}
            }
        	Log.d("KienLT", "captrue success! result = " + result);
        	hideLoadingDialog();
        	
        	showSmallAvatar();
        	
        	if (mImageBitmaps.size() == 4) {
//        		saveGifFile();
        	}
        	
        	try {
                startCameraPreview();
                mCamera.setPreviewDisplay(mHolder);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public byte[] generateGIF() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        encoder.start(bos);
        for (Bitmap bitmap : mImageBitmaps) {
            encoder.addFrame(bitmap);
        }
        encoder.finish();
        return bos.toByteArray();
    }
    
    private void saveGifFile() {
    	String path = "";
    	File dir = getDir("photos", MODE_PRIVATE);;
        if (dir != null) {
            path = dir.getAbsolutePath();
        }
        
    	FileOutputStream outStream = null;
        try {
//            outStream = new FileOutputStream(path + "/avatar.gif");
            outStream = new FileOutputStream(new File(Environment.getExternalStorageDirectory(), "Rotate/test.gif"));
            outStream.write(generateGIF());
            outStream.close();
            Log.d("KienLT", "create gif conplete");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private void showSmallAvatar() {
    	String path = "";
    	File dir = getDir("photos", MODE_PRIVATE);;
        if (dir != null) {
            path = dir.getAbsolutePath();
        }
        
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inSampleSize = 8;
        
    	File imgFile = null;
    	if (mImageList[0]) {
    		imgFile = new File(path, SMALL_AVATAR_NAME + 0 + ".jpg");
    		if (imgFile.exists()) {
    			
//    			try {
//    		        ExifInterface exif = new ExifInterface(imgFile.getPath());
//    		        int orientation = exif.getAttributeInt(
//    		                ExifInterface.TAG_ORIENTATION,
//    		                ExifInterface.ORIENTATION_NORMAL);
//
//    		        int angle = 0;
//
//    		        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
//    		            angle = 90;
//    		        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
//    		            angle = 180;
//    		        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
//    		            angle = 270;
//    		        }
//
//    		        Matrix mat = new Matrix();
//    		        mat.postRotate(angle);
//    		        BitmapFactory.Options options = new BitmapFactory.Options();
//    		        options.inSampleSize = 2;
//
//    		        Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(imgFile),
//    		                null, options);
//    		        Bitmap bitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
//    		                bmp.getHeight(), mat, true);
//    		        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//    		        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
//    		        mIvAvatar1.setImageBitmap(bitmap);
//
//    		    } catch (IOException e) {
//    		        Log.w("TAG", "-- Error in setting image");
//    		    } catch (OutOfMemoryError oom) {
//    		        Log.w("TAG", "-- OOM Error in setting image");
//    		    }
    			
    			Bitmap avarBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), opt);
    			mIvAvatar1.setImageBitmap(avarBitmap);
    			mImageBitmaps.add(avarBitmap);
    		}
    	}
    	
    	if (mImageList[1]) {
    		imgFile = new File(path, SMALL_AVATAR_NAME + 1 + ".jpg");
    		if (imgFile.exists()) {
    			Bitmap avarBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), opt);
    			mIvAvatar2.setImageBitmap(avarBitmap);
    			mImageBitmaps.add(avarBitmap);
    		}
    	}
    	
    	if (mImageList[2]) {
    		imgFile = new File(path, SMALL_AVATAR_NAME + 2 + ".jpg");
    		if (imgFile.exists()) {
    			Bitmap avarBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), opt);
    			mIvAvatar3.setImageBitmap(avarBitmap);
    			mImageBitmaps.add(avarBitmap);
    		}
    	}
    	
    	if (mImageList[3]) {
    		imgFile = new File(path, SMALL_AVATAR_NAME + 3 + ".jpg");
    		if (imgFile.exists()) {
    			Bitmap avarBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), opt);
    			mIvAvatar4.setImageBitmap(avarBitmap);
    			mImageBitmaps.add(avarBitmap);
    		}
    	}
    	
    }
    
    private void deleteAllAvatar() {
    	String path = "";
    	for (int i = 0 ; i < 4 ; i ++) {
    		
    		File dir = getDir("photos", MODE_PRIVATE);;
            if (dir != null) {
                path = dir.getAbsolutePath();
            }
            
        	File imgFile = new File(path, SMALL_AVATAR_NAME + i + ".jpg");
        	if (imgFile.exists()) {
        		imgFile.delete();
        	}
    	}
    	
    	for (int i = 0; i < 4; i++) {
        	mImageList[i] = false;
        }
    	
    	mIvAvatar1.setImageResource(R.drawable.small_avatar);
    	mIvAvatar2.setImageResource(R.drawable.small_avatar);
    	mIvAvatar3.setImageResource(R.drawable.small_avatar);
    	mIvAvatar4.setImageResource(R.drawable.small_avatar);
    }
    
    private void cropImage(Bitmap bitmap) {
    	Bitmap faceTemplate = BitmapFactory.decodeResource(getResources(), R.drawable.face_oblong1);
        faceTemplate = Bitmap.createScaledBitmap(faceTemplate, bitmap.getWidth(), bitmap.getHeight(), true);
        // Crop image using the correct template size.
        Bitmap croppedImg = ImageProcess.cropImage(bitmap, faceTemplate, bitmap.getWidth(), bitmap.getHeight());
//                savePhoto(croppedImg);
    }
    
	private void setOrientation(int cameraDegree) {
		int degree = cameraDegree % 360;
//		Log.d("KienLT", "screen orientation = "
//				+ getResources().getConfiguration().orientation + " degree = "
//				+ degree);
		switch (degree) {
		case 0:
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			break;
		case 90:
			if (mCurrentCameraId == CameraInfo.CAMERA_FACING_BACK) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			} else {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
			}
			break;
		case 180:
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
			break;
		case 270:
			if (mCurrentCameraId == CameraInfo.CAMERA_FACING_BACK) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
			} else {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
			break;
		}

	}
}
