package com.dasi.infrastructure.repository;

import com.dasi.domain.login.repository.ILoginRepository;
import com.dasi.infrastructure.persistent.dao.IUserDao;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class LoginRepository implements ILoginRepository {

    @Resource
    private IUserDao userDao;

    @Override
    public User queryByUsername(String username) {
        com.dasi.infrastructure.persistent.po.User po = userDao.queryByUsername(username);
        return toDomain(po);
    }

    @Override
    public User queryById(Long id) {
        com.dasi.infrastructure.persistent.po.User po = userDao.queryById(id);
        return toDomain(po);
    }

    @Override
    public void insertUser(User user) {
        com.dasi.infrastructure.persistent.po.User po = toPo(user);
        userDao.insert(po);
        user.setId(po.getId());
    }

    @Override
    public void updateUser(User user) {
        userDao.update(toPo(user));
    }

    @Override
    public boolean existsByUsername(String username) {
        com.dasi.infrastructure.persistent.po.User po = userDao.queryByUsername(username);
        return po != null;
    }

    @Override
    public boolean existsByUsernameExcludeId(String username, Long excludeId) {
        com.dasi.infrastructure.persistent.po.User po = userDao.queryByUsername(username);
        if (po == null) {
            return false;
        }
        return !po.getId().equals(excludeId);
    }

    private User toDomain(com.dasi.infrastructure.persistent.po.User po) {
        if (po == null) {
            return null;
        }
        return User.builder()
                .id(po.getId())
                .username(po.getUsername())
                .password(po.getPassword())
                .role(po.getRole())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private com.dasi.infrastructure.persistent.po.User toPo(User user) {
        if (user == null) {
            return null;
        }
        com.dasi.infrastructure.persistent.po.User po = new com.dasi.infrastructure.persistent.po.User();
        po.setId(user.getId());
        po.setUsername(user.getUsername());
        po.setPassword(user.getPassword());
        po.setRole(user.getRole());
        po.setCreateTime(user.getCreateTime());
        po.setUpdateTime(user.getUpdateTime());
        return po;
    }
}
