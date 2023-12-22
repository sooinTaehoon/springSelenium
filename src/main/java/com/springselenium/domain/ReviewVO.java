package com.springselenium.domain;

import org.json.JSONArray;

public class ReviewVO {
    private int id;
    private String user_id;
    private String product_name;
    private String title;
    private String text;
    private String date;
    private String shopping_list;
    private int grade;
    private String state;
    private String img;
    private String site;
    private int img_count;
    private String product_id;

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getUser_id() { return user_id; }

    public void setUser_id(String user_id) { this.user_id = user_id; }

    public String getProduct_name() { return product_name; }

    public void setProduct_name(String product_name) { this.product_name = product_name; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getText() { return text; }

    public void setText(String text) { this.text = text; }

    public String getDate() { return date; }

    public void setDate(String date) { this.date = date; }

    public String getShopping_list() { return shopping_list; }

    public void setShopping_list(String shopping_list) { this.shopping_list = shopping_list; }

    public int getGrade() { return grade; }

    public void setGrade(int grade) { this.grade = grade; }

    public String getState() { return state; }

    public void setState(String state) { this.state = state; }

    public String getImg() { return img; }

    public void setImg(String img) { this.img = img; }

    public String getSite() { return site; }

    public void setSite(String site) { this.site = site; }

    public int getImg_count() { return img_count; }

    public void setImg_count(int img_count) { this.img_count = img_count; }

    public String getProduct_id() { return product_id; }

    public void setProduct_id(String product_id) { this.product_id = product_id; }
}
