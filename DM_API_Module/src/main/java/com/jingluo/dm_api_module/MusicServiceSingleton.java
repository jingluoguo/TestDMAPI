package com.jingluo.dm_api_module;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import com.jingluo.dm_api_module.constant.Constant;
import com.jingluo.dm_api_module.util.CommonUtil;
import com.jingluo.dm_api_module.util.SHAUtil;
import com.jingluo.dm_api_module.util.SharedPreferencesUtil;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Headers;

public class MusicServiceSingleton {
    private static class SingletonHolder {
        @SuppressLint("StaticFieldLeak")
        private static MusicServiceSingleton INSTANCE = new MusicServiceSingleton();
    }

    private String firmAcctId = "";
    private String clientId = "";
    private String tvsAuthCode = "";
    private String qqMusicAuthCode = "";

    public static MusicServiceSingleton getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static final String TAG = "MusicServiceSingleton";

    public void initSDK(String accountId, Context context) {
        if (firmAcctId.isEmpty()) {
            firmAcctId = accountId;
            SharedPreferencesUtil.getInstance().initSPUtil(context);
        }
    }

    /// 获取sig
    public void getSig(CommonUtil.GetSigCallback callback) {
        if (firmAcctId.equals("")) {
            callback.onCallback(-100, "not init sdk");
            return;
        }
        String encryptSecret = CommonUtil.MD5Lower(Constant.DEFAULT_APP_KEY+":"+Constant.DEFAULT_APP_SECRET+":"+firmAcctId);
        if (encryptSecret != null) {
            JSONObject Header  = new JSONObject();
            JSONObject Payload  = new JSONObject();
            JSONObject param = new JSONObject();
            try {
                Header.put("DSN", Constant.DEFAULT_APP_KEY + "_" + firmAcctId);
                Payload.put("encryptsecret", encryptSecret);
                Payload.put("firmacctid", firmAcctId);

                param.put("Header", Header);
                param.put("Payload", Payload);
            } catch (Exception e) {
                Log.i(TAG, e.getLocalizedMessage());
                callback.onCallback(-3, e.getMessage());
                return;
            }

            String fullUrl = CommonUtil.getRealRequestUrl("/v2/account/firm/oauth2/get-sig");

            CommonUtil.commonRequestWithHeader(fullUrl, param.toString(), Headers.of(), (code, data, msg) -> {
                Log.i(TAG, "code:" + String.valueOf(code) + " data:" + data.toString() + " msg:" + msg);
                if (code == 0) {
                    try {
                        if (data.getInt("ret") == 0) {
                            callback.onCallback(code, data.getString("sig"));
                        } else {
                            callback.onCallback(data.getInt("ret"), data.getString("msg"));
                        }
                    } catch (Exception e) {
                        callback.onCallback(-1, e.getMessage());
                    }
                    return;
                }
                callback.onCallback(code, msg);
            });
        }
    }

    /// 根据sig登录云小微
    public void loginWithSig(String sig, CommonUtil.CommonCallback callback) {
        if (firmAcctId.isEmpty()) {
            callback.onCallback(-100, new JSONObject(), "not init SDK");
            return;
        }

        JSONObject Header  = new JSONObject();
        JSONObject Payload  = new JSONObject();
        JSONObject param = new JSONObject();
        try {
            Header.put("DSN", Constant.DEFAULT_APP_KEY + "_" + firmAcctId);
            Payload.put("sig", sig);

            param.put("Header", Header);
            param.put("Payload", Payload);
        } catch (Exception e) {
            Log.i(TAG, e.getLocalizedMessage());
            callback.onCallback(-3, new JSONObject(), e.getMessage());
            return;
        }

        String fullUrl = CommonUtil.getRealRequestUrl("/v2/account/openapi/auth");

        CommonUtil.commonRequestWithHeader(fullUrl, param.toString(), Headers.of(), (code, data, msg) -> {
            Log.i(TAG, "code:" + String.valueOf(code) + " data:" + data.toString() + " msg:" + msg);
            int reCode = code;
            String reMsg = msg;
            if (reCode == 0) {
                try {
                    if (data.getInt("ret") == 0) {
                        String userAccessToken = data.getString("accessToken");
                        String userOpenId = data.getString("openId");
                        String userRefreshToken = data.getString("refreshToken");
                        SharedPreferencesUtil.getInstance().setString(Constant.USER_ACCESS_TOKEN, userAccessToken);
                        SharedPreferencesUtil.getInstance().setString(Constant.USER_OPEN_ID, userOpenId);
                        SharedPreferencesUtil.getInstance().setString(Constant.USER_REFRESH_TOKEN, userRefreshToken);
                    } else {
                        reMsg = data.getString("msg");
                    }
                    reCode = data.getInt("ret");
                } catch (Exception e) {
                    reCode = -1;
                    Log.i(TAG, e.getMessage());
                }
                return;
            }
            callback.onCallback(reCode, data, reMsg);
        });
    }

    /// 云小微账号票据刷新
    public void refreshAccessToken(CommonUtil.CommonCallback callback) {
        if (firmAcctId.isEmpty()) {
            callback.onCallback(-100, new JSONObject(), "not init SDK");
            return;
        }

        String refreshToken = SharedPreferencesUtil.getInstance().getString(Constant.USER_REFRESH_TOKEN);

        if(refreshToken.isEmpty()) {
            callback.onCallback(-99, new JSONObject(), "not login");
            return;
        }

        JSONObject Header  = new JSONObject();
        JSONObject Payload  = new JSONObject();
        JSONObject param = new JSONObject();
        try {
            Header.put("DSN", Constant.DEFAULT_APP_KEY + "_" + firmAcctId);
            Payload.put("refreshToken", refreshToken);

            param.put("Header", Header);
            param.put("Payload", Payload);
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
            callback.onCallback(-3, new JSONObject(), e.getMessage());
            return;
        }

        String fullUrl = CommonUtil.getRealRequestUrl("/v2/account/openapi/refresh");

        CommonUtil.commonRequestWithHeader(fullUrl, param.toString(), Headers.of(), (code, data, msg) -> {
            Log.i(TAG, "code:" + String.valueOf(code) + " data:" + data.toString() + " msg:" + msg);
            int reCode = code;
            String reMsg = msg;
            if (reCode == 0) {
                try {
                    if (data.getInt("ret") == 0) {
                        String userAccessToken = data.getString("accessToken");
                        String userOpenId = data.getString("openId");
                        String userRefreshToken = data.getString("refreshToken");
                        SharedPreferencesUtil.getInstance().setString(Constant.USER_ACCESS_TOKEN, userAccessToken);
                        SharedPreferencesUtil.getInstance().setString(Constant.USER_OPEN_ID, userOpenId);
                        SharedPreferencesUtil.getInstance().setString(Constant.USER_REFRESH_TOKEN, userRefreshToken);
                    } else {
                        reMsg = data.getString("msg");
                    }
                    reCode = data.getInt("ret");
                } catch (Exception e) {
                    Log.i(TAG, e.getMessage());
                    reCode = -1;
                }
            }
            callback.onCallback(reCode, data, reMsg);
        });
    }

