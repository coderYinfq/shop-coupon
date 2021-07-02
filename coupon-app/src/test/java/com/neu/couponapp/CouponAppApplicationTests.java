package com.neu.couponapp;

import com.alibaba.fastjson.JSON;
import com.neu.couponapp.domain.Coupon;
import com.neu.couponapp.mapper.CouponMapper;
import com.neu.couponapp.service.impl.CouponServiceImp;
import com.neu.couponserviceapi.DTO.CouponNoticeDTO;
import com.neu.couponserviceapi.DTO.UserCouponDTO;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CouponAppApplication.class)
class CouponAppApplicationTests {

    @Resource
    private CouponMapper couponMapper;

    @Resource
    private CouponServiceImp couponService;

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void insertTest(){
        for(int i=0;i<2;i++) {
            Coupon coupon = new Coupon();
            coupon.setAchieveAmount(500);
            coupon.setReduceAmount(20);
            coupon.setCreateTime(new Date());
            coupon.setCode(UUID.randomUUID().toString());
            coupon.setPicUrl("1.jpg");
            coupon.setStatus(0);
            coupon.setStock(100);
            coupon.setTitle("测试coupon");
            couponMapper.insert(coupon);
        }
    }

    @Test
    public void testSaveUserCoupon(){
        UserCouponDTO dto = new UserCouponDTO();
        dto.setUserId(1);  //用户 1
        dto.setCouponId(1);  //领取一个优惠券 1
        dto.setOrderId(10086);  //订单编号是 10086
        System.err.println(couponService.saveUserCoupon(dto));
    }

    @Test
    public void testUserCouponList(){
        System.out.println(JSON.toJSONString(couponService.userCouponList(1)));
    }

    @Test
    public void testRedis(){
        redisTemplate.opsForValue().set("name1","秦");
        System.out.println(redisTemplate.opsForValue().get("name1"));
    }

    @Test
    public void testSortSet(){
        redisTemplate.opsForZSet().add("mySet","one",1);
        redisTemplate.opsForZSet().add("mySet","two",2);
        redisTemplate.opsForZSet().add("mySet","three",3);
        redisTemplate.opsForZSet().add("mySet","four",4);
        //redisTemplate.opsForZSet().remove("mySet","four");
        System.out.println(JSON.toJSONString(redisTemplate.opsForZSet().range("mySet",0,-1)));
    }

    @Test
    public void testUpdateCoupon(){
        for(int i=1;i<3;i++){
            couponService.updateCoupon(i+"_"+i);
            System.out.println("成功"+i);
        }
    }

    @Test
    public void getCoupon(){
        List<String> list = couponService.queryCouponList();
        for(String s:list){
            System.out.println(s);
        }
    }

    @Test
    public void getQueryCouponNotice(){
        List<CouponNoticeDTO> list = couponService.queryCouponNotice();
        for(CouponNoticeDTO s:list){
            System.out.println(s.getCode());
        }
    }


}
