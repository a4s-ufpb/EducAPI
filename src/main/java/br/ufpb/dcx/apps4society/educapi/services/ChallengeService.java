package br.ufpb.dcx.apps4society.educapi.services;

import java.util.*;

import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.challenge.ChallengeRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.InvalidUserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ufpb.dcx.apps4society.educapi.domain.Challenge;
import br.ufpb.dcx.apps4society.educapi.domain.Context;
import br.ufpb.dcx.apps4society.educapi.repositories.ChallengeRepository;
import br.ufpb.dcx.apps4society.educapi.repositories.ContextRepository;
import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.ObjectNotFoundException;

@Service
public class ChallengeService {
    @Autowired
    private JWTService jwtService;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContextRepository contextRepository;

    public Challenge find(String token, Long id) throws ObjectNotFoundException, InvalidUserException {
        Optional<String> usuarioId = jwtService.recoverUser(token);
        if (usuarioId.isEmpty()) {
            throw new InvalidUserException();
        }

        Optional<Challenge> challengeOptional = challengeRepository.findById(id);
        if (challengeOptional.isEmpty()) {
            throw new ObjectNotFoundException("Object not found! Id: " + id + ", Type: " + Challenge.class.getName());
        }

        return challengeOptional.get();
    }

    @Transactional
    public Challenge insert(String token, ChallengeRegisterDTO obj, Long contextID) throws ObjectNotFoundException, InvalidUserException {
        User user = validateUser(token);

        Optional<Context> contextOptional = contextRepository.findById(contextID);
        if (contextOptional.isEmpty()) {
            throw new ObjectNotFoundException();
        }
        Challenge challenge = obj.toChallenge();
        Context context = contextOptional.get();
        challenge.setCreator(user);
        challenge.getContexts().add(context);

        return challengeRepository.save(challenge);
    }

    public List<Challenge> findChallengesByCreator(String token) throws ObjectNotFoundException, InvalidUserException {
        User user = validateUser(token);
        return challengeRepository.findChallengesByCreator(user);
    }

    public Challenge update(String token, ChallengeRegisterDTO obj, Long id) throws ObjectNotFoundException, InvalidUserException {
        User user = validateUser(token);

        Challenge newObj = find(token, id);
        if (!newObj.getCreator().equals(user)) {
            throw new InvalidUserException();
        }

        updateData(newObj, obj.toChallenge());
        return challengeRepository.save(newObj);
    }

    public void delete(String token, Long id) throws ObjectNotFoundException, InvalidUserException {
        User user = validateUser(token);

        Challenge obj = find(token, id);
        if (obj.getCreator().equals(user)) {
            for (Context x : obj.getContexts()) {
                x.getChallenges().remove(obj);
                contextRepository.save(x);
            }
            challengeRepository.deleteById(id);
        } else {
            throw new InvalidUserException();
        }
    }

    public Page<Challenge> findChallengesByParams(String prefix, Pageable pageable) {
        if (prefix != null) {
            return challengeRepository.findByWordStartsWithIgnoreCase(prefix, pageable);
        }

        return challengeRepository.findAll(pageable);
    }

    private User validateUser(String token) throws ObjectNotFoundException, InvalidUserException {
        Optional<String> userEmail = jwtService.recoverUser(token);
        if (userEmail.isEmpty()) {
            throw new InvalidUserException();
        }

        Optional<User> userOptional = userRepository.findByEmail(userEmail.get());
        if (userOptional.isEmpty()) {
            throw new ObjectNotFoundException();
        }

        return userOptional.get();
    }

    private void updateData(Challenge newObj, Challenge obj) {
        newObj.setWord(obj.getWord());
        newObj.setSoundUrl(obj.getSoundUrl());
        newObj.setVideoUrl(obj.getVideoUrl());
        newObj.setImageUrl(obj.getImageUrl());
    }

}