    /// 预绑定生成设备clientId
    public void generateClientIdAndAuthCode(CommonUtil.CommonCallback callback) {

        if (firmAcctId.isEmpty()) {
            callback.onCallback(-100, new JSONObject(), "not init SDK");
            return;
        }

        String accessToken = SharedPreferencesUtil.getInstance().getString(Constant.USER_ACCESS_TOKEN);

        if(accessToken.isEmpty()) {
            callback.onCallback(-99, new JSONObject(), "not login");
            return;
        }

        Headers headers = Headers.of(
                "Content-Type", "application/json;charset=utf-8",
                "Authorization", "Bearer " + accessToken
        );
        String fullUrl = CommonUtil.getRealRequestUrl("/v2/account/openapi/device/auth");

        JSONObject Header  = new JSONObject();
        JSONObject Payload  = new JSONObject();
        JSONObject param = new JSONObject();
        JSONObject codeChallengeInfo = new JSONObject();
        try {
            codeChallengeInfo.put("codeChallenge", SHAUtil.SHA256(firmAcctId));
            codeChallengeInfo.put("method", "SHA256");
            Header.put("DSN", Constant.DEFAULT_APP_KEY + "_" + firmAcctId);
            Payload.put("productId", Constant.ProductId);
            Payload.put("codeChallengeInfo", codeChallengeInfo);

            param.put("Header", Header);
            param.put("Payload", Payload);
        } catch (Exception e) {
            Log.i(TAG, e.getLocalizedMessage());
            callback.onCallback(-3, new JSONObject(), e.getMessage());
            return;
        }

        CommonUtil.commonRequestWithHeader(fullUrl, param.toString(), headers, (code, data, msg) -> {
            Log.i(TAG, "code:" + String.valueOf(code) + " data:" + data.toString() + " msg:" + msg);
            int reCode = code;
            String reMsg = msg;
            if (reCode == 0) {
                try {
                    if (data.getInt("ret") == 0) {
                        tvsAuthCode = data.getString("authCode");
                        clientId = data.getString("clientId");
                    } else {
                        reMsg = data.getString("msg");
                    }
                    reCode = data.getInt("ret");
                } catch (JSONException e) {
                    Log.i(TAG, e.getMessage());
                    reCode = -1;
                }
            }
            callback.onCallback(reCode, data, reMsg);
        });
    }

    /// 查询已绑定设备
    public void checkBindingDevices(CommonUtil.CommonCallback callback) {

        if (firmAcctId.isEmpty()) {
            callback.onCallback(-100, new JSONObject(), "not init SDK");
            return;
        }

        String accessToken = SharedPreferencesUtil.getInstance().getString(Constant.USER_ACCESS_TOKEN);

        if(accessToken.isEmpty()) {
            callback.onCallback(-99, new JSONObject(), "not login");
            return;
        }

        Headers headers = Headers.of(
                "Content-Type", "application/json;charset=utf-8",
                "Authorization", "Bearer " + accessToken
        );

        JSONObject Header  = new JSONObject();
        JSONObject param = new JSONObject();
        try {
            Header.put("DSN", Constant.DEFAULT_APP_KEY + "_" + firmAcctId);

            param.put("Header", Header);
        } catch (Exception e) {
            Log.i(TAG, e.getLocalizedMessage());
            callback.onCallback(-3, new JSONObject(), e.getMessage());
            return;
        }

        String fullUrl = CommonUtil.getRealRequestUrl("/v2/account/openapi/get-device");

        CommonUtil.commonRequestWithHeader(fullUrl, param.toString(), headers, (code, data, msg) -> {
            Log.i(TAG, "code:" + String.valueOf(code) + " data:" + data.toString() + " msg:" + msg);
            int reCode = code;
            String reMsg = msg;
            if (reCode == 0) {
                try {
                    if (data.getInt("ret") != 0) {
                        reMsg = data.getString("msg");
                    }
                    reCode = data.getInt("ret");
                } catch (JSONException e) {
                    Log.i(TAG, e.getMessage());
                    reCode = -1;
                }
            }
            callback.onCallback(reCode, data, reMsg);
        });
    }

    /// 设备绑定
    public void bindingDevice(CommonUtil.CommonCallback callback) {

        if (firmAcctId.isEmpty()) {
            callback.onCallback(-100, new JSONObject(), "not init SDK");
            return;
        }

        if (clientId.equals("") || tvsAuthCode.equals("")) {
            callback.onCallback(-100, new JSONObject(), "不存在clientId或tvs_auth_code");
            return;
        }

        JSONObject Header  = new JSONObject();
        JSONObject Payload  = new JSONObject();
        JSONObject param = new JSONObject();
        try {
            Header.put("DSN", Constant.DEFAULT_APP_KEY + "_" + firmAcctId);

            Payload.put("clientId", clientId);
            Payload.put("authCode", tvsAuthCode);
            Payload.put("codeVerify", firmAcctId);

            param.put("Header", Header);
            param.put("Payload", Payload);
        } catch (Exception e) {
            Log.i(TAG, e.getLocalizedMessage());
            callback.onCallback(-3, new JSONObject(), e.getMessage());
            return;
        }

        String fullUrl = CommonUtil.getRealRequestUrl("/v2/account/openapi/grant-device");

        CommonUtil.commonRequestWithHeader(fullUrl, param.toString(), Headers.of(), (code, data, msg) -> {
            int reCode = code;
            String reMsg = msg;
            if (reCode == 0) {
                try {
                    reCode = data.getInt("ret");
                    if (reCode != 0) {
                        reMsg = data.getString("msg");
                    } else {
                        String deviceAccessToken = data.getString("accessToken");
                        String deviceRefreshToken = data.getString("refreshToken");
                        SharedPreferencesUtil.getInstance().setString(Constant.DEVICE_ACCESS_TOKEN, deviceAccessToken);
                        SharedPreferencesUtil.getInstance().setString(Constant.DEVICE_REFRESH_TOKEN, deviceRefreshToken);
                    }
                } catch (Exception e) {
                    reCode = -1;
                }
            }
            callback.onCallback(reCode, data, reMsg);
        });
    }

