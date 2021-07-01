package com.commodots.umcaaconnecta.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.commodots.umcaaconnecta.R;
import com.commodots.umcaaconnecta.interfaces.ChatItemClickListener;
import com.commodots.umcaaconnecta.interfaces.ContextualModeInteractor;
import com.commodots.umcaaconnecta.models.Chat;
import com.commodots.umcaaconnecta.utils.GetTimeAgo;
import com.commodots.umcaaconnecta.utils.Helper;

import java.util.ArrayList;

/**
 * Created by a_man on 5/10/2017.
 */

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<Chat> dataList;
    private ChatItemClickListener itemClickListener;
    private ContextualModeInteractor contextualModeInteractor;
    private int selectedCount = 0;

    public ChatAdapter(Context context, ArrayList<Chat> dataList) {
        this.context = context;
        this.dataList = dataList;

        if (context instanceof ChatItemClickListener) {
            this.itemClickListener = (ChatItemClickListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ChatItemClickListener");
        }

        if (context instanceof ContextualModeInteractor) {
            this.contextualModeInteractor = (ContextualModeInteractor) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ContextualModeInteractor");
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_item_user, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setData(dataList.get(position));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView status, name, lastMessage, time;
        private ImageView image;
        private LinearLayout user_details_container;

        MyViewHolder(View itemView) {
            super(itemView);
            status = itemView.findViewById(R.id.emotion);
            name = itemView.findViewById(R.id.user_name);
            time = itemView.findViewById(R.id.time);
            lastMessage = itemView.findViewById(R.id.message);
            image = itemView.findViewById(R.id.user_image);
            user_details_container = itemView.findViewById(R.id.user_details_container);

            itemView.setOnClickListener(v -> {
                if (contextualModeInteractor.isContextualMode()) {
                    toggleSelection(dataList.get(getAdapterPosition()), getAdapterPosition());
                } else {
                    int pos = getAdapterPosition();
                    if (pos != -1) {
                        Chat chat = dataList.get(pos);
                        itemClickListener.onChatItemClick(chat.getChatId(),chat.getChatName(), pos, image);
                    }
                }
            });
            itemView.setOnLongClickListener(view -> {
                contextualModeInteractor.enableContextualMode();
                toggleSelection(dataList.get(getAdapterPosition()), getAdapterPosition());
                return true;
            });
        }

        private void setData(Chat chat) {
            Glide.with(context).load(chat.getChatImage()).apply(new RequestOptions().placeholder(R.drawable.avatar)).into(image);

            name.setText(chat.getChatName());
            name.setCompoundDrawablesWithIntrinsicBounds(0, 0, !chat.isRead() ? R.drawable.ring_blue : 0, 0);

            if (chat.isGroup()) {
                status.setText(chat.getChatStatus());
            } else {
                DatabaseReference def = FirebaseDatabase.getInstance().getReference("users").child(chat.getChatId());
                def.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {

                            boolean online = (boolean) dataSnapshot.child("online").getValue();
                            if (online)
                                status.setText("active");
                            else {

                                try {
                                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                                    long lastTime = (long) dataSnapshot.child("time").getValue();
                                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, context);
                                    status.setText(lastSeenTime);

                                }catch (Exception e) {

                                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                                    long lastTime = chat.getTimeUpdated();
                                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, context);
                                    status.setText(lastSeenTime);
                                }

                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            time.setText(Helper.getDateTime(chat.getTimeUpdated()));
            lastMessage.setText(chat.getLastMessage());
            lastMessage.setTextColor(ContextCompat.getColor(context, !chat.isRead() ? R.color.textColorPrimary : R.color.textColorSecondary));

            user_details_container.setBackgroundColor(ContextCompat.getColor(context, (chat.isSelected() ? R.color.bg_gray : R.color.colorIcon)));
        }
    }

    private void toggleSelection(Chat chat, int position) {
        chat.setSelected(!chat.isSelected());
        notifyItemChanged(position);

        if (chat.isSelected())
            selectedCount++;
        else
            selectedCount--;

        contextualModeInteractor.updateSelectedCount(selectedCount);
    }

    public void disableContextualMode() {
        selectedCount = 0;
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).isSelected()) {
                dataList.get(i).setSelected(false);
                notifyItemChanged(i);
            }
        }
    }

}
