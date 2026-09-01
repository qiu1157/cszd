package com.changsha.zhongdian.pile.repository;

import java.util.Optional;

import com.changsha.zhongdian.pile.entity.Pile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PileRepository extends JpaRepository<Pile, Long> {

    Optional<Pile> findTopByPileCodeStartingWithOrderByPileCodeDesc(String pileCodePrefix);
}
