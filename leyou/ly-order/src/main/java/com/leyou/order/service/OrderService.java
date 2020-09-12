package com.leyou.order.service;

import com.leyou.auth.entity.UserInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.pojo.Sku;
import com.leyou.order.client.AddressClient;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.dto.OrderDto;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.inteceptor.UserInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.AddressDTO;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.utils.PayHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;
@Slf4j
@Service
public class OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private OrderStatusMapper orderStatusMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private PayHelper payHelper;

    @Transactional
    public Long createOrder(OrderDto orderDto) {
        // 1 新增订单
        Order order = new Order();
        // 1.1 订单编号，基本信息 -- 订单ID，雪花算法（snowflake）生成全局唯一的ID
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);
        order.setCreateTime(new Date());
        order.setPaymentType(orderDto.getPaymentType());

        // 1.2 用户信息
        UserInfo user = UserInterceptor.getUser();
        order.setUserId(user.getId());
        order.setBuyerNick(user.getUsername());
        order.setBuyerRate(false);

        // 1.3 收货人地址信息 -- orderDTO中只有地址ID（addressID），要根据地址ID去数据库中查询(假数据)
        AddressDTO addr = AddressClient.findById(orderDto.getAddressId());
        order.setReceiver(addr.getName());//收货人
        order.setReceiverMobile(addr.getPhone());//收货人手机号码
        order.setReceiverAddress(addr.getAddress());//收货所在街道
        order.setReceiverState(addr.getState());//收货人所在省
        order.setReceiverCity(addr.getCity());//收货人所在城市
        order.setReceiverDistrict(addr.getDistrict());//收货人所在区
        order.setReceiverZip(addr.getZipCode());//收货人邮编

        //1.4金额
        Map<Long, Integer> numMap = orderDto.getCarts().stream()
                .collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));

        Set<Long> ids = numMap.keySet();
        List<Sku> skus = goodsClient.querySkuByIds(new ArrayList<>(ids));
        List<OrderDetail> details = new ArrayList<>();
        Long totalPay = 0L;
        for (Sku sku : skus) {
            totalPay += sku.getPrice() * numMap.get(sku.getId());
            //封装orderDetail
            OrderDetail detail = new OrderDetail();
            detail.setImage(StringUtils.substringBefore(sku.getImages(),","));
            detail.setNum(numMap.get(sku.getId()));
            detail.setOrderId(orderId);
            detail.setOwnSpec(sku.getOwnSpec());
            detail.setPrice(sku.getPrice());
            detail.setSkuId(sku.getId());
            detail.setTitle(sku.getTitle());

            details.add(detail);
        }

        order.setTotalPay(totalPay);
        // 实付金额= 总金额 + 邮费 - 优惠金额
        order.setActualPay(totalPay + order.getPostFee() - 0 );
        // 1.5 写入数据库
        int count = orderMapper.insertSelective(order);
        if(count != 1){
            log.error("[创建订单] 创建订单失败，orderID:{}", orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }
        // 2 新增订单详情
        count = orderDetailMapper.insertList(details);
        if(count != details.size()){
            log.error("[创建订单] 创建订单失败，orderID:{}", orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }
        //3 新增订单状态
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setCreateTime(order.getCreateTime());
        orderStatus.setStatus(OrderStatusEnum.UN_PAY.value());
        count = orderStatusMapper.insertSelective(orderStatus);
        if(count != 1){
            log.error("[创建订单] 创建订单失败，orderID:{}", orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }
        //减库存
        List<CartDTO> carts = orderDto.getCarts();
        goodsClient.decreaseStock(carts);
        return orderId;
    }

    public Order queryOrderById(Long id) {
        //查询订单
        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null){
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        //查询订单详情
        OrderDetail detail=new OrderDetail();
        detail.setOrderId(id);
        List<OrderDetail> details = orderDetailMapper.select(detail);
        if (CollectionUtils.isEmpty(details)){
            throw new LyException(ExceptionEnum.ORDER_DETAIL_NOT_FOUND);
        }
        order.setOrderDetails(details);
        //查询订单状态
        OrderStatus status = orderStatusMapper.selectByPrimaryKey(id);
        if (status==null){
            throw new LyException(ExceptionEnum.ORDER_STATUS_NOT_FOUND);
        }
        order.setOrderStatus(status);
        return order;
    }

    public String createPayUrl(Long orderId) {
        //查询订单
        Order order = queryOrderById(orderId);
        //订单状态
        Integer status = order.getOrderStatus().getStatus();
        if (status !=OrderStatusEnum.UN_PAY.value()){
            throw new LyException(ExceptionEnum.ORDER_DETAIL_NOT_FOUND);
        }
        //支付金额
        Long actualPay = order.getActualPay();
        //订单描述
        OrderDetail detail = order.getOrderDetails().get(0);

        String desc = detail.getTitle();
        return payHelper.createOrder(orderId,actualPay,desc);
    }
}