    /// 设备解绑接口
    public void unbindingDevice(CommonUtil.CommonCallback callback) {

        if (firmAcctId.isEmpty()) {
            callback.onCallback(-100, new JSONObject(), "not init SDK");
            return;
        }

        String accessToken = SharedPreferencesUtil.getInstance().getString(Constant.USER_ACCESS_TOKEN);

        if(accessToken.isEmpty()) {
            callback.onCallback(-99, new JSONObject(), "not login");
            return;
        }

        Headers headers = Headers.of(
                "Content-Type", "application/json;charset=utf-8",
                "Authorization", "Bearer " + accessToken
        );

        JSONObject Payload  = new JSONObject();
        JSONObject Header  = new JSONObject();
        JSONObject param = new JSONObject();
        try {
            Payload.put("productId", String.format("%s:%s", Constant.DEFAULT_APP_KEY, Constant.DEFAULT_APP_ACCESS_TOKEN));
            Payload.put("disable_token", false);
            Header.put("DSN", Constant.DEFAULT_APP_KEY + "_" + firmAcctId);

            param.put("Header", Header);
            param.put("Payload", Payload);
        } catch (Exception e) {
            Log.i(TAG, e.getLocalizedMessage());
            callback.onCallback(-3, new JSONObject(), "params error");
            return;
        }

        String fullUrl = CommonUtil.getRealRequestUrl("/v2/account/openapi/device/remove-auth");

        CommonUtil.commonRequestWithHeader(fullUrl, param.toString(), headers, (code, data, msg) -> {
            Log.i(TAG, "code:" + String.valueOf(code) + " data:" + data.toString() + " msg:" + msg);
            int reCode = code;
            String reMsg = msg;
            if (reCode == 0) {
                try {
                    reCode = data.getInt("ret");
                    if (reCode != 0) {
                        reMsg = data.getString("msg");
                    } else {
                        SharedPreferencesUtil.getInstance().setString(Constant.DEVICE_ACCESS_TOKEN, "");
                        SharedPreferencesUtil.getInstance().setString(Constant.DEVICE_REFRESH_TOKEN, "");
                    }
                } catch (Exception e) {
                    reCode = -1;
                    Log.i(TAG, e.getLocalizedMessage());
                }
            }
            callback.onCallback(reCode, data, reMsg);
        });
    }

    /// 设备票据刷新
    public void refreshDeviceToken(CommonUtil.CommonCallback callback) {
        if (firmAcctId.isEmpty()) {
            callback.onCallback(-100, new JSONObject(), "not init SDK");
            return;
        }

        String refreshToken = SharedPreferencesUtil.getInstance().getString(Constant.DEVICE_REFRESH_TOKEN);

        if(refreshToken.isEmpty()) {
            callback.onCallback(-99, new JSONObject(), "not binding device");
            return;
        }

        JSONObject Payload  = new JSONObject();
        JSONObject Header  = new JSONObject();
        JSONObject param = new JSONObject();
        try {
            Header.put("DSN", Constant.DEFAULT_APP_KEY + "_" + firmAcctId);
            Payload.put("grantType", "refresh_token");
            Payload.put("refreshToken", refreshToken);
            Payload.put("version", "v2");

            param.put("Header", Header);
            param.put("Payload", Payload);
        } catch (Exception e) {
            Log.i(TAG, e.getLocalizedMessage());
            callback.onCallback(-3, new JSONObject(), "params error");
            return;
        }

        String fullUrl = CommonUtil.getRealRequestUrl("/v2/auth/o2/token");
        CommonUtil.commonRequestWithHeader(fullUrl, param.toString(), Headers.of(), (code, data, msg) -> {
            int reCode = code;
            String reMsg = msg;
            if (reCode == 0) {
                try {
                    reCode = data.getInt("ret");
                    if (reCode != 0) {
                        reMsg = data.getString("msg");
                    } else {
                        String deviceAccessToken = data.getString("accessToken");
                        String deviceRefreshToken = data.getString("refreshToken");
                        SharedPreferencesUtil.getInstance().setString(Constant.DEVICE_ACCESS_TOKEN, deviceAccessToken);
                        SharedPreferencesUtil.getInstance().setString(Constant.DEVICE_REFRESH_TOKEN, deviceRefreshToken);
                    }
                } catch (Exception e) {
                    reCode = -1;
                }
            }
            callback.onCallback(reCode, data, reMsg);
        });
    }

    /// 获取QQ音乐授权二维码
    public void getQQMusicQRCode(CommonUtil.CommonCallback callback) {
        if (firmAcctId.isEmpty()) {
            callback.onCallback(-100, new JSONObject(), "not init SDK");
            return;
        }

        String accessToken = SharedPreferencesUtil.getInstance().getString(Constant.USER_ACCESS_TOKEN);

        if(accessToken.isEmpty()) {
            callback.onCallback(-99, new JSONObject(), "not login");
            return;
        }

        Headers headers = Headers.of(
                "Content-Type", "application/json;charset=utf-8",
                "Authorization", "Bearer " + accessToken
        );

        JSONObject Payload  = new JSONObject();
        JSONObject Header  = new JSONObject();
        JSONObject param = new JSONObject();
        try {
            Payload.put("qrCodeType", "qqmusic");
            Header.put("DSN", Constant.DEFAULT_APP_KEY + "_" + firmAcctId);

            param.put("Header", Header);
            param.put("Payload", Payload);
        } catch (Exception e) {
            Log.i(TAG, e.getLocalizedMessage());
            callback.onCallback(-3, new JSONObject(), "params error");
            return;
        }

        String fullUrl = CommonUtil.getRealRequestUrl("/v2/account/openapi/get-qr-code");
        CommonUtil.commonRequestWithHeader(fullUrl, param.toString(), headers, (code, data, msg) -> {
            int reCode = code;
            String reMsg = msg;
            if (reCode == 0) {
                try {
                    reCode = data.getInt("ret");
                    if (reCode != 0) {
                        reMsg = data.getString("msg");
                    } else {
                        qqMusicAuthCode = data.getString("authCode");
                    }
                } catch (Exception e) {
                    reCode = -1;
                }
            }
            callback.onCallback(reCode, data, reMsg);
        });
    }

    /// 查询QQ音乐二维码授权状态
    public void checkQQMusicQRCodeStatus(CommonUtil.CommonCallback callback) {
        if (firmAcctId.isEmpty()) {
            callback.onCallback(-100, new JSONObject(), "not init SDK");
            return;
        }

        if (qqMusicAuthCode == null || qqMusicAuthCode.equals("")) {
            callback.onCallback(-5, new JSONObject(), "请重新生成QQ音乐授权二维码");
            return;
        }

        String accessToken = SharedPreferencesUtil.getInstance().getString(Constant.USER_ACCESS_TOKEN);

        if(accessToken.isEmpty()) {
            callback.onCallback(-99, new JSONObject(), "not login");
            return;
        }

        Headers headers = Headers.of(
                "Content-Type", "application/json;charset=utf-8",
                "Authorization", "Bearer " + accessToken
        );

        JSONObject Payload  = new JSONObject();
        JSONObject Header  = new JSONObject();
        JSONObject param = new JSONObject();
        try {
            Payload.put("qrCodeType", "qqmusic");
            Payload.put("authCode", qqMusicAuthCode);
            Header.put("DSN", Constant.DEFAULT_APP_KEY + "_" + firmAcctId);

            param.put("Header", Header);
            param.put("Payload", Payload);
        } catch (Exception e) {
            Log.i(TAG, e.getLocalizedMessage());
            callback.onCallback(-3, new JSONObject(), "params error");
            return;
        }

        String fullUrl = CommonUtil.getRealRequestUrl("/v2/account/openapi/query-qr-code-status");
        CommonUtil.commonRequestWithHeader(fullUrl, param.toString(), headers, (code, data, msg) -> {
            int reCode = code;
            String reMsg = msg;
            if (reCode == 0) {
                try {
                    reCode = data.getInt("ret");
                    if (reCode != 0) {
                        reMsg = data.getString("msg");
                    } else {
                        qqMusicAuthCode = null;
                    }
                } catch (Exception e) {
                    reCode = -1;
                }
            }
            callback.onCallback(reCode, data, reMsg);
        });
    }

