package com.example.liew.idelivery.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.liew.idelivery.Interface.ItemClickListener;
import com.example.liew.idelivery.R;

import org.w3c.dom.Text;

public class OrderViewHolder extends RecyclerView.ViewHolder{

    public TextView txtOrderId, txtOrderStatus, txtOrderPhone, txtOrderAddress, txtOrderDate, txtOrderName, txtOrderPrice;
    public Button btnDirection, btnDeleteOrder, btnConfirmShip;

    public OrderViewHolder(View itemView) {
        super(itemView);

        txtOrderId = (TextView)itemView.findViewById(R.id.order_id);
        txtOrderStatus = (TextView)itemView.findViewById(R.id.order_status);
        txtOrderPhone = (TextView)itemView.findViewById(R.id.order_phone);
        txtOrderAddress = (TextView)itemView.findViewById(R.id.order_address);
        txtOrderDate = (TextView)itemView.findViewById(R.id.order_date);
        txtOrderName = (TextView)itemView.findViewById(R.id.order_name);
        txtOrderPrice = (TextView)itemView.findViewById(R.id.order_price);

        btnDeleteOrder = (Button)itemView.findViewById(R.id.btnDeleteOrder);
        btnDirection = (Button)itemView.findViewById(R.id.btnDirection);
        btnConfirmShip = (Button)itemView.findViewById(R.id.btnConfirmShip);
    }

}
