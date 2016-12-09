package org.qcode.resourcedetector.base.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import org.qcode.resourcedetector.base.UITaskRunner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 与UI相关的帮助类
 *
 */
public final class UIUtil {

	private static final String TAG = "UIUtil";

	private UIUtil() { }

	/**
	* 根据手机的分辨率从 dip 的单位 转成为 px(像素)
	*/
	public static int dip2px(Context context, double dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5);
	}

	/**
	* 根据手机的分辨率从 px(像素) 的单位 转成为 dip
	*/
	public static int px2dip(Context context, double pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5);
	}

	/**
     * 获取状态栏的高度
     *
     * @param window 窗口
     * @return  状态栏高度（单位：px）
     */
    public static int getStatusBarHeight(Window window) {
    	int statusBarHeight = 0;
		if (null != window) {
			//获取状态栏的高度
			Rect frame = new Rect();
			window.getDecorView().getWindowVisibleDisplayFrame(frame);
			statusBarHeight = frame.top;
		}
		if (0 == statusBarHeight) {
			statusBarHeight = 50;
		}
		return statusBarHeight;
	}

    /**
     * 显示或者隐藏状态栏
     * @param window
     * @param show    是否显示
     */
    public static void showOrHideStatusBar(Window window, boolean show) {
    	if (window == null) {
			return;
		}
    	WindowManager.LayoutParams lp = window.getAttributes();
        if (show) {
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            window.setAttributes(lp);
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            lp.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.setAttributes(lp);
            window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    /**
     * 开启Window级别的硬件加速
     * @param window
     */
	public static void openHardwareAccelarate(Window window) {
		Logging.d(TAG, "openHardwareAccelarate()");

		//Android从3.0开始提供此接口
		if (Build.VERSION.SDK_INT >= 11) {
			try {
	    		@SuppressWarnings("rawtypes")
				Class clazz = Class.forName("android.view.WindowManager$LayoutParams");
	    		Field flagField = clazz.getDeclaredField("FLAG_HARDWARE_ACCELERATED");
	    		Field maskField = clazz.getDeclaredField("FLAG_HARDWARE_ACCELERATED");
	    		int flags = flagField.getInt(null);
	    		int mask = maskField.getInt(null);
				Method method = Window.class.getDeclaredMethod("setFlags", int.class, int.class);
				method.invoke(window, flags, mask);
			} catch (Exception e) {
				Logging.d(TAG, "openHardwareAccelarate()", e);
			}
		}
	}

	/**
     * 关闭View级别的硬件加速
     * @param view
     */
	public static void closeLayerHardware(View view) {
		if (Build.VERSION.RELEASE.startsWith("4.")) {
			try {
				@SuppressWarnings("rawtypes")
				Class[] argClass = new Class[2];
				argClass[0] = int.class;
				argClass[1] = Paint.class;
				Method setLayerType = View.class.getDeclaredMethod(
						"setLayerType", argClass);
				setLayerType.setAccessible(true);

				Field LAYER_TYPE_SOFTWARE = View.class
						.getDeclaredField("LAYER_TYPE_SOFTWARE");
				LAYER_TYPE_SOFTWARE.setAccessible(true);
				int value = LAYER_TYPE_SOFTWARE.getInt(null);

				Object[] argValue = new Object[2];
				argValue[0] = value;
				argValue[1] = null;
				setLayerType.invoke(view, argValue);
			} catch (Exception e) {
				Logging.d(TAG, "closeLayerHardware()", e);
			}
		}
	}

	/**
	 * 弹出软键盘
	 * @param context    上下文
	 * @param view       在哪个视图上显示
	 */
	public static void showSoftInput(Context context, View view) {
		if (context == null || view == null) {
			return;
		}

		final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
		}
	}

	/**
	 * 弹出软键盘
	 * @param context    上下文
	 * @param view       在哪个视图上显示
	 * @param delayTime  延迟时间（单位：ms）
	 */
	public static void showSoftInputDelay(final Context context, final View view, int delayTime) {
		if (context == null || view == null || delayTime < 0) {
			return;
		}

		if (delayTime == 0) {
			showSoftInput(context, view);
		} else {
			Timer timer = new Timer();
	        timer.schedule(new TimerTask() {

				@Override
				public void run() {
		            showSoftInput(context, view);
				}
			}, delayTime);
		}
	}

	/**
	 * 隐藏软键盘
	 * @param context    上下文
	 * @param view       在哪个视图上显示
	 */
	public static void hideSoftInput(Context context, View view) {
		if (context == null || view == null) {
			return;
		}

		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null && imm.isActive(view)) {
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	/**
	 * 输入法是否已激活
	 * @param context
	 * @param packageName 输入法应用的包名
	 * @return
	 */
	public static boolean isInputMethodEnabled(Context context, String packageName) {
		return getEnableInputMethodInfor(context, packageName) != null;
	}

	/**
	 * 获取已激活输入法的详细信息
	 * @param context
	 * @param packageName 输入法应用的包名
	 * @return
	 */
	public static InputMethodInfo getEnableInputMethodInfor(Context context, String packageName) {
		if (packageName == null) {
			return null;
		}
		final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		List<InputMethodInfo> imeInfoList = imm.getEnabledInputMethodList();

		if (imeInfoList != null) {
			for (InputMethodInfo imeInfo : imeInfoList) {
				if (packageName.equals(imeInfo.getPackageName())) {
					return imeInfo;
				}
			}
		}
		return null;
	}

	/**
	 * 输入法是否已启用
	 * @param context
	 * @param packageName 输入法应用的包名
	 * @return
	 */
	public static boolean isInputMethodInUse(Context context, String packageName) {
		InputMethodInfo imeInfo = getEnableInputMethodInfor(context, packageName);
		if (imeInfo != null) {
			String ourId = imeInfo.getId();
			// 当前输入法id
			String curId = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.DEFAULT_INPUT_METHOD);

			if (ourId != null && ourId.equals(curId)) {
				return true;
			}
			return false;
		}
		return false;
	}

	private static class XY {
		//含有2个元素的一维数组，表示距离屏幕左上角的点，此处作为一个域变量是为了避免重复new
		private static int[] locationOfViewOnScreen = new int[2];
	}
	/**
	 * 判断触摸点是否在给定的view上
	 * @param event  触摸事件
	 * @param view   给定的view
	 * @return
	 */
	public static boolean isInMyView(MotionEvent event, View view) {
		//如果此时view被隐藏掉了，触摸点肯定不会落在此view上
		if (view.getVisibility() == View.GONE) {
			return false;
		}

		//获取此view在屏幕上的位置（以屏幕左上角为参照点）
		view.getLocationOnScreen(XY.locationOfViewOnScreen);

		//获取触摸点相对于屏幕左上角的偏移量
		float rawX = event.getRawX();
		float rawY = event.getRawY();

		//如果触摸点处于此view的矩形区域内
		return rawX >= XY.locationOfViewOnScreen[0]
			&& rawX <= (XY.locationOfViewOnScreen[0] + view.getWidth())
			&& rawY >= XY.locationOfViewOnScreen[1]
			&& rawY <= (XY.locationOfViewOnScreen[1] + view.getHeight());
	}

    /**
     * 显示Toast
     */
    public static void showToast(Context context, String toast) {
        toast(context, toast, false);
    }

	private static Toast mStaticToastImpl = null;
    public static void toast(final Context context, final String msg, final boolean isLong) {
		if (null == mStaticToastImpl) {
			//需要在主线程创建toast实例
			if (Looper.myLooper() != Looper.getMainLooper()) {
				UITaskRunner.getHandler().post(new Runnable() {
					@Override
					public void run() {
						toast(context, msg, isLong);
					}
				});
				return;
			}
		}

		if(null == mStaticToastImpl) {
			synchronized (UIUtil.class) {
				if(null == mStaticToastImpl) {
					mStaticToastImpl = Toast.makeText(
							context.getApplicationContext(), "", Toast.LENGTH_SHORT);
				}
			}
		}

		mStaticToastImpl.setText(msg);
		mStaticToastImpl.setDuration(isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);

		if(Looper.myLooper() == Looper.getMainLooper()) {
			//当前在zh主线程
			mStaticToastImpl.show();
		} else {
			UITaskRunner.getHandler().post(new Runnable() {
				@Override
				public void run() {
					mStaticToastImpl.show();
				}
			});
		}
    }

    public static void toast(Context context, String msg) {
        toast(context, msg, true);
    }

    /***
	 * 对TextView显示文本,没有文本时隐藏TextView
	 * @param txtView
	 * @param text
	 */
	public static void setViewText(TextView txtView, String text) {
		setViewText(txtView, text, View.GONE);
	}

	/***
	 * 对TextView显示文本,没有文本时按noTextVisibility展示View
	 * @param txtView
	 * @param text
	 * @param noTextVisibility
	 */
	public static void setViewText(TextView txtView, String text, int noTextVisibility) {
		if (null == txtView) {
			return;
		}
		if (Utils.isEmpty(text)) {
			txtView.setVisibility(noTextVisibility);
		} else {
			txtView.setText(text);
			txtView.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 将bitmap处理成圆形
	 *
	 * @param bitmap
	 * @return
	 */
	public static Bitmap getRoundBitmap(Bitmap bitmap) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		RectF rectF = new RectF(rect);
		canvas.drawARGB(0, 0, 0, 0);
		canvas.drawRoundRect(rectF, bitmap.getWidth() / 2, bitmap.getHeight() / 2, paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}

	public static boolean isSoftInputShow(Context context) {
		try {
			InputMethodManager inputMethodManager =
					(InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			return inputMethodManager.isActive();
//			return context.getWindow().getAttributes().softInputMode==WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED;
		} catch (Exception ex) {
			return false;
		}
	}

	public static Drawable getAppIcon(Context context, String packageName) {
		try {
			ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
			return info.loadIcon(context.getPackageManager());
		} catch (Exception e) {
			Logging.d(TAG, "getAppIcon() e = " + e);
		}
		return null;
	}

	public static void addViewSafely(ViewGroup viewGroup, View view, ViewGroup.LayoutParams params) {
		if(null == viewGroup || null == view) {
			return;
		}

		if(view.getParent() == viewGroup) {
			Logging.d(TAG, "addViewSafely()| view view parent right, do nothing");
		} else {
			//add view to current pos
			ViewGroup prevParent = (ViewGroup) view.getParent();
			if(null != prevParent) {
				prevParent.removeView(view);
			}
			if(null != params) {
				viewGroup.addView(view, params);
			} else {
				viewGroup.addView(view);
			}
		}
	}

	public static String timeIntToString(int totalSecond) {
		if(totalSecond <= 0) {
			return "00:00";
		}
		int totalMinute = totalSecond / 60;
		int hour = totalMinute / 60;
		int minute = totalMinute - hour * 60;
		int second = totalSecond - totalMinute * 60;
		String minuteStr = minute < 10 ? "0" + minute : String.valueOf(minute);
		String secondStr = second < 10 ? "0" + second : String.valueOf(second);
		if(hour > 0) {
			String hourStr = hour < 10 ? "0" + hour : String.valueOf(hour);
			return hourStr + ":" + minuteStr + ":" + secondStr;
		} else {
			return minuteStr + ":" + secondStr;
		}
	}
}
