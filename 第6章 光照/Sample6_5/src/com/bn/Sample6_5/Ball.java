package com.bn.Sample6_5;

import java.nio.ByteBuffer;

import static com.bn.Sample6_5.Constant.*;

import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import android.opengl.GLES20;

//球
public class Ball {
	int mProgram;// 自定义渲染管线着色器程序id
	int muMVPMatrixHandle;// 总变换矩阵引用
    int muMMatrixHandle;//位置、旋转变换矩阵引用
	int muRHandle;// 球的半径属性引用   
	int maPositionHandle; // 顶点位置属性引用
    int maNormalHandle; //顶点法向量属性引用
    int maLightLocationHandle;//光源位置属性引用
    int maCameraHandle; //摄像机位置属性引用 
    
    
	String mVertexShader;// 顶点着色器
	String mFragmentShader;// 片元着色器

	FloatBuffer mVertexBuffer;// 顶点坐标数据缓冲
	FloatBuffer mNormalBuffer;//顶点法向量数据缓冲
	int vCount = 0;
	float yAngle = 0;// 绕y轴旋转的角度
	float xAngle = 0;// 绕x轴旋转的角度
	float zAngle = 0;// 绕z轴旋转的角度
	float r = 0.8f;
	public Ball(MySurfaceView mv) {
		// 初始化顶点坐标与着色数据
		initVertexData();
		// 初始化shader
		initShader(mv);
	}

	// 初始化顶点坐标数据的方法
	public void initVertexData() {
		// 顶点坐标数据的初始化================begin============================
		ArrayList<Float> alVertix = new ArrayList<Float>();// 存放顶点坐标的ArrayList
		final int angleSpan = 10;// 将球进行单位切分的角度
		for (int vAngle = -90; vAngle < 90; vAngle = vAngle + angleSpan)// 垂直方向angleSpan度一份
		{
			for (int hAngle = 0; hAngle <= 360; hAngle = hAngle + angleSpan)// 水平方向angleSpan度一份
			{// 纵向横向各到一个角度后计算对应的此点在球面上的坐标
				float x0 = (float) (r * UNIT_SIZE
						* Math.cos(Math.toRadians(vAngle)) * Math.cos(Math
						.toRadians(hAngle)));
				float y0 = (float) (r * UNIT_SIZE
						* Math.cos(Math.toRadians(vAngle)) * Math.sin(Math
						.toRadians(hAngle)));
				float z0 = (float) (r * UNIT_SIZE * Math.sin(Math
						.toRadians(vAngle)));

				float x1 = (float) (r * UNIT_SIZE
						* Math.cos(Math.toRadians(vAngle)) * Math.cos(Math
						.toRadians(hAngle + angleSpan)));
				float y1 = (float) (r * UNIT_SIZE
						* Math.cos(Math.toRadians(vAngle)) * Math.sin(Math
						.toRadians(hAngle + angleSpan)));
				float z1 = (float) (r * UNIT_SIZE * Math.sin(Math
						.toRadians(vAngle)));

				float x2 = (float) (r * UNIT_SIZE
						* Math.cos(Math.toRadians(vAngle + angleSpan)) * Math
						.cos(Math.toRadians(hAngle + angleSpan)));
				float y2 = (float) (r * UNIT_SIZE
						* Math.cos(Math.toRadians(vAngle + angleSpan)) * Math
						.sin(Math.toRadians(hAngle + angleSpan)));
				float z2 = (float) (r * UNIT_SIZE * Math.sin(Math
						.toRadians(vAngle + angleSpan)));

				float x3 = (float) (r * UNIT_SIZE
						* Math.cos(Math.toRadians(vAngle + angleSpan)) * Math
						.cos(Math.toRadians(hAngle)));
				float y3 = (float) (r * UNIT_SIZE
						* Math.cos(Math.toRadians(vAngle + angleSpan)) * Math
						.sin(Math.toRadians(hAngle)));
				float z3 = (float) (r * UNIT_SIZE * Math.sin(Math
						.toRadians(vAngle + angleSpan)));

				// 将计算出来的XYZ坐标加入存放顶点坐标的ArrayList
				alVertix.add(x1);
				alVertix.add(y1);
				alVertix.add(z1);
				alVertix.add(x3);
				alVertix.add(y3);
				alVertix.add(z3);
				alVertix.add(x0);
				alVertix.add(y0);
				alVertix.add(z0);

				alVertix.add(x1);
				alVertix.add(y1);
				alVertix.add(z1);
				alVertix.add(x2);
				alVertix.add(y2);
				alVertix.add(z2);
				alVertix.add(x3);
				alVertix.add(y3);
				alVertix.add(z3);
			}
		}
		vCount = alVertix.size() / 3;// 顶点的数量为坐标值数量的1/3，因为一个顶点有3个坐标

		// 将alVertix中的坐标值转存到一个float数组中
		float vertices[] = new float[vCount * 3];
		for (int i = 0; i < alVertix.size(); i++) {
			vertices[i] = alVertix.get(i);
		}

		// 创建顶点坐标数据缓冲
		// vertices.length*4是因为一个整数四个字节
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
		vbb.order(ByteOrder.nativeOrder());// 设置字节顺序
		mVertexBuffer = vbb.asFloatBuffer();// 转换为int型缓冲
		mVertexBuffer.put(vertices);// 向缓冲区中放入顶点坐标数据
		mVertexBuffer.position(0);// 设置缓冲区起始位置
		// 特别提示：由于不同平台字节顺序不同数据单元不是字节的一定要经过ByteBuffer
		// 转换，关键是要通过ByteOrder设置nativeOrder()，否则有可能会出问题
		
		//创建绘制顶点法向量缓冲
        ByteBuffer nbb = ByteBuffer.allocateDirect(vertices.length*4);
        nbb.order(ByteOrder.nativeOrder());//设置字节顺序
        mNormalBuffer = nbb.asFloatBuffer();//转换为float型缓冲
        mNormalBuffer.put(vertices);//向缓冲区中放入顶点坐标数据
        mNormalBuffer.position(0);//设置缓冲区起始位置     
	}

