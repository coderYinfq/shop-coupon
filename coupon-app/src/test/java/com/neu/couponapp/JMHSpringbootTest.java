package com.neu.couponapp;


import com.neu.couponapp.service.impl.CouponServiceImp;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;


@State(Scope.Thread)
public class JMHSpringbootTest {

    private ConfigurableApplicationContext context;
    private CouponServiceImp couponService;

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder().include(JMHSpringbootTest.class.getName() + ".*")
                .warmupIterations(2).measurementIterations(2)
                .forks(1).build();
        new Runner(options).run();
    }

    /**
     * setup初始化容器的时候只执行一次
     */
    @Setup(Level.Trial)
    public void init(){
        String arg = "";
        //获取spring容器
        context = SpringApplication.run(CouponAppApplication.class,arg);
        //通过spring容器获取 couponService 对象
        couponService = context.getBean(CouponServiceImp.class);
    }


    /**
     * benchmark执行多次，此注解代表触发我们所要进行基准测试的方法
     */
    /*@Benchmark
    public void test1(){
        System.out.println(couponService.getCouponList());
    }
*/
    /*@Benchmark
    public void test2(){
        System.out.println(couponService.getCouponListByIds02("1,2,3,4"));
    }*/

    @Benchmark
    public void test3(){
        System.out.println(couponService.getCouponListByCouponMap());
    }



}
