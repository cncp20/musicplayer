package com.aaron.musicplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aaron.adapter.MainVPAdapter;
import com.aaron.bean.MusicInfo;
import com.aaron.fragment.LocalMusicFragment;
import com.aaron.fragment.NetMusicFragment;
import com.aaron.service.PlayService;
import com.aaron.utils.MusicUtil;
import com.aaron.utils.T;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        LocalMusicFragment.MainActivityCallBack,PlayService.ServiceCallback,PlayService.StateChange{

    @BindView(R.id.iv_top_menu)
    ImageView ivTopMenu;
    @BindView(R.id.iv_top_search)
    ImageView ivTopSearch;
    @BindView(R.id.iv_top_music)
    ImageView ivTopMusic;
    @BindView(R.id.iv_top_discover)
    ImageView ivTopDiscover;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.vp_main)
    ViewPager vpMain;

    @BindView(R.id.iv_bottom_music_pic)
    ImageView ivBottomMusicPic;
    @BindView(R.id.tv_bottom_music_name)
    TextView tvBottomMusicName;
    @BindView(R.id.tv_bottom_music_author)
    TextView tvBottomMusicAuthor;
    @BindView(R.id.ib_bottom_play)
    ImageButton ibBottomPlay;
    @BindView(R.id.ib_bottom_next)
    ImageButton ibBottomNext;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.ll_bottom_control)
    LinearLayout llControl;
    @BindView(R.id.bt_menu_finish)
    Button btFinish;
    @BindView(R.id.bt_menu_setting)
    Button btSetting;


    private Boolean isOpen = false;
    private List<Fragment> mFragments;
    private PlayService mService;
    private LocalMusicFragment lmFragment;
    private NetMusicFragment ntFragment;
    private List<MusicInfo> musics;
    public static int position = 0 ;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;



    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService = ((PlayService.MyBinder) iBinder).getService();
            mService.addServiceCallback(MainActivity.this);
            mService.setPosition(position);
            mService.setSChange(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initData();
        initView();
    }

    private void initData(){
        mFragments = new ArrayList<>();
        lmFragment = new LocalMusicFragment();
        ntFragment = new NetMusicFragment();
        musics = new ArrayList<>();
        musics = MusicUtil.getMusicList(this);
        //设置回调
        lmFragment.setMainActivityCallBack(this);
        mFragments.add(lmFragment);
        mFragments.add(ntFragment);
        sp = getSharedPreferences("music_player",MODE_PRIVATE);
        editor = sp.edit();
        position = sp.getInt("position" , 0);
        Intent intent = new Intent(MainActivity.this,PlayService.class);
        bindService(intent,conn, Context.BIND_AUTO_CREATE);
    }

    private void initView(){
        ivTopMenu.setOnClickListener(this);
        ivTopMusic.setOnClickListener(this);
        ivTopDiscover.setOnClickListener(this);
        ivTopSearch.setOnClickListener(this);
        ibBottomPlay.setOnClickListener(this);
        ibBottomNext.setOnClickListener(this);
        llControl.setOnClickListener(this);
        btFinish.setOnClickListener(this);
        btSetting.setOnClickListener(this);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                isOpen = true;
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                isOpen = false;
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        vpMain.setAdapter(new MainVPAdapter(getSupportFragmentManager(),mFragments));
        setPageSelected(0);
        vpMain.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        //显示上次播放的歌曲信息
        MusicInfo music = musics.get(position);
        tvBottomMusicName.setText(music.title);
        tvBottomMusicAuthor.setText(music.artist);
        ivBottomMusicPic.setImageBitmap(MusicUtil.getArtwork(this,music.id,music.albumId,true,true));
        ibBottomPlay.setImageResource(R.drawable.playbar_btn_play);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_top_menu:
                if (!isOpen){
                    drawerLayout.openDrawer(Gravity.LEFT);
                    isOpen = true;
                }
                break;
            case R.id.iv_top_music:
                setPageSelected(0);
                vpMain.setCurrentItem(0);
                break;
            case R.id.iv_top_discover:
                setPageSelected(1);
                vpMain.setCurrentItem(1);
                break;
            case R.id.ib_bottom_play:
                MusicInfo music = musics.get(position);
                if (mService.state == mService.STATE_STOP){
                    mService.playMusic(music);
                    ibBottomPlay.setImageResource(R.drawable.playbar_btn_pause);
                }else if (mService.state == mService.STATE_PLAY){
                    mService.pauseMusic();
                    ibBottomPlay.setImageResource(R.drawable.playbar_btn_play);
                }else if (mService.state == mService.STATE_PAUSE){
                    mService.resumeMusic();
                    ibBottomPlay.setImageResource(R.drawable.playbar_btn_pause);
                }
                break;

            case R.id.ib_bottom_next:
                mService.nextMusic();
                break;
            case R.id.ll_bottom_control:
                Intent intent = new Intent(MainActivity.this,MusicActivity.class);
                intent.putExtra("musicposition",position);
                intent.putExtra("firstInto" , mService.isPlay);
                startActivity(intent);
                break;
            case R.id.bt_menu_finish:
                drawerLayout.closeDrawer(Gravity.LEFT);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("是否关闭音乐播放器").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MainActivity.this.finish();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
                break;
            case R.id.bt_menu_setting:
                T.show(this,"setting");
                drawerLayout.closeDrawer(Gravity.LEFT);
                break;
        }
    }

    private void setPageSelected(int position){
        switch (position){
            case 0:
                ivTopMusic.setSelected(true);
                ivTopDiscover.setSelected(false);
                break;
            case 1:
                ivTopMusic.setSelected(false);
                ivTopDiscover.setSelected(true);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
        if (mService != null){
            mService.removeServiceCallback(MainActivity.this);
            mService = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (isOpen){
            drawerLayout.closeDrawer(Gravity.LEFT);
            return;
        }
        if (mService.isPlay){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            return;
        }
        super.onBackPressed();
    }


    /**
     * fragment的回调函数
     * @param i
     */
    @Override
    public void onMusicPosition(int i) {
        setBottomInfo(i);
        mService.setPosition(i);
        MusicInfo music = musics.get(i);
        mService.playMusic(music);
        editor.putInt("position" , i).commit();
        position = i;
    }
    /**
     * 设置底部状态
     * @param i
     */
    private void setBottomInfo(int i){
        MusicInfo music = musics.get(i);
        tvBottomMusicName.setText(music.title);
        tvBottomMusicAuthor.setText(music.artist);
        ivBottomMusicPic.setImageBitmap(MusicUtil.getArtwork(this,music.id,music.albumId,true,true));
        if (mService.isPlay){
            ibBottomPlay.setImageResource(R.drawable.playbar_btn_pause);
        }else {
            ibBottomPlay.setImageResource(R.drawable.playbar_btn_play);
        }
    }

    /**
     * service 播放的歌曲发生变化的回调
     * @param i
     */
    @Override
    public void onPositonChanged(int i) {
        position = i;
        setBottomInfo(i);
        editor.putInt("position" , i).commit();
    }

    @Override
    public void onStateChanged(int sta) {
        if (sta == mService.STATE_STOP){
            ibBottomPlay.setImageResource(R.drawable.playbar_btn_play);
        }else if (sta == mService.STATE_PLAY){
            ibBottomPlay.setImageResource(R.drawable.playbar_btn_pause);
        }else if (sta == mService.STATE_PAUSE){
            ibBottomPlay.setImageResource(R.drawable.playbar_btn_play);
        }
    }
//    private void initNotification(int j){
//        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification);
//        contentView.setTextViewText(R.id.tv_nf_music_name,"world");
//        contentView.setInt();
//        contentView.setTextViewText(R.id.tv_nf_music_name,musics.get(position).title);
//        contentView.setTextViewText(R.id.tv_nf_music_author,musics.get(position).artist);
//        if (mService.isPlay){
//            contentView.setImageViewResource(R.id.ib_nf_play,R.drawable.playbar_btn_pause);
//        }else {
//            contentView.setImageViewResource(R.id.ib_nf_play,R.drawable.playbar_btn_play);
//        }
//    }
}
