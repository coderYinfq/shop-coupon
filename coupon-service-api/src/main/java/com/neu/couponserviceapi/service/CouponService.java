package com.neu.couponserviceapi.service;

import com.neu.couponserviceapi.DTO.CouponDTO;
import com.neu.couponserviceapi.DTO.CouponNoticeDTO;
import com.neu.couponserviceapi.DTO.UserCouponDTO;
import com.neu.couponserviceapi.DTO.UserCouponInfoDTO;

import java.util.List;

public interface CouponService {

    //获取可用优惠券
    public List<CouponDTO> getCouponList();

    //根据id获取coupon
    public CouponDTO selectCouponByPrimaryKey(Integer id);

    //用户领券功能
    public String saveUserCoupon(UserCouponDTO dto);

    //用户查询自己可用的优惠券
    public List<UserCouponInfoDTO> userCouponList(Integer userId);

    //查询公告栏的数据
    public List<CouponNoticeDTO> queryCouponNotice();

}