    /// 查询当前云小微账号下第三方账号绑定状态
    public void getThirdBindingStatus(CommonUtil.CommonCallback callback) {

        if (firmAcctId.isEmpty()) {
            callback.onCallback(-100, new JSONObject(), "not init SDK");
            return;
        }

        String accessToken = SharedPreferencesUtil.getInstance().getString(Constant.USER_ACCESS_TOKEN);

        if(accessToken.isEmpty()) {
            callback.onCallback(-99, new JSONObject(), "not login");
            return;
        }

        Headers headers = Headers.of(
                "Content-Type", "application/json;charset=utf-8",
                "Authorization", "Bearer " + accessToken
        );

        JSONObject Payload  = new JSONObject();
        JSONObject Header  = new JSONObject();
        JSONObject param = new JSONObject();
        try {
            Payload.put("operType", "get_bind_state");
            Payload.put("skillId", "caabf231-e655-11e7-8130-68cc6ea8c1f8");
            Header.put("DSN", Constant.DEFAULT_APP_KEY + "_" + firmAcctId);

            param.put("Header", Header);
            param.put("Payload", Payload);
        } catch (Exception e) {
            Log.i(TAG, e.getLocalizedMessage());
            callback.onCallback(-3, new JSONObject(), "params error");
            return;
        }

        String fullUrl = CommonUtil.getRealRequestUrl("/v2/account/openapi/get-bind-state");
        CommonUtil.commonRequestWithHeader(fullUrl, param.toString(), headers, (code, data, msg) -> {
            int reCode = code;
            String reMsg = msg;
            if (reCode == 0) {
                try {
                    JSONObject error = data.getJSONObject("error");
                    reCode = error.getInt("code");
                    if (reCode != 0) {
                        reMsg = error.getString("msg");
                    }
                } catch (Exception e) {
                    reCode = -1;
                }
            }
            callback.onCallback(reCode, data, reMsg);
        });
    }

    /// 绑定或解绑技能子账号
    public void bindingOrUnbindingAccount(boolean binding, String appId, String acctId, CommonUtil.CommonCallback callback) {

        String state = "Enable";
        if (firmAcctId.isEmpty()) {
            callback.onCallback(-100, new JSONObject(), "not init SDK");
            return;
        }

        String accessToken = SharedPreferencesUtil.getInstance().getString(Constant.USER_ACCESS_TOKEN);

        if(accessToken.isEmpty()) {
            callback.onCallback(-99, new JSONObject(), "not login");
            return;
        }

        Headers headers = Headers.of(
                "Content-Type", "application/json;charset=utf-8",
                "Authorization", "Bearer " + accessToken
        );

        if (!binding) {
            state = "Disable";
        }

        JSONObject Payload  = new JSONObject();
        JSONObject accountBaseInfo  = new JSONObject();
        JSONObject Header  = new JSONObject();
        JSONObject param = new JSONObject();
        try {
            Payload.put("operType", "account_binding");
            Payload.put("state", state);
            Payload.put("skillId", "caabf231-e655-11e7-8130-68cc6ea8c1f8");
            Header.put("DSN", Constant.DEFAULT_APP_KEY + "_" + firmAcctId);
            if (binding) {
                accountBaseInfo.put("acctType", "QQMusicOpenId");
                accountBaseInfo.put("appId", appId);
                accountBaseInfo.put("acctId", acctId);
                Payload.put("accountBaseInfo", accountBaseInfo);
            }
            param.put("Header", Header);
            param.put("Payload", Payload);
        } catch (Exception e) {
            Log.i(TAG, e.getLocalizedMessage());
            callback.onCallback(-3, new JSONObject(), "params error");
            return;
        }

        String fullUrl = CommonUtil.getRealRequestUrl("/v2/account/openapi/bind-account");

        CommonUtil.commonRequestWithHeader(fullUrl, param.toString(), headers, (code, data, msg) -> {
            int reCode = code;
            String reMsg = msg;
            if (reCode == 0) {
                try {
                    JSONObject error = data.getJSONObject("error");
                    reCode = error.getInt("code");
                    if (reCode != 0) {
                        reMsg = error.getString("msg");
                    }
                } catch (Exception e) {
                    reCode = -1;
                }
            }
            callback.onCallback(reCode, data, reMsg);
        });
    }

    /*
    以下为QQ音乐服务
     */

    /*
    搜索歌曲、专辑、歌单

    keyword - 搜索词
    page - 单曲、专辑、mv、歌单搜索页码最大4页，默认1页
    type - 搜索类型 0:单曲搜索(默认) 8:专辑搜索 3:歌单搜索 12: mv搜索
    pageSize - 单曲、专辑、mv、歌单搜索页数最大50，默认20个
     */
    public void searchMusicCustom(String keyword, int page, int type, int pageSize, CommonUtil.CommonCallback callback) {
        if (firmAcctId.isEmpty()) {
            callback.onCallback(-100, new JSONObject(), "not init SDK");
            return;
        }

        if (page == 0 || page > 4) {
            page = 1;
        }

        if (type != 0 && type != 8 && type != 3 && type != 12) {
            type = 0;
        }

        if (pageSize == 0 || pageSize > 50) {
            pageSize = 20;
        }

        String accessToken = SharedPreferencesUtil.getInstance().getString(Constant.USER_ACCESS_TOKEN);

        if(accessToken.isEmpty()) {
            callback.onCallback(-99, new JSONObject(), "not login");
            return;
        }

        Headers headers = Headers.of(
                "Content-Type", "application/json;charset=utf-8",
                "Authorization", "Bearer " + accessToken
        );

        JSONObject Payload  = new JSONObject();
        JSONObject Header  = new JSONObject();
        JSONObject param = new JSONObject();
        try {
            Payload.put("keyword", keyword);
            Payload.put("page", page);
            Payload.put("type", type);
            Payload.put("pageSize", pageSize);
            Header.put("DSN", Constant.DEFAULT_APP_KEY + "_" + firmAcctId);

            param.put("Header", Header);
            param.put("Payload", Payload);
        } catch (Exception e) {
            Log.i(TAG, e.getLocalizedMessage());
            callback.onCallback(-3, new JSONObject(), "params error");
            return;
        }

        String fullUrl = CommonUtil.getRealRequestUrl("/music/api/v1/search-custom");
        CommonUtil.commonRequestWithHeader(fullUrl, param.toString(), headers, (code, data, msg) -> {
            int reCode = code;
            String reMsg = msg;
            if (reCode == 0) {
                try {
                    reCode = data.getInt("ret");
                    if (reCode != 0) {
                        reMsg = data.getString("msg");
                    }
                } catch (Exception e) {
                    reCode = -1;
                }
            }
            callback.onCallback(reCode, data, reMsg);
        });
    }

