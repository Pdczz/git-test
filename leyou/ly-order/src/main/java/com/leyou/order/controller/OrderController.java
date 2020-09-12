package com.leyou.order.controller;

import com.leyou.order.dto.OrderDto;
import com.leyou.order.pojo.Order;
import com.leyou.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     * @param orderDto
     * @return
     */
    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody OrderDto orderDto){

        return ResponseEntity.ok(orderService.createOrder(orderDto));
    }

    /**
     * 根据id查询订单
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<Order> queryOrderById(@PathVariable("id")Long id){
        return ResponseEntity.ok(orderService.queryOrderById(id));
    }
    @GetMapping("/url/{id}")
    public ResponseEntity<String> createPayUrl(@PathVariable("id")Long orderId){
        return ResponseEntity.ok(orderService.createPayUrl(orderId));
    }


}
