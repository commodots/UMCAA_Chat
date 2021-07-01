package com.commodots.umcaaconnecta.viewHolders;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.commodots.umcaaconnecta.R;
import com.commodots.umcaaconnecta.interfaces.OnMessageItemClick;
import com.commodots.umcaaconnecta.models.Message;
import com.commodots.umcaaconnecta.utils.Helper;

import org.json.JSONException;
import org.json.JSONObject;

public class MessageAttachmentLocationViewHolder extends BaseMessageViewHolder {
    private TextView text;
    private ImageView locationImage;
    private LinearLayout ll;

    private String staticMap = "https://maps.googleapis.com/maps/api/staticmap?center=%s,%s&zoom=18&size=512x350&format=jpg";
    private String latitude, longitude, place, placeName;

    public MessageAttachmentLocationViewHolder(View itemView, OnMessageItemClick itemClickListener) {
        super(itemView, itemClickListener);
        text = itemView.findViewById(R.id.text);
        locationImage = itemView.findViewById(R.id.locationImage);
        ll = itemView.findViewById(R.id.container);
        itemView.setOnClickListener(v -> onItemClick(true));

        itemView.setOnLongClickListener(v -> {
            onItemClick(false);
            return true;
        });

        locationImage.setOnClickListener(v -> {

            if (!Helper.CHAT_CAB && !TextUtils.isEmpty(latitude) && !TextUtils.isEmpty(longitude)) {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + latitude + "," + longitude+"("+ placeName + " , " + place +")");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(mapIntent);
                }
            }
        });

        // under onlick upper
//                Builder builder = new Uri.Builder();
//                builder.scheme("https")
//                        .authority("www.google.com")
//                        .appendPath("maps")
//                        .appendPath("dir")
//                        .appendPath("")
//                        .appendQueryParameter("api", "1")
//                        .appendQueryParameter("destination", 80.00023 + "," + 13.0783);
//                String url = builder.build().toString();
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setData(Uri.parse(url));
//                context.startActivity(i);
    }

    @Override
    public void setData(Message message, int position) {
        super.setData(message, position);
        try {
            JSONObject placeData = new JSONObject(message.getAttachment().getData());
            place = placeData.getString("address");
            placeName = placeData.getString("addressName");
            latitude = placeData.getString("latitude");
            longitude = placeData.getString("longitude");

            text.setText(place);

            String link = String.format(staticMap + "&key=" + context.getResources().getString(R.string.geo_api_key) + "&markers=color:red|label:Y|%s,%s&markers=color:red", latitude, longitude, latitude, longitude);
            Glide.with(context).load(String.format(staticMap + "&key=" + context.getResources().getString(R.string.geo_api_key) + "&markers=color:red|label:Y|%s,%s&markers=color:red", latitude, longitude, latitude, longitude)).into(locationImage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        cardView.setCardBackgroundColor(ContextCompat.getColor(context, message.isSelected() ? R.color.colorPrimary : R.color.colorBgLight));
        ll.setBackgroundColor(message.isSelected() ? ContextCompat.getColor(context, R.color.colorPrimary) : isMine() ? Color.WHITE : ContextCompat.getColor(context, R.color.colorBgLight));
    }
}
