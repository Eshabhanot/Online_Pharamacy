package in.cg.main.config;

import in.cg.main.entities.User;
import in.cg.main.entities.UserAddress;
import in.cg.main.repository.UserAddressRepository;
import in.cg.main.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SampleDataConfig {
    private static final String ADMIN_EMAIL = "admin@gmail.com";
    private static final String ADMIN_PASSWORD = "admin1234";

    @Bean
    CommandLineRunner seedAuthSampleData(UserRepository userRepository,
                                         UserAddressRepository userAddressRepository,
                                         BCryptPasswordEncoder passwordEncoder) {
        return args -> {
            seedUser(userRepository, userAddressRepository, passwordEncoder,
                    ADMIN_EMAIL, "System Admin", "9876543211", "ADMIN",
                    "MG Road", "Bengaluru", "Karnataka", "560001");
            seedUser(userRepository, userAddressRepository, passwordEncoder,
                    "demo.user@gmail.com", "Demo User", "9876543210", "USER",
                    "221 Sample Street", "Bengaluru", "Karnataka", "560001");
            seedUser(userRepository, userAddressRepository, passwordEncoder,
                    "rahul.sharma@gmail.com", "Rahul Sharma", "9876543212", "USER",
                    "45 MG Road", "Mumbai", "Maharashtra", "400001");
            seedUser(userRepository, userAddressRepository, passwordEncoder,
                    "priya.patel@gmail.com", "Priya Patel", "9876543213", "USER",
                    "78 Link Road", "Delhi", "Delhi", "110001");
            seedUser(userRepository, userAddressRepository, passwordEncoder,
                    "amit.kumar@gmail.com", "Amit Kumar", "9876543214", "USER",
                    "123 Park Street", "Kolkata", "West Bengal", "700001");
            seedUser(userRepository, userAddressRepository, passwordEncoder,
                    "sneha.reddy@gmail.com", "Sneha Reddy", "9876543215", "USER",
                    "56 Jubilee Hills", "Hyderabad", "Telangana", "500001");
            seedUser(userRepository, userAddressRepository, passwordEncoder,
                    "vikram.singh@gmail.com", "Vikram Singh", "9876543216", "USER",
                    "89 Civil Lines", "Jaipur", "Rajasthan", "302001");
            seedUser(userRepository, userAddressRepository, passwordEncoder,
                    "anita.desai@gmail.com", "Anita Desai", "9876543217", "USER",
                    "34 Boat Club Road", "Pune", "Maharashtra", "411001");
            seedUser(userRepository, userAddressRepository, passwordEncoder,
                    "rajesh.gupta@gmail.com", "Rajesh Gupta", "9876543218", "USER",
                    "67 Sector 17", "Chandigarh", "Punjab", "160001");
            seedUser(userRepository, userAddressRepository, passwordEncoder,
                    "meera.iyer@gmail.com", "Meera Iyer", "9876543219", "USER",
                    "12 T Nagar", "Chennai", "Tamil Nadu", "600001");
            seedUser(userRepository, userAddressRepository, passwordEncoder,
                    "deepak.joshi@gmail.com", "Deepak Joshi", "9876543220", "USER",
                    "91 Mall Road", "Lucknow", "Uttar Pradesh", "226001");
        };
    }

    private void seedUser(UserRepository userRepository,
                          UserAddressRepository userAddressRepository,
                          BCryptPasswordEncoder passwordEncoder,
                          String email,
                          String name,
                          String mobile,
                          String role,
                          String addressLine1,
                          String city,
                          String state,
                          String pincode) {
        User existing = userRepository.findByEmailIgnoreCase(email);
        if (existing == null) {
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setMobile(mobile);
            user.setPassword(passwordEncoder.encode(resolvePassword(email)));
            user.setRole(role);
            user.setStatus("ACTIVE");
            existing = userRepository.save(user);
        } else if (ADMIN_EMAIL.equalsIgnoreCase(email)) {
            existing.setName(name);
            existing.setMobile(mobile);
            existing.setRole(role);
            existing.setStatus("ACTIVE");
            existing.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
            existing = userRepository.save(existing);
        }

        if (userAddressRepository.findByUserIdAndIsDefaultTrue(existing.getId()).isEmpty()) {
            UserAddress address = new UserAddress();
            address.setUserId(existing.getId());
            address.setFullName(name);
            address.setPhone(mobile);
            address.setAddressLine1(addressLine1);
            address.setAddressLine2("Near City Center");
            address.setCity(city);
            address.setState(state);
            address.setPincode(pincode);
            address.setDefault(true);
            userAddressRepository.save(address);
        }
    }

    private String resolvePassword(String email) {
        if (ADMIN_EMAIL.equalsIgnoreCase(email)) {
            return ADMIN_PASSWORD;
        }
        return "Password@123";
    }
}
