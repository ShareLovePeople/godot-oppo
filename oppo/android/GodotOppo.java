package org.godotengine.godot;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.nearme.game.sdk.GameCenterSDK;
import com.nearme.game.sdk.callback.*;
import com.nearme.game.sdk.common.model.biz.PayInfo;
import com.nearme.game.sdk.common.util.AppUtil;


import com.oppo.mobad.api.InitParams;
import com.oppo.mobad.api.MobAdManager;
import com.oppo.mobad.api.ad.BannerAd;
import com.oppo.mobad.api.ad.InterstitialAd;
import com.oppo.mobad.api.ad.RewardVideoAd;
import com.oppo.mobad.api.listener.IBannerAdListener;
import com.oppo.mobad.api.listener.IInterstitialAdListener;
import com.oppo.mobad.api.listener.IRewardVideoAdListener;
import com.oppo.mobad.api.params.RewardVideoAdParams;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * chenlvtang
 */
public class GodotOppo extends Godot.SingletonBase{
    private final String CALLBACK = "http://gamecenter.wanyol.com:8080/gamecenter/callback_test_url";
    private Activity activity = null; // The main activity of the game
    private boolean isReal = false;
    private static final String TAG = "godot_oppo";
    private static final String APP_TITLE = "APP_TITLE";
    private static final String APP_DESC = "APP_TITLE";
    public static int INSTANCE_ID = 0;  //id 由Godot传入

    /**
     * 要支付的权限列表
     */
    private List<String> mNeedRequestPMSList = new ArrayList<>();

    private FrameLayout layout = null; // Store the layout

    /**
     * 从请求广告到广告展示出来最大耗时时间，只能在[3000,5000]ms之内。
     */
    private static final int FETCH_TIME_OUT = 3000;
    /**
     * 插页广告列表
     */
    private Map<String, InterstitialAd> interAdMap = new HashMap<>();
    private Map<String, BannerAd> bannerAdMap = new HashMap<>();
    private Map<String, RewardVideoAd> rewardVideoMap = new HashMap<>();


    /**
     * 打开游戏时候调用一次 初始化SDK
     *
     * @param appId      传入的广告应用ID
     * @param payId
     * @param isReal     true真实环境 false测试
     * @param instanceId GodotID
     */

    public void init(String appId,String payId, boolean isReal, int instanceId) {
        INSTANCE_ID = instanceId;
        this.isReal = isReal;
//        initAd(appId);
        initPay(payId);
    }

    /**
     * 初始化广告SDK
     * @param appid
     */
    private void initAd(String appid) {
        InitParams initParams = new InitParams.Builder()
                .setDebug(isReal)
//true打开SDK日志，当应用发布Release版本时，必须注释掉这行代码的调用，或者设为false
                .build();

        MobAdManager.getInstance().init(activity, appid, initParams);
        Logi(TAG, "init oppo: appId:" + appid + "  isReal" + isReal
                + "  isSupportedMobile" + MobAdManager.getInstance().isSupportedMobile()
                + "  VerCode:" + MobAdManager.getInstance().getSdkVerCode() + "  VerName:" + MobAdManager.getInstance().getSdkVerName());

    }
    private void initPay(String payId) {
        GameCenterSDK.init(payId, activity);
		Logi(TAG,"init oppoPay:"+ payId);
    }

    /**
     * 初始化插页广告
     *
     * @param id 广告位ID
     */
    private void idInitInterstitial(final String id) {
        InterstitialAd interstitialAd = new InterstitialAd(activity, id);
        interstitialAd.setAdListener(new IInterstitialAdListener() {

            @Override
            public void onAdShow() {
                idReutrnShowAdResult(0,id,"",0);
            }

            @Override
            public void onAdFailed(String s) {
                idReutrnShowAdResult(1,id,s,0);
            }

            @Override
            public void onAdClick() {

            }

            @Override
            public void onAdReady() {

            }

            @Override
            public void onAdClose() {

            }
        });
        interAdMap.put(id, interstitialAd);
    }

