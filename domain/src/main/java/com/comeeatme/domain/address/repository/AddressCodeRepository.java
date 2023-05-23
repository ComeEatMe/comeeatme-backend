package com.comeeatme.domain.address.repository;


import com.comeeatme.domain.address.AddressCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressCodeRepository extends JpaRepository<AddressCode, String> {

    boolean existsByNameStartingWith(String name);

    Optional<AddressCode> findByNameStartingWith(String name);

    List<AddressCode> findAllByNameStartingWith(String name);

    List<AddressCode> findAllByParentCodeIn(List<AddressCode> parents);

    List<AddressCode> findAllByParentCodeAndUseYnIsTrue(AddressCode parentCode);

    List<AddressCode> findAllByTerminalIsTrueAndUseYnIsTrue();

}
