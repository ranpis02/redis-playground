package com.review.controller;

import com.review.service.VoucherOrderService;
import com.review.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {
    @Autowired
    private VoucherOrderService voucherOrderService;

    @PostMapping("/grab")
    public R grabSeckillVoucher(@RequestParam Long id) {
        return voucherOrderService.grabSeckillVoucherNaive(id);
    }

    @PostMapping("/grab/one")
    public R grabSeckillVoucherOne(@RequestParam Long id) {
        return voucherOrderService.grabSeckillVoucherAsyncExecute(id);
    }


}
