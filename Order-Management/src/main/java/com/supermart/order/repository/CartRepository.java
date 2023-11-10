package com.supermart.order.repository;

import com.supermart.order.dto.CartResponse;
import com.supermart.order.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart,Integer> {

    Cart findCartById(Integer id);
    Cart findCartByCustomerId(Integer customerId);

    Cart findByCustomerId(Integer customerId);

    @Query("SELECT DISTINCT c.id FROM Cart c " +
            "JOIN c.cartItems ci " +
            "WHERE ci.skuCode = :skuCode")
    List<Integer> findCartIdsBySkuCode(@Param("skuCode") String skuCode);

}
