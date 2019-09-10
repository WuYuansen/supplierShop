package com.ruoyi.app.modular.shop.service.impl;


//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itmuch.lightsecurity.exception.LightSecurityException;
import com.ruoyi.app.common.exception.BadRequestException;
import com.ruoyi.app.common.persistence.dao.StoreGoodsCollectMapper;
import com.ruoyi.app.common.persistence.dao.StoreGoodsMapper;
import com.ruoyi.app.common.persistence.dao.StoreSpecGoodsPriceMapper;
import com.ruoyi.app.common.persistence.model.StoreGoods;
import com.ruoyi.app.common.persistence.model.StoreGoodsCollect;
import com.ruoyi.app.common.persistence.model.StoreSpecGoodsPrice;
import com.ruoyi.app.modular.shop.service.IGoodsService;
import com.ruoyi.app.modular.shop.service.dto.GoodsDTO;
import com.ruoyi.app.modular.shop.service.dto.ItemDTO;
import com.ruoyi.app.modular.shop.service.dto.SpecItemDTO;
import com.ruoyi.app.modular.shop.service.mapper.GoodsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GoodsServiceImpl extends ServiceImpl<StoreGoodsMapper, StoreGoods>  implements IGoodsService {

    private final GoodsMapper goodsMapper;
    private final StoreGoodsMapper storeGoodsMapper;
    private final StoreGoodsCollectMapper storeGoodsCollectMapper;
    private final StoreSpecGoodsPriceMapper storeSpecGoodsPriceMapper;


    /**
     * 商品列表
     * @param cateId
     * @param page
     * @param limit
     * @param userId
     * @param keywords
     * @param order
     * @return
     */
    @Override
    public List<GoodsDTO> getList(int cateId, int page, int limit, int userId,
                                  String keywords,int order) {

        QueryWrapper<StoreGoods> wrapper = new QueryWrapper<>();

        if(cateId > 0){
            wrapper.eq("cate_id",cateId);
        }
        if(StrUtil.isNotEmpty(keywords)){
            wrapper.like("goods_name",keywords);
        }

        //todo order = 1 推荐  order=2  新品
        switch (order){
            case 1:
                wrapper.eq("is_recommend",1);
                break;
            case 2:
                wrapper.eq("is_new",1);
                break;
        }
        Page<StoreGoods> pageModel = new Page<>(page, limit);

        IPage<StoreGoods> pageList = storeGoodsMapper.selectPage(pageModel,wrapper);

        List<GoodsDTO> list = goodsMapper.toDto(pageList.getRecords());

        for (GoodsDTO goodsDTO : list) {
            goodsDTO.setIsCollect(isCollect(goodsDTO.getGoodsId(),userId));
        }

        return list;
        //return null;
    }

    /**
     * 收藏判断
     * @param goodsId
     * @param userId
     * @return
     */
    @Override
    public boolean isCollect(int goodsId, int userId) {
        QueryWrapper<StoreGoodsCollect> wrapper = new QueryWrapper<>();
        wrapper.eq("goods_id",goodsId).eq("user_id",userId);
        StoreGoodsCollect goodsCollect = storeGoodsCollectMapper.selectOne(wrapper);
        if(ObjectUtil.isNotNull(goodsCollect)) return true;
        return false;
    }


    @Override
    public List<StoreGoods> collectGoods(int page,int limit,int userId) {
        Page<StoreGoods> pageModel = new Page<>(page, limit);
        List<StoreGoods> list = baseMapper.collectGoods(pageModel,userId);
        //System.out.println(list);
        return list;
    }


    @Override
    public Map<String,StoreSpecGoodsPrice> goodsSpecPrice(int goodsId) {
        QueryWrapper<StoreSpecGoodsPrice> wrapper = new QueryWrapper<>();
        wrapper.eq("goods_id",goodsId);
        List<StoreSpecGoodsPrice> specGoodsPriceList = storeSpecGoodsPriceMapper
                .selectList(wrapper);
        Map<String, StoreSpecGoodsPrice> listMap = specGoodsPriceList.stream().collect(
                Collectors.toMap(StoreSpecGoodsPrice::getKey,o->o));

        return listMap;
    }

    public Map<String, SpecItemDTO> goodsSpec(int goodsId){

        String keys = storeSpecGoodsPriceMapper.goodsSpecKey(goodsId);
        String[] strArr = keys.split("_");
        Map<String,SpecItemDTO> itemMap = new HashMap<>();
        List<ItemDTO> items =  storeSpecGoodsPriceMapper.goodsSpec(Arrays.asList(strArr));
        for (ItemDTO itemDTO : items) {
            SpecItemDTO specItemDTO = new SpecItemDTO();
            specItemDTO.setItemId(itemDTO.getId());
            specItemDTO.setItem(itemDTO.getItem());
            itemMap.put(itemDTO.getName(),specItemDTO);
        }
        return itemMap;
    }


    @Override
    public boolean addOrCancelCollect(int goodsId, int userId, int type) {
        if(type == 1){//1-添加 2-取消
            boolean isCollect = isCollect(goodsId,userId);
            if(isCollect)  throw new LightSecurityException("已经收藏过");
            System.out.println("======");
        }
        return false;
    }
}
