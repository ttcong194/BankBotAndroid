package vn.aiteam.hackathon.Views;

import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.messages.MessageHolders;

import vn.aiteam.hackathon.Model.Message;
import vn.aiteam.hackathon.R;

public class IncomingCameraMessageViewHolder extends MessageHolders.IncomingTextMessageViewHolder<Message> {

    private ImageView imv;

    public IncomingCameraMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        imv = (ImageView) itemView.findViewById(R.id.image);
    }

    @Override
    public void onBind(Message message) {
        super.onBind(message);
        Picasso.with(imv.getContext()).load(message.getFile()).into(imv);
    }
}