    /*
    搜索歌词

    keyword - 搜索词
    page - 页码1-10
    pageSize - 每页大小1-10
     */
    public void searchMusicByLyric(String keyword, int page, int pageSize, CommonUtil.CommonCallback callback) {
        if (firmAcctId.isEmpty()) {
            callback.onCallback(-100, new JSONObject(), "not init SDK");
            return;
        }

        if (page == 0 || page > 10) {
            page = 1;
        }

        if (pageSize == 0 || pageSize > 10) {
            pageSize = 10;
        }

        String accessToken = SharedPreferencesUtil.getInstance().getString(Constant.USER_ACCESS_TOKEN);

        if(accessToken.isEmpty()) {
            callback.onCallback(-99, new JSONObject(), "not login");
            return;
        }

        Headers headers = Headers.of(
                "Content-Type", "application/json;charset=utf-8",
                "Authorization", "Bearer " + accessToken
        );

        JSONObject Payload  = new JSONObject();
        JSONObject Header  = new JSONObject();
        JSONObject param = new JSONObject();
        try {
            Payload.put("keyword", keyword);
            Payload.put("page", page);
            Payload.put("pageSize", pageSize);
            Header.put("DSN", Constant.DEFAULT_APP_KEY + "_" + firmAcctId);

            param.put("Header", Header);
            param.put("Payload", Payload);
        } catch (Exception e) {
            Log.i(TAG, e.getLocalizedMessage());
            callback.onCallback(-3, new JSONObject(), "params error");
            return;
        }

        String fullUrl = CommonUtil.getRealRequestUrl("/music/api/v1/search-lyric");
        CommonUtil.commonRequestWithHeader(fullUrl, param.toString(), headers, (code, data, msg) -> {
            int reCode = code;
            String reMsg = msg;
            if (reCode == 0) {
                try {
                    reCode = data.getInt("ret");
                    if (reCode != 0) {
                        reMsg = data.getString("msg");
                    }
                } catch (Exception e) {
                    reCode = -1;
                }
            }
            callback.onCallback(reCode, data, reMsg);
        });
    }

    /*
    获取歌单广场歌单

    start - 开始索引，从0开始
    size - 拉取歌单数量，默认10
    order - 拉取方式，2：最新 5：最热
    categoryId：分类id，默认为0。如果不为0，则根据该分类id获取歌单列表，如果不填则拉取歌单广场的歌单

    ps. start入参申明:第一页start填0，size填10，第二页start就是第一页的start偏移10，所以第二页start填10， 最大能拉取的歌单数上限40
     */
    public void getSquareSongList(int start, int size, int order, int categoryId, CommonUtil.CommonCallback callback) {
        if (firmAcctId.isEmpty()) {
            callback.onCallback(-100, new JSONObject(), "not init SDK");
            return;
        }

        String accessToken = SharedPreferencesUtil.getInstance().getString(Constant.USER_ACCESS_TOKEN);

        if(accessToken.isEmpty()) {
            callback.onCallback(-99, new JSONObject(), "not login");
            return;
        }

        if (size > 10 || size == 0) {
            size = 10;
        }

        if (order != 2 && order != 5) {
            order = 2;
        }

        Headers headers = Headers.of(
                "Content-Type", "application/json;charset=utf-8",
                "Authorization", "Bearer " + accessToken
        );

        JSONObject Header  = new JSONObject();
        JSONObject Payload  = new JSONObject();
        JSONObject param = new JSONObject();
        try {
            Header.put("DSN", Constant.DEFAULT_APP_KEY + "_" + firmAcctId);
            Payload.put("start", start);
            Payload.put("size", size);
            Payload.put("order", order);
            if (categoryId != 0) {
                Payload.put("categoryId", categoryId);
            }

            param.put("Header", Header);
            param.put("Payload", Payload);
        } catch (Exception e) {
            Log.i(TAG, e.getLocalizedMessage());
            callback.onCallback(-3, new JSONObject(), "params error");
            return;
        }

        String fullUrl = CommonUtil.getRealRequestUrl("/music/api/v1/get-songlist-square");
        CommonUtil.commonRequestWithHeader(fullUrl, param.toString(), headers, (code, data, msg) -> {
            int reCode = code;
            String reMsg = msg;
            if (reCode == 0) {
                try {
                    reCode = data.getInt("ret");
                    if (reCode != 0) {
                        reMsg = data.getString("msg");
                    }
                } catch (Exception e) {
                    reCode = -1;
                }
            }
            callback.onCallback(reCode, data, reMsg);
        });
    }

    /*
    获取个人歌单目录
     */
    public void getSelfSongList(CommonUtil.CommonCallback callback) {
        if (firmAcctId.isEmpty()) {
            callback.onCallback(-100, new JSONObject(), "not init SDK");
            return;
        }

        String accessToken = SharedPreferencesUtil.getInstance().getString(Constant.USER_ACCESS_TOKEN);

        if(accessToken.isEmpty()) {
            callback.onCallback(-99, new JSONObject(), "not login");
            return;
        }

        Headers headers = Headers.of(
                "Content-Type", "application/json;charset=utf-8",
                "Authorization", "Bearer " + accessToken
        );

        JSONObject Header  = new JSONObject();
        JSONObject param = new JSONObject();
        try {
            Header.put("DSN", Constant.DEFAULT_APP_KEY + "_" + firmAcctId);

            param.put("Header", Header);
        } catch (Exception e) {
            Log.i(TAG, e.getLocalizedMessage());
            callback.onCallback(-3, new JSONObject(), "params error");
            return;
        }

        String fullUrl = CommonUtil.getRealRequestUrl("/music/api/v1/get-songlist-self");
        CommonUtil.commonRequestWithHeader(fullUrl, param.toString(), headers, (code, data, msg) -> {
            int reCode = code;
            String reMsg = msg;
            if (reCode == 0) {
                try {
                    reCode = data.getInt("ret");
                    if (reCode != 0) {
                        reMsg = data.getString("msg");
                    }
                } catch (Exception e) {
                    reCode = -1;
                }
            }
            callback.onCallback(reCode, data, reMsg);
        });
    }

    /*
    获取收藏的歌单
    cmd - 操作类型，1:收藏，2:取消收藏，3:获取收藏歌单
    songListId - 操作的歌单id cmd为1必填
     */
    public void getSelfFavSongList(int cmd, int songListId, CommonUtil.CommonCallback callback) {
        if (firmAcctId.isEmpty()) {
            callback.onCallback(-100, new JSONObject(), "not init SDK");
            return;
        }

        String accessToken = SharedPreferencesUtil.getInstance().getString(Constant.USER_ACCESS_TOKEN);

        if(accessToken.isEmpty()) {
            callback.onCallback(-99, new JSONObject(), "not login");
            return;
        }

        if (cmd != 1 && cmd != 2 && cmd != 3) {
            cmd = 3;
        }

        if (cmd == 1 && songListId == 0) {
            callback.onCallback(-51, new JSONObject(), "songListId is empty");
            return;
        }

        Headers headers = Headers.of(
                "Content-Type", "application/json;charset=utf-8",
                "Authorization", "Bearer " + accessToken
        );

        JSONObject Header  = new JSONObject();
        JSONObject Payload  = new JSONObject();
        JSONObject param = new JSONObject();
        try {
            Header.put("DSN", Constant.DEFAULT_APP_KEY + "_" + firmAcctId);
            Payload.put("cmd", cmd);
            Payload.put("songListId", songListId);
            param.put("Header", Header);
            param.put("Payload", Payload);
        } catch (Exception e) {
            Log.i(TAG, e.getLocalizedMessage());
            callback.onCallback(-3, new JSONObject(), "params error");
            return;
        }

        String fullUrl = CommonUtil.getRealRequestUrl("/music/api/v1/get-songlist-fav");
        CommonUtil.commonRequestWithHeader(fullUrl, param.toString(), headers, (code, data, msg) -> {
            int reCode = code;
            String reMsg = msg;
            if (reCode == 0) {
                try {
                    reCode = data.getInt("ret");
                    if (reCode != 0) {
                        reMsg = data.getString("msg");
                    }
                } catch (Exception e) {
                    reCode = -1;
                }
            }
            callback.onCallback(reCode, data, reMsg);
        });
    }

