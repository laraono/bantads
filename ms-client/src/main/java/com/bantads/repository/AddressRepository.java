/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bantads.repository;

import com.bantads.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author lenovo
 */
public interface AddressRepository extends JpaRepository<Address, Long> {
    
}
