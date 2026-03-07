package com.ticketing.order.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ticketing.order.infrastructure.repository.entity.OrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * MyBatis Plus Mapper for Order entity
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {

    /**
     * Update order with optimistic lock
     * Updates only if the current version matches the expected version
     * 
     * @return affected rows: 1 if success, 0 if version conflict
     */
    int updateWithVersion(
            @Param("id") String id,
            @Param("newVersion") Integer newVersion,
            @Param("currentVersion") Integer currentVersion,
            @Param("entity") OrderEntity entity);
}
