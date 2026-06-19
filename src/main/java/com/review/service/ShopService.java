package com.review.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.review.model.entity.Shop;
import com.review.utils.R;

public interface ShopService extends IService<Shop> {
    // R queryShopByType(Long id)
    R queryById(Long id);
}