    public void idLoadInterstitial(final String id) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!hasNecessaryPMSGranted()){
                    return;
                }
                if (interAdMap.get(id) == null) {
                    idInitInterstitial(id);
                }

                interAdMap.get(id).loadAd();
            }
        });
    }

    public void idShowInterstitial(final String id) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!hasNecessaryPMSGranted()){
                    return;
                }
                if (interAdMap.get(id) == null) {
                    idInitInterstitial(id);
                    interAdMap.get(id).loadAd();
                }
                interAdMap.get(id).showAd();
            }
        });
    }

    private void idInitBanner(final String id) {
//            layout = ((Godot) activity).layout;
        /**
         * new bannerAd
         */
        BannerAd bannerAd = new BannerAd(activity, id);
        /**
         * set banner action listener.
         */
        bannerAd.setAdListener(new IBannerAdListener() {

            @Override
            public void onAdShow() {

            }

            @Override
            public void onAdFailed(String s) {

            }

            @Override
            public void onAdClick() {

            }

            @Override
            public void onAdReady() {

            }

            @Override
            public void onAdClose() {

            }
        });
        /**
         * get banner view and add it to your window.
         *
         */
        View adView = bannerAd.getAdView();
        /**
         * adView maye be null.here must judge whether adView is null.
         */
        if (null != adView) {
            layout.addView(adView);
        }
        /**
         * invoke loadAd() method to request ad.
         */
        bannerAdMap.put(id, bannerAd);

    }

    public void idLoadBanner(final String id) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (bannerAdMap.get(id) == null) {
                    idInitBanner(id);
                }
                bannerAdMap.get(id).loadAd();
            }
        });
    }

    private void idInitRewardVideo(final String id) {
        final RewardVideoAd rewardVideoAd = new RewardVideoAd(activity, id, new IRewardVideoAdListener() {
            @Override
            public void onAdSuccess() {
                idUpAdloadStatus(id, true);
                idReutrnShowAdResult(0,id,"",0);
                rewardVideoMap.get(id).loadAd();
                Logd(TAG, "RewardVideo onAdSuccess: id:" + id);
            }

            @Override
            public void onAdFailed(String s) {
                idReutrnShowAdResult(1,id,s,0);
                Toast("RewardVideo onAdFailed  s:" + s + " id:" + id);
            }

            @Override
            public void onAdClick(long l) {
                Logd(TAG, "RewardVideo onAdClick: id:" + id);
            }

            @Override
            public void onVideoPlayStart() {
                Logd(TAG, "RewardVideo onVideoPlayStart: id:" + id);
            }

            @Override
            public void onVideoPlayComplete() {
                rewardVideoMap.get(id).loadAd(getRewardVideoAdParams());
                Logd(TAG, "RewardVideo onVideoPlayComplete: id:" + id);
            }

            @Override
            public void onVideoPlayError(String s) {
                Toast("RewardVideo onVideoPlayError  s:" + s + " id:" + id);
            }

            @Override
            public void onVideoPlayClose(long l) {
                Logd(TAG, "RewardVideo onVideoPlayClose: id:" + id);
            }

            @Override
            public void onLandingPageOpen() {
                Logd(TAG, "RewardVideo onLandingPageOpen: id:" + id);
            }

            @Override
            public void onLandingPageClose() {
                Logd(TAG, "RewardVideo onLandingPageClose: id:" + id);
            }

            @Override
            public void onReward(Object... objects) {
                Logd(TAG, "RewardVideo onReward: id:" + id);
            }
        });
        rewardVideoMap.put(id, rewardVideoAd);
    }

    public void idLoadRewardedVideo(final String id) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!hasNecessaryPMSGranted()){
                    return;
                }
                if (rewardVideoMap.get(id) == null) {
                    idInitRewardVideo(id);
                }

                if (rewardVideoMap.get(id).isReady()) {
                    return;
                }

                RewardVideoAdParams rewardVideoAdParams = new RewardVideoAdParams.Builder()
                        .setFetchTimeout(3000)
                        .build();
                rewardVideoMap.get(id).loadAd(rewardVideoAdParams);
            }
        });
    }

    public void idShowRewardedVideo(final String id) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!hasNecessaryPMSGranted()){
                    return;
                }
                if (rewardVideoMap.get(id) == null) {
                    idInitRewardVideo(id);
                }

                if (!rewardVideoMap.get(id).isReady()) {

                    rewardVideoMap.get(id).loadAd(getRewardVideoAdParams());
                } else {
                    rewardVideoMap.get(id).showAd();
                }

                idUpAdloadStatus(id, rewardVideoMap.get(id).isReady());
            }
        });
    }

    private RewardVideoAdParams getRewardVideoAdParams() {
        RewardVideoAdParams rewardVideoAdParams = new RewardVideoAdParams.Builder()
                .setFetchTimeout(3000)
                .build();
        return rewardVideoAdParams;
    }


    /**
     * 判断是否已经获取必要的权限
     *
     * @return
     */
    private boolean hasNecessaryPMSGranted() {
        if (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE)) {
            if (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                return true;
            }
        }
        Toast("ERROR :缺少必要的权限");
        Logw(TAG, "hasNecessaryPMSGranted:  申请权限未通过缺少必要的权限");
        return false;
    }

    /**
     * @param id
     * @param amount 价格(分)
     */
    public void pay(final int id, final int amount){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Logw(TAG, "Java  Run pay");
                if (!hasNecessaryPMSGranted()){
                    checkAndRequestPermissions();
                    return;
                }
                GameCenterSDK.getInstance().doSinglePay(activity, getOrder(id, amount), new SinglePayCallback() {
                    @Override
                    public void onCallCarrierPay(PayInfo payInfo, boolean b) {
                        Toast("运营商支付:"+payInfo.toString()+"  result:"+b);


                    }

                    @Override
                    public void onSuccess(String s) {
                        Logd(TAG, "onSuccess: ");
                        Toast("支付成功");
                        idReutrnPayResult(0,id,s);
                    }

                    @Override
                    public void onFailure(String s, int i) {
                        Toast("支付失败:id:"+id+" i:"+i+"s");
                        idReutrnPayResult(1,id,s);
                    }
                });
            }
        });
    }

    /**
     * 生成订单
     * @param id     产品号
     * @param amount  价格(分)
     */
    private PayInfo getOrder(int id, int amount){
        PayInfo payInfo = new PayInfo(getOrderID(id),"Adflash",amount);
        payInfo.setProductDesc("测试支OPPO付");
        payInfo.setProductName("测试产品:"+id);

      //  payInfo.setCallbackUrl(CALLBACK);
        Logd(TAG, "产生的订单: "+payInfo);
        return payInfo;
    }

    /**
     * 根据购买的产品号 生成订单号
     *
     * @param code 产品号
     */
    private  String getOrderID(int code) {
        String order = "oppo" + code+System.currentTimeMillis() + new Random().nextInt(1000)+ Build.SERIAL;
        Logd(TAG,"产生的订单号："+order);
        return  order;
    }

    /**
     * 退出游戏时候调用(释放资源)
     */
    public void exit() {
//        MobAdManager.getInstance().exit(activity);
		payExit();
    }

	private void payExit(){
		GameCenterSDK.getInstance().onExit(activity,
				new GameExitCallback() {
					@Override
					public void exitGame() {
						// CP 实现游戏退出操作，也可以直接调用
						// AppUtil工具类里面的实现直接强杀进程~
						AppUtil.exitGameProcess(activity);
					}
				});
	}

    /**
     * 申请SDK运行需要的权限
     * 注意：READ_PHONE_STATE、WRITE_EXTERNAL_STORAGE 两个权限是必须权限，没有这两个权限SDK无法正常获得广告。
     * WRITE_CALENDAR、ACCESS_FINE_LOCATION 是两个可选权限；没有不影响SDK获取广告；但是如果应用申请到该权限，会显著提升应用的广告收益。
     */
    private void checkAndRequestPermissions() {
        /**
         * READ_PHONE_STATE、WRITE_EXTERNAL_STORAGE 两个权限是必须权限，没有这两个权限SDK无法正常获得广告。
         */
        if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE)) {
            mNeedRequestPMSList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            mNeedRequestPMSList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        /**
         * WRITE_CALENDAR、ACCESS_FINE_LOCATION 是两个可选权限；没有不影响SDK获取广告；但是如果应用申请到该权限，会显著提升应用的广告收益。
         */
        if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CALENDAR)) {
            mNeedRequestPMSList.add(Manifest.permission.WRITE_CALENDAR);
        }
        if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
            mNeedRequestPMSList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        //
        if (0 == mNeedRequestPMSList.size()) {
            /**
             * 权限都已经有了，那么直接调用SDK请求广告。
             */
//            fetchSplashAd();
        } else {
            /**
             * 有权限需要申请，主动申请。
             */
            String[] temp = new String[mNeedRequestPMSList.size()];
            mNeedRequestPMSList.toArray(temp);
            ActivityCompat.requestPermissions(activity, temp, 100);
        }
    }

    private void guidePermissions(){

    }
    /*
    * ******************************************************************/

    private void idUpAdloadStatus(final String id, boolean status) {
        GodotLib.calldeferred(INSTANCE_ID, "_id_up_adload_status", new Object[]{id,status});
        Logd(TAG, "更新广告加载情况列表  id:" + id + " bool:" + status);
    }

    /**
     * @param code
     * @param id
     * @param reward
     * @param number
     */
    private void idReutrnShowAdResult(final int code, final String id, final String reward, final int number) {
        GodotLib.calldeferred(INSTANCE_ID, "_id_showad_result", new Object[]{code,id,reward,number});
    }

    /**
     * @param code 支付结果
     * @param id 订单
     * @param desc 描述
     */
    private void idReutrnPayResult(final int code, final int id,final String desc) {
        GodotLib.calldeferred(INSTANCE_ID, "_id_pay_result", new Object[]{code,id,desc});
    }


    /*Debug
     * *************************************************************/
    private void Toast(String string) {
        if (!isReal) {
            Toast.makeText(activity, string, Toast.LENGTH_SHORT).show();
        }
    }

    private void Logi(final String TAG, final String log) {
        if (!isReal) {
            Log.i(TAG, log);
        }
    }

    private void Logw(final String TAG, final String log) {
        if (!isReal) {
            Log.w(TAG, log);
        }
    }

    private void Logd(final String TAG, final String log) {
        if (!isReal) {
            Log.d(TAG, log);
        }
    }

    //----------------------------------------------------
    static public Godot.SingletonBase initialize(Activity activity) {
        return new GodotOppo(activity);
    }



    public GodotOppo(Activity p_activity) {
        registerClass("Oppo", new String[]{
                "init",
                // banner
                "idLoadBanner", "idShowBanner", "idHideBanner",
                // Interstitial
                "idLoadInterstitial", "idShowInterstitial",
                // Rewarded video
                "idLoadRewardedVideo", "idShowRewardedVideo", "exit","pay"
        });
        activity = p_activity;
    }
}
