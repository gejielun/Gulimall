package com.gezq.coupon.service.impl;

import com.gezq.common.to.MemberPrice;
import com.gezq.common.to.SkuReductionTo;
import com.gezq.coupon.entity.MemberPriceEntity;
import com.gezq.coupon.entity.SkuLadderEntity;
import com.gezq.coupon.service.MemberPriceService;
import com.gezq.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gezq.common.utils.PageUtils;
import com.gezq.common.utils.Query;

import com.gezq.coupon.dao.SkuFullReductionDao;
import com.gezq.coupon.entity.SkuFullReductionEntity;
import com.gezq.coupon.service.SkuFullReductionService;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    SkuLadderService skuLadderService;

    @Autowired
    MemberPriceService memberPriceService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        // 1. 保存满减打折
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuReductionTo.getSkuId());
        skuLadderEntity.setFullCount(skuReductionTo.getFullCount());
        skuLadderEntity.setDiscount(skuReductionTo.getDiscount());
        skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());

        if(skuReductionTo.getFullCount() > 0)
            skuLadderService.save(skuLadderEntity);

        SkuFullReductionEntity reductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo, reductionEntity);

        if(reductionEntity.getFullPrice().compareTo(new BigDecimal("0")) == 0)
            this.save(reductionEntity);


        List<MemberPrice> memberPrice = skuReductionTo.getMemberPrice();
        List<MemberPriceEntity> collect = memberPrice.stream().map(item -> {
            MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
            memberPriceEntity.setSkuId(skuReductionTo.getSkuId());
            memberPriceEntity.setMemberLevelId(item.getId());
            memberPriceEntity.setMemberLevelName(item.getName());
            memberPriceEntity.setMemberPrice(item.getPrice());
            memberPriceEntity.setAddOther(1);
            return memberPriceEntity;
        }).filter(item ->{
            return item.getMemberPrice().compareTo(new BigDecimal("0"))  == 1;
        }).collect(Collectors.toList());

        memberPriceService.saveBatch(collect);
    }

}