    /*
    获取分类歌单
    cmd - 请求标识，1:获取所有的分类标签，2:获取标签下歌单列表
    categoryId - cmd为2时有效，获取标签ID下的歌单列表
    page - 页码，取值从0开始(仅cmd=2时有效)
    pageSize - 每页的数量，最大50(仅cmd=2时有效)
     */
    public void getCategorySongList(int cmd, int categoryId, int page, int pageSize, CommonUtil.CommonCallback callback) {
        if (firmAcctId.isEmpty()) {
            callback.onCallback(-100, new JSONObject(), "not init SDK");
            return;
        }

        String accessToken = SharedPreferencesUtil.getInstance().getString(Constant.USER_ACCESS_TOKEN);

        if(accessToken.isEmpty()) {
            callback.onCallback(-99, new JSONObject(), "not login");
            return;
        }

        if (cmd != 1 && cmd != 2) {
            cmd = 1;
        }

        if (pageSize > 50) {
            pageSize = 50;
        }

        Headers headers = Headers.of(
                "Content-Type", "application/json;charset=utf-8",
                "Authorization", "Bearer " + accessToken
        );

        JSONObject Header  = new JSONObject();
        JSONObject Payload  = new JSONObject();
        JSONObject param = new JSONObject();
        try {
            Header.put("DSN", Constant.DEFAULT_APP_KEY + "_" + firmAcctId);
            Payload.put("cmd", cmd);
            Payload.put("categoryId", categoryId);
            Payload.put("page", page);
            Payload.put("pageSize", pageSize);

            param.put("Header", Header);
            param.put("Payload", Payload);
        } catch (Exception e) {
            Log.i(TAG, e.getLocalizedMessage());
            callback.onCallback(-3, new JSONObject(), "params error");
            return;
        }

        String fullUrl = CommonUtil.getRealRequestUrl("/music/api/v1/get-songlist-category");
        CommonUtil.commonRequestWithHeader(fullUrl, param.toString(), headers, (code, data, msg) -> {
            int reCode = code;
            String reMsg = msg;
            if (reCode == 0) {
                try {
                    reCode = data.getInt("ret");
                    if (reCode != 0) {
                        reMsg = data.getString("msg");
                    }
                } catch (Exception e) {
                    reCode = -1;
                }
            }
            callback.onCallback(reCode, data, reMsg);
        });
    }

    /*
    获取歌单中歌曲列表
    songListId - 操作的歌单id
    page - 页码，取值从0开始
    pageSize - 每页的数量，最大50
     */
    public void getSongListDetail(long songListId, int page, int pageSize, CommonUtil.CommonCallback callback) {
        if (firmAcctId.isEmpty()) {
            callback.onCallback(-100, new JSONObject(), "not init SDK");
            return;
        }

        String accessToken = SharedPreferencesUtil.getInstance().getString(Constant.USER_ACCESS_TOKEN);

        if(accessToken.isEmpty()) {
            callback.onCallback(-99, new JSONObject(), "not login");
            return;
        }

        if (pageSize > 50) {
            pageSize = 50;
        }

        Headers headers = Headers.of(
                "Content-Type", "application/json;charset=utf-8",
                "Authorization", "Bearer " + accessToken
        );

        JSONObject Header  = new JSONObject();
        JSONObject Payload  = new JSONObject();
        JSONObject param = new JSONObject();
        try {
            Header.put("DSN", Constant.DEFAULT_APP_KEY + "_" + firmAcctId);
            Payload.put("page", page);
            Payload.put("pageSize", pageSize);
            Payload.put("songListId", songListId);
            param.put("Header", Header);
            param.put("Payload", Payload);
        } catch (Exception e) {
            Log.i(TAG, e.getLocalizedMessage());
            callback.onCallback(-3, new JSONObject(), "params error");
            return;
        }

        String fullUrl = CommonUtil.getRealRequestUrl("/music/api/v1/get-songlist-detail");
        CommonUtil.commonRequestWithHeader(fullUrl, param.toString(), headers, (code, data, msg) -> {
            int reCode = code;
            String reMsg = msg;
            if (reCode == 0) {
                try {
                    reCode = data.getInt("ret");
                    if (reCode != 0) {
                        reMsg = data.getString("msg");
                    }
                } catch (Exception e) {
                    reCode = -1;
                }
            }
            callback.onCallback(reCode, data, reMsg);
        });
    }

    /*
    获取新歌推荐
    tag - 12:内地;9:韩国;13:港台;3:欧美;8:日本;1:最新
     */
    public void getNewTrack(int tag, CommonUtil.CommonCallback callback) {
        if (firmAcctId.isEmpty()) {
            callback.onCallback(-100, new JSONObject(), "not init SDK");
            return;
        }

        String accessToken = SharedPreferencesUtil.getInstance().getString(Constant.USER_ACCESS_TOKEN);

        if(accessToken.isEmpty()) {
            callback.onCallback(-99, new JSONObject(), "not login");
            return;
        }

        if (tag != 1 && tag != 12 && tag != 9 && tag != 13 && tag != 3 && tag != 8) {
            tag = 1;
        }

        Headers headers = Headers.of(
                "Content-Type", "application/json;charset=utf-8",
                "Authorization", "Bearer " + accessToken
        );

        JSONObject Header  = new JSONObject();
        JSONObject Payload  = new JSONObject();
        JSONObject param = new JSONObject();
        try {
            Header.put("DSN", Constant.DEFAULT_APP_KEY + "_" + firmAcctId);
            Payload.put("tag", tag);
            param.put("Header", Header);
            param.put("Payload", Payload);
        } catch (Exception e) {
            Log.i(TAG, e.getLocalizedMessage());
            callback.onCallback(-3, new JSONObject(), "params error");
            return;
        }

        String fullUrl = CommonUtil.getRealRequestUrl("/music/api/v1/get-new-track");
        CommonUtil.commonRequestWithHeader(fullUrl, param.toString(), headers, (code, data, msg) -> {
            int reCode = code;
            String reMsg = msg;
            if (reCode == 0) {
                try {
                    reCode = data.getInt("ret");
                    if (reCode != 0) {
                        reMsg = data.getString("msg");
                    }
                } catch (Exception e) {
                    reCode = -1;
                }
            }
            callback.onCallback(reCode, data, reMsg);
        });
    }

