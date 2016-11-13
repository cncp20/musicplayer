package com.aaron.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.aaron.bean.MusicInfo;
import com.aaron.musicplayer.R;

import java.util.List;

/**
 * Created by Administrator on 2016/8/19.
 */
public class LocalMusicAdapter extends BaseAdapter{
    private Context context;
    private List<MusicInfo> musics;

    public LocalMusicAdapter(Context c, List<MusicInfo> l){
        context = c;
        musics = l;
    }

    @Override
    public int getCount() {
        return musics.size();
    }

    @Override
    public Object getItem(int i) {
        return musics.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null){
            view = View.inflate(context, R.layout.item_music_list,null);
            holder = new ViewHolder();
            holder.tvNum = (TextView) view.findViewById(R.id.tv_item_number);
            holder.tvMusic = (TextView) view.findViewById(R.id.tv_item_music_name);
            holder.tvMusicAuth = (TextView) view.findViewById(R.id.tv_item_music_author);
            view.setTag(holder);
        }else {
            holder = (ViewHolder) view.getTag();
        }
        holder.tvNum.setText(i +1 + "");
        holder.tvMusic.setText(musics.get(i).title);
        holder.tvMusicAuth.setText(musics.get(i).artist);
//        if (MusicUtil.CURPOSITION == i){
//            holder.tvMusic.setTextColor(context.getResources().getColor(R.color.toolabar_color));
//        }else {
//            holder.tvMusic.setTextColor(Color.BLACK);
//        }
//        holder.tvMusic.setTag(i);
        return view;
    }

    class ViewHolder{
        TextView tvNum;
        TextView tvMusic;
        TextView tvMusicAuth;
    }


}
