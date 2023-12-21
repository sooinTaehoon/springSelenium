package com.springselenium.config.dao;

import com.springselenium.domain.ReviewVO;

import java.sql.SQLException;
import java.util.List;

public interface PostgresDBMapper {
    public int setReviewList(List<ReviewVO> reviewVOList) throws SQLException;
}
