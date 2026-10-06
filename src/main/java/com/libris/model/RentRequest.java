package com.libris.model;

import java.time.LocalDateTime;
import java.util.List;

// Java record: DTO bất biến cho yêu cầu thuê sách (Controller -> DAO)
public record RentRequest(int customerId, List<Book> books, LocalDateTime expectedReturnDate) {
    public RentRequest {
        books = List.copyOf(books);
    }
}
