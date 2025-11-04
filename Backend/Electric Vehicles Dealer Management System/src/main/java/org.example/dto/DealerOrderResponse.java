package org.example.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DealerOrderResponse {
    private OrderInfoResponse orderInfo;
    private CustomerInfoResponse customer;
    private DealerInfoResponse dealer;
    private List<OrderDetailResponse> orderDetails;
}
