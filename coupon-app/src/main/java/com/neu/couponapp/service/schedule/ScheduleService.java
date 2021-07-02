package com.neu.couponapp.service.schedule;


import com.neu.couponapp.service.impl.CouponServiceImp;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 该类是一个定时任务类,是异步执行的，可以用来定时加载缓存，替代loading cache
 */
@Service
public class ScheduleService {

    @Resource
    private CouponServiceImp couponService;

    //每个 * 依次代表 秒、分、时、日、月、年
    //@Scheduled(cron = "0/30 * * * * ?")    //表示scheduleTest()方法，每5 秒执行一次
    //@Scheduled(cron = "* 0/1 * * * ?")    //表示scheduleTest()方法，每1 分钟执行一次
    //@Scheduled(cron = "* * 0/1 * * ?")    //表示scheduleTest()方法，每1 小时执行一次
    public void scheduleTest(){
        // System.out.println("执行一次scheduleTest(),加载一次coupon数据放入couponMap");
        couponService.updateCouponMap();
    }
}
