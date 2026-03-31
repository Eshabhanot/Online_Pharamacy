package in.cg.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.cg.main.entities.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long> {


}
