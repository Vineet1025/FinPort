package com.expensetracker.expensetracker.services;

import com.expensetracker.expensetracker.DTO.CustomUserDetails;
import com.expensetracker.expensetracker.DTO.WebUser;
import com.expensetracker.expensetracker.models.Client;
import com.expensetracker.expensetracker.models.Role;
import com.expensetracker.expensetracker.models.User;
import com.expensetracker.expensetracker.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    RoleService roleService;
    ClientService clientService;
    PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleService roleService, ClientService clientService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.clientService = clientService;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public User findUserByUserName(String username) {
        return userRepository.findByUserName(username);
    }

    @Transactional
    @Override
    public void save(WebUser webUser) {
        Client client = new Client();
        client.setFirstName(webUser.getFirstName());
        client.setLastName(webUser.getLastName());
        client.setEmail(webUser.getEmail());
        User user = new User();
        user.setUserName(webUser.getUsername());
        user.setPassword(passwordEncoder.encode(webUser.getPassword()));
        user.setClient(client);
        user.setEnabled(true);
        user.setRoles(Arrays.asList(roleService.findRoleByName("ROLE_STANDARD")));
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserName(username);
        if (user == null)
            throw new UsernameNotFoundException("user not found");
        return new CustomUserDetails(user.getUserName(), user.getPassword(),
                mapRolesToAuthorityes(user.getRoles()), user.getClient().getId());
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorityes(Collection<Role> roles) {
        return roles.stream().map(role->new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }
}
