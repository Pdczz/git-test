package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {
    @Autowired
    private BrandMapper brandMapper;

    public PageResult<Brand> queryBrandByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key) {
        //分页
        PageHelper.startPage(page,rows);//原理把通用mapper查询语句拦截，再自动拼接limit
        /*
        * WHERE NAME LIKE "%华%" OR letter = 'h'
          ORDER BY id asc
        */
        //过滤
        Example example = new Example(Brand.class);
        if (StringUtils.isNotBlank(key)){
            //过滤条件
            example.createCriteria().orLike("name","%"+key+"%")
                    .orEqualTo("letter",key.toUpperCase());
        }
        //排序
        if (StringUtils.isNotBlank(sortBy)){
            String orderByClause=sortBy+(desc ? " DESC":" ASC");
            example.setOrderByClause(orderByClause);
        }
        //查询
        List<Brand> list = brandMapper.selectByExample(example);//返回的是page对象
        if (CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        //解析分页结果
        PageInfo<Brand> info = new PageInfo<>(list);
        return new PageResult<Brand>(info.getTotal(),list);
    }
    @Transactional
    public void saveBrand(Brand brand, List<Long> categories) {
        //新增品牌
        brand.setId(null);
        int count = brandMapper.insert(brand);
        if (count!=1){
            throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
        }
        //新增中间表
        for (Long cid : categories) {
            count = brandMapper.insertCategoryBrand(cid, brand.getId());
            if (count!=1){
                throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
            }
        }
    }
    public Brand queryBrandById(Long id){
        Brand brand=brandMapper.selectByPrimaryKey(id);
        if (brand==null){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brand;
    }

    public List<Brand> queryBrandByCid(Long cid) {
        List<Brand> brands = brandMapper.queryByCategotyId(cid);
        if (CollectionUtils.isEmpty(brands)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brands;
    }

    public List<Brand> queryBrandByIds(List<Long> ids) {
        List<Brand> brands = brandMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(brands)){
            throw  new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brands;
    }
}
