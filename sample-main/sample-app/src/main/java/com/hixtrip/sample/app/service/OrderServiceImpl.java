package com.hixtrip.sample.app.service;

import com.hixtrip.sample.app.api.OrderService;
import com.hixtrip.sample.app.convertor.CommandPayConvertor;
import com.hixtrip.sample.app.convertor.OrderConvertor;
import com.hixtrip.sample.app.strategy.CommandPayStrategy;
import com.hixtrip.sample.app.strategy.PayResultAbstract;
import com.hixtrip.sample.app.strategy.PaySuccess;
import com.hixtrip.sample.app.utils.ServiceLocatorUtils;
import com.hixtrip.sample.client.order.dto.CommandOderCreateDTO;
import com.hixtrip.sample.client.order.dto.CommandPayDTO;
import com.hixtrip.sample.client.order.dto.OrderReq;
import com.hixtrip.sample.client.order.vo.OrderVO;
import com.hixtrip.sample.domain.order.OrderDomainService;
import com.hixtrip.sample.domain.order.model.Order;
import com.hixtrip.sample.domain.pay.PayDomainService;
import com.hixtrip.sample.domain.pay.model.CommandPay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.context.ApplicationContext;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * app层负责处理request请求，调用领域服务
 */
@Component
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderDomainService orderDomainService;

    @Autowired
    PayDomainService payDomainService;

    @Autowired
    ServiceLocatorUtils serviceLocatorUtils;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 创建订单
     * @param param
     * @return
     */
    @Override
    public OrderVO createOrder(CommandOderCreateDTO param) {
        Order order = new Order();
        order.setSkuId(param.getSkuId());
        order.setAmount(param.getAmount());
        order.setUserId(param.getUserId());
        /**
         * 调用领域的创建订单接口
         */

        Order orderResult = orderDomainService.createOrder(order);
        OrderVO result  = OrderConvertor.INSTANCE.orderToOrderVO(orderResult);
        if(orderResult.getId()>0){
            result.setCode("200");
            result.setMsg("创建成功!");
            return result;
        }
        else if(orderResult.getId()<0){
            result.setCode("401");
            result.setMsg("操作有误，请刷新页面");
            return result;
        }
        result.setCode("400");
        result.setMsg("创建失败!");
        return result;
    }


    /**
     * 模拟支付回调
     * @param commandPayDTO
     * @return
     */
    @Override
    public String payCallback(CommandPayDTO commandPayDTO){
        //先记录支付回调
        CommandPay commandPay = CommandPayConvertor.INSTANCE.commandPayDTOToCommandPay(commandPayDTO);
        payDomainService.payRecord(commandPay);

        /**
         * 模拟拿到支付回调的报文后，解析后，得到了订单id 和支付结果
         */
        String payResult = "success";

        /**
         * 此处的理念为给每种处理方式创建一个实现类
         * 通过某个key关联起来， 实现类统一继承一个接口。通过拿到接口的所有实现类的方式和专有的Key去匹配
         * 匹配到后只需要调用这个实现类的处理方法就行了。
         * 后续如果新增处理方式 只需要继续新增实现类和处理方法，和约定实现类的key即可。
         *
         *  此方式更适合 如选择支付方式：wx二维码扫描,wx人脸，wxAPP支付，wx网页支付。支付宝二维码 xxxxxxxx....
         *  等多种，只需要约定好支付方式的Key，创建支付方式的实现类和方式即可。
         *
         *  或者营销活动 (加个建造模式):打包一口价，限时折扣，满减。。。。。。。。
         *
         */

        /**
         * 有的失败情况是 比如用户付款码被扫，但是需要输入密码，用户迟迟为输入
         * 这个时候也是支付失败，但是情况特殊
         * 需要自定去轮询查询订单状态（比如微信的就去微信的orderFind接口）
         * 一定时间内 （一般1分钟内）未支付成功的话，需要去掉支付平台的取消订单接口，然后把
         * 自己的订单走支付失败的方式
         * 如果支付完成则走成功。
         */
        PayResultAbstract payResultAbstract = appraisalStrategy(payResult);
        if(payResultAbstract!=null){
            if(payResultAbstract.payCallBack(commandPay)){
                return "返回结果报文";
            }
        }
        return "fail";
    }


    /**
     * 通过名称去匹配应该调用哪个处理类
     * @param param
     * @return
     */
    private PayResultAbstract appraisalStrategy(String param) {
        Map<String, PayResultAbstract> payResultBean = serviceLocatorUtils.getMap();
        for (PayResultAbstract payResult : payResultBean.values()) {
            if (param.equals(payResult.getPayResultName())) {
                return payResult;
            }
        }
        return null;
    }




    /**
     * 定义一个内部类，用于封装请求
     */
    class Request{
        String serialNo; //内部生成的序列号
        String orderId;  // APP送入的订单id

        CompletableFuture<Map<String,Object>> future;
    }

    // 队列
    LinkedBlockingDeque<Request> queue = new LinkedBlockingDeque<>();

    /**
     * 批量查询订单，批量方式处理
     * @param orderReq
     * @return
     */
    public Map<String,Object> queryOrderBatch(OrderReq orderReq) throws ExecutionException, InterruptedException {
        String serialNo = UUID.randomUUID().toString();// uuid随机唯一
        // CompletableFuture: 监听结果(线程)
        CompletableFuture<Map<String,Object>> future = new CompletableFuture<>();

        Request request = new Request();
        request.serialNo = serialNo;
        request.future = future;
        request.orderId = orderReq.getOrderId();
        queue.add(request); //把数据放入队列中
        return future.get(); //拿结果
    }


    /**
     * 定时任务
     *
     * 定时每隔 XX 时间把请求包装起来，一次性统一访问接口
     */
//    @PostConstruct
//    public void  doBusiness(){
//
//        //定时的线程池，每隔 10ms 运行一次
//        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(1);
//        threadPool.scheduleAtFixedRate(new Runnable() {
//            @Override
//            public void run() {
//                int size = queue.size(); //获取请求的数量 假设1000
//                if(size==0){
//                    return;
//                }
//
//                //根据接口来封装批量参数
//                List<Map<String,Object>> params = new ArrayList<>(); //批量调用的时候，请求参数
//                List<Request> requests = new ArrayList<>();
//                for (int i = 0; i < size; i++) {  //把队列中的1000个元素拿到
//                    Request request = queue.poll();
//                    Map<String,Object> map = new HashMap<>();
//                    map.put("serialNo",request.serialNo);
//                    map.put("orderId",request.orderId);
//                    params.add(map);
//                    requests.add(request);
//                }
//                System.out.println("批量处理的数据量-----       :"+size); //模拟log4j
//                //调用批量接口
//                List<Map<String,Object>> responses = r ;
//
//                for (Request request:responses){
//                    String serialNo = request.serialNo;
//                    for (Map<String Object>)
//                }
//
//            }
//        });
//
//    }
}