	// 初始化shader
	public void initShader(MySurfaceView mv) {
		// 加载顶点着色器的脚本内容
		mVertexShader = ShaderUtil.loadFromAssetsFile("vertex.sh",
				mv.getResources());
		// 加载片元着色器的脚本内容
		mFragmentShader = ShaderUtil.loadFromAssetsFile("frag.sh",
				mv.getResources());
		// 基于顶点着色器与片元着色器创建程序
		mProgram = ShaderUtil.createProgram(mVertexShader, mFragmentShader);
		// 获取程序中顶点位置属性引用
		maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
		// 获取程序中总变换矩阵引用
		muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        //获取位置、旋转变换矩阵引用
        muMMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMMatrix");  
		// 获取程序中球半径引用
		muRHandle = GLES20.glGetUniformLocation(mProgram, "uR");
        //获取程序中顶点法向量属性引用  
        maNormalHandle= GLES20.glGetAttribLocation(mProgram, "aNormal");
        //获取程序中光源位置引用
        maLightLocationHandle=GLES20.glGetUniformLocation(mProgram, "uLightLocation");
        //获取程序中摄像机位置引用
        maCameraHandle=GLES20.glGetUniformLocation(mProgram, "uCamera"); 
	}

	public void drawSelf() {		
    	MatrixState.rotate(xAngle, 1, 0, 0);//绕X轴转动
    	MatrixState.rotate(yAngle, 0, 1, 0);//绕Y轴转动
    	MatrixState.rotate(zAngle, 0, 0, 1);//绕Z轴转动
		// 制定使用某套着色器程序
		GLES20.glUseProgram(mProgram);
		// 将最终变换矩阵传入着色器程序
		GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false,
				MatrixState.getFinalMatrix(), 0); 
        //将位置、旋转变换矩阵传入着色器程序
        GLES20.glUniformMatrix4fv(muMMatrixHandle, 1, false, MatrixState.getMMatrix(), 0);
		// 将半径尺寸传入着色器程序
		GLES20.glUniform1f(muRHandle, r * UNIT_SIZE);  
        //将光源位置传入着色器程序   
        GLES20.glUniform3fv(maLightLocationHandle, 1, MatrixState.lightPositionFB);
        //将摄像机位置传入着色器程序   
        GLES20.glUniform3fv(maCameraHandle, 1, MatrixState.cameraFB);
        
		// 将顶点位置数据传入渲染管线
		GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT,
				false, 3 * 4, mVertexBuffer);
        //将顶点法向量数据传入渲染管线
		GLES20.glVertexAttribPointer(maNormalHandle, 3, GLES20.GL_FLOAT, false,
				3 * 4, mNormalBuffer);
		// 启用顶点位置数据
		GLES20.glEnableVertexAttribArray(maPositionHandle); 
        GLES20.glEnableVertexAttribArray(maNormalHandle);// 启用顶点法向量数据
		// 绘制球		
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);
	}
}
