package com.jingluo.testdmapi;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.jingluo.dm_api_module.MusicServiceSingleton;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private String sig = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button initSDKBtn = findViewById(R.id.initSdk_btn);
        Button getSigBtn = findViewById(R.id.get_sig_btn);
        Button loginBtn = findViewById(R.id.login_btn);
        Button refreshTokenBtn = findViewById(R.id.refresh_token_btn);
        Button generateClientIdBtn = findViewById(R.id.generate_client_id_btn);
        Button checkDeviceBtn = findViewById(R.id.check_device_btn);
        Button bindingBtn = findViewById(R.id.binding_btn);
        Button unbindingBtn = findViewById(R.id.unbinding_btn);
        Button refreshDeviceTokenBtn = findViewById(R.id.refresh_device_token_btn);
        Button genQRCodeBtn = findViewById(R.id.gen_qr_code_btn);
        Button checkQRCodeStatusBtn = findViewById(R.id.check_qr_code_status_btn);
        Button unbindingThirdAccountBtn = findViewById(R.id.unbinding_third_account_btn);
        Button bindingThirdAccountInfoBtn = findViewById(R.id.binding_third_account_info_btn);

        Button searchMusicBtn = findViewById(R.id.search_music_btn);
        Button searchMusicByLyricBtn = findViewById(R.id.search_music_by_lyric);

        Button squareSongListBtn = findViewById(R.id.square_songlist);
        Button categorySongListBtn = findViewById(R.id.category_songlist_btn);
        Button selfSongListBtn = findViewById(R.id.self_songlist_btn);
        Button selfFavSongListBtn = findViewById(R.id.self_fav_songlist_btn);
        Button songListDetailBtn = findViewById(R.id.songlist_detail_btn);
        Button newTrackBtn = findViewById(R.id.new_track_btn);
        Button songDetailBatchBtn = findViewById(R.id.song_detail_batch_btn);
        Button songLyricBtn = findViewById(R.id.song_lyric_btn);

        Button topListBtn = findViewById(R.id.top_list_btn);
        Button topListDetailBtn = findViewById(R.id.top_list_detail_btn);

        Button recSongsBtn = findViewById(R.id.rec_songs_btn);
        Button recIndividualSongsBtn = findViewById(R.id.rec_individual_songs_btn);
        Button recHomePageBtn = findViewById(R.id.rec_homepage_btn);

        Button greenVipInfoBtn = findViewById(R.id.green_vip_info_btn);

        Context context = this;

        MusicServiceSingleton.getInstance().initSDK("a09f107e2cb2", this);

        initSDKBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicServiceSingleton.getInstance().initSDK("a09f107e2cb2", context);
            }
        });

        getSigBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MusicServiceSingleton.getInstance().getSig((code, data) -> {
                    Log.i(TAG, "获取sig 操作---状态码：" + String.valueOf(code) + " 信息：" + data);
                    if (code == 0) {
                        sig = data;
                    }
                });
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicServiceSingleton.getInstance().loginWithSig(sig, (code, data, msg) -> {
                    Log.i(TAG, "登录 操作---状态码：" + String.valueOf(code) + " data：" + data.toString() + " msg：" + msg);
                });
            }
        });

        refreshTokenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicServiceSingleton.getInstance().refreshAccessToken((code, data, msg) -> {
                    Log.i(TAG, "刷新token 操作---状态码：" + String.valueOf(code) + " data：" + data.toString() + " msg：" + msg);
                });
            }
        });

        generateClientIdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicServiceSingleton.getInstance().generateClientIdAndAuthCode((code, data, msg) -> {
                    Log.i(TAG, "生成绑定设备的clientId 操作---状态码：" + String.valueOf(code) + " data：" + data.toString() + " msg：" + msg);
                });
            }
        });

        bindingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicServiceSingleton.getInstance().bindingDevice((code, data, msg) -> {
                    Log.i(TAG, "绑定设备 操作---状态码：" + String.valueOf(code) + " data：" + data.toString() + " msg：" + msg);
                });
            }
        });

        checkDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicServiceSingleton.getInstance().checkBindingDevices((code, data, msg) -> {
                    Log.i(TAG, "查询绑定的设备 操作---状态码：" + String.valueOf(code) + " data：" + data.toString() + " msg：" + msg);
                });
            }
        });

        unbindingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicServiceSingleton.getInstance().unbindingDevice((code, data, msg) -> {
                    Log.i(TAG, "解绑设备 操作---状态码：" + String.valueOf(code) + " data：" + data.toString() + " msg：" + msg);
                });
            }
        });

        refreshDeviceTokenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicServiceSingleton.getInstance().refreshDeviceToken((code, data, msg) -> {
                    Log.i(TAG, "刷新设备token 操作---状态码：" + String.valueOf(code) + " data：" + data.toString() + " msg：" + msg);
                });
            }
        });

        genQRCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicServiceSingleton.getInstance().getQQMusicQRCode((code, data, msg) -> {
                    Log.i(TAG, "生成qq音乐授权二维码 操作---状态码：" + String.valueOf(code) + " data：" + data.toString() + " msg：" + msg);
                });
            }
        });

        checkQRCodeStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicServiceSingleton.getInstance().checkQQMusicQRCodeStatus((code, data, msg) -> {
                    Log.i(TAG, "查询qq音乐授权二维码状态 操作---状态码：" + String.valueOf(code) + " data：" + data.toString() + " msg：" + msg);
                });
            }
        });

        unbindingThirdAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicServiceSingleton.getInstance().bindingOrUnbindingAccount(false, "", "", (code, data, msg) -> {
                    Log.i(TAG, "解绑第三方授权状态 操作---状态码：" + String.valueOf(code) + " data：" + data.toString() + " msg：" + msg);
                });
            }
        });

        bindingThirdAccountInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicServiceSingleton.getInstance().getThirdBindingStatus((code, data, msg) -> {
                    Log.i(TAG, "查询第三方授权状态 操作---状态码：" + String.valueOf(code) + " data：" + data.toString() + " msg：" + msg);
                });
            }
        });

        searchMusicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicServiceSingleton.getInstance().searchMusicCustom("陈奕迅", 1, 0, 20, (code, data, msg) -> {
                    Log.i(TAG, "搜索陈奕迅的歌 操作---状态码：" + String.valueOf(code) + " data：" + data.toString() + " msg：" + msg);
                });
            }
        });

        searchMusicByLyricBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicServiceSingleton.getInstance().searchMusicByLyric("山茶花读不懂白玫瑰", 1, 10, (code, data, msg) -> {
                    Log.i(TAG, "搜索歌词 '山茶花读不懂白玫瑰' 操作---状态码：" + String.valueOf(code) + " data：" + data.toString() + " msg：" + msg);
                });
            }
        });

        squareSongListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicServiceSingleton.getInstance().getSquareSongList(0, 10, 0, 0, (code, data, msg) -> {
                    Log.i(TAG, "获取热门歌单 操作---状态码：" + String.valueOf(code) + " data：" + data.toString() + " msg：" + msg);
                });
            }
        });

        categorySongListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicServiceSingleton.getInstance().getCategorySongList(2, 3418, 0, 2, (code, data, msg) -> {
                    Log.i(TAG, "获取分类歌单 操作---状态码：" + String.valueOf(code) + " data：" + data.toString() + " msg：" + msg);
                });
            }
        });

        selfSongListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicServiceSingleton.getInstance().getSelfSongList((code, data, msg) -> {
                    Log.i(TAG, "个人歌单 操作---状态码：" + String.valueOf(code) + " data：" + data.toString() + " msg：" + msg);
                });
            }
        });

        selfFavSongListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicServiceSingleton.getInstance().getSelfFavSongList(3, 0, (code, data, msg) -> {
                    Log.i(TAG, "我的收藏歌单 操作---状态码：" + String.valueOf(code) + " data：" + data.toString() + " msg：" + msg);
                });
            }
        });

        songListDetailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicServiceSingleton.getInstance().getSongListDetail(3527089223L, 0, 50, (code, data, msg) -> {
                    Log.i(TAG, "获取歌单下歌曲 操作---状态码：" + String.valueOf(code) + " data：" + data.toString() + " msg：" + msg);
                });
            }
        });

        newTrackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicServiceSingleton.getInstance().getNewTrack(1, (code, data, msg) -> {
                    Log.i(TAG, "获取新歌推荐 操作---状态码：" + String.valueOf(code) + " data：" + data.toString() + " msg：" + msg);
                });
            }
        });

        songDetailBatchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicServiceSingleton.getInstance().getSongDetailBatch("340584113", (code, data, msg) -> {
                    Log.i(TAG, "获取歌曲详情 操作---状态码：" + String.valueOf(code) + " data：" + data.toString() + " msg：" + msg);
                });
            }
        });

        songLyricBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicServiceSingleton.getInstance().getSongLyric(340584113, (code, data, msg) -> {
                    Log.i(TAG, "获取歌曲歌词 操作---状态码：" + String.valueOf(code) + " data：" + data.toString() + " msg：" + msg);
                });
            }
        });

        topListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicServiceSingleton.getInstance().getTopList((code, data, msg) -> {
                    Log.i(TAG, "获取排行榜榜单 操作---状态码：" + String.valueOf(code) + " data：" + data.toString() + " msg：" + msg);
                });
            }
        });

        topListDetailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicServiceSingleton.getInstance().getTopListDetail(62, 0, 20, (code, data, msg) -> {
                    Log.i(TAG, "获取排行榜榜单详情 操作---状态码：" + String.valueOf(code) + " data：" + data.toString() + " msg：" + msg);
                });
            }
        });

        recSongsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicServiceSingleton.getInstance().getRecSongs((code, data, msg) -> {
                    Log.i(TAG, "获取每日30首推荐 操作---状态码：" + String.valueOf(code) + " data：" + data.toString() + " msg：" + msg);
                });
            }
        });

        recIndividualSongsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicServiceSingleton.getInstance().getRecIndividualSongs((code, data, msg) -> {
                    Log.i(TAG, "获取个性化推荐歌曲 操作---状态码：" + String.valueOf(code) + " data：" + data.toString() + " msg：" + msg);
                });
            }
        });

        recHomePageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicServiceSingleton.getInstance().getRecHomePage("a09f107e2cb2", "200,500", (code, data, msg) -> {
                    Log.i(TAG, "获取首页推荐 操作---状态码：" + String.valueOf(code) + " data：" + data.toString() + " msg：" + msg);
                });
            }
        });

        greenVipInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicServiceSingleton.getInstance().getGreenVipInfo((code, data, msg) -> {
                    Log.i(TAG, "获取绿钻会员 操作---状态码：" + String.valueOf(code) + " data：" + data.toString() + " msg：" + msg);
                });
            }
        });
    }
}