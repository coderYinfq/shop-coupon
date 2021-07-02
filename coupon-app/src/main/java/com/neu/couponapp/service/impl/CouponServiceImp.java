package com.neu.couponapp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.neu.couponapp.constant.Constant;
import com.neu.couponapp.domain.Coupon;
import com.neu.couponapp.domain.CouponExample;
import com.neu.couponapp.domain.UserCoupon;
import com.neu.couponapp.domain.UserCouponExample;
import com.neu.couponapp.mapper.CouponMapper;
import com.neu.couponapp.mapper.UserCouponMapper;
import com.neu.couponapp.util.SnowflakeIdWorker;
import com.neu.couponserviceapi.DTO.CouponDTO;
import com.neu.couponserviceapi.DTO.CouponNoticeDTO;
import com.neu.couponserviceapi.DTO.UserCouponDTO;
import com.neu.couponserviceapi.DTO.UserCouponInfoDTO;
import com.neu.couponserviceapi.service.CouponService;
import com.neu.userserviceapi.DTO.UserDTO;
import com.neu.userserviceapi.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CouponServiceImp implements CouponService {
    @Resource
    CouponMapper couponMapper;

    @Resource
    UserCouponMapper UserCouponMapper;

    /*从他暴露的接口中中获取service对象，获取远方注册中心提供的服务*/
    @Reference
    UserService userservice;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private AmqpTemplate template;

    private static final String COUPON = "couponSet";
    private static final int COUPON_NUM = 10;

    /**
     * 设置缓存，类似于map的方式存储缓存，key是Integer类型的，value是对应的coupon列表
     * 缓存时间为 10 分钟，每隔 5 分钟进行异步刷缓存，去数据库拿数据，通过执行重写的 load() 方法
     * */
    LoadingCache<Integer,List<Coupon>> couponCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES).refreshAfterWrite(5,TimeUnit.MINUTES)
            .build(new CacheLoader<Integer, List<Coupon>>() {
                @Override
                public List<Coupon> load(Integer o) throws Exception {
                    //将返回结果，放入couponCache.get(o)
                    return loadCoupon(o);
                }
            });


    /**
     * 这个是用caffeine代替loadingCache 缓存，其余的都没变，将缓存的List<Coupon>放在couponCaffeine中
     * 该缓存和loadingCache的主要区别是缓存清理算法，在数据量较大的时候，couponCaffeine效率会高一些
     * */
    com.github.benmanes.caffeine.cache.LoadingCache<Integer,List<Coupon>> couponCaffeine = Caffeine.newBuilder()
            .expireAfterWrite(10,TimeUnit.MINUTES).refreshAfterWrite(5,TimeUnit.MINUTES)
            .build(new com.github.benmanes.caffeine.cache.CacheLoader<Integer, List<Coupon>>() {
                @Override
                public List<Coupon> load(Integer o) throws Exception {
                    return loadCoupon(o);
                }
            });

    //这个缓存是将coupon的id作为key，coupon数据作为value
    LoadingCache<Integer,Coupon> couponIdsCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10,TimeUnit.MINUTES).refreshAfterWrite(5,TimeUnit.MINUTES)
            .build(new CacheLoader<Integer,Coupon>() {
                @Override
                public Coupon load(Integer id) throws Exception {
                    return loadIdCoupon(id);
                }
            });
    /*这就相当于把 id 作为key，返回值 coupon 作为value存入缓存*/
    private Coupon loadIdCoupon(Integer id) {
        return couponMapper.selectByPrimaryKey(id);
    }


    /*这就相当于把 o 作为key，返回值List<Coupon>作为value存入缓存*/
    private List<Coupon> loadCoupon(Integer o) {
        CouponExample example = new CouponExample();
        example.createCriteria().andStatusEqualTo(Constant.USERFUL)
                .andStartTimeLessThan(new Date()).andEndTimeGreaterThan(new Date());
        return couponMapper.selectByExample(example);
    }

    /***
     * 获取有效时间的可用优惠券列表
     * @return coupon list
     */
    @Override
    public List<CouponDTO> getCouponList() {
        List<Coupon> Coupons = Lists.newArrayList();
        List<CouponDTO> dtos = Lists.newArrayList();
        try {
            //直接从缓存中去取，这个key值随便写，会去缓存中找，如果有就返回，没有的话就去数据库中找然后放入该位置
            Coupons =  couponCache.get(1);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        //将coupon列表转成couponDTO列表
        Coupons.forEach(Coupon -> {
            CouponDTO dto = new CouponDTO();
            BeanUtils.copyProperties(Coupon,dto);
            dtos.add(dto);
        });

        return dtos;
    }
    /**
     * 从couponCaffeine缓存中取数据
     */
    public List<Coupon> getCouponListFromCaffeine() {
        List<Coupon> Coupons = Lists.newArrayList();
        try {
            //直接从缓存中去取，这个key值随便写，会去缓存中找，如果有就返回，没有的话就去数据库中找然后放入该位置
            Coupons =  couponCaffeine.get(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Coupons;
    }



    /**
     * 如果想要获取一系列的id值对应的coupon,可以先查缓存，将缓存中没有的记录下来getIfPresent()
     * 然后打包一起去数据库中查，这样只用查一次。
     * 如果是分开查，用get()方法的话，每次遇到不在缓存中的都要去数据库中查一次，效率比较低
     * @param ids 逗号隔开的多个 id 号
     * @return  对应的 coupon list
     */
    public List<Coupon> getCouponListByIds(String ids){
        String[] idStr = ids.split(",");
        List<String> idList = Lists.newArrayList(idStr);

        List<Coupon> Coupons = Lists.newArrayList();
        //用于存放缓存中没有的id
        List<Integer> loadFromDB = Lists.newArrayList();

        for (String id:idList) {
            //如果缓存中有就直接返回该对象，没有就返回null
            Coupon tCoupon =  couponIdsCache.getIfPresent(id);
            if (tCoupon==null){
                //没有的话记录在loadFromDB集合中
                loadFromDB.add(Integer.parseInt(id));
            }else {
                Coupons.add(tCoupon);
            }
        }
        //打包一起去数据库中查一次
        List<Coupon> CouponsFromDb = couponByIds(loadFromDB);
        //将结果转成map
        Map<Integer,Coupon> CouponMap = CouponsFromDb.stream().collect(Collectors.toMap(Coupon::getId, TCoupon->TCoupon));
        //将返回结果会写到缓存里面
        couponIdsCache.putAll(CouponMap);
        //放入返回集
        Coupons.addAll(CouponsFromDb);

        return Coupons;
    }

    /**
     * 这个是作为对比，用get()方法每次遇到不在缓存中的都从数据库中去查
     * @param ids
     * @return
     */
    public List<Coupon> getCouponListByIds02(String ids) {
        String[] idStr = ids.split(",");
        List<String> idList = Lists.newArrayList(idStr);

        List<Coupon> Coupons = Lists.newArrayList();

        for (String id:idList) {
            //如果缓存中有就直接返回该对象，没有就返回null
            Coupon Coupon = null;
            try {
                Coupon = couponIdsCache.get(Integer.valueOf(id));
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            Coupons.add(Coupon);
        }
        return Coupons;
    }


    private List<Coupon> couponByIds(List<Integer> loadFromDB) {
        CouponExample example = new CouponExample();
        example.createCriteria().andIdIn(loadFromDB);
        return couponMapper.selectByExample(example);
    }


    @Override
    public CouponDTO selectCouponByPrimaryKey(Integer id) {
        Coupon coupon = couponMapper.selectByPrimaryKey(id);
        CouponDTO couponDTO = new CouponDTO();

        //将 user 转成 userVO
        BeanUtils.copyProperties(coupon, couponDTO);
        return couponDTO;
    }

    public UserDTO getUserById(Integer id) {
        return userservice.getUserById(id);
    }


    /**
     * 手动用 ConcurrentHashMap 写一个缓存，存的内容是 coupon list
     */
    private Map<Integer,List<Coupon>> couponMap = new ConcurrentHashMap();

    public void updateCouponMap(){
        Map couponMap1 = new ConcurrentHashMap();
        List<Coupon> Coupons= this.loadCoupon(1);
        couponMap1.put(1,Coupons);
        couponMap = couponMap1;
    }

    public List<Coupon> getCouponListByCouponMap() {
        List<Coupon> Coupons = new ArrayList<>();
        //直接从couponMap中去取
        Coupons =  (List<Coupon>)couponMap.get(1);
        return Coupons;
    }



    /*用户领券功能 */
    @Override
    public String saveUserCoupon(UserCouponDTO dto) {
        String check = check(dto);
        if(check!=null){
            return check;
        }
        //根据传进来的couponId查询其对应coupon
        Coupon coupon = couponMapper.selectByPrimaryKey(dto.getCouponId());
        if(coupon==null){
            return "couponId:"+dto.getCouponId()+"无效";
        }
        //入库
        return save2DB(dto,coupon);
    }


    /*检查是否合法*/
    public String check(UserCouponDTO dto){
        Integer couponId = dto.getCouponId();
        Integer userId = dto.getUserId();
        if(couponId==null || userId==null){
            return "couponID 或 userID不能为null";
        }
        return null;
    }

    /*入库*/
    private String save2DB(UserCouponDTO dto,Coupon coupon){
        UserCoupon userCoupon = new UserCoupon();
        BeanUtils.copyProperties(dto,userCoupon);
        userCoupon.setPicUrl(coupon.getPicUrl());
        userCoupon.setCreateTime(new Date());
        SnowflakeIdWorker worker = new SnowflakeIdWorker(0,0);
        userCoupon.setUserCouponCode(worker.nextId()+"");
        UserCouponMapper.insertSelective(userCoupon);
        return "领取成功";
    }

    /**
     *获取用户可用的优惠券列表
     * */
    @Override
    public List<UserCouponInfoDTO> userCouponList(Integer userId) {
        List<UserCouponInfoDTO> dtos = Lists.newArrayList();
        if(userId==null){
            return dtos;
        }
        List<UserCoupon> userCoupon = getUserCoupon(userId);
        if(userCoupon.isEmpty()){
            return dtos;
        }
        //将对应的coupon和其id存在一个map里
        Map<Integer,Coupon> idCouponMap = getCouponMap(userCoupon);
        //封装coupon，需要用到查询出来的idCouponMap以及userCoupon中的信息
        return wrapCoupon(userCoupon,idCouponMap);
    }


    private List<UserCoupon> getUserCoupon(Integer userId){
        //查出用户未使用的券
        UserCouponExample example = new UserCouponExample();
        example.createCriteria().andUserIdEqualTo(userId).andStatusEqualTo(0);
        List<UserCoupon> userCoupon = UserCouponMapper.selectByExample(example);
        return userCoupon;
    }

    //提取出coupon对应的id及优惠券map
    private Map<Integer,Coupon> getCouponMap(List<UserCoupon> userCoupon){
        //拿到userCoupon的id的set集合
        Set<Integer> couponIds = getCouponIds(userCoupon);
        //将couponIds转成一个字符串，作为参数输入至getCouponListByIds，查询对应的coupon列表
        List<Coupon> coupons = getCouponListByIds(StringUtils.join(couponIds,","));
        Map<Integer,Coupon> idCouponMap = couponList2Map(coupons);
        return idCouponMap;
    }

    /**
     *获取couponIds
     */
    private Set<Integer> getCouponIds(List<UserCoupon> userCoupons){
        Set<Integer> couponIds= userCoupons.stream().map(userCoupon -> userCoupon.getCouponId()).collect(Collectors.toSet());
        return couponIds;
    }

    private Map<Integer,Coupon> couponList2Map(List<Coupon> coupons){
        return coupons.stream().collect(Collectors.toMap(o -> o.getId(),o -> o));
    }


    //程序员就要拥抱变化，这里用的是jdk8的写法，流操作
    private List<UserCouponInfoDTO> wrapCoupon(List<UserCoupon> userCoupons,Map<Integer,Coupon> idCouponMap){
        List<UserCouponInfoDTO> dtos = userCoupons.stream().map(userCoupon -> {
            UserCouponInfoDTO dto = new UserCouponInfoDTO();
            int couponId = userCoupon.getCouponId();
            Coupon coupon = idCouponMap.get(couponId);
            BeanUtils.copyProperties(userCoupon,dto);
            dto.setAchieveAmount(coupon.getAchieveAmount());
            dto.setReduceAmount(coupon.getReduceAmount());
            return dto;
        }).collect(Collectors.toList());
        return dtos;
    }


    /**接收coupon优惠券核销的消息时候被调用,维护数据 */
    /*修改优惠券状态核销*/
    public void updateCouponStatue(int orderId,String couponCode){
        //首先查
        UserCouponExample example = new UserCouponExample();
        example.createCriteria().andOrderIdEqualTo(orderId).andUserCouponCodeEqualTo(couponCode);
        List<UserCoupon> userCoupons = UserCouponMapper.selectByExample(example);
        //根据查的结果更新 coupon 状态
        UserCoupon userCoupon = userCoupons.get(0);
        userCoupon.setStatus(1);
        UserCouponMapper.updateByPrimaryKeySelective(userCoupon);
    }

    /*公告栏展示，传过来的是一个userCouponStr代表userId_couponId */
    public void updateCoupon(String userCouponStr){
        //先将 userCouponStr 存入redis
        redisTemplate.opsForZSet().add(COUPON,userCouponStr,System.currentTimeMillis());
        //获取一下当前 set 中的数据
        Set<String> couponSet = redisTemplate.opsForZSet().range(COUPON,0,-1);
        //看有没有超
        if(couponSet.size()>COUPON_NUM){
            //以流的方式获取 set 第一个数据
            String remUserCouponStr = couponSet.stream().findFirst().get();
            //删除
            redisTemplate.opsForZSet().remove(COUPON,remUserCouponStr);
        }
    }

    /** 从redis中获取公告栏需要展示的数据，是List<String>类型的 userId_couponId */
    public List<String> queryCouponList(){
        //拿到所有数据
        Set<String> couponSet = redisTemplate.opsForZSet().reverseRange(COUPON,0,-1);
        return couponSet.stream().collect(Collectors.toList());
    }

    /**
     *  这里将从redis中拿到的String类型的userId_couponId转为对象
     */
    @Override
    public List<CouponNoticeDTO> queryCouponNotice(){
        List<String> userCouponStrs = queryCouponList();
        //拆分 userCouponStrs 用一个map存起来，key是couponId，value是userId
        Map<String,String> couponUserMap =userCouponStrs.stream()
                                            .collect(Collectors
                                            .toMap(o -> o.split("_")[1],o -> o.split("_")[0]));
        Set<String> couponSet = couponUserMap.keySet();
        //将 couponId 组装成一个长字符串
        String couponIdStrs = StringUtils.join(couponSet,",");
        //从本地缓存中获取coupon
        List<Coupon> couponList =  getCouponListByIds(couponIdStrs);

        List<CouponNoticeDTO> dtos = couponList.stream().map(Coupon -> {
            CouponNoticeDTO dto = new CouponNoticeDTO();
            BeanUtils.copyProperties(Coupon,dto);
            dto.setUserId(Integer.parseInt(couponUserMap.get(dto.getId()+"")));
            return dto;
        }).collect(Collectors.toList());
        return dtos;
    }



    @RabbitListener(queues = {"bootDirectQueue"})
    public void receiveMassage(String message) {
        JSONObject user = JSONObject.parseObject(message);
        System.out.println("监听器得到的消息---"+user);
    }

}