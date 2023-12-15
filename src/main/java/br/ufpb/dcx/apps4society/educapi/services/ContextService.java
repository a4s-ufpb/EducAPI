package br.ufpb.dcx.apps4society.educapi.services;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.context.ContextRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ufpb.dcx.apps4society.educapi.domain.Context;
import br.ufpb.dcx.apps4society.educapi.dto.context.ContextDTO;
import br.ufpb.dcx.apps4society.educapi.repositories.ContextRepository;

@Service
public class ContextService {    
    @Autowired
    private JWTService jwtService;

    @Autowired
    private ContextRepository contextRepository;

    @Autowired
    private UserRepository userRepository;

    public ContextService(JWTService jwtService, ContextRepository contextRepository, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.contextRepository = contextRepository;
        this.userRepository = userRepository;
    }

    public Context find(Long id) throws ObjectNotFoundException {

        Optional<Context> obgOptional = contextRepository.findById(id);
        if (obgOptional.isEmpty()) {
            throw new ObjectNotFoundException("Object not found! Id: " + id + ", Type: " + Context.class.getName());
        }
        return obgOptional.get();
    }

    @Transactional
    public ContextDTO insert(String token, ContextRegisterDTO contextRegisterDTO) throws ContextAlreadyExistsException, ObjectNotFoundException, InvalidUserException {
        User user = validateUser(token);

        Context context = contextRegisterDTO.contextRegisterDTOToContext();

        context.setCreator(user);

        Optional<Context> contextOptional = contextRepository.findContextByNameIgnoreCase(context.getName());

        if (contextOptional.isPresent()){
            throw new ContextAlreadyExistsException("There is already a context with this name registered for this user!");
        }

        contextRepository.save(context);
        return new ContextDTO(context);
    }

    public ContextDTO update(String token, ContextRegisterDTO contextRegisterDTO, Long id) throws ObjectNotFoundException, InvalidUserException, InvalidContextException {
        User user = validateUser(token);


        Optional<Context> contextOptional = contextRepository.findById(id);

        if (!contextOptional.isPresent()){
            throw new ObjectNotFoundException();
        }

        if(Objects.equals(contextRegisterDTO.getName(), contextOptional.get().getName())
                && Objects.equals(contextRegisterDTO.getImageUrl(), contextOptional.get().getImageUrl())
                && Objects.equals(contextRegisterDTO.getSoundUrl(), contextOptional.get().getSoundUrl())
                && Objects.equals(contextRegisterDTO.getVideoUrl(), contextOptional.get().getVideoUrl())){
            throw new InvalidContextException();
        }

        Context newObj = find(id);
        if (!newObj.getCreator().equals(user)) {
            throw new InvalidUserException("User: " + user.getName() + " is not the owner of the context: "
                    + newObj.getName() + ".");
        }

        updateData(newObj, contextRegisterDTO.contextRegisterDTOToContext());
        contextRepository.save(newObj);
        return new ContextDTO(newObj);
    }

    public ContextDTO delete(String token, Long id) throws ObjectNotFoundException, InvalidUserException {
        User user = validateUser(token);

        Optional<Context> contextOptional = contextRepository.findById(id);

        if (!contextOptional.isPresent()){
            throw new ObjectNotFoundException();
        }

        Context context = find(id);
        if (!context.getCreator().equals(user)) {
            throw new InvalidUserException("User: " + user.getName() + " is not the owner of the context: "
                    + context.getName() + ".");
        }

        contextRepository.deleteById(id);
        return new ContextDTO(context);
    }

    public Page<Context> findContextsByParams(String email, String name, Pageable pageable) {
        if (email != null && name != null){
            return contextRepository.findAllByCreatorEmailLikeAndNameStartsWithIgnoreCase(email, name, pageable);
        }else if (email != null){
            return contextRepository.findAllByCreatorEmailEqualsIgnoreCase(email, pageable);
        }else if (name != null){
            return contextRepository.findAllByNameStartsWithIgnoreCase(name, pageable);
        }else{
            return contextRepository.findAll(pageable);
        }
    }

    public List<ContextDTO> findContextsByCreator(String token) throws  ObjectNotFoundException, InvalidUserException {
        User user = validateUser(token);

        List<Context> contextListByCreator = contextRepository.findContextsByCreator(user);
        if(contextListByCreator.isEmpty()) {
            throw new ObjectNotFoundException();
        }

        return contextListByCreator.stream().map(ContextDTO::new).collect(Collectors.toList());
    }

    private void updateData(Context newObj, Context obj) {
        newObj.setName(obj.getName());
        newObj.setImageUrl(obj.getImageUrl());
        newObj.setSoundUrl(obj.getSoundUrl());
        newObj.setVideoUrl(obj.getVideoUrl());
    }

    private User validateUser(String token) throws ObjectNotFoundException, InvalidUserException {
        String userEmail = jwtService.recoverUser(token);
        if (userEmail.isEmpty()) {
            throw new InvalidUserException();
        }

        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isEmpty()) {
            throw new ObjectNotFoundException();
        }

        return userOptional.get();
    }

}