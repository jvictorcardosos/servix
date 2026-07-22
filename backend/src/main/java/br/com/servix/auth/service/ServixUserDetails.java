package br.com.servix.auth.service;

import br.com.servix.auth.domain.User;
import br.com.servix.auth.domain.UserStatus;
import java.util.Collection;
import java.util.UUID;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class ServixUserDetails implements UserDetails {

    private final UUID userId;
    private final UUID companyId;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;

    public ServixUserDetails(User user) {
        this.userId = user.getId();
        this.companyId = user.getCompany().getId();
        this.username = user.getEmail();
        this.password = user.getPasswordHash();
        this.authorities = user.getProfiles().stream()
                .map(profile -> new SimpleGrantedAuthority("ROLE_" + profile.getName().name()))
                .toList();
        this.enabled = user.getStatus() == UserStatus.ACTIVE;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
