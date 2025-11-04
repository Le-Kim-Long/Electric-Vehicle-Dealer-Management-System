package org.example.repository;

import org.example.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    //Tìm customer theo email
    Optional<Customer> findByEmail(String email);

    //Tìm customer theo số điện thoại
    Optional<Customer> findByPhoneNumber(String phoneNumber);

    Customer findByEmailAndPhoneNumber(String email, String phoneNumber);

    //Tìm customer theo id
    Optional<Customer> findByCustomerId(Integer customerId);
}
