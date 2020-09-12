package com.leyou.page.service;

import com.leyou.item.pojo.*;
import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import com.netflix.discovery.converters.Auto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
@Service
public class PageService {
    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specClient;

    @Autowired
    private TemplateEngine templateEngine;

    public Map<String, Object> loadModel(Long id) {
        Map<String,Object> model=new HashMap<>();
        //查询spu
        Spu spu = goodsClient.querySpuById(id);
        //查询sku
        List<Sku> skus = goodsClient.querySkuBySpuId(id);
        //查询详情
        SpuDetail detail = goodsClient.queryDetailById(id);
        //查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        //查询分类
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        //查询规格参数
        List<SpecGroup> groups = specClient.queryListByCid(spu.getCid3());
        model.put("title",spu.getTitle());
        model.put("subTitle",spu.getSubTitle());
        model.put("skus",skus);
        model.put("spuDetail",detail);
        model.put("brand",brand);
        model.put("categories",categories);
        model.put("groups",groups);
        return model;
    }

    public void createHtml(Long spuId){
        //上下文
        Context context=new Context();
        context.setVariables(loadModel(spuId));

        File dest=new File("C:\\Users\\pdc\\yun6\\upload\\"+spuId+".html");
        if (dest.exists()){
            dest.delete();
        }
        try(PrintWriter write=new PrintWriter(dest,"UTF-8")) {
            templateEngine.process("item",context,write);
        } catch (Exception e) {
            log.error("静态页面异常",e);

        }


    }

    public void deleteHtml(Long spuId) {
        File dest=new File("C:\\Users\\pdc\\yun6\\upload\\"+spuId+".html");
        if (dest.exists()){
            dest.delete();
        }
    }
}
