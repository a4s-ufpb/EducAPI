package br.ufpb.dcx.apps4society.educapi.services;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufpb.dcx.apps4society.educapi.domain.Challenge;
import br.ufpb.dcx.apps4society.educapi.domain.Context;
import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.repositories.ChallengeRepository;
import br.ufpb.dcx.apps4society.educapi.repositories.ContextRepository;
import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;


@Service
public class DBService {
	@Autowired
	private ChallengeRepository challengeRepository;
	@Autowired
	private ContextRepository contextRepository;
	@Autowired
	private UserRepository userRepository;

	public boolean instantiateDatabase() {
		return true;
	}
}
