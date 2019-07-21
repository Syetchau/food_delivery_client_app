package com.example.liew.idelivery.ViewHolder;

import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.liew.idelivery.R;

import org.w3c.dom.Text;

public class ShowCommentViewHolder extends RecyclerView.ViewHolder {

    public TextView txtUserPhone, txtComment, txtFoodName;
    public RatingBar ratingBar;
    public ImageView commentImage;

    public ShowCommentViewHolder(View itemView) {
        super(itemView);
        txtComment = (TextView)itemView.findViewById(R.id.comment);
        txtFoodName = (TextView)itemView.findViewById(R.id.comment_item_name);
        txtUserPhone = (TextView)itemView.findViewById(R.id.comment_user_phone);
        ratingBar = (RatingBar)itemView.findViewById(R.id.ratingBar);
        commentImage = (ImageView)itemView.findViewById(R.id.comment_image);
    }
}
