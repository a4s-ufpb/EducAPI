package br.ufpb.dcx.apps4society.educapi.resources;

import br.ufpb.dcx.apps4society.educapi.domain.Challenge;
import br.ufpb.dcx.apps4society.educapi.dto.challenge.ChallengeRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.services.ChallengeService;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.ChallengeAlreadyExistsException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value="/v1/api/")
public class ChallengeResource {
	@Autowired
	private ChallengeService challengeService;

	@Operation(summary = "Returns a Challenge present in the service, if the token and the Challenge ID are valid.")
	@GetMapping("auth/challenges/{idChallenge}")
	public ResponseEntity<Challenge> find(@RequestHeader("Authorization") String token,
										  @PathVariable Long idChallenge) {
		return ResponseEntity.ok(challengeService.find(token, idChallenge));
	}

	@Operation(summary = "Adds a new Challenge to a Context, if the token and the Context ID are valid.")
	@PostMapping("auth/challenges/{idContext}")
	public ResponseEntity<Challenge> insert(@RequestHeader("Authorization") String token,
											@Valid @RequestBody ChallengeRegisterDTO objDto,
											@PathVariable Long idContext) throws ChallengeAlreadyExistsException{
		return ResponseEntity.status(HttpStatus.CREATED).body(challengeService.insert(token, objDto, idContext));
	}

	@Operation(summary = "Updates a User Challenge, if the token and the Challenge ID are valid.")
	@PutMapping("auth/challenges/{idChallenge}")
	public ResponseEntity<Challenge> update(@RequestHeader("Authorization") String token,
											@Valid @RequestBody ChallengeRegisterDTO objDto,
											@PathVariable Long idChallenge){
		return ResponseEntity.ok(challengeService.update(token, objDto, idChallenge));
	}

	@Operation(summary = "Deletes a User Challenge, if the token and the Challenge ID are valid.")
	@DeleteMapping("auth/challenges/{idChallenge}")
	public ResponseEntity<Void> delete(@RequestHeader("Authorization") String token,
									   @PathVariable Long idChallenge){
		challengeService.delete(token, idChallenge);
		return ResponseEntity.status(HttpStatus.OK).build();
	}

	@Operation(summary = "Returns a list of all Challenges registered by the request User, if the token is valid.")
	@GetMapping("auth/challenges")
	public ResponseEntity<List<Challenge>> findAllByUser(@RequestHeader("Authorization") String token){
		return ResponseEntity.ok(challengeService.findChallengesByCreator(token));
	}

	@Operation(summary = "Returns a page with Challenges registered in the service.")
	@GetMapping("challenges")
	public ResponseEntity<Page<Challenge>> findAllChallenges(
			@RequestParam(value = "word", required = false) String word,
			@RequestParam(value = "size", defaultValue = "20") Integer size,
			@RequestParam(value = "page", defaultValue = "0") Integer page,
			Pageable pageable){
		return new ResponseEntity<>(challengeService.findChallengesByParams(word, pageable), HttpStatus.OK);
	}

}