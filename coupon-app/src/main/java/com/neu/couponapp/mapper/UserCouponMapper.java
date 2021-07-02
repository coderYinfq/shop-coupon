package com.neu.couponapp.mapper;

import com.neu.couponapp.domain.UserCoupon;
import com.neu.couponapp.domain.UserCouponExample;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.type.JdbcType;

public interface UserCouponMapper {
    @SelectProvider(type=UserCouponSqlProvider.class, method="countByExample")
    int countByExample(UserCouponExample example);

    @DeleteProvider(type=UserCouponSqlProvider.class, method="deleteByExample")
    int deleteByExample(UserCouponExample example);

    @Delete({
        "delete from t_user_coupon",
        "where id = #{id,jdbcType=INTEGER}"
    })
    int deleteByPrimaryKey(Integer id);

    @Insert({
        "insert into t_user_coupon (user_coupon_code, pic_url, ",
        "coupon_id, user_id, ",
        "status, order_id, ",
        "create_time)",
        "values (#{userCouponCode,jdbcType=VARCHAR}, #{picUrl,jdbcType=VARCHAR}, ",
        "#{couponId,jdbcType=INTEGER}, #{userId,jdbcType=INTEGER}, ",
        "#{status,jdbcType=INTEGER}, #{orderId,jdbcType=INTEGER}, ",
        "#{createTime,jdbcType=TIMESTAMP})"
    })
    @Options(useGeneratedKeys=true,keyProperty="id")
    int insert(UserCoupon record);

    @InsertProvider(type=UserCouponSqlProvider.class, method="insertSelective")
    @Options(useGeneratedKeys=true,keyProperty="id")
    int insertSelective(UserCoupon record);

    @SelectProvider(type=UserCouponSqlProvider.class, method="selectByExample")
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="user_coupon_code", property="userCouponCode", jdbcType=JdbcType.VARCHAR),
        @Result(column="pic_url", property="picUrl", jdbcType=JdbcType.VARCHAR),
        @Result(column="coupon_id", property="couponId", jdbcType=JdbcType.INTEGER),
        @Result(column="user_id", property="userId", jdbcType=JdbcType.INTEGER),
        @Result(column="status", property="status", jdbcType=JdbcType.INTEGER),
        @Result(column="order_id", property="orderId", jdbcType=JdbcType.INTEGER),
        @Result(column="create_time", property="createTime", jdbcType=JdbcType.TIMESTAMP)
    })
    List<UserCoupon> selectByExample(UserCouponExample example);

    @Select({
        "select",
        "id, user_coupon_code, pic_url, coupon_id, user_id, status, order_id, create_time",
        "from t_user_coupon",
        "where id = #{id,jdbcType=INTEGER}"
    })
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="user_coupon_code", property="userCouponCode", jdbcType=JdbcType.VARCHAR),
        @Result(column="pic_url", property="picUrl", jdbcType=JdbcType.VARCHAR),
        @Result(column="coupon_id", property="couponId", jdbcType=JdbcType.INTEGER),
        @Result(column="user_id", property="userId", jdbcType=JdbcType.INTEGER),
        @Result(column="status", property="status", jdbcType=JdbcType.INTEGER),
        @Result(column="order_id", property="orderId", jdbcType=JdbcType.INTEGER),
        @Result(column="create_time", property="createTime", jdbcType=JdbcType.TIMESTAMP)
    })
    UserCoupon selectByPrimaryKey(Integer id);

    @UpdateProvider(type=UserCouponSqlProvider.class, method="updateByExampleSelective")
    int updateByExampleSelective(@Param("record") UserCoupon record, @Param("example") UserCouponExample example);

    @UpdateProvider(type=UserCouponSqlProvider.class, method="updateByExample")
    int updateByExample(@Param("record") UserCoupon record, @Param("example") UserCouponExample example);

    @UpdateProvider(type=UserCouponSqlProvider.class, method="updateByPrimaryKeySelective")
    int updateByPrimaryKeySelective(UserCoupon record);

    @Update({
        "update t_user_coupon",
        "set user_coupon_code = #{userCouponCode,jdbcType=VARCHAR},",
          "pic_url = #{picUrl,jdbcType=VARCHAR},",
          "coupon_id = #{couponId,jdbcType=INTEGER},",
          "user_id = #{userId,jdbcType=INTEGER},",
          "status = #{status,jdbcType=INTEGER},",
          "order_id = #{orderId,jdbcType=INTEGER},",
          "create_time = #{createTime,jdbcType=TIMESTAMP}",
        "where id = #{id,jdbcType=INTEGER}"
    })
    int updateByPrimaryKey(UserCoupon record);
}