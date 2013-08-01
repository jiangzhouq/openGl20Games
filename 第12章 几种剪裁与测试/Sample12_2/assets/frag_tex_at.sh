precision mediump float;
varying vec2 vTextureCoord; //接收从顶点着色器过来的参数
uniform sampler2D sTexture;//纹理内容数据
void main() { 
   vec4 bcolor = texture2D(sTexture, vTextureCoord);//给此片元从纹理中采样出颜色值 
   if(bcolor.a<0.6) {
   		discard;
   } else {
      gl_FragColor=bcolor;
}}