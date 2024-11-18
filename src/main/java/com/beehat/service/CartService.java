package com.beehat.service;

import com.beehat.entity.CartDetail;
import com.beehat.repository.CartDetailRepo;
import com.beehat.repository.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.List;

@Service

public class CartService {
    @Autowired
    ProductRepo productRepo;

}
