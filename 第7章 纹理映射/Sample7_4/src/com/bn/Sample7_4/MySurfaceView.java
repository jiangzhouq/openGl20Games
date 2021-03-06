package com.bn.Sample7_4;
import java.io.IOException;
import java.io.InputStream;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.view.MotionEvent;
import android.opengl.GLES20;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

class MySurfaceView extends GLSurfaceView 
{
	private final float TOUCH_SCALE_FACTOR = 180.0f/320;//角度缩放比例
    private SceneRenderer mRenderer;//场景渲染器
    
    private float mPreviousX;//上次的触控位置X坐标
    private float mPreviousY;//上次的触控位置Y坐标
    
    int textureIdEarth;//系统分配的地球纹理id
    int textureIdEarthNight;//系统分配的地球夜晚纹理id
    int textureIdMoon;//系统分配的月球纹理id    

    float yAngle=0;//太阳灯光绕y轴旋转的角度
    float xAngle=0;//摄像机绕X轴旋转的角度
    
    float eAngle=0;//地球自转角度    
    float cAngle=0;//天球自转的角度
	
	public MySurfaceView(Context context) {
        super(context);
        this.setEGLContextClientVersion(2); //设置使用OPENGL ES2.0
        mRenderer = new SceneRenderer();	//创建场景渲染器
        setRenderer(mRenderer);				//设置渲染器		        
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);//设置渲染模式为主动渲染   
    }
	
	//触摸事件回调方法
    @Override 
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        switch (e.getAction()) {
        case MotionEvent.ACTION_MOVE:
        	//触控横向位移太阳绕y轴旋转
            float dx = x - mPreviousX;//计算触控笔X位移 
            yAngle += dx * TOUCH_SCALE_FACTOR;//设置太阳绕y轴旋转的角度
            float dy = y - mPreviousY;//计算触控笔Y位移 
            xAngle += dy * TOUCH_SCALE_FACTOR;//设置太阳绕y轴旋转的角度
        }
        mPreviousX = x;//记录触控笔位置
        mPreviousY = y;
        return true; 
    } 

	private class SceneRenderer implements GLSurfaceView.Renderer 
    {   
    	Earth earth;//地球
    	
        public void onDrawFrame(GL10 gl) 
        { 
        	//清除深度缓冲与颜色缓冲
            GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);   
            
            //保护现场
            MatrixState.pushMatrix();
            //地球自转
//            MatrixState.rotate(eAngle, 0, 1, 0);
        	//绘制纹理圆球
            MatrixState.rotate(-xAngle, 1, 0, 0);
            MatrixState.rotate(-yAngle, 0, 1, 0);
            earth.drawSelf(textureIdEarth,textureIdEarth);     
            MatrixState.popMatrix();
        }   

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            //设置视窗大小及位置 
        	GLES20.glViewport(0, 0, width, height); 
        	//计算GLSurfaceView的宽高比
            float d = 0.5f;
            float w = (float) Math.sqrt((1-d*d)/2);
            float h= (float) height * w / width;
            
            //调用此方法计算产生透视投影矩阵
//            MatrixState.setProjectOrtho(-ratio/2.0f, ratio/2.0f, -0.5f, 0.5f, 0.0f, 100);
            MatrixState.setProjectFrustum(-w, w, -h, h, d, 100);
            //调用此方法产生摄像机9参数位置矩阵
            MatrixState.setCamera(0,0,0.0f,0.0f,0f,-1.0f,0.0f,1.0f,0.0f);       
            //打开背面剪裁
            GLES20.glFrontFace(GLES20.GL_CW);
            GLES20.glEnable(GLES20.GL_CULL_FACE);  
            //初始化纹理
            textureIdEarth=initTexture(R.drawable.earth);
            textureIdEarthNight=initTexture(R.drawable.earthn);
            textureIdMoon=initTexture(R.drawable.moon);            
            //设置太阳灯光的初始位置
            MatrixState.setLightLocationSun(0.5f,0.5f,0.5f);       
            
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            //设置屏幕背景色RGBA
            GLES20.glClearColor(0.0f,0.0f,0.0f, 1.0f);  
            //创建地球对象 
            earth=new Earth(MySurfaceView.this,2.0f);
            //打开深度检测
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            //初始化变换矩阵
            MatrixState.setInitStack();  
        }
    }
	
	public int initTexture(int drawableId)//textureId
	{
		//生成纹理ID
		int[] textures = new int[1];
		GLES20.glGenTextures
		(
				1,          //产生的纹理id的数量
				textures,   //纹理id的数组
				0           //偏移量
		);    
		int textureId=textures[0];    
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
        
        //通过输入流加载图片===============begin===================
        InputStream is = this.getResources().openRawResource(drawableId);
        Bitmap bitmapTmp;
        try 
        {
        	bitmapTmp = BitmapFactory.decodeStream(is);
        } 
        finally 
        {
            try 
            {
                is.close();
            } 
            catch(IOException e) 
            {
                e.printStackTrace();
            }
        }
        //通过输入流加载图片===============end=====================  
        
        //实际加载纹理
        GLUtils.texImage2D
        (
        		GLES20.GL_TEXTURE_2D,   //纹理类型，在OpenGL ES中必须为GL10.GL_TEXTURE_2D
        		0, 					  //纹理的层次，0表示基本图像层，可以理解为直接贴图
        		bitmapTmp, 			  //纹理图像
        		0					  //纹理边框尺寸
        );
        bitmapTmp.recycle(); 		  //纹理加载成功后释放图片
        
        return textureId;
	}
}
