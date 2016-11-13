package com.aaron.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import com.aaron.bean.MusicInfo;
import com.aaron.musicplayer.MainActivity;
import com.aaron.musicplayer.R;
import com.aaron.utils.MusicUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/16.
 */
public class PlayService extends Service implements AudioManager.OnAudioFocusChangeListener {
    private MediaPlayer mMediaPlayer;
    //play mode
    public static final int MODE_LOOP_ALL = 0;
    public static final int MODE_LOOP_ONE = 1;
    public static final int MODE_RANDOM = 2;

    // music play state
    public static final int STATE_STOP = 0;
    public static final int STATE_PLAY = 1;
    public static final int STATE_PAUSE = 2;
    //init state and mode
    public int state = STATE_STOP;
    public int mode = MODE_LOOP_ALL;
    public boolean isPlay = false;
    public int currTime = 0;
    public int position = 0;
//    private Messenger messenger;
//    private Timer timer;

    public List<MusicInfo> musicList;
    private List<ServiceCallback> mListeners;
    private Notification mNotification;

    @Override
    public IBinder onBind(Intent intent) {
        musicList.clear();
        musicList = MusicUtil.getMusicList(this);
        return new MyBinder();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new MediaPlayer();
        musicList = new ArrayList<>();
        mListeners = new ArrayList<>();
//        timer = new Timer();
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (mode == MODE_LOOP_ONE) {
                    mediaPlayer.start();
                } else if (mode == MODE_LOOP_ALL) {
                    //如果是循环播放，就在playactivity中进行回调
                    nextMusic();
                }
            }
        });
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer player) {
                initNotification();
                startForeground(1,mNotification);
            }
        });

//        timer.schedule(new TimerTask() {
//            public void run() {
//                try {
//                    Message msg = Message.obtain();
//                    msg.what = 0;
//                    msg.arg1 = mMediaPlayer.getCurrentPosition();
//                    msg.arg2 = mMediaPlayer.getDuration();
//                    messenger.send(msg);
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, 0, 1000);

        //电话监听
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        if (messenger == null) {
//            messenger = (Messenger) intent.getExtras().get("messenger");
//        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        stopForeground(true);
//        timer.cancel();
    }

    public void playMusic(MusicInfo music) {
        mMediaPlayer.reset();
        try {
            mMediaPlayer.setDataSource(PlayService.this, Uri.parse(music.url));
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            mMediaPlayer.reset();
        }

        mMediaPlayer.start();
        isPlay = true;
        state = STATE_PLAY;
        //state changes
        sChange.onStateChanged(state);
    }

    public void pauseMusic() {
        if (mMediaPlayer != null && isPlay) {
            mMediaPlayer.pause();
            isPlay = false;
            //获取当前播放时长，getduration是获取歌曲总长度
            currTime = mMediaPlayer.getCurrentPosition();
            state = STATE_PAUSE;
            //state changed
            sChange.onStateChanged(state);
        }
    }

    /**
     * next music
     */
    public void nextMusic() {
        //全部循环
        if (mode == MODE_LOOP_ALL) {
            if (position < musicList.size() - 1) {
                ++position;
                MusicInfo nextMusic = musicList.get(position);
                playMusic(nextMusic);
            } else {
                position = 0;
                playMusic(musicList.get(0));
            }
        } else if (mode == MODE_LOOP_ONE) {//单曲循环
            playMusic(musicList.get(position));
        } else {

        }
        notifyListeners(position);

    }

    /**
     * previous music
     */
    public void preMusic() {
        if (mode == MODE_LOOP_ALL) {//全部循环
            if (position == 0) {
                position = musicList.size() - 1;
                MusicInfo preMusic = musicList.get(position);
                playMusic(preMusic);
            } else {
                --position;
                playMusic(musicList.get(position));
            }
        } else if (mode == MODE_LOOP_ONE) {//单曲循环
            playMusic(musicList.get(position));
        } else {

        }
        notifyListeners(position);
    }

    public void resumeMusic() {
        if (mMediaPlayer != null && !isPlay) {
            if (currTime > 0) {
                mMediaPlayer.seekTo(currTime);
            }
            mMediaPlayer.start();
            isPlay = true;
            state = STATE_PLAY;
            //state change
            sChange.onStateChanged(state);
        }
    }

    public void stopMusic() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            try {
                mMediaPlayer.prepare(); // 在调用stop后如果需要再次通过start进行播放,需要之前调用prepare函数
            } catch (Exception e) {
                e.printStackTrace();
            }
            state = STATE_STOP;
            //state change
            sChange.onStateChanged(state);
        }
    }

    public int getCurrentPosition(){
        return mMediaPlayer.getCurrentPosition();
    }
    public void seekPlay(int progress){
        if (mMediaPlayer != null && state == STATE_PLAY){
            mMediaPlayer.seekTo(progress);
        }
    }

    //返回service对象
    public class MyBinder extends Binder {
        public PlayService getService() {
            return PlayService.this;
        }
    }

    /**
     * 在playActivity中设置当前播放模式：单曲、循环
     *
     * @param mode 循环模式 : 可选 MODE_LOOP_ALL/MODE_LOOP_ONE
     */
    public void setMode(int mode) {
        this.mode = mode;
    }
    public void setPosition(int i) {
        position = i;
    }
    /*
     * 监听者模式
     */
    public interface ServiceCallback {
        void onPositonChanged(int i);
    }

    public void addServiceCallback(ServiceCallback callback) {
        mListeners.add(callback);
    }

    public void removeServiceCallback(ServiceCallback callback) {
        mListeners.remove(callback);
    }

    private void notifyListeners(int i) {
        for (ServiceCallback listener : mListeners) {
            listener.onPositonChanged(i);
        }

    }
    //监听者模式结束

    public interface StateChange{
        void onStateChanged(int sta);
    }
    private StateChange sChange;

    public void setSChange(StateChange s){
        sChange = s;
    }



    //音频焦点监听器
    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange){
            case AudioManager.AUDIOFOCUS_GAIN://gain focus
                resumeMusic();
                break;
            case AudioManager.AUDIOFOCUS_LOSS://loss focus
                stopMusic();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT://loss temporary
                pauseMusic();
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void initNotification(){
        //NotificationManager用来管理notification的显示和消失等
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this,1,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        mNotification = new Notification.Builder(this)
                .setContentIntent(pIntent)
                .setSmallIcon(R.drawable.music_bottom)
                .setContentTitle(musicList.get(position).title)
                .setContentText(musicList.get(position).artist)
                .setOngoing(true)
        .build();
//        mNManager.notify(0,mNotification);//会显示两个
    }


}
