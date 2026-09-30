package com.example.tourisminformation.model;

import java.util.List;

public record LivePricesResponse(int days, List<LiveHotelPrice> prices) {}
