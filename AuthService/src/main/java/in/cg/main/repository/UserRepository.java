package in.cg.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.cg.main.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

	User findByEmail(String email);
	User findByEmailIgnoreCase(String email);
	User findByMobile(String mobile);

}
