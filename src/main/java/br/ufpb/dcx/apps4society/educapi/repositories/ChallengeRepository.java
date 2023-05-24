package br.ufpb.dcx.apps4society.educapi.repositories;
import br.ufpb.dcx.apps4society.educapi.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.ufpb.dcx.apps4society.educapi.domain.Challenge;
import java.util.List;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
	List<Challenge> findChallengesByCreator(User creator);
	Page<Challenge> findByWordStartsWithIgnoreCase(String word, Pageable pageable);
}