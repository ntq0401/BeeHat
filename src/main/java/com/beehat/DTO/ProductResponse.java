package com.beehat.DTO;


import com.beehat.entity.ProductDetail;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {

    String name;
    int sl;
    String image;
    int price;
    int lowestPrice;
    int highestPrice;
    List<ProductDetail> productDetail;
}