    /*
    批量获取歌曲信息
    songMIds - 表示歌曲mid，多个mid用逗号分割(优先判断)上限50
    songIds - 表示歌曲id，多个id用逗号分割, 上限50
     */
    public void getSongDetailBatch(String songIds, CommonUtil.CommonCallback callback) {
        if (firmAcctId.isEmpty()) {
            callback.onCallback(-100, new JSONObject(), "not init SDK");
            return;
        }

        if (songIds.equals("")) {
            callback.onCallback(-3, new JSONObject(), "songIds error");
            return;
        }

        String accessToken = SharedPreferencesUtil.getInstance().getString(Constant.USER_ACCESS_TOKEN);

        if(accessToken.isEmpty()) {
            callback.onCallback(-99, new JSONObject(), "not login");
            return;
        }

        Headers headers = Headers.of(
                "Content-Type", "application/json;charset=utf-8",
                "Authorization", "Bearer " + accessToken
        );

        JSONObject Header  = new JSONObject();
        JSONObject Payload  = new JSONObject();
        JSONObject param = new JSONObject();
        try {
            Header.put("DSN", Constant.DEFAULT_APP_KEY + "_" + firmAcctId);
            Payload.put("songIds", songIds);
            param.put("Header", Header);
            param.put("Payload", Payload);
        } catch (Exception e) {
            Log.i(TAG, e.getLocalizedMessage());
            callback.onCallback(-3, new JSONObject(), "params error");
            return;
        }

        String fullUrl = CommonUtil.getRealRequestUrl("/music/api/v1/get-song-detail-batch");
        CommonUtil.commonRequestWithHeader(fullUrl, param.toString(), headers, (code, data, msg) -> {
            int reCode = code;
            String reMsg = msg;
            if (reCode == 0) {
                try {
                    reCode = data.getInt("ret");
                    if (reCode != 0) {
                        reMsg = data.getString("msg");
                    }
                } catch (Exception e) {
                    reCode = -1;
                }
            }
            callback.onCallback(reCode, data, reMsg);
        });
    }

    /*
    获取歌曲的歌词
    songMId - 表示歌曲mid String
    songId - 表示歌曲id int
     */
    public void getSongLyric(int songId, CommonUtil.CommonCallback callback) {
        if (firmAcctId.isEmpty()) {
            callback.onCallback(-100, new JSONObject(), "not init SDK");
            return;
        }

        String accessToken = SharedPreferencesUtil.getInstance().getString(Constant.USER_ACCESS_TOKEN);

        if(accessToken.isEmpty()) {
            callback.onCallback(-99, new JSONObject(), "not login");
            return;
        }

        Headers headers = Headers.of(
                "Content-Type", "application/json;charset=utf-8",
                "Authorization", "Bearer " + accessToken
        );

        JSONObject Header  = new JSONObject();
        JSONObject Payload  = new JSONObject();
        JSONObject param = new JSONObject();
        try {
            Header.put("DSN", Constant.DEFAULT_APP_KEY + "_" + firmAcctId);
            Payload.put("songId", songId);
            param.put("Header", Header);
            param.put("Payload", Payload);
        } catch (Exception e) {
            Log.i(TAG, e.getLocalizedMessage());
            callback.onCallback(-3, new JSONObject(), "params error");
            return;
        }

        String fullUrl = CommonUtil.getRealRequestUrl("/music/api/v1/get-lyric");
        CommonUtil.commonRequestWithHeader(fullUrl, param.toString(), headers, (code, data, msg) -> {
            int reCode = code;
            String reMsg = msg;
            if (reCode == 0) {
                try {
                    reCode = data.getInt("ret");
                    if (reCode != 0) {
                        reMsg = data.getString("msg");
                    }
                } catch (Exception e) {
                    reCode = -1;
                }
            }
            callback.onCallback(reCode, data, reMsg);
        });
    }

    /*
    获取排行榜榜单
     */
    public void getTopList(CommonUtil.CommonCallback callback) {
        if (firmAcctId.isEmpty()) {
            callback.onCallback(-100, new JSONObject(), "not init SDK");
            return;
        }

        String accessToken = SharedPreferencesUtil.getInstance().getString(Constant.USER_ACCESS_TOKEN);

        if(accessToken.isEmpty()) {
            callback.onCallback(-99, new JSONObject(), "not login");
            return;
        }

        Headers headers = Headers.of(
                "Content-Type", "application/json;charset=utf-8",
                "Authorization", "Bearer " + accessToken
        );

        JSONObject Header  = new JSONObject();
        JSONObject param = new JSONObject();
        try {
            Header.put("DSN", Constant.DEFAULT_APP_KEY + "_" + firmAcctId);

            param.put("Header", Header);
        } catch (Exception e) {
            Log.i(TAG, e.getLocalizedMessage());
            callback.onCallback(-3, new JSONObject(), "params error");
            return;
        }

        String fullUrl = CommonUtil.getRealRequestUrl("/music/api/v1/get-toplist");
        CommonUtil.commonRequestWithHeader(fullUrl, param.toString(), headers, (code, data, msg) -> {
            int reCode = code;
            String reMsg = msg;
            if (reCode == 0) {
                try {
                    reCode = data.getInt("ret");
                    if (reCode != 0) {
                        reMsg = data.getString("msg");
                    }
                } catch (Exception e) {
                    reCode = -1;
                }
            }
            callback.onCallback(reCode, data, reMsg);
        });
    }

    /*
    获取榜单详情
     */
    public void getTopListDetail(int topId, int page, int pageSize, CommonUtil.CommonCallback callback) {
        if (firmAcctId.isEmpty()) {
            callback.onCallback(-100, new JSONObject(), "not init SDK");
            return;
        }

        String accessToken = SharedPreferencesUtil.getInstance().getString(Constant.USER_ACCESS_TOKEN);

        if(accessToken.isEmpty()) {
            callback.onCallback(-99, new JSONObject(), "not login");
            return;
        }

        if (pageSize > 50) {
            pageSize = 20;
        }

        Headers headers = Headers.of(
                "Content-Type", "application/json;charset=utf-8",
                "Authorization", "Bearer " + accessToken
        );

        JSONObject Header  = new JSONObject();
        JSONObject Payload  = new JSONObject();
        JSONObject param = new JSONObject();
        try {
            Header.put("DSN", Constant.DEFAULT_APP_KEY + "_" + firmAcctId);
            Payload.put("topId", topId);
            Payload.put("page", page);
            Payload.put("pageSize", pageSize);

            param.put("Header", Header);
            param.put("Payload", Payload);
        } catch (Exception e) {
            Log.i(TAG, e.getLocalizedMessage());
            callback.onCallback(-3, new JSONObject(), "params error");
            return;
        }

        String fullUrl = CommonUtil.getRealRequestUrl("/music/api/v1/get-toplist-detail");
        CommonUtil.commonRequestWithHeader(fullUrl, param.toString(), headers, (code, data, msg) -> {
            int reCode = code;
            String reMsg = msg;
            if (reCode == 0) {
                try {
                    reCode = data.getInt("ret");
                    if (reCode != 0) {
                        reMsg = data.getString("msg");
                    }
                } catch (Exception e) {
                    reCode = -1;
                }
            }
            callback.onCallback(reCode, data, reMsg);
        });
    }

