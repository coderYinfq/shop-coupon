package com.neu.couponserviceapi.DTO;


import java.io.Serializable;

/*只保留需要用到的字段，DTO类是作为接口输入参数的*/
public class UserCouponDTO implements Serializable {

    private Integer couponId;

    private Integer userId;

    private Integer orderId;

    private String userCouponCode;

    public String getUserCouponCode() {
        return userCouponCode;
    }

    public void setUserCouponCode(String userCouponCode) {
        this.userCouponCode = userCouponCode;
    }

    public Integer getCouponId() {
        return couponId;
    }

    public void setCouponId(Integer couponId) {
        this.couponId = couponId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return "UserCouponDTO{" +
                "couponId=" + couponId +
                ", userId=" + userId +
                ", orderId=" + orderId +
                '}';
    }
}