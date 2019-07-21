package com.example.liew.idelivery.Model;

public class ShippingInformation {

    private String orderId,shipperPhone;
    private Double lat,lng;

    public ShippingInformation(){

    }

    public ShippingInformation(String orderId, String shipperPhone, Double lat, Double lng) {
        this.orderId = orderId;
        this.shipperPhone = shipperPhone;
        this.lat = lat;
        this.lng = lng;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getShipperPhone() {
        return shipperPhone;
    }

    public void setShipperPhone(String shipperPhone) {
        this.shipperPhone = shipperPhone;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}

