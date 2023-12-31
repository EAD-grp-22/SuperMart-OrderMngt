package com.supermart.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UseMultipleResponse {
    List<InventoryResponse> inventoryResponseList;
    Double totalPrice;
}