package desafio.gustavo.services;

import desafio.gustavo.dto.RoleDTO;
import desafio.gustavo.dto.UserDTO;
import desafio.gustavo.dto.UserInsertDTO;
import desafio.gustavo.dto.UserUpdateDTO;
import desafio.gustavo.entities.Role;
import desafio.gustavo.entities.User;
import desafio.gustavo.projections.UserDetailsProjection;
import desafio.gustavo.repositories.RoleRepository;
import desafio.gustavo.repositories.UserRepository;
import desafio.gustavo.services.exceptions.DatabaseException;
import desafio.gustavo.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(BCryptPasswordEncoder bCryptPasswordEncoder, UserRepository userRepository, RoleRepository roleRepository, BCryptPasswordEncoder passwordEncoder) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Transactional(readOnly = true)
    public Page<UserDTO> findAll(Pageable pageable) {
        Page<User> list = userRepository.findAll(pageable);
        return list.map(UserDTO::new);
    }

    @Transactional(readOnly = true)
    public UserDTO findById(Long id) {
        Optional<User> obj = userRepository.findById(id);
        User user = obj.orElseThrow(() -> new ResourceNotFoundException("Usuário com id: " + id + ", não encontrada."));
        return new UserDTO(user);
    }

    @Transactional
    public UserDTO insert(UserInsertDTO dto) {
        var user = new User();
        copyDtoToEntity(dto, user);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user = userRepository.save(user);
        return new UserDTO(user);
    }

    @Transactional
    public UserDTO update(Long id, UserUpdateDTO dto) {
        try {
            var user = userRepository.getReferenceById(id);
            copyDtoToEntity(dto, user);
            user = userRepository.save(user);
            return new UserDTO(user);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException("Usuário com id: " + id + ", não encontrado.");
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Recurso não encontrado");
        }
        try {
            userRepository.deleteById(id);
        }
        catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Falha de integridade referencial");
        }
    }

    private void copyDtoToEntity(UserDTO dto, User entity) {
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());

        entity.getRoles().clear();
        for (RoleDTO roleDTO : dto.getRoles()) {
            var role = roleRepository.getReferenceById(roleDTO.getId());
            entity.getRoles().add(role);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        List<UserDetailsProjection> result  = userRepository.searchUserAndRolesByEmail(username);
        if (result.size() == 0) {
            throw new UsernameNotFoundException("Email não encontrado");
        }

        var user = new User();
        user.setEmail(username);
        user.setPassword(result.get(0).getPassword());
        for (UserDetailsProjection projection : result) {
            user.addRole(new Role(projection.getRoleId(), projection.getAuthority()));
        }

        return user;
    }
}
