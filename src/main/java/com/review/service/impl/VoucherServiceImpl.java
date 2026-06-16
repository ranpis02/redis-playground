package com.review.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.review.mapper.VoucherMapper;
import com.review.model.entity.Voucher;
import com.review.service.VoucherService;
import org.springframework.stereotype.Service;

@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements VoucherService {
}
