package vn.aiteam.hackathon.Views;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.utils.DateFormatter;

import java.io.IOException;

import vn.aiteam.hackathon.Model.Message;
import vn.aiteam.hackathon.R;
import vn.aiteam.hackathon.Utils.FormatUtils;

public class OutcomingVoiceMessageViewHolder
        extends MessageHolders.OutcomingTextMessageViewHolder<Message> {

    private TextView tvDuration;
    private TextView tvTime;
    private ImageButton btnPlay;

    private MediaPlayer mPlayer;
    String mFileName;
    public OutcomingVoiceMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        tvDuration = (TextView) itemView.findViewById(R.id.duration);
        tvTime = (TextView) itemView.findViewById(R.id.time);
        btnPlay = (ImageButton) itemView.findViewById(R.id.btnPlay);
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/AudioRecording.mp3"; //"/AudioRecording.3gp";
    }

    @Override
    public void onBind(Message message) {
        super.onBind(message);
        MediaPlayer mp = MediaPlayer.create(tvDuration.getContext(), Uri.parse(mFileName));
        int duration = mp.getDuration();
        /*tvDuration.setText(
                FormatUtils.getDurationString(
                        message.getVoice().getDuration()));*/

        tvDuration.setText(
                FormatUtils.getDurationString(
                        duration/1000));

        tvTime.setText(DateFormatter.format(message.getCreatedAt(), DateFormatter.Template.TIME));

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnPlay.setImageResource(R.drawable.ic_stop_black_24dp);
                mPlayer = new MediaPlayer();
                mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mPlayer.release();
                        mPlayer = null;
                        btnPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    }

                });
                try {

                    mPlayer.setDataSource(mFileName);
                    mPlayer.prepare();
                    mPlayer.start();
                } catch (IOException e) {
                    Log.e("BEM", e.getMessage());
                }
            }
        });
    }
}