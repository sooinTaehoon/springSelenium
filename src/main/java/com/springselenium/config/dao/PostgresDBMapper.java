package com.springselenium.config.dao;

import com.springselenium.domain.ReviewVO;
import org.apache.ibatis.annotations.Param;

import java.sql.SQLException;
import java.util.List;

public interface PostgresDBMapper {
    public int setReviewList(List<ReviewVO> reviewVOList) throws SQLException;
    public ReviewVO getRecentReview(@Param("product_id") String product_id) throws SQLException;
    public Long getReviewCount(@Param("product_id") String product_id) throws SQLException;
}
