package com.foodexpiry.demo;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderRepository orderRepository;

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // Place a new order
    @PostMapping
    public Order placeOrder(@RequestBody Order order) {
        order.setStatus("PLACED");
        order.setPaymentMethod(
            order.getPaymentMethod() == null
                ? "COD"
                : order.getPaymentMethod()
        );

        return orderRepository.save(order);
    }

    // Get all orders
    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // Get one order by ID
    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() ->
                new RuntimeException("Order not found: " + id)
            );
    }
@PutMapping("/{id}/status")
public Order updateOrderStatus(
        @PathVariable Long id,
        @RequestParam String status) {

    Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found"));

    order.setStatus(status);

    return orderRepository.save(order);
}
}