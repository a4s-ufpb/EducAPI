package br.ufpb.dcx.apps4society.educapi.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.domain.Context;
import java.util.List;

@Repository
public interface ContextRepository extends JpaRepository<Context, Long> {
	List<Context> findContextsByCreator(User creator);
	Page<Context> findAllByCreatorEmailLikeAndNameStartsWithIgnoreCase(String email, String name, Pageable pageable);
	Page<Context> findAllByCreatorEmailEqualsIgnoreCase(String email, Pageable pageable);
	Page<Context> findAllByNameStartsWithIgnoreCase(String name, Pageable pageable);
}