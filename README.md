## 一个简单实用的验证码/密码输入控件

![android](https://img.shields.io/badge/platfom-android-important)

#### 一、如何使用
##### 1.引用
```gradle
implementation 'com.martinchengsj:verification-code:1.0.1'
```
##### 2.实例
```xml
<com.martingcheng.yzm.VerificationCodeView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:drawablePadding="10dp"
    android:paddingLeft="30dp"
    android:paddingRight="30dp"
    android:textColor="@android:color/black"
    android:textSize="30sp"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintLeft_toLeftOf="parent"
    app:layout_constraintRight_toRightOf="parent"
    app:layout_constraintTop_toTopOf="parent"
    app:layout_constraintVertical_bias="0.38"
    app:vcv_cursorColor="#CACCD8"
    app:vcv_cursorWidth="2dp"
    app:vcv_length="4"
    app:vcv_outlook="line"
    app:vcv_outlookStrokeWidth="2dp"
    app:vcv_perSpacing="40dp" />
```
```kt
addOnVerificationCodeChangeListener(object :VerificationCodeView.OnVerificationCodeWatcher {
         override fun onTextChanged(text: CharSequence?,start: Int,lengthBefore: Int,lengthAfter: Int) {
                Log.d("VerificationCodeView","onTextChanged() called with: text = $text, start = $start, lengthBefore = $lengthBefore, lengthAfter= $lengthAfter")
            }

         override fun onTextCompleted(text: CharSequence?) {
                text?.let { Toast.makeText(this@Activity, it, Toast.LENGTH_SHORT).show() }
            }    

        })
```
##### 3.可配置的属性
```xml
 <declare-styleable name="VerificationCodeView">
    <attr name="vcv_length" format="integer" /><!--输入长度-->
    <attr name="vcv_cursorColor" format="color" /> <!--光标颜色-->
    <attr name="vcv_cursorWidth" format="dimension" /><!--光标粗细-->
    <attr name="vcv_outlook" format="enum"><!--外观样式-->
       <enum name="line" value="0" /><!--线条样式-->
       <enum name="rect" value="1" /><!--矩形样式-->
    </attr>
    <attr name="vcv_outlookStrokeWidth" format="dimension" /> <!--线条粗细-->
    <attr name="vcv_outlookStrokeNormalColor" format="color" /><!--线条颜色-->
    <attr name="vcv_outlookStrokeActiveColor" format="color" /><!--正在输入的线条颜色-->
    <attr name="vcv_perSpacing" format="dimension" /><!--每个输入框的间隔-->
    <attr name="vcv_outlookBackgroundColor" format="color" /><!--矩形框的背景色-->
    <attr name="cvc_outlookRadius" format="dimension" /><!--矩形框的圆角大小-->
    <attr name="cvc_passwordStyle" format="boolean" />    <!--是否密码输入 可以设置为圆点或者星号等-->
</declare-styleable>
```
#### 二、预览
[![s8PGwV.md.gif](https://s3.ax1x.com/2021/01/11/s8PGwV.md.gif)](https://imgchr.com/i/s8PGwV)

#### Apache License
```
   Copyright 2021 martinchengsj

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
