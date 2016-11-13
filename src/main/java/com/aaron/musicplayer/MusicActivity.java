package com.aaron.musicplayer;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.aaron.bean.MusicInfo;
import com.aaron.service.PlayService;
import com.aaron.utils.MusicUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/8/20.
 */
public class MusicActivity extends AppCompatActivity implements View.OnClickListener,PlayService.ServiceCallback{
    @BindView(R.id.iv_music_back)
    ImageView ivMusicBack;
    //bact to mainactivity
    @BindView(R.id.tv_music_title)
    TextView tvMusicTitle;//toolbar title
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_music_play_time)
    TextView tvMusicPlayTime;
    @BindView(R.id.seekbar_music)
    SeekBar seekbarMusic;
    @BindView(R.id.tv_music_duration)
    TextView tvMusicDuration;
    @BindView(R.id.iv_music_play_mode)
    ImageView ivMusicPlayMode;
    @BindView(R.id.iv_music_previous)
    ImageView ivMusicPrevious;
    @BindView(R.id.iv_music_play)
    ImageView ivMusicPlay;
    @BindView(R.id.iv_music_next)
    ImageView ivMusicNext;
    @BindView(R.id.iv_music_playlist)
    ImageView ivMusicPlaylist;
    @BindView(R.id.iv_music_display)
    ImageView ivMusicDisplay;
    @BindView(R.id.ll_music_container)
    LinearLayout llContainer;



    private int position;//当前播放曲目
    private PlayService mService;
    private List<MusicInfo> musics;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService = ((PlayService.MyBinder) iBinder).getService();
            mService.addServiceCallback(MusicActivity.this);
            updateSeekBar();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
        }
    };
    private boolean firstInto;//第一次进入播放界面
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private Intent mIntent;//service intent;
    private Handler mHandler = new Handler();
// {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what){
//                case 0:
//                    int currTime = msg.arg1;
//                    int duration = msg.arg2;
//                    String time = MusicUtil.formatTime(currTime);
//                    tvMusicPlayTime.setText(time);
//                    seekbarMusic.setProgress(currTime/duration);
//                    break;
//            }
//        }
//    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        ButterKnife.bind(this);
        initData();
        initView();
    }

    private void initView() {
        ivMusicBack.setOnClickListener(this);
        ivMusicPrevious.setOnClickListener(this);
        ivMusicNext.setOnClickListener(this);
        ivMusicPrevious.setOnClickListener(this);
        ivMusicPlay.setOnClickListener(this);
        ivMusicPlayMode.setOnClickListener(this);
        ivMusicPlaylist.setOnClickListener(this);
        setPlayingMusic(position,firstInto);
        int playMode = sp.getInt("mode", PlayService.MODE_LOOP_ALL);
        if (playMode == PlayService.MODE_LOOP_ALL){
            ivMusicPlayMode.setImageResource(R.drawable.play_icn_loop_prs);
        }else if (playMode == PlayService.MODE_LOOP_ONE){
            ivMusicPlayMode.setImageResource(R.drawable.play_icn_one_prs);
        }
        seekbarMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                if (fromUser){
                    mService.seekPlay(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void initData() {
        musics = new ArrayList<>();
        musics = MusicUtil.getMusicList(this);
        Intent intent1 = getIntent();
        position = intent1.getIntExtra("musicposition",0);
        firstInto = intent1.getBooleanExtra("firstInto",true);
        mIntent = new Intent(MusicActivity.this, PlayService.class);
//        mIntent.putExtra("messenger", new Messenger(mHandler));
//        startService(mIntent);
        bindService(mIntent,conn, Context.BIND_AUTO_CREATE);
        sp = getSharedPreferences("play_mode" , MODE_PRIVATE);
        editor = sp.edit();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_music_back:
                this.finish();
                break;
            case R.id.iv_music_playlist:
                this.finish();
                break;
            case R.id.iv_music_previous:
                mService.preMusic();
                break;
            case R.id.iv_music_next:
                mService.nextMusic();
                break;
            case R.id.iv_music_play:
                MusicInfo music = musics.get(position);
                if (mService.state == mService.STATE_STOP){
                    mService.playMusic(music);
                    ivMusicPlay.setImageResource(R.drawable.play_rdi_btn_pause);
                }else if (mService.state == mService.STATE_PLAY){
                    mService.pauseMusic();
                    ivMusicPlay.setImageResource(R.drawable.play_rdi_btn_play);
                }else if (mService.state == mService.STATE_PAUSE){
                    mService.resumeMusic();
                    ivMusicPlay.setImageResource(R.drawable.play_rdi_btn_pause);
                    updateSeekBar();
                }
                break;
            case R.id.iv_music_play_mode:
                if (mService.mode == PlayService.MODE_LOOP_ALL){
                    mService.setMode(PlayService.MODE_LOOP_ONE);
                    ivMusicPlayMode.setImageResource(R.drawable.play_icn_one_prs);
                    editor.putInt("mode",PlayService.MODE_LOOP_ONE).commit();
                }else if (mService.mode == PlayService.MODE_LOOP_ONE){
                    mService.setMode(PlayService.MODE_LOOP_ALL);
                    ivMusicPlayMode.setImageResource(R.drawable.play_icn_loop_prs);
                    editor.putInt("mode",PlayService.MODE_LOOP_ALL).commit();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
//        stopService(mIntent);
        mService.removeServiceCallback(this);
    }

    /**
     * service  call back
     * @param i
     */
    @Override
    public void onPositonChanged(int i) {
        position = i;
        setPlayingMusic(i,mService.isPlay);
    }

//    public void onStateChanged(int state) {
//        if (mService.state == mService.STATE_STOP){
//            ivMusicPlay.setImageResource(R.drawable.play_rdi_btn_pause);
//        }else if (mService.state == mService.STATE_PLAY){
//            ivMusicPlay.setImageResource(R.drawable.play_rdi_btn_play);
//        }else if (mService.state == mService.STATE_PAUSE){
//            ivMusicPlay.setImageResource(R.drawable.play_rdi_btn_pause);
//        }
//    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setPlayingMusic(int i, boolean play) {
        MusicInfo info  = musics.get(i);
        tvMusicTitle.setText(info.title);
        if (mService != null){
            tvMusicPlayTime.setText(MusicUtil.formatTime(mService.getCurrentPosition()));
        }else {
            tvMusicPlayTime.setText("00:00");
        }
        tvMusicDuration.setText(MusicUtil.formatTime(info.duration));
        Bitmap artwork = MusicUtil.getArtwork(this, info.id, info.albumId, true, false);
        ivMusicDisplay.setImageBitmap(artwork);
        if (play){
            ivMusicPlay.setImageResource(R.drawable.play_rdi_btn_pause);
        }else {
            ivMusicPlay.setImageResource(R.drawable.play_rdi_btn_play);
        }
        Bitmap artwork1 = MusicUtil.getArtwork(this, info.id, info.albumId, false, false);
        if (artwork1 != null){
            llContainer.setBackground(new BitmapDrawable(MusicUtil.fastblur(artwork1,50)));
        }else {
            llContainer.setBackground(new ColorDrawable(Color.BLACK));
        }

    }

    private void updateSeekBar(){
        if (mService == null)
            return;
        if (mService.state == PlayService.STATE_PLAY) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                        int currTime = mService.getCurrentPosition();
                        int duration = (int) musics.get(position).duration;
                        String time = MusicUtil.formatTime(currTime);
                        tvMusicPlayTime.setText(time);
                        seekbarMusic.setMax(duration);
                        seekbarMusic.setProgress(currTime);
                        updateSeekBar();
                }
            }, 1000);
        }
    }

}
