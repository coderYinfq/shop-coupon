package com.neu.couponapp.controller;

import com.neu.couponapp.service.impl.CouponServiceImp;
import com.neu.couponserviceapi.DTO.CouponDTO;
import com.neu.userserviceapi.DTO.UserDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

@Controller
public class TestController {
    @Resource
    private CouponServiceImp couponService;


    @RequestMapping("/coupon")
    public @ResponseBody CouponDTO coupon(Integer id){
        return couponService.selectCouponByPrimaryKey(id);
    }

    @RequestMapping("/user")
    public @ResponseBody UserDTO user(Integer id){
        return couponService.getUserById(id);
    }

    @RequestMapping("/list")
    public @ResponseBody List<CouponDTO> list(){
        return couponService.getCouponList();
    }
}