    /*
    获取每日推荐30首
     */
    public void getRecSongs(CommonUtil.CommonCallback callback) {
        if (firmAcctId.isEmpty()) {
            callback.onCallback(-100, new JSONObject(), "not init SDK");
            return;
        }

        String accessToken = SharedPreferencesUtil.getInstance().getString(Constant.USER_ACCESS_TOKEN);

        if(accessToken.isEmpty()) {
            callback.onCallback(-99, new JSONObject(), "not login");
            return;
        }

        Headers headers = Headers.of(
                "Content-Type", "application/json;charset=utf-8",
                "Authorization", "Bearer " + accessToken
        );

        JSONObject Header  = new JSONObject();
        JSONObject param = new JSONObject();
        try {
            Header.put("DSN", Constant.DEFAULT_APP_KEY + "_" + firmAcctId);

            param.put("Header", Header);
        } catch (Exception e) {
            Log.i(TAG, e.getLocalizedMessage());
            callback.onCallback(-3, new JSONObject(), "params error");
            return;
        }

        String fullUrl = CommonUtil.getRealRequestUrl("/music/api/v1/rec-songs");
        CommonUtil.commonRequestWithHeader(fullUrl, param.toString(), headers, (code, data, msg) -> {
            int reCode = code;
            String reMsg = msg;
            if (reCode == 0) {
                try {
                    reCode = data.getInt("ret");
                    if (reCode != 0) {
                        reMsg = data.getString("msg");
                    }
                } catch (Exception e) {
                    reCode = -1;
                }
            }
            callback.onCallback(reCode, data, reMsg);
        });
    }

    /*
    个性化推荐歌曲
     */
    public void getRecIndividualSongs(CommonUtil.CommonCallback callback) {
        if (firmAcctId.isEmpty()) {
            callback.onCallback(-100, new JSONObject(), "not init SDK");
            return;
        }

        String accessToken = SharedPreferencesUtil.getInstance().getString(Constant.USER_ACCESS_TOKEN);

        if(accessToken.isEmpty()) {
            callback.onCallback(-99, new JSONObject(), "not login");
            return;
        }

        Headers headers = Headers.of(
                "Content-Type", "application/json;charset=utf-8",
                "Authorization", "Bearer " + accessToken
        );

        JSONObject Header  = new JSONObject();
        JSONObject param = new JSONObject();
        try {
            Header.put("DSN", Constant.DEFAULT_APP_KEY + "_" + firmAcctId);

            param.put("Header", Header);
        } catch (Exception e) {
            Log.i(TAG, e.getLocalizedMessage());
            callback.onCallback(-3, new JSONObject(), "params error");
            return;
        }

        String fullUrl = CommonUtil.getRealRequestUrl("/music/api/v1/rec-individual-songs");
        CommonUtil.commonRequestWithHeader(fullUrl, param.toString(), headers, (code, data, msg) -> {
            int reCode = code;
            String reMsg = msg;
            if (reCode == 0) {
                try {
                    reCode = data.getInt("ret");
                    if (reCode != 0) {
                        reMsg = data.getString("msg");
                    }
                } catch (Exception e) {
                    reCode = -1;
                }
            }
            callback.onCallback(reCode, data, reMsg);
        });
    }

    /*
    个性化推荐歌曲
    sn - 机器码;序列号;唯一标识
    type - 要获取的内容类型.200-单曲;500-歌单.可多选如 200,500
     */
    public void getRecHomePage(String sn, String type, CommonUtil.CommonCallback callback) {
        if (firmAcctId.isEmpty()) {
            callback.onCallback(-100, new JSONObject(), "not init SDK");
            return;
        }

        if (sn.equals("")) {
            callback.onCallback(-3, new JSONObject(), "param error");
            return;
        }

        if (!type.contains("200") && !type.contains("500")) {
            type = "200";
        }

        String accessToken = SharedPreferencesUtil.getInstance().getString(Constant.USER_ACCESS_TOKEN);

        if(accessToken.isEmpty()) {
            callback.onCallback(-99, new JSONObject(), "not login");
            return;
        }

        Headers headers = Headers.of(
                "Content-Type", "application/json;charset=utf-8",
                "Authorization", "Bearer " + accessToken
        );

        JSONObject Header  = new JSONObject();
        JSONObject Payload  = new JSONObject();
        JSONObject param = new JSONObject();
        try {
            Header.put("DSN", Constant.DEFAULT_APP_KEY + "_" + firmAcctId);
            Payload.put("sn", sn);
            Payload.put("type", type);

            param.put("Header", Header);
            param.put("Payload", Payload);
        } catch (Exception e) {
            Log.i(TAG, e.getLocalizedMessage());
            callback.onCallback(-3, new JSONObject(), "params error");
            return;
        }

        String fullUrl = CommonUtil.getRealRequestUrl("/music/api/v1/rec-homepage");
        CommonUtil.commonRequestWithHeader(fullUrl, param.toString(), headers, (code, data, msg) -> {
            int reCode = code;
            String reMsg = msg;
            if (reCode == 0) {
                try {
                    reCode = data.getInt("ret");
                    if (reCode != 0) {
                        reMsg = data.getString("msg");
                    }
                } catch (Exception e) {
                    reCode = -1;
                }
            }
            callback.onCallback(reCode, data, reMsg);
        });
    }

    /*
    查询QQ音乐绿钻会员
     */
    public void getGreenVipInfo(CommonUtil.CommonCallback callback) {
        if (firmAcctId.isEmpty()) {
            callback.onCallback(-100, new JSONObject(), "not init SDK");
            return;
        }

        String accessToken = SharedPreferencesUtil.getInstance().getString(Constant.USER_ACCESS_TOKEN);

        if(accessToken.isEmpty()) {
            callback.onCallback(-99, new JSONObject(), "not login");
            return;
        }

        Headers headers = Headers.of(
                "Content-Type", "application/json;charset=utf-8",
                "Authorization", "Bearer " + accessToken
        );

        JSONObject Header  = new JSONObject();
        JSONObject param = new JSONObject();
        try {
            Header.put("DSN", Constant.DEFAULT_APP_KEY + "_" + firmAcctId);

            param.put("Header", Header);
        } catch (Exception e) {
            Log.i(TAG, e.getLocalizedMessage());
            callback.onCallback(-3, new JSONObject(), "params error");
            return;
        }

        String fullUrl = CommonUtil.getRealRequestUrl("/music/api/v1/get-green-vip-info");
        CommonUtil.commonRequestWithHeader(fullUrl, param.toString(), headers, (code, data, msg) -> {
            int reCode = code;
            String reMsg = msg;
            if (reCode == 0) {
                try {
                    reCode = data.getInt("ret");
                    if (reCode != 0) {
                        reMsg = data.getString("msg");
                    }
                } catch (Exception e) {
                    reCode = -1;
                }
            }
            callback.onCallback(reCode, data, reMsg);
        });
    }